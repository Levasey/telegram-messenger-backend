package com.telegram.messenger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.telegram.messenger.domain.Client;
import com.telegram.messenger.domain.LessonAppointment;
import com.telegram.messenger.repo.LessonAppointmentRepository;

@ExtendWith(MockitoExtension.class)
class LessonBookingServiceTest {

	private static final ZoneId MOSCOW = ZoneId.of("Europe/Moscow");

	@Mock
	private LessonAppointmentRepository appointmentRepository;

	private LessonBookingService service;

	@BeforeEach
	void setUp() {
		service = new LessonBookingService(appointmentRepository);
	}

	@Test
	void parseLocalDateTime_russianAndIso() {
		assertThat(LessonBookingService.parseLocalDateTime("25.03.2030 18:00"))
				.contains(LocalDateTime.of(2030, 3, 25, 18, 0));
		assertThat(LessonBookingService.parseLocalDateTime("2030-03-25 09:30"))
				.contains(LocalDateTime.of(2030, 3, 25, 9, 30));
		assertThat(LessonBookingService.parseLocalDateTime("nope")).isEmpty();
	}

	@Test
	void handleBookCommand_savesAppointment() {
		Client client = new Client();
		client.setId(1L);
		client.setTelegramUserId(99L);
		when(appointmentRepository.existsByClientIdAndLessonAt(any(), any())).thenReturn(false);
		when(appointmentRepository.save(any(LessonAppointment.class))).thenAnswer(inv -> inv.getArgument(0));

		String reply = service.handleBookCommand(client, "/book 2030-06-01 15:00", MOSCOW);

		assertThat(reply).contains("записаны");
		ArgumentCaptor<LessonAppointment> cap = ArgumentCaptor.forClass(LessonAppointment.class);
		verify(appointmentRepository).save(cap.capture());
		assertThat(cap.getValue().getClient()).isSameAs(client);
		assertThat(cap.getValue().getLessonAt())
				.isEqualTo(LocalDateTime.of(2030, 6, 1, 15, 0).atZone(MOSCOW).toInstant());
	}

	@Test
	void handleBookCommand_rejectsDuplicate() {
		Client client = new Client();
		client.setId(1L);
		Instant at = LocalDateTime.of(2030, 6, 1, 15, 0).atZone(MOSCOW).toInstant();
		when(appointmentRepository.existsByClientIdAndLessonAt(1L, at)).thenReturn(true);

		String reply = service.handleBookCommand(client, "/book 2030-06-01 15:00", MOSCOW);

		assertThat(reply).contains("уже записаны");
		verify(appointmentRepository, never()).save(any());
	}
}
