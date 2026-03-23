package com.telegram.messenger.service;

/**
 * Событие с данными для отправки приветствия; публикуется из транзакции и обрабатывается
 * {@link WelcomeMessageAfterCommitListener} после успешного коммита.
 */
public record WelcomeSendIntent(long chatId, String text) {
}
