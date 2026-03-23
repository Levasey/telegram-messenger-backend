package com.telegram.messenger.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdateDto {

	@JsonProperty("update_id")
	private Long updateId;

	private TelegramMessageDto message;

	public Long getUpdateId() {
		return updateId;
	}

	public void setUpdateId(Long updateId) {
		this.updateId = updateId;
	}

	public TelegramMessageDto getMessage() {
		return message;
	}

	public void setMessage(TelegramMessageDto message) {
		this.message = message;
	}
}
