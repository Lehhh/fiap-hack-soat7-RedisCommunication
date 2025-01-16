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
@RequestMapping("/redis-message/images")
@RequiredArgsConstructor
public class ImageUploadCommunicationController {

	private final RedisMessageService redisMessageService;

	@GetMapping("/queue")
	ResponseEntity<List<String>> fetchQueue(){
		return ResponseEntity.ok(redisMessageService.fetchOnlyKeysWithOutNextPostion(Stage.UPLOAD_S3_IMAGES_QUEUE));
	}

	@GetMapping
	public ResponseEntity<List<String>> fetchKeysByImage(@RequestParam String imageId){
		return ResponseEntity.ok(redisMessageService.fetchAllKeys("*:*:" + imageId + "*"));
	}

	@PostMapping("/status")
	public ResponseEntity<InfoVideo> uploadImagesS3Status(@RequestBody InfoVideo infoVideo){
		boolean transactionStatus = redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
		return transactionStatus ? ResponseEntity.ok(infoVideo) : ResponseEntity.notFound().build();
	}

	@DeleteMapping("/fix")
	public ResponseEntity<Map<String, Boolean>> fixImage(@RequestBody InfoVideo infoVideo){
		redisMessageService.deleteKeysByPattern("*:*:"+infoVideo.getImageId() + ":" +  infoVideo.getVersion()  +":UPLOAD_S3_IMAGES*");
		infoVideo.setStage(Stage.UPLOAD_S3_IMAGES_QUEUE);
		redisMessageService.setIfAbsent(infoVideo.redisKeyStatus());
		return ResponseEntity.ok(Map.of("message", true));
	}
}
