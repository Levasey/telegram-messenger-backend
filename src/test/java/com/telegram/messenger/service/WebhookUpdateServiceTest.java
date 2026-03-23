package com.telegram.messenger.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telegram.messenger.config.TelegramBotProperties;
import com.telegram.messenger.domain.Client;
import com.telegram.messenger.repo.ClientRepository;
import com.telegram.messenger.telegram.TelegramApiClient;

@ExtendWith(MockitoExtension.class)
class WebhookUpdateServiceTest {

	private static final String NEW_USER_UPDATE = """
			{"update_id":1,"message":{"message_id":1,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"привет"}}\
			""";

	private static final String START_UPDATE = """
			{"update_id":2,"message":{"message_id":2,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"/start"}}\
			""";

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private TelegramApiClient telegramApiClient;

	private WebhookUpdateService service;

	@BeforeEach
	void setUp() {
		TelegramBotProperties props = new TelegramBotProperties();
		props.setWelcomeMessage("Здравствуйте, {name}!");
		service = new WebhookUpdateService(
				new ObjectMapper(),
				clientRepository,
				telegramApiClient,
				props);
	}

	@Test
	void newClient_savesAndSendsWelcome() {
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.empty());
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		service.handleRawUpdate(NEW_USER_UPDATE);

		verify(clientRepository).save(any(Client.class));
		verify(telegramApiClient).sendMessage(eq(99L), contains("Иван"));
	}

	@Test
	void existingClient_plainMessage_noWelcome() {
		Client existing = new Client();
		existing.setTelegramUserId(42L);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.of(existing));
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		service.handleRawUpdate(NEW_USER_UPDATE);

		verify(telegramApiClient, never()).sendMessage(org.mockito.ArgumentMatchers.anyLong(), any());
	}

	@Test
	void existingClient_start_sendsWelcome() {
		Client existing = new Client();
		existing.setTelegramUserId(42L);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.of(existing));
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		service.handleRawUpdate(START_UPDATE);

		verify(telegramApiClient).sendMessage(eq(99L), contains("Иван"));
	}
}
