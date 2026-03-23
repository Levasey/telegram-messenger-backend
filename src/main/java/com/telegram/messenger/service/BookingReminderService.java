package com.telegram.messenger.service;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.telegram.messenger.domain.LessonAppointment;
import com.telegram.messenger.repo.LessonAppointmentRepository;
import com.telegram.messenger.telegram.TelegramApiClient;

@Service
public class BookingReminderService {

	private static final Logger log = LoggerFactory.getLogger(BookingReminderService.class);

	private final LessonAppointmentRepository appointmentRepository;
	private final TelegramApiClient telegramApiClient;

	public BookingReminderService(
			LessonAppointmentRepository appointmentRepository,
			TelegramApiClient telegramApiClient) {
		this.appointmentRepository = appointmentRepository;
		this.telegramApiClient = telegramApiClient;
	}

	/**
	 * Находит занятия на «завтра» в указанном поясе и отправляет напоминание один раз.
	 *
	 * @return число отправленных напоминаний
	 */
	@Transactional
	public int sendRemindersForTomorrow(ZoneId schoolZone) {
		LocalDate tomorrow = LocalDate.now(schoolZone).plusDays(1);
		Instant from = tomorrow.atStartOfDay(schoolZone).toInstant();
		Instant to = tomorrow.plusDays(1).atStartOfDay(schoolZone).toInstant();
		List<LessonAppointment> due = appointmentRepository
				.findByLessonAtGreaterThanEqualAndLessonAtLessThanAndReminderSentIsFalse(from, to);
		DateTimeFormatter dateFmt = DateTimeFormatter.ofPattern("dd.MM.yyyy").withZone(schoolZone);
		DateTimeFormatter timeFmt = DateTimeFormatter.ofPattern("HH:mm").withZone(schoolZone);
		String dateStr = dateFmt.format(from);
		for (LessonAppointment a : due) {
			long chatId = a.getClient().getTelegramUserId();
			String timeStr = timeFmt.format(a.getLessonAt());
			String text = "Напоминание: завтра, " + dateStr + ", в " + timeStr + " у вас занятие в школе вокала.";
			boolean ok = telegramApiClient.sendMessage(chatId, text);
			if (ok) {
				a.setReminderSent(true);
			}
			else {
				log.warn("Напоминание не доставлено (chat_id={}, appointment_id={})", chatId, a.getId());
			}
		}
		return due.size();
	}
}
