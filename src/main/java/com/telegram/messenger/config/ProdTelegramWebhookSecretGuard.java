package com.telegram.messenger.config;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * В профиле {@code prod} пустой секрет webhook недопустим — иначе эндпоинт остаётся открытым для поддельных апдейтов.
 */
@Component
@Profile("prod")
public class ProdTelegramWebhookSecretGuard implements ApplicationRunner {

	private final TelegramBotProperties botProperties;

	public ProdTelegramWebhookSecretGuard(TelegramBotProperties botProperties) {
		this.botProperties = botProperties;
	}

	@Override
	public void run(ApplicationArguments args) {
		String secret = botProperties.getWebhookSecretToken();
		if (secret == null || secret.isBlank()) {
			throw new IllegalStateException(
					"Profile 'prod' requires non-empty telegram.bot.webhook-secret-token "
							+ "(or env TELEGRAM_WEBHOOK_SECRET). Pass the same value as secret_token in setWebhook.");
		}
	}
}
