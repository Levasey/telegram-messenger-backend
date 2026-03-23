package com.telegram.messenger.telegram.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TelegramMessageDto {

	@JsonProperty("message_id")
	private Long messageId;

	private TelegramUserDto from;

	private TelegramChatDto chat;

	private String text;

	public Long getMessageId() {
		return messageId;
	}

	public void setMessageId(Long messageId) {
		this.messageId = messageId;
	}

	public TelegramUserDto getFrom() {
		return from;
	}

	public void setFrom(TelegramUserDto from) {
		this.from = from;
	}

	public TelegramChatDto getChat() {
		return chat;
	}

	public void setChat(TelegramChatDto chat) {
		this.chat = chat;
	}

	public String getText() {
		return text;
	}

	public void setText(String text) {
		this.text = text;
	}
}
