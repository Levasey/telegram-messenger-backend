package com.telegram.messenger.telegram;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import com.telegram.messenger.config.TelegramBotProperties;

@Component
public class TelegramApiClient {

	private static final Logger log = LoggerFactory.getLogger(TelegramApiClient.class);

	private final TelegramBotProperties properties;
	private final RestClient restClient;

	public TelegramApiClient(TelegramBotProperties properties) {
		this.properties = properties;
		this.restClient = RestClient.builder()
				.baseUrl("https://api.telegram.org")
				.build();
	}

	public void sendMessage(long chatId, String text) {
		if (!StringUtils.hasText(properties.getToken())) {
			log.debug("TELEGRAM_BOT_TOKEN пуст — сообщение не отправлено");
			return;
		}
		String path = "/bot" + properties.getToken() + "/sendMessage";
		try {
			restClient.post()
					.uri(path)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"chat_id", chatId,
							"text", text
					))
					.retrieve()
					.toBodilessEntity();
		}
		catch (Exception e) {
			log.warn("Не удалось отправить сообщение в Telegram: {}", e.getMessage());
		}
	}
}
