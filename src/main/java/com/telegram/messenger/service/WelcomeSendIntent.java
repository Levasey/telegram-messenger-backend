package com.telegram.messenger.service;

/**
 * Данные для отправки приветствия после успешного коммита транзакции с клиентом.
 */
public record WelcomeSendIntent(long chatId, String text) {
}
