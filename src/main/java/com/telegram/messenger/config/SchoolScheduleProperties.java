package com.telegram.messenger.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "telegram.school")
public class SchoolScheduleProperties {

	/**
	 * Часовой пояс школы для разбора даты в /book и для напоминаний.
	 */
	private String timeZone = "Europe/Moscow";

	/**
	 * Cron выражение Spring: когда проверять записи и слать напоминание на завтра.
	 */
	private String reminderCron = "0 0 9 * * *";

	public String getTimeZone() {
		return timeZone;
	}

	public void setTimeZone(String timeZone) {
		this.timeZone = timeZone;
	}

	public String getReminderCron() {
		return reminderCron;
	}

	public void setReminderCron(String reminderCron) {
		this.reminderCron = reminderCron;
	}
}
