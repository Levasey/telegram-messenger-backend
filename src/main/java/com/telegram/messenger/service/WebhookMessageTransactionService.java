package com.telegram.messenger.service;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.telegram.messenger.config.TelegramBotProperties;
import com.telegram.messenger.domain.Client;
import com.telegram.messenger.repo.ClientRepository;
import com.telegram.messenger.telegram.dto.TelegramMessageDto;
import com.telegram.messenger.telegram.dto.TelegramUserDto;

@Service
public class WebhookMessageTransactionService {

	private static final Logger log = LoggerFactory.getLogger(WebhookMessageTransactionService.class);

	private final ClientRepository clientRepository;
	private final TelegramBotProperties botProperties;

	public WebhookMessageTransactionService(
			ClientRepository clientRepository,
			TelegramBotProperties botProperties) {
		this.clientRepository = clientRepository;
		this.botProperties = botProperties;
	}

	/**
	 * Только работа с БД; вызов Telegram API выполняется снаружи после коммита.
	 */
	@Transactional
	public Optional<WelcomeSendIntent> upsertClientAndMaybeWelcomeIntent(TelegramMessageDto message) {
		TelegramUserDto from = message.getFrom();
		if (from == null || Boolean.TRUE.equals(from.getBot())) {
			return Optional.empty();
		}
		if (from.getId() == null) {
			log.debug("Пропуск сообщения: у from отсутствует id");
			return Optional.empty();
		}
		if (message.getChat() == null || message.getChat().getId() == null) {
			return Optional.empty();
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

		if (isNew || isStartCommand(message.getText())) {
			String name = displayName(from);
			String text = botProperties.getWelcomeMessage().replace("{name}", name);
			return Optional.of(new WelcomeSendIntent(chatId, text));
		}
		return Optional.empty();
	}

	static boolean isStartCommand(String rawText) {
		String cmd = trimCommand(rawText);
		if (cmd.isEmpty()) {
			return false;
		}
		int at = cmd.indexOf('@');
		if (at > 0) {
			cmd = cmd.substring(0, at);
		}
		return "/start".equalsIgnoreCase(cmd);
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
