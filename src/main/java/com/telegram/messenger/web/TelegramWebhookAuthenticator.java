package com.telegram.messenger.web;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

import org.springframework.stereotype.Component;

import com.telegram.messenger.config.TelegramBotProperties;

/**
 * Проверка заголовка {@code X-Telegram-Bot-Api-Secret-Token} при заданном
 * {@link TelegramBotProperties#getWebhookSecretToken() secret_token} в {@code setWebhook}.
 */
@Component
public class TelegramWebhookAuthenticator {

	public static final String SECRET_HEADER = "X-Telegram-Bot-Api-Secret-Token";

	private final TelegramBotProperties botProperties;

	public TelegramWebhookAuthenticator(TelegramBotProperties botProperties) {
		this.botProperties = botProperties;
	}

	public boolean isAuthorized(String headerValue) {
		String expected = botProperties.getWebhookSecretToken();
		if (expected == null || expected.isBlank()) {
			return true;
		}
		if (headerValue == null || headerValue.isBlank()) {
			return false;
		}
		byte[] a = expected.getBytes(StandardCharsets.UTF_8);
		byte[] b = headerValue.getBytes(StandardCharsets.UTF_8);
		return MessageDigest.isEqual(a, b);
	}
}
