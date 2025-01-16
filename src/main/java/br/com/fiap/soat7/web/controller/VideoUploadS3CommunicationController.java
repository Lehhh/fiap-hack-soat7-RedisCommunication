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
@RequestMapping("/redis-message/video/s3")
@RequiredArgsConstructor
public class VideoUploadS3CommunicationController {

	private final RedisMessageService redisMessageService;

	@GetMapping
	ResponseEntity<List<String>> fetchKeysByVideo(@RequestParam String videoId){
		return ResponseEntity.ok(redisMessageService.fetchAllKeys("*:" + videoId + ":*:*"));
	}
	@GetMapping("/queue")
	ResponseEntity<List<String>> fetchQueue(){
		return ResponseEntity.ok(redisMessageService.fetchOnlyKeysWithOutNextPostion(Stage.UPLOAD_S3_QUEUE));
	}

	@PostMapping("/status")
	public ResponseEntity<InfoVideo> uploadToS3Status(@RequestBody InfoVideo infoVideo){
		boolean transactionStatus = false;
		if(infoVideo.getStage().equals(Stage.UPLOAD_S3_DONE)){
			transactionStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			infoVideo.setStage(Stage.PROCESS_VIDEO_QUEUE);
			boolean queueStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			return transactionStatus && queueStatus ? ResponseEntity.ok(infoVideo) : ResponseEntity.notFound().build();
		}
		else{
			transactionStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
			return transactionStatus ? ResponseEntity.ok(infoVideo) : ResponseEntity.notFound().build();

		}
	}
	@DeleteMapping("/fix")
	public ResponseEntity<Map<String, Boolean>> fixSendVideo(@RequestBody InfoVideo infoVideo){
		redisMessageService.deleteKeysByPattern("*:"+ infoVideo.getVideoId() + ":*:" +infoVideo.getVersion() + ":UPLOAD_S3*");
		redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
		return ResponseEntity.ok(Map.of("message", true));
	}
}
