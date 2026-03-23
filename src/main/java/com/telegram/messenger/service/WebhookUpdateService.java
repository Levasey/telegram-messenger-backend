package com.telegram.messenger.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.messenger.telegram.TelegramApiClient;
import com.telegram.messenger.telegram.dto.TelegramUpdateDto;

@Service
public class WebhookUpdateService {

	private static final Logger log = LoggerFactory.getLogger(WebhookUpdateService.class);

	private final ObjectMapper objectMapper;
	private final WebhookMessageTransactionService messageTransactionService;
	private final TelegramApiClient telegramApiClient;

	public WebhookUpdateService(
			ObjectMapper objectMapper,
			WebhookMessageTransactionService messageTransactionService,
			TelegramApiClient telegramApiClient) {
		this.objectMapper = objectMapper;
		this.messageTransactionService = messageTransactionService;
		this.telegramApiClient = telegramApiClient;
	}

	public void handleRawUpdate(String body) {
		if (!StringUtils.hasText(body)) {
			return;
		}
		try {
			TelegramUpdateDto update = objectMapper.readValue(body, TelegramUpdateDto.class);
			if (update.getMessage() == null) {
				return;
			}
			messageTransactionService
					.upsertClientAndMaybeWelcomeIntent(update.getMessage())
					.ifPresent(intent -> telegramApiClient.sendMessage(intent.chatId(), intent.text()));
		}
		catch (Exception e) {
			log.warn("Не удалось разобрать webhook: {}", e.getMessage());
		}
	}
}
