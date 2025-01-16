package br.com.fiap.soat7.web.controller;

import br.com.fiap.soat7.application.service.RedisMessageService;
import br.com.fiap.soat7.domain.dto.InfoVideo;
import br.com.fiap.soat7.domain.enums.Stage;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/redis-message/video/process")
@RequiredArgsConstructor
public class ProcessVideoCommuncationController {


	private final RedisMessageService redisMessageService;

	@PostMapping("/status")
	public ResponseEntity<InfoVideo> processVideoStatus(@RequestBody InfoVideo infoVideo){
		boolean transactionStatus = false;
		if(infoVideo.getStage().equals(Stage.PROCESS_VIDEO_DONE)){
			transactionStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			infoVideo.setStage(Stage.UPLOAD_S3_IMAGES_QUEUE);
			boolean uploadS3Image = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			return transactionStatus && uploadS3Image ? ResponseEntity.ok(infoVideo) : ResponseEntity.notFound().build();
		}
		else{
			transactionStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			return transactionStatus ? ResponseEntity.ok(infoVideo) : ResponseEntity.notFound().build();
		}
	}
	@DeleteMapping("/fix")
	public ResponseEntity<Map<String, Boolean>> fixProcessVideo(@RequestBody InfoVideo infoVideo){
		redisMessageService.deleteKeysByPattern("*:"+ infoVideo.getVideoId() + ":*:"+ infoVideo.getVersion() + ":PROCESS_VIDEO*");
		redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
		return ResponseEntity.ok(Map.of("message", true));
	}

	@GetMapping("/queue")
	ResponseEntity<List<String>> fetchQueue(){
		return ResponseEntity.ok(redisMessageService.fetchOnlyKeysWithOutNextPostion(Stage.PROCESS_VIDEO_QUEUE));
	}
}
