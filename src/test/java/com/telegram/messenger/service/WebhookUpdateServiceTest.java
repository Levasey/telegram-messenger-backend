package com.telegram.messenger.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class WebhookUpdateServiceTest {

	private static final String NEW_USER_UPDATE = """
			{"update_id":1,"message":{"message_id":1,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"привет"}}\
			""";

	private static final String BUSINESS_START_UPDATE = """
			{"update_id":2,"business_message":{"message_id":2,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"/start"}}\
			""";

	@Mock
	private WebhookMessageTransactionService messageTransactionService;

	private WebhookUpdateService service;

	@BeforeEach
	void setUp() {
		service = new WebhookUpdateService(
				new ObjectMapper(),
				messageTransactionService);
	}

	@Test
	void delegatesToTransactionService_whenMessagePresent() {
		service.handleRawUpdate(NEW_USER_UPDATE);

		verify(messageTransactionService).upsertClientAndMaybeWelcomeIntent(any());
	}

	@Test
	void delegatesToTransactionService_whenBusinessMessagePresent() {
		service.handleRawUpdate(BUSINESS_START_UPDATE);

		verify(messageTransactionService).upsertClientAndMaybeWelcomeIntent(any());
	}
}
