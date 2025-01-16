package br.com.fiap.soat7.application.service;

import br.com.fiap.soat7.domain.enums.Stage;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Log4j2
public class RedisMessageService {

	private final RedisTemplate<String, Object> redisTemplate;

	public boolean setIfAbsent(String key) {
		// Tenta configurar o valor na chave, caso não exista
		Boolean success = redisTemplate.opsForValue().setIfAbsent(key, System.currentTimeMillis());
		return Boolean.TRUE.equals(success); // Retorna true se a chave foi configurada com sucesso
	}

	public List<String> fetchAllKeys(String matches) {
		List<String> keys = new ArrayList<>();
		try (RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection()) {
			ScanOptions options = ScanOptions.scanOptions().match(matches).count(100).build();
			Cursor c = redisConnection.scan(options);
			while (c.hasNext()) {
				keys.add(new String((byte[]) c.next()));
			}
		}
		return keys;
	}
	public List<String> fetchOnlyKeysWithOutNextPostion(Stage stage){
		String matches = "*:*:*"+ stage.name();
		List<String> keysStage = new ArrayList<>();
		List<String> keysNextStage = new ArrayList<>();
		try {
			RedisConnection redisConnection = redisTemplate.getConnectionFactory().getConnection();
			ScanOptions options = ScanOptions.scanOptions().match(matches).count(100).build();
			Cursor c = redisConnection.scan(options);
			while (c.hasNext()) {
				keysStage.add(new String((byte[]) c.next()));
			}
			matches = matches.replace(stage.name(), Stage.fetchStageOnly(stage).name());
			options = ScanOptions.scanOptions().match(matches).count(100).build();
			c = redisConnection.scan(options);
			while (c.hasNext()) {
				keysNextStage.add(new String((byte[]) c.next()));
			}
			List<String> keysWithOutNextStage = keysNextStage.stream().map(kn -> kn.substring(0, kn.lastIndexOf(":"))).toList();
			return keysStage.stream().filter(k -> !keysWithOutNextStage.contains(k.substring(0, k.lastIndexOf(":")))).toList();
		}
		catch (Exception e) {
			return new ArrayList<>();
		}
	}


	public void deleteKeysByPattern(String pattern) {
		Set<String> keys = redisTemplate.keys(pattern);
		if (keys != null && !keys.isEmpty()) {
			redisTemplate.delete(keys);
		}
	}
}