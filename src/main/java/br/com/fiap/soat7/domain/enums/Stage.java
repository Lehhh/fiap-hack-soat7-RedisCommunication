package br.com.fiap.soat7.domain.enums;

import lombok.Getter;

import java.util.Arrays;
import java.util.List;

@Getter
public enum Stage {

	UPLOAD_DISK_DONE("upload_disk_done", 1),
	UPLOAD_S3_QUEUE("upload_s3_queue",2),
	UPLOAD_S3_IN_PROGRESS("upload_s3_in_progress",3),
	UPLOAD_S3_DONE("upload_s3_done",4),
	UPLOAD_S3_ERROR("upload_s3_error", 5),
	PROCESS_VIDEO_QUEUE("process_video_queue", 6),
	PROCESS_VIDEO_IN_PROGRESS("process_video_in_progress", 7),
	PROCESS_VIDEO_DONE("process_video_done", 8),
	PROCESS_VIDEO_ERROR("process_video_error", 9),
	UPLOAD_S3_IMAGES_QUEUE("upload_s3_images_queue", 10),
	UPLOAD_S3_IMAGES_IN_PROGRESS("upload_s3_images_in_progress", 11),
	UPLOAD_S3_IMAGES_DONE("upload_s3_images_done",12),
	UPLOAD_S3_IMAGES_ERROR("upload_s3_images_error", 13);


	private final String name;
	private final Integer position;

	Stage(String name, Integer position) {
		this.name = name;
		this.position = position;
	}

	public static Stage fetchStageOnly(Stage stage){
		int nextPosition = stage.getPosition() + 1;
		return Arrays.stream(values()).filter(p -> p.getPosition()== nextPosition).findFirst().orElse(stage);
	}
}
