package br.com.fiap.soat7.web.controller;

import br.com.fiap.soat7.application.service.RedisMessageService;
import br.com.fiap.soat7.domain.dto.InfoVideo;
import br.com.fiap.soat7.domain.enums.Stage;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiskUploadCommunicationControllerTest {

    @Mock
    private RedisMessageService redisMessageService;

    @InjectMocks
    private DiskUploadCommunicationController diskUploadCommunicationController;

    private InfoVideo infoVideo;
    @BeforeEach
    void setUp() {
        infoVideo = new InfoVideo();
        infoVideo.setStage(Stage.UPLOAD_S3_QUEUE);
    }

    @Test
    void testUploadInDiskStatus_WhenStageIsNotUploadDiskDone_ShouldExecuteElseBlock() {
        // Configurando o comportamento simulado do RedisMessageService
        when(redisMessageService.setIfAbsent(anyString())).thenReturn(true);

        // Executando o método
        ResponseEntity<InfoVideo> response = diskUploadCommunicationController.uploadInDiskStatus(infoVideo);

        // Verificando se o método retornou a resposta esperada
        assertNotNull(response);
        assertEquals(200, response.getStatusCodeValue());
        assertEquals(infoVideo, response.getBody());

        // Verificando se o método foi chamado corretamente
        verify(redisMessageService, times(1)).setIfAbsent(anyString());
    }

    @Test
    void testUploadInDiskStatus_WhenRedisFails_ShouldReturnNotFound() {
        // Simulando falha ao gravar no Redis
        when(redisMessageService.setIfAbsent(anyString())).thenReturn(false);

        // Executando o método
        ResponseEntity<InfoVideo> response = diskUploadCommunicationController.uploadInDiskStatus(infoVideo);

        // Verificando se o método retornou a resposta esperada
        assertNotNull(response);
        assertEquals(404, response.getStatusCodeValue());
        assertNull(response.getBody());

        // Verificando se o método foi chamado corretamente
        verify(redisMessageService, times(1)).setIfAbsent(anyString());
    }

    @Test
    void uploadInDiskStatus_uploadDiskDoneStage_shouldSetAbsentAndReturnOk() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class); // Create the mock object

        //Mock the getStage method
        when(infoVideo.getStage()).thenReturn(Stage.UPLOAD_DISK_DONE);

        String redisKeyStatusUploadDiskDone = "redisKey1";
        String redisKeyStatusUploadS3Queue = "redisKey2";

        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatusUploadDiskDone).thenReturn(redisKeyStatusUploadS3Queue); // Mocking chained calls
        doNothing().when(infoVideo).setStage(any(Stage.class));


        when(redisMessageService.setIfAbsent(redisKeyStatusUploadDiskDone)).thenReturn(true);
        when(redisMessageService.setIfAbsent(redisKeyStatusUploadS3Queue)).thenReturn(true);

        // Act
        ResponseEntity<InfoVideo> response = diskUploadCommunicationController.uploadInDiskStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
        verify(redisMessageService).setIfAbsent(redisKeyStatusUploadDiskDone);
        verify(redisMessageService).setIfAbsent(redisKeyStatusUploadS3Queue);
    }

    @Test
    void uploadInDiskStatus_uploadDiskDoneStage_setIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class); // Create the mock object

        //Mock the getStage method
        when(infoVideo.getStage()).thenReturn(Stage.UPLOAD_DISK_DONE);

        String redisKeyStatusUploadDiskDone = "redisKey1";
        String redisKeyStatusUploadS3Queue = "redisKey2";

        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatusUploadDiskDone).thenReturn(redisKeyStatusUploadS3Queue); // Mocking chained calls

        doNothing().when(infoVideo).setStage(any(Stage.class));

        when(redisMessageService.setIfAbsent(redisKeyStatusUploadDiskDone)).thenReturn(false);  // First set fails
        when(redisMessageService.setIfAbsent(redisKeyStatusUploadS3Queue)).thenReturn(true); // Second set returns true (doesn't matter because first already failed)

        // Act
        ResponseEntity<InfoVideo> response = diskUploadCommunicationController.uploadInDiskStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
        verify(redisMessageService).setIfAbsent(redisKeyStatusUploadDiskDone);
    }

    @Test
    void uploadInDiskStatus_otherStage_shouldSetAbsentAndReturnOk() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class); // Create the mock object

        when(infoVideo.getStage()).thenReturn(Stage.UPLOAD_DISK_DONE);

        String redisKeyStatus = "redisKey1";
        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatus);
        when(redisMessageService.setIfAbsent(redisKeyStatus)).thenReturn(true);
        doNothing().when(infoVideo).setStage(any(Stage.class));


        // Act
        ResponseEntity<InfoVideo> response = diskUploadCommunicationController.uploadInDiskStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.OK, response.getStatusCode());
    }

    @Test
    void uploadInDiskStatus_otherStage_setIfAbsentFails_shouldReturnNotFound() {
        // Arrange
        InfoVideo infoVideo = mock(InfoVideo.class); // Create the mock object
        when(infoVideo.getStage()).thenReturn(Stage.UPLOAD_DISK_DONE);

        String redisKeyStatus = "redisKey1";
        when(infoVideo.redisKeyStatus()).thenReturn(redisKeyStatus);
        when(redisMessageService.setIfAbsent(redisKeyStatus)).thenReturn(false);

        //The mock setStage method
        doNothing().when(infoVideo).setStage(any(Stage.class));


        // Act
        ResponseEntity<InfoVideo> response = diskUploadCommunicationController.uploadInDiskStatus(infoVideo);

        // Assert
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertNull(response.getBody());
    }
}