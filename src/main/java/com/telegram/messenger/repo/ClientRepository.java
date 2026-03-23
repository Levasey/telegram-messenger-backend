package com.telegram.messenger.repo;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.telegram.messenger.domain.Client;

public interface ClientRepository extends JpaRepository<Client, Long> {

	Optional<Client> findByTelegramUserId(Long telegramUserId);
}
