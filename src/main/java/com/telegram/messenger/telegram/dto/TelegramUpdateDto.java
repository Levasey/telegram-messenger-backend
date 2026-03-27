package com.telegram.messenger.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramUpdateDto {

	@JsonProperty("update_id")
	private Long updateId;

	private TelegramMessageDto message;

	/** Сообщение из подключённого Business-аккаунта (Bot API 7.2+); иначе апдейт приходит в {@link #message}. */
	@JsonProperty("business_message")
	private TelegramMessageDto businessMessage;

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

	public TelegramMessageDto getBusinessMessage() {
		return businessMessage;
	}

	public void setBusinessMessage(TelegramMessageDto businessMessage) {
		this.businessMessage = businessMessage;
	}

	/** Сообщение для обработки вебхуком: обычное или бизнес-сообщение. */
	public TelegramMessageDto resolveIncomingMessage() {
		if (message != null) {
			return message;
		}
		return businessMessage;
	}
}
