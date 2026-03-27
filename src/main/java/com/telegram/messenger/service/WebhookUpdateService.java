package com.telegram.messenger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.messenger.telegram.dto.TelegramMessageDto;
import com.telegram.messenger.telegram.dto.TelegramUpdateDto;

@Service
public class WebhookUpdateService {

	private static final Logger log = LoggerFactory.getLogger(WebhookUpdateService.class);

	private final ObjectMapper objectMapper;
	private final WebhookMessageTransactionService messageTransactionService;

	public WebhookUpdateService(
			ObjectMapper objectMapper,
			WebhookMessageTransactionService messageTransactionService) {
		this.objectMapper = objectMapper;
		this.messageTransactionService = messageTransactionService;
	}

	public void handleRawUpdate(String body) {
		if (!StringUtils.hasText(body)) {
			return;
		}
		try {
			TelegramUpdateDto update = objectMapper.readValue(body, TelegramUpdateDto.class);
			TelegramMessageDto incoming = update.resolveIncomingMessage();
			if (incoming == null) {
				return;
			}
			messageTransactionService.upsertClientAndMaybeWelcomeIntent(incoming);
		}
		catch (Exception e) {
			log.warn("Не удалось разобрать webhook: {}", e.getMessage());
		}
	}
}
