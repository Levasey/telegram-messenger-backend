package com.telegram.messenger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.bot")
public class TelegramBotProperties {

	/**
	 * Токен от BotFather. Если пустой — ответы в Telegram не отправляются.
	 */
	private String token = "";

	/**
	 * Шаблон приветствия; подстановка {name} — имя из профиля Telegram (или «гость»).
	 */
	private String welcomeMessage = "Добро пожаловать, {name}! Рады видеть вас.";

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String getWelcomeMessage() {
		return welcomeMessage;
	}

	public void setWelcomeMessage(String welcomeMessage) {
		this.welcomeMessage = welcomeMessage;
	}
}
