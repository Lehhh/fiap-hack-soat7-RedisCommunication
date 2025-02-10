package br.com.fiap.soat7.domain.enums;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class StageTest {

    @Test
    void fetchStageOnly_shouldReturnNextStage() {
        // Arrange
        Stage currentStage = Stage.UPLOAD_DISK_DONE;

        // Act
        Stage nextStage = Stage.fetchStageOnly(currentStage);

        // Assert
        assertEquals(Stage.UPLOAD_S3_QUEUE, nextStage);
    }

    @Test
    void fetchStageOnly_shouldReturnSameStageIfNoNextStage() {
        // Arrange
        Stage currentStage = Stage.UPLOAD_S3_IMAGES_ERROR;

        // Act
        Stage nextStage = Stage.fetchStageOnly(currentStage);

        // Assert
        assertEquals(currentStage, nextStage);
    }
}