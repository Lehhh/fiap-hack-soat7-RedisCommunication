package br.com.fiap.soat7.web.controller;

import br.com.fiap.soat7.application.service.RedisMessageService;
import br.com.fiap.soat7.domain.dto.InfoVideo;
import br.com.fiap.soat7.domain.enums.Stage;
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
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VideoUploadS3CommunicationControllerTest {

    @InjectMocks
    private VideoUploadS3CommunicationController controller;

    @Mock
    private RedisMessageService redisMessageService;

    private InfoVideo infoVideo;

    @BeforeEach
    void setUp() {
        infoVideo = new InfoVideo();
        infoVideo.setVideoId(1L);
        infoVideo.setVersion(1);
    }

    @Test
    void fetchKeysByVideo_shouldReturnOkAndListOfKeys() {
        // Arrange
        String videoId = "video123";
        String pattern = "*:" + videoId + ":*:*";
        List<String> mockKeys = Arrays.asList("key1", "key2");
        when(redisMessageService.fetchAllKeys(pattern)).thenReturn(mockKeys);

        // Act
        ResponseEntity<List<String>> response = controller.fetchKeysByVideo(videoId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockKeys, response.getBody());
    }

    @Test
    void fetchQueue_shouldReturnOkAndListOfKeys() {
        // Arrange
        List<String> mockKeys = Arrays.asList("key3", "key4");
        when(redisMessageService.fetchOnlyKeysWithOutNextPostion(Stage.UPLOAD_S3_QUEUE)).thenReturn(mockKeys);

        // Act
        ResponseEntity<List<String>> response = controller.fetchQueue();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(mockKeys, response.getBody());
    }


    @Test
    void uploadToS3Status_whenStageIsNotUploadS3Done_shouldSetKeyAndReturnOk() {
        // Arrange
        infoVideo.setStage(Stage.UPLOAD_S3_QUEUE);
        when(redisMessageService.setIfAbsent(infoVideo.redisKeyStatus())).thenReturn(true);

        // Act
        ResponseEntity<InfoVideo> response = controller.uploadToS3Status(infoVideo);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(infoVideo.getStage(), response.getBody().getStage());
        verify(redisMessageService, times(1)).setIfAbsent(anyString());
    }

    @Test
    void uploadToS3Status_whenSetIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        infoVideo.setStage(Stage.UPLOAD_S3_QUEUE);
        when(redisMessageService.setIfAbsent(infoVideo.redisKeyStatus())).thenReturn(false);

        // Act
        ResponseEntity<InfoVideo> response = controller.uploadToS3Status(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void uploadToS3Status_whenStageIsUploadS3DoneAndSetIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        infoVideo.setStage(Stage.UPLOAD_S3_DONE);
        when(redisMessageService.setIfAbsent(infoVideo.redisKeyStatus())).thenReturn(false);

        // Act
        ResponseEntity<InfoVideo> response = controller.uploadToS3Status(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void uploadToS3Status_whenStageIsNotUploadS3DoneAndSetIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        infoVideo.setStage(Stage.UPLOAD_S3_QUEUE);
        when(redisMessageService.setIfAbsent(infoVideo.redisKeyStatus())).thenReturn(false);

        // Act
        ResponseEntity<InfoVideo> response = controller.uploadToS3Status(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }


}