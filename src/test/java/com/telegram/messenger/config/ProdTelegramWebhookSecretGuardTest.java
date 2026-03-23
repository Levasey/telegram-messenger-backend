package com.telegram.messenger.config;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.DefaultApplicationArguments;

class ProdTelegramWebhookSecretGuardTest {

	@Test
	void blankSecret_failsStartup() {
		TelegramBotProperties props = new TelegramBotProperties();
		props.setWebhookSecretToken("  ");
		ProdTelegramWebhookSecretGuard guard = new ProdTelegramWebhookSecretGuard(props);

		assertThatThrownBy(() -> guard.run(new DefaultApplicationArguments()))
				.isInstanceOf(IllegalStateException.class)
				.hasMessageContaining("webhook-secret-token");
	}

	@Test
	void nonBlankSecret_ok() {
		TelegramBotProperties props = new TelegramBotProperties();
		props.setWebhookSecretToken("ok");
		ProdTelegramWebhookSecretGuard guard = new ProdTelegramWebhookSecretGuard(props);

		assertThatCode(() -> guard.run(new DefaultApplicationArguments())).doesNotThrowAnyException();
	}
}
