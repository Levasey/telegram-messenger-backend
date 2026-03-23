package com.telegram.messenger.web;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.telegram.messenger.config.TelegramBotProperties;

class TelegramWebhookAuthenticatorTest {

	@Test
	void blankSecret_allowsAnyHeader() {
		TelegramBotProperties props = new TelegramBotProperties();
		props.setWebhookSecretToken("");
		TelegramWebhookAuthenticator auth = new TelegramWebhookAuthenticator(props);

		assertThat(auth.isAuthorized(null)).isTrue();
		assertThat(auth.isAuthorized("")).isTrue();
		assertThat(auth.isAuthorized("anything")).isTrue();
	}

	@Test
	void secretSet_requiresMatchingHeader() {
		TelegramBotProperties props = new TelegramBotProperties();
		props.setWebhookSecretToken("correct");
		TelegramWebhookAuthenticator auth = new TelegramWebhookAuthenticator(props);

		assertThat(auth.isAuthorized(null)).isFalse();
		assertThat(auth.isAuthorized("wrong")).isFalse();
		assertThat(auth.isAuthorized("correct")).isTrue();
	}
}
