package com.telegram.messenger.repo;

import java.time.Instant;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegram.messenger.domain.LessonAppointment;

public interface LessonAppointmentRepository extends JpaRepository<LessonAppointment, Long> {

	boolean existsByClientIdAndLessonAt(Long clientId, Instant lessonAt);

	List<LessonAppointment> findByLessonAtGreaterThanEqualAndLessonAtLessThanAndReminderSentIsFalse(
			Instant fromInclusive,
			Instant toExclusive);
}
