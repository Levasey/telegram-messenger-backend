package com.telegram.messenger.service;

import java.time.ZoneId;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.telegram.messenger.config.SchoolScheduleProperties;

@Component
public class LessonReminderScheduler {

	private static final Logger log = LoggerFactory.getLogger(LessonReminderScheduler.class);

	private final BookingReminderService bookingReminderService;
	private final SchoolScheduleProperties schoolScheduleProperties;

	public LessonReminderScheduler(
			BookingReminderService bookingReminderService,
			SchoolScheduleProperties schoolScheduleProperties) {
		this.bookingReminderService = bookingReminderService;
		this.schoolScheduleProperties = schoolScheduleProperties;
	}

	@Scheduled(cron = "${telegram.school.reminder-cron:0 0 9 * * *}", zone = "${telegram.school.time-zone:Europe/Moscow}")
	public void sendTomorrowReminders() {
		ZoneId zone = ZoneId.of(schoolScheduleProperties.getTimeZone());
		int n = bookingReminderService.sendRemindersForTomorrow(zone);
		if (n > 0) {
			log.debug("Отправлены напоминания о занятиях на завтра: {}", n);
		}
	}
}
