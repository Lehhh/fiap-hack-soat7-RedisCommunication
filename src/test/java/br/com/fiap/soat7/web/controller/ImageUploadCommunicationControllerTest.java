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
class ImageUploadCommunicationControllerTest {

    @Mock
    private RedisMessageService redisMessageService;

    @InjectMocks
    private ImageUploadCommunicationController imageUploadCommunicationController;

    @Test
    void fetchQueue_shouldReturnListOfKeys() {
        // Arrange
        List<String> expectedKeys = Arrays.asList("key1", "key2", "key3");
        when(redisMessageService.fetchOnlyKeysWithOutNextPostion(Stage.UPLOAD_S3_IMAGES_QUEUE)).thenReturn(expectedKeys);

        // Act
        ResponseEntity<List<String>> response = imageUploadCommunicationController.fetchQueue();

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedKeys, response.getBody());
        verify(redisMessageService).fetchOnlyKeysWithOutNextPostion(Stage.UPLOAD_S3_IMAGES_QUEUE);
    }

    @Test
    void fetchKeysByImage_shouldReturnListOfKeys() {
        // Arrange
        String imageId = "testImageId";
        String matchPattern = "*:*:" + imageId + "*";
        List<String> expectedKeys = Arrays.asList("key1", "key2");
        when(redisMessageService.fetchAllKeys(matchPattern)).thenReturn(expectedKeys);

        // Act
        ResponseEntity<List<String>> response = imageUploadCommunicationController.fetchKeysByImage(imageId);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedKeys, response.getBody());
        verify(redisMessageService).fetchAllKeys(matchPattern);
    }


    @Test
    void uploadImagesS3Status_setIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class);
        String redisKeyStatus = "testKey";
        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatus);
        //when(infoVideo.getStage()).thenReturn(Stage.UPLOAD_S3_IMAGES_QUEUE);  // Mock getStage()!  Remove this line
        when(redisMessageService.setIfAbsent(redisKeyStatus)).thenReturn(false);

        // Act
        ResponseEntity<InfoVideo> response = imageUploadCommunicationController.uploadImagesS3Status(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(redisMessageService).setIfAbsent(redisKeyStatus);
    }

}