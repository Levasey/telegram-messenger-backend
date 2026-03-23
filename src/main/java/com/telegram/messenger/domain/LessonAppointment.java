package com.telegram.messenger.domain;

import java.time.Instant;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "lesson_appointments")
public class LessonAppointment {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(optional = false, fetch = FetchType.LAZY)
	@JoinColumn(name = "client_id", nullable = false)
	private Client client;

	@Column(name = "lesson_at", nullable = false)
	private Instant lessonAt;

	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@Column(name = "reminder_sent", nullable = false)
	private boolean reminderSent;

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Client getClient() {
		return client;
	}

	public void setClient(Client client) {
		this.client = client;
	}

	public Instant getLessonAt() {
		return lessonAt;
	}

	public void setLessonAt(Instant lessonAt) {
		this.lessonAt = lessonAt;
	}

	public Instant getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Instant createdAt) {
		this.createdAt = createdAt;
	}

	public boolean isReminderSent() {
		return reminderSent;
	}

	public void setReminderSent(boolean reminderSent) {
		this.reminderSent = reminderSent;
	}
}
