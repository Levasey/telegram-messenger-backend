package com.telegram.messenger.service;

import java.time.ZoneId;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.telegram.messenger.config.SchoolScheduleProperties;
import com.telegram.messenger.config.TelegramBotProperties;
import com.telegram.messenger.domain.Client;
import com.telegram.messenger.repo.ClientRepository;
import com.telegram.messenger.telegram.dto.TelegramChatDto;
import com.telegram.messenger.telegram.dto.TelegramMessageDto;
import com.telegram.messenger.telegram.dto.TelegramUserDto;

@Service
public class WebhookMessageTransactionService {

	private static final Logger log = LoggerFactory.getLogger(WebhookMessageTransactionService.class);

	private final ClientRepository clientRepository;
	private final TelegramBotProperties botProperties;
	private final SchoolScheduleProperties schoolScheduleProperties;
	private final LessonBookingService lessonBookingService;
	private final ApplicationEventPublisher eventPublisher;

	public WebhookMessageTransactionService(
			ClientRepository clientRepository,
			TelegramBotProperties botProperties,
			SchoolScheduleProperties schoolScheduleProperties,
			LessonBookingService lessonBookingService,
			ApplicationEventPublisher eventPublisher) {
		this.clientRepository = clientRepository;
		this.botProperties = botProperties;
		this.schoolScheduleProperties = schoolScheduleProperties;
		this.lessonBookingService = lessonBookingService;
		this.eventPublisher = eventPublisher;
	}

	/**
	 * Сохранение клиента в транзакции; при необходимости публикует {@link WelcomeSendIntent}
	 * (в личке — на каждое сообщение, в группах — при первом обращении или {@code /start}),
	 * обрабатывается после успешного коммита (см. {@link WelcomeMessageAfterCommitListener}).
	 */
	@Transactional
	public void upsertClientAndMaybeWelcomeIntent(TelegramMessageDto message) {
		TelegramUserDto from = message.getFrom();
		if (from == null || Boolean.TRUE.equals(from.getBot())) {
			return;
		}
		Long telegramUserId = from.getId();
		if (telegramUserId == null) {
			log.debug("Пропуск сообщения: у from отсутствует id");
			return;
		}
		if (message.getChat() == null || message.getChat().getId() == null) {
			return;
		}
		long chatId = message.getChat().getId();
		Optional<Client> existing = clientRepository.findByTelegramUserId(telegramUserId);
		boolean isNew = existing.isEmpty();
		Client client = existing.orElseGet(Client::new);
		client.setTelegramUserId(telegramUserId);
		client.setUsername(from.getUsername());
		client.setFirstName(from.getFirstName());
		client.setLastName(from.getLastName());
		clientRepository.save(client);

		if (shouldSendWelcome(message, isNew)) {
			String name = displayName(from);
			String text = botProperties.getWelcomeMessage().replace("{name}", name);
			eventPublisher.publishEvent(new WelcomeSendIntent(chatId, text));
		}

		if (isBookCommand(message.getText())) {
			ZoneId schoolZone = ZoneId.of(schoolScheduleProperties.getTimeZone());
			String bookingReply = lessonBookingService.handleBookCommand(client, message.getText(), schoolZone);
			eventPublisher.publishEvent(new WelcomeSendIntent(chatId, bookingReply));
		}
	}

	/**
	 * Команда /start, в т.ч. в группах как {@code /start@BotUsername} (Telegram шлёт такой текст в апдейте).
	 * При необходимости точнее можно опираться на {@code entities} типа bot_command в DTO.
	 */
	static boolean isStartCommand(String rawText) {
		String cmd = firstToken(rawText);
		if (cmd.isEmpty()) {
			return false;
		}
		int at = cmd.indexOf('@');
		if (at > 0) {
			cmd = cmd.substring(0, at);
		}
		return "/start".equalsIgnoreCase(cmd);
	}

	/**
	 * Команда {@code /book}, в т.ч. {@code /book@BotUsername}.
	 */
	static boolean isBookCommand(String rawText) {
		String cmd = firstToken(rawText);
		if (cmd.isEmpty()) {
			return false;
		}
		int at = cmd.indexOf('@');
		if (at > 0) {
			cmd = cmd.substring(0, at);
		}
		return "/book".equalsIgnoreCase(cmd);
	}

	/**
	 * Текст после первого токена (команды), без ведущих пробелов.
	 */
	static String restAfterFirstToken(String rawText) {
		if (rawText == null) {
			return "";
		}
		String t = rawText.trim();
		int n = t.length();
		int i = 0;
		while (i < n && !Character.isWhitespace(t.charAt(i))) {
			i++;
		}
		while (i < n && Character.isWhitespace(t.charAt(i))) {
			i++;
		}
		return t.substring(i).trim();
	}

	/** Первый «словесный» токен текста (до любого whitespace), после trim всего текста. */
	static String firstToken(String text) {
		if (text == null) {
			return "";
		}
		String t = text.trim();
		int n = t.length();
		int i = 0;
		while (i < n && !Character.isWhitespace(t.charAt(i))) {
			i++;
		}
		return t.substring(0, i);
	}

	/**
	 * В личных чатах — приветствие на каждое сообщение; в группах и супергруппах — только при первом
	 * сохранении клиента или по команде {@code /start}, чтобы не заспамить чат.
	 */
	static boolean shouldSendWelcome(TelegramMessageDto message, boolean isNewClient) {
		return isPrivateChat(message) || isNewClient || isStartCommand(message.getText());
	}

	private static boolean isPrivateChat(TelegramMessageDto message) {
		TelegramChatDto chat = message.getChat();
		return chat != null && "private".equalsIgnoreCase(chat.getType());
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
