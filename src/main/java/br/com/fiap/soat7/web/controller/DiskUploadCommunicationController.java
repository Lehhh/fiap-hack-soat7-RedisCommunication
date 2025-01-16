package br.com.fiap.soat7.web.controller;

import br.com.fiap.soat7.application.service.RedisMessageService;
import br.com.fiap.soat7.domain.dto.InfoVideo;
import br.com.fiap.soat7.domain.enums.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/redis-message/disk")
@RequiredArgsConstructor
@Log4j2
public class DiskUploadCommunicationController {

	private final RedisMessageService redisMessageService;

	@PostMapping("/status")
	public ResponseEntity<InfoVideo> uploadInDiskStatus(@RequestBody InfoVideo infoVideo){
		boolean transactionStatus = false;
		log.info(infoVideo.toString());
		if(infoVideo.getStage().equals(Stage.UPLOAD_DISK_DONE)){
			transactionStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			infoVideo.setStage(Stage.UPLOAD_S3_QUEUE);
			boolean queueStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			return transactionStatus && queueStatus ? ResponseEntity.ok(infoVideo) : ResponseEntity.notFound().build();
		}
		else{
			transactionStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			return transactionStatus ? ResponseEntity.ok(infoVideo) : ResponseEntity.notFound().build();
		}
	}

}
