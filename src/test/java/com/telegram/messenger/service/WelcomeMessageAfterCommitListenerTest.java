package com.telegram.messenger.service;

import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telegram.messenger.telegram.TelegramApiClient;

@ExtendWith(MockitoExtension.class)
class WelcomeMessageAfterCommitListenerTest {

	@Mock
	private TelegramApiClient telegramApiClient;

	private WelcomeMessageAfterCommitListener listener;

	@BeforeEach
	void setUp() {
		listener = new WelcomeMessageAfterCommitListener(telegramApiClient);
	}

	@Test
	void forwardsIntentToTelegramClient() {
		listener.onWelcomeIntent(new WelcomeSendIntent(7L, "hello"));

		verify(telegramApiClient).sendMessage(7L, "hello");
	}
}
