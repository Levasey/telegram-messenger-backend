package com.telegram.messenger.telegram;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClient;

import com.telegram.messenger.config.TelegramBotProperties;
import com.telegram.messenger.telegram.dto.TelegramBotApiResponse;

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

	/**
	 * @return {@code true}, если Telegram вернул {@code ok: true}; иначе {@code false} (в т.ч. при сетевой ошибке)
	 */
	public boolean sendMessage(long chatId, String text) {
		if (!StringUtils.hasText(properties.getToken())) {
			log.debug("TELEGRAM_BOT_TOKEN пуст — сообщение не отправлено");
			return false;
		}
		String path = "/bot" + properties.getToken() + "/sendMessage";
		try {
			TelegramBotApiResponse response = restClient.post()
					.uri(path)
					.contentType(MediaType.APPLICATION_JSON)
					.body(Map.of(
							"chat_id", chatId,
							"text", text
					))
					.retrieve()
					.body(TelegramBotApiResponse.class);
			if (response == null) {
				log.warn("Telegram sendMessage: пустое тело ответа (chat_id={})", chatId);
				return false;
			}
			if (!response.isOk()) {
				log.warn(
						"Telegram sendMessage отклонён: chat_id={}, error_code={}, description={}",
						chatId,
						response.getErrorCode(),
						response.getDescription());
				return false;
			}
			return true;
		}
		catch (RestClientException e) {
			log.warn("Не удалось отправить сообщение в Telegram (chat_id={}): {}", chatId, e.getMessage());
			return false;
		}
	}
}
