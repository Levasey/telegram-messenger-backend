package com.telegram.messenger.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.messenger.config.TelegramBotProperties;
import com.telegram.messenger.domain.Client;
import com.telegram.messenger.repo.ClientRepository;
import com.telegram.messenger.telegram.TelegramApiClient;
import com.telegram.messenger.telegram.dto.TelegramMessageDto;
import com.telegram.messenger.telegram.dto.TelegramUpdateDto;
import com.telegram.messenger.telegram.dto.TelegramUserDto;

@Service
public class WebhookUpdateService {

	private static final Logger log = LoggerFactory.getLogger(WebhookUpdateService.class);

	private final ObjectMapper objectMapper;
	private final ClientRepository clientRepository;
	private final TelegramApiClient telegramApiClient;
	private final TelegramBotProperties botProperties;

	public WebhookUpdateService(
			ObjectMapper objectMapper,
			ClientRepository clientRepository,
			TelegramApiClient telegramApiClient,
			TelegramBotProperties botProperties) {
		this.objectMapper = objectMapper;
		this.clientRepository = clientRepository;
		this.telegramApiClient = telegramApiClient;
		this.botProperties = botProperties;
	}

	@Transactional
	public void handleRawUpdate(String body) {
		if (!StringUtils.hasText(body)) {
			return;
		}
		try {
			TelegramUpdateDto update = objectMapper.readValue(body, TelegramUpdateDto.class);
			if (update.getMessage() == null) {
				return;
			}
			processMessage(update.getMessage());
		}
		catch (Exception e) {
			log.warn("Не удалось разобрать webhook: {}", e.getMessage());
		}
	}

	private void processMessage(TelegramMessageDto message) {
		TelegramUserDto from = message.getFrom();
		if (from == null || Boolean.TRUE.equals(from.getBot())) {
			return;
		}
		if (message.getChat() == null || message.getChat().getId() == null) {
			return;
		}
		long chatId = message.getChat().getId();
		Optional<Client> existing = clientRepository.findByTelegramUserId(from.getId());
		boolean isNew = existing.isEmpty();
		Client client = existing.orElseGet(Client::new);
		client.setTelegramUserId(from.getId());
		client.setUsername(from.getUsername());
		client.setFirstName(from.getFirstName());
		client.setLastName(from.getLastName());
		clientRepository.save(client);

		boolean startCommand = "/start".equalsIgnoreCase(trimCommand(message.getText()));
		if (isNew || startCommand) {
			String name = displayName(from);
			String text = botProperties.getWelcomeMessage().replace("{name}", name);
			telegramApiClient.sendMessage(chatId, text);
		}
	}

	private static String trimCommand(String text) {
		if (text == null) {
			return "";
		}
		String t = text.trim();
		int space = t.indexOf(' ');
		return space > 0 ? t.substring(0, space) : t;
	}

	private static String displayName(TelegramUserDto from) {
		if (StringUtils.hasText(from.getFirstName())) {
			return from.getFirstName();
		}
		if (StringUtils.hasText(from.getUsername())) {
			return "@" + from.getUsername();
		}
		return "гость";
	}
}
