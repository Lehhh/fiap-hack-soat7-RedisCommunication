package br.com.fiap.soat7.web.controller;

import br.com.fiap.soat7.application.service.RedisMessageService;
import br.com.fiap.soat7.domain.dto.InfoVideo;
import br.com.fiap.soat7.domain.enums.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProcessVideoCommuncationControllerTest {

    @Mock
    private RedisMessageService redisMessageService;

    @InjectMocks
    private ProcessVideoCommuncationController processVideoCommuncationController;

    @Test
    void processVideoStatus_processVideoDoneStage_shouldSetAbsentAndReturnOk() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class);
        when(infoVideo.getStage()).thenReturn(Stage.UPLOAD_S3_IMAGES_QUEUE);
        String redisKeyStatus1 = "key1";
        String redisKeyStatus2 = "key2";
        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatus1).thenReturn(redisKeyStatus2);
        when(redisMessageService.setIfAbsent(redisKeyStatus1)).thenReturn(true);

        // Act
        ResponseEntity<InfoVideo> response = processVideoCommuncationController.processVideoStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(Stage.UPLOAD_S3_IMAGES_QUEUE, response.getBody().getStage()); // Check stage is updated
        verify(redisMessageService).setIfAbsent(redisKeyStatus1);
    }

    @Test
    void processVideoStatus_processVideoDoneStage_setIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class);
        when(infoVideo.getStage()).thenReturn(Stage.PROCESS_VIDEO_DONE);
        String redisKeyStatus1 = "key1";
        String redisKeyStatus2 = "key2";
        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatus1).thenReturn(redisKeyStatus2);
        when(redisMessageService.setIfAbsent(redisKeyStatus1)).thenReturn(false);
        when(redisMessageService.setIfAbsent(redisKeyStatus2)).thenReturn(true); // Doesn't matter

        // Act
        ResponseEntity<InfoVideo> response = processVideoCommuncationController.processVideoStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(redisMessageService).setIfAbsent(redisKeyStatus1);
    }

    @Test
    void processVideoStatus_otherStage_shouldSetAbsentAndReturnOk() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class);
        when(infoVideo.getStage()).thenReturn(Stage.PROCESS_VIDEO_QUEUE);
        String redisKeyStatus = "key1";
        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatus);
        when(redisMessageService.setIfAbsent(redisKeyStatus)).thenReturn(true);

        // Act
        ResponseEntity<InfoVideo> response = processVideoCommuncationController.processVideoStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(redisMessageService).setIfAbsent(redisKeyStatus);
    }

    @Test
    void processVideoStatus_otherStage_setIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class);
        when(infoVideo.getStage()).thenReturn(Stage.PROCESS_VIDEO_QUEUE);
        String redisKeyStatus = "key1";
        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatus);
        when(redisMessageService.setIfAbsent(redisKeyStatus)).thenReturn(false);

        // Act
        ResponseEntity<InfoVideo> response = processVideoCommuncationController.processVideoStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(redisMessageService).setIfAbsent(redisKeyStatus);
    }


    @Test
    void fetchQueue_shouldReturnListOfKeys() {
        // Arrange
        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3");
        when(redisMessageService.fetchOnlyKeysWithOutNextPostion(Stage.PROCESS_VIDEO_QUEUE)).thenReturn(expectedKeys);

        // Act
        ResponseEntity<List<String>> response = processVideoCommuncationController.fetchQueue();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedKeys, response.getBody());
        verify(redisMessageService).fetchOnlyKeysWithOutNextPostion(Stage.PROCESS_VIDEO_QUEUE);
    }

    @Test
    void fixProcessVideo_shouldDeleteKeysAndSetIfAbsent() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class);
        when(infoVideo.getVideoId()).thenReturn(2l);
        when(infoVideo.getVersion()).thenReturn(1);
        when(infoVideo.redisKeyStatus()).thenReturn("key1");

        // Act
        ResponseEntity<Map<String, Boolean>> response = processVideoCommuncationController.fixProcessVideo(infoVideo);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertTrue(response.getBody().get("message"));
        verify(redisMessageService).deleteKeysByPattern("*:2:*:1:PROCESS_VIDEO*");
        verify(redisMessageService).setIfAbsent("key1");
    }
}