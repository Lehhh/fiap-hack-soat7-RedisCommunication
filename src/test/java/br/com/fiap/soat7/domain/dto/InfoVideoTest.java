package br.com.fiap.soat7.domain.dto;

import br.com.fiap.soat7.domain.enums.Stage;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InfoVideoTest {

    @Test
    void redisKeyStatus_shouldReturnCorrectKey() {
        // Arrange
        InfoVideo infoVideo = new InfoVideo();
        infoVideo.setUserId(1L);
        infoVideo.setVideoId(2L);
        infoVideo.setImageId(3L);
        infoVideo.setVersion(1);
        infoVideo.setStage(Stage.UPLOAD_S3_DONE);

        // Act
        String result = infoVideo.redisKeyStatus();

        // Assert
        assertEquals("1:2:3:1:UPLOAD_S3_DONE", result);
    }

    @Test
    void redisKeyWithoutStatus_shouldReturnCorrectKey() {
        // Arrange
        InfoVideo infoVideo = new InfoVideo();
        infoVideo.setUserId(1L);
        infoVideo.setVideoId(2L);
        infoVideo.setImageId(3L);
        infoVideo.setVersion(1);

        // Act
        String result = infoVideo.redisKeyWithoutStatus();

        // Assert
        assertEquals("1:2:3:1", result);
    }
}