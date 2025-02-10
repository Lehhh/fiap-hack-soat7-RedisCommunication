package br.com.fiap.soat7.web.controller;

import br.com.fiap.soat7.application.service.RedisMessageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RedisCommunicationControllerTest {

    @InjectMocks
    private RedisCommunicationController redisCommunicationController;

    @Mock
    private RedisMessageService redisMessageService;

    private List<String> mockKeys;

    @BeforeEach
    void setUp() {
        mockKeys = Arrays.asList("key1", "key2", "key3");
    }

    @Test
    void fetchKeysByUser_shouldReturnOkAndListOfKeys() {
        // Arrange
        String userId = "user123";
        String pattern = userId + ":*:*:*";
        when(redisMessageService.fetchAllKeys(pattern)).thenReturn(mockKeys);

        // Act
        ResponseEntity<List<String>> response = redisCommunicationController.fetchKeysByUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockKeys, response.getBody());
    }

    @Test
    void fetchKeysByUser_shouldReturnEmptyListWhenNoKeysFound() {
        // Arrange
        String userId = "user456";
        String pattern = userId + ":*:*:*";
        when(redisMessageService.fetchAllKeys(pattern)).thenReturn(List.of()); // Returns an empty list

        // Act
        ResponseEntity<List<String>> response = redisCommunicationController.fetchKeysByUser(userId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size()); // Verify empty list
    }

    @Test
    void fetchKeysByStatus_shouldReturnOkAndListOfKeys() {
        // Arrange
        String status = "pending";
        String pattern = "*:*:*:*:" + status;
        when(redisMessageService.fetchAllKeys(pattern)).thenReturn(mockKeys);

        // Act
        ResponseEntity<List<String>> response = redisCommunicationController.fetchKeysByStatus(status);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockKeys, response.getBody());
    }

     @Test
    void fetchKeysByStatus_shouldReturnEmptyListWhenNoKeysFound() {
        // Arrange
        String status = "completed";
        String pattern = "*:*:*:*:" + status;
        when(redisMessageService.fetchAllKeys(pattern)).thenReturn(List.of()); // Returns an empty list

        // Act
        ResponseEntity<List<String>> response = redisCommunicationController.fetchKeysByStatus(status);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(0, response.getBody().size()); // Verify empty list
    }
}