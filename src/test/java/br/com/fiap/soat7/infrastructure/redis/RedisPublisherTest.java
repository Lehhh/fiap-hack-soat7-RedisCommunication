package br.com.fiap.soat7.infrastructure.redis;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisPublisherTest {

    @Mock
    private StringRedisTemplate redisTemplate;

    @InjectMocks
    private RedisPublisher redisPublisher;

    @Test
    void publish_shouldConvertAndSendToRedis() {
        // Arrange
        String channel = "testChannel";
        String message = "testMessage";

        // Act
        redisPublisher.publish(channel, message);

        // Assert
        verify(redisTemplate).convertAndSend(channel, message);
        verifyNoMoreInteractions(redisTemplate);
    }
}