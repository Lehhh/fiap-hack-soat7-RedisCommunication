package br.com.fiap.soat7.domain.dto;

import br.com.fiap.soat7.domain.enums.Stage;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class InfoVideo {

	private Long videoId;
	private Long userId;
	private Long imageId =0L;
	private Integer version;
	private Stage stage;

	public String redisKeyStatus() {
		return userId + ":" + videoId + ":" + imageId + ":" + version + ":"+ stage.name();
	}
	public String redisKeyWithoutStatus() {
		return userId + ":" + videoId + ":" + imageId +  ":" + version;
	}
}
