package com.telegram.messenger.service;

import static org.mockito.ArgumentMatchers.any;
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
import com.telegram.messenger.telegram.TelegramApiClient;

@ExtendWith(MockitoExtension.class)
class WebhookUpdateServiceTest {

	private static final String NEW_USER_UPDATE = """
			{"update_id":1,"message":{"message_id":1,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"привет"}}\
			""";

	@Mock
	private WebhookMessageTransactionService messageTransactionService;

	@Mock
	private TelegramApiClient telegramApiClient;

	private WebhookUpdateService service;

	@BeforeEach
	void setUp() {
		service = new WebhookUpdateService(
				new ObjectMapper(),
				messageTransactionService,
				telegramApiClient);
	}

	@Test
	void delegatesToTransactionService_andSendsWhenIntentPresent() {
		when(messageTransactionService.upsertClientAndMaybeWelcomeIntent(org.mockito.ArgumentMatchers.any()))
				.thenReturn(Optional.of(new WelcomeSendIntent(99L, "Здравствуйте, Иван!")));

		service.handleRawUpdate(NEW_USER_UPDATE);

		verify(telegramApiClient).sendMessage(eq(99L), eq("Здравствуйте, Иван!"));
	}

	@Test
	void noSendWhenTransactionReturnsEmpty() {
		when(messageTransactionService.upsertClientAndMaybeWelcomeIntent(org.mockito.ArgumentMatchers.any()))
				.thenReturn(Optional.empty());

		service.handleRawUpdate(NEW_USER_UPDATE);

		verify(telegramApiClient, never()).sendMessage(org.mockito.ArgumentMatchers.anyLong(), any());
	}
}
