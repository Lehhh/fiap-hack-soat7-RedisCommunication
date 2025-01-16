package br.com.fiap.soat7.web.controller;

import br.com.fiap.soat7.application.service.RedisMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/redis-message")
@RequiredArgsConstructor
public class RedisCommunicationController {

	private final RedisMessageService redisMessageService;

	@GetMapping("/user")
	ResponseEntity<List<String>> fetchKeysByUser(@RequestParam String userId){
		return ResponseEntity.ok(redisMessageService.fetchAllKeys(userId + ":*:*:*"));
	}
	@GetMapping("/status")
	ResponseEntity<List<String>> fetchKeysByStatus(@RequestParam String status){
		return ResponseEntity.ok(redisMessageService.fetchAllKeys("*:*:*:*:" + status));
	}

}
