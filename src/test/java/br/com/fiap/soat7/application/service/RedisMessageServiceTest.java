package br.com.fiap.soat7.application.service;

import br.com.fiap.soat7.domain.enums.Stage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.ValueOperations;

import java.nio.charset.StandardCharsets;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisMessageServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @InjectMocks
    private RedisMessageService redisMessageService;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    @Mock
    private RedisConnectionFactory redisConnectionFactory;

    @Mock
    private RedisConnection redisConnection;

    @Mock
    private Cursor cursor;

    // Removed from setUp()
    //@BeforeEach
    //void setUp() {
    //    when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    //}

    @Test
    void setIfAbsent_keyDoesNotExist_shouldReturnTrue() {
        String key = "testKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // Add here, if needed in this test
        when(valueOperations.setIfAbsent(eq(key), anyLong())).thenReturn(true);

        boolean result = redisMessageService.setIfAbsent(key);

        assertTrue(result);
        verify(valueOperations).setIfAbsent(eq(key), anyLong());
    }

    @Test
    void setIfAbsent_keyExists_shouldReturnFalse() {
        String key = "testKey";
        when(redisTemplate.opsForValue()).thenReturn(valueOperations); // Add here, if needed in this test
        when(valueOperations.setIfAbsent(eq(key), anyLong())).thenReturn(false);

        boolean result = redisMessageService.setIfAbsent(key);

        assertFalse(result);
        verify(valueOperations).setIfAbsent(eq(key), anyLong());
    }

    @Test
    void fetchAllKeys_shouldReturnListOfKeys() {
        String match = "test:*";
        List<byte[]> expectedKeys = Arrays.asList("test:key1".getBytes(StandardCharsets.UTF_8), "test:key2".getBytes(StandardCharsets.UTF_8));

        when(redisTemplate.getConnectionFactory()).thenReturn(redisConnectionFactory);
        when(redisConnectionFactory.getConnection()).thenReturn(redisConnection);
        when(redisConnection.scan(any(ScanOptions.class))).thenReturn(cursor);
        when(cursor.hasNext()).thenReturn(true, true, false);
        when(cursor.next()).thenReturn(expectedKeys.get(0), expectedKeys.get(1));

        List<String> actualKeys = redisMessageService.fetchAllKeys(match);

        assertEquals(2, actualKeys.size());
        assertEquals("test:key1", actualKeys.get(0));
        assertEquals("test:key2", actualKeys.get(1));
        verify(redisTemplate.getConnectionFactory()).getConnection();
        verify(redisConnection).scan(any(ScanOptions.class));
        verify(cursor, times(3)).hasNext();
        verify(cursor, times(2)).next();
        verify(redisConnection).close();
    }


    @Test
    void fetchOnlyKeysWithOutNextPostion_exceptionThrown_shouldReturnEmptyList() {
        Stage stage = Stage.PROCESS_VIDEO_DONE;
        when(redisTemplate.getConnectionFactory()).thenThrow(new RuntimeException("Connection error"));

        List<String> result = redisMessageService.fetchOnlyKeysWithOutNextPostion(stage);

        assertTrue(result.isEmpty());
    }


    @Test
    void deleteKeysByPattern_keysExist_shouldDeleteKeys() {
        String pattern = "test:*";
        Set<String> keysToDelete = new HashSet<>(Arrays.asList("test:key1", "test:key2"));

        when(redisTemplate.keys(pattern)).thenReturn(keysToDelete);

        redisMessageService.deleteKeysByPattern(pattern);

        verify(redisTemplate).delete(keysToDelete);
    }

    @Test
    void deleteKeysByPattern_keysDoNotExist_shouldNotAttemptToDelete() {
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(null);

        redisMessageService.deleteKeysByPattern(pattern);

        verify(redisTemplate, never()).delete(anySet());
    }

    @Test
    void deleteKeysByPattern_keysIsEmpty_shouldNotAttemptToDelete() {
        String pattern = "test:*";
        when(redisTemplate.keys(pattern)).thenReturn(new HashSet<>());

        redisMessageService.deleteKeysByPattern(pattern);

        verify(redisTemplate, never()).delete(anySet());
    }
}