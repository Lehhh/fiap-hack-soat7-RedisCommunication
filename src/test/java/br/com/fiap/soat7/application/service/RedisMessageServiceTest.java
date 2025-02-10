package br.com.fiap.soat7.application.service;

import static org.mockito.Mockito.*;
import static org.junit.Assert.*;

import br.com.fiap.soat7.domain.enums.Stage;
import org.junit.Before;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.*;
import org.springframework.data.redis.connection.*;

import java.util.*;

@ExtendWith(MockitoExtension.class)
public class RedisMessageServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private Cursor<byte[]> cursor;

    private RedisMessageService redisMessageService;

    @Before
    public void setUp() {
        MockitoAnnotations.openMocks(this);
        redisMessageService = new RedisMessageService(redisTemplate);

        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    // Testes para setIfAbsent

    @Test
    public void testSetIfAbsent_KeyDoesNotExist_ShouldReturnTrue() {
        // Arrange
        String key = "testKey";
        when(valueOperations.setIfAbsent(eq(key), anyLong())).thenReturn(true);

        // Act
        boolean result = redisMessageService.setIfAbsent(key);

        // Assert
        assertTrue(result);
        verify(valueOperations).setIfAbsent(eq(key), anyLong());
    }

    @Test
    public void testSetIfAbsent_KeyAlreadyExists_ShouldReturnFalse() {
        // Arrange
        String key = "existingKey";
        when(valueOperations.setIfAbsent(eq(key), anyLong())).thenReturn(false);

        // Act
        boolean result = redisMessageService.setIfAbsent(key);

        // Assert
        assertFalse(result);
        verify(valueOperations).setIfAbsent(eq(key), anyLong());
    }

    @Test
    public void testSetIfAbsent_ExceptionHandling_ShouldReturnFalse() {
        // Arrange
        String key = "errorKey";
        when(valueOperations.setIfAbsent(eq(key), anyLong())).thenThrow(new RuntimeException("Redis error"));

        // Act
        boolean result = redisMessageService.setIfAbsent(key);

        // Assert
        assertFalse(result);
    }

    // Testes para fetchAllKeys

    @Test
    public void testFetchAllKeys_ShouldReturnListOfKeys() {
        // Arrange
        String pattern = "prefix:*";
        List<String> expectedKeys = Arrays.asList("prefix:1", "prefix:2", "prefix:3");

        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, true, false);
        when(cursor.next()).thenReturn("prefix:1".getBytes(), "prefix:2".getBytes(), "prefix:3".getBytes());

        // Act
        List<String> result = redisMessageService.fetchAllKeys(pattern);

        // Assert
        assertNotNull(result);
        assertEquals(expectedKeys.size(), result.size());
        assertTrue(result.containsAll(expectedKeys));
    }

    @Test(expected = RuntimeException.class)
    public void testFetchAllKeys_ShouldThrowException_WhenRedisFails() {
        // Arrange
        String pattern = "prefix:*";
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Redis error"));

        // Act
        redisMessageService.fetchAllKeys(pattern);
    }

    // ✅ Testando fetchOnlyKeysWithOutNextPostion - Caminho Feliz
    @Test
    public void testFetchOnlyKeysWithOutNextPostion_Success() {
        // Arrange
        Stage stage = Stage.UPLOAD_S3_QUEUE;
        String matches = "*:*:*" + stage.name();
        String nextMatches = "*:*:*" + Stage.fetchStageOnly(stage).name();

        // Simulando chaves no estágio atual
        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, true, false);
        when(cursor.next()).thenReturn("key1:UPLOAD_S3_QUEUE".getBytes(), "key2:UPLOAD_S3_QUEUE".getBytes(), "key3:UPLOAD_S3_QUEUE".getBytes());

        // Simulando chaves no próximo estágio
        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn("key1:UPLOAD_S3_IN_PROGRESS".getBytes(), "key4:UPLOAD_S3_IN_PROGRESS".getBytes());

        // Act
        List<String> result = redisMessageService.fetchOnlyKeysWithOutNextPostion(stage);

        // Assert
        assertNotNull(result);
        assertEquals(2, result.size());
    }

    // ✅ Testando fetchOnlyKeysWithOutNextPostion - Nenhuma Chave Encontrada
    @Test
    public void testFetchOnlyKeysWithOutNextPostion_NoKeysFound_ShouldReturnEmptyList() {
        // Arrange
        Stage stage = Stage.UPLOAD_S3_QUEUE;

        // Simula nenhum resultado no scan
        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(false);

        // Act
        List<String> result = redisMessageService.fetchOnlyKeysWithOutNextPostion(stage);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ✅ Testando fetchOnlyKeysWithOutNextPostion - Erro na Conexão com Redis
    @Test
    public void testFetchOnlyKeysWithOutNextPostion_ExceptionHandling_ShouldReturnEmptyList() {
        // Arrange
        Stage stage = Stage.UPLOAD_S3_QUEUE;

        // Simula uma falha ao obter a conexão
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Redis connection failed"));

        // Act
        List<String> result = redisMessageService.fetchOnlyKeysWithOutNextPostion(stage);

        // Assert
        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    // ✅ Testando deleteKeysByPattern - Caminho Feliz
    @Test
    public void testDeleteKeysByPattern_Success() {
        // Arrange
        String pattern = "prefix:*";
        Set<String> mockKeys = new HashSet<>(Arrays.asList("key1", "key2", "key3"));

        when(redisTemplate.keys(pattern)).thenReturn(mockKeys);

        // Act
        redisMessageService.deleteKeysByPattern(pattern);

        // Assert
        verify(redisTemplate, times(1)).delete(mockKeys);
    }

    // ✅ Testando deleteKeysByPattern - Nenhuma Chave Encontrada
    @Test
    public void testDeleteKeysByPattern_NoKeysFound_ShouldNotDeleteAnything() {
        // Arrange
        String pattern = "prefix:*";
        when(redisTemplate.keys(pattern)).thenReturn(Collections.emptySet());

        // Act
        redisMessageService.deleteKeysByPattern(pattern);

        // Assert
        verify(redisTemplate, never()).delete(anySet());
    }
}
