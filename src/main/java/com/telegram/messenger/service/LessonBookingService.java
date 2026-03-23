package com.telegram.messenger.service;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.telegram.messenger.domain.Client;
import com.telegram.messenger.domain.LessonAppointment;
import com.telegram.messenger.repo.LessonAppointmentRepository;

@Service
public class LessonBookingService {

	private static final DateTimeFormatter ISO_SPACE = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
	private static final DateTimeFormatter RU_SPACE = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

	private final LessonAppointmentRepository appointmentRepository;

	public LessonBookingService(LessonAppointmentRepository appointmentRepository) {
		this.appointmentRepository = appointmentRepository;
	}

	/**
	 * Обрабатывает команду {@code /book ...}; клиент должен быть уже сохранён (есть id).
	 *
	 * @return текст ответа пользователю
	 */
	@Transactional
	public String handleBookCommand(Client client, String messageText, ZoneId schoolZone) {
		if (client.getId() == null) {
			return "Внутренняя ошибка: клиент не сохранён. Напишите ещё раз.";
		}
		String args = WebhookMessageTransactionService.restAfterFirstToken(messageText);
		if (!StringUtils.hasText(args)) {
			return bookingHelpText();
		}
		String normalized = args.replaceAll("\\s+", " ").trim();
		Optional<LocalDateTime> local = parseLocalDateTime(normalized);
		if (local.isEmpty()) {
			return "Не удалось разобрать дату и время. Примеры:\n"
					+ "/book 25.03.2026 18:00\n"
					+ "/book 2026-03-25 18:00";
		}
		Instant lessonAt = local.get().atZone(schoolZone).toInstant();
		if (!lessonAt.isAfter(Instant.now())) {
			return "Нельзя записаться на прошедшее время.";
		}
		if (appointmentRepository.existsByClientIdAndLessonAt(client.getId(), lessonAt)) {
			return "Вы уже записаны на это время.";
		}
		LessonAppointment row = new LessonAppointment();
		row.setClient(client);
		row.setLessonAt(lessonAt);
		appointmentRepository.save(row);
		String when = lessonAt.atZone(schoolZone).format(DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm"));
		return "Вы записаны на " + when + " ("
				+ schoolZone.getId()
				+ "). Накануне занятия пришлём напоминание в Telegram.";
	}

	static Optional<LocalDateTime> parseLocalDateTime(String normalizedArgs) {
		for (DateTimeFormatter f : new DateTimeFormatter[] { RU_SPACE, ISO_SPACE }) {
			try {
				return Optional.of(LocalDateTime.parse(normalizedArgs, f));
			}
			catch (DateTimeParseException ignored) {
				// try next
			}
		}
		return Optional.empty();
	}

	private static String bookingHelpText() {
		return """
				Запись на занятие: укажите дату и время в часовом поясе школы.

				Примеры:
				/book 25.03.2026 18:00
				/book 2026-03-25 18:00

				Накануне занятия бот отправит напоминание.""";
	}
}
