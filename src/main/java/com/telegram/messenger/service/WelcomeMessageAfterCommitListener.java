package com.telegram.messenger.service;

import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.telegram.messenger.telegram.TelegramApiClient;

@Component
public class WelcomeMessageAfterCommitListener {

	private final TelegramApiClient telegramApiClient;

	public WelcomeMessageAfterCommitListener(TelegramApiClient telegramApiClient) {
		this.telegramApiClient = telegramApiClient;
	}

	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT, fallbackExecution = true)
	public void onWelcomeIntent(WelcomeSendIntent intent) {
		telegramApiClient.sendMessage(intent.chatId(), intent.text());
	}
}
