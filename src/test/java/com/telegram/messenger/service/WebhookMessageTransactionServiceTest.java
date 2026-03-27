package com.telegram.messenger.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.ZoneId;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.telegram.messenger.config.SchoolScheduleProperties;
import com.telegram.messenger.config.TelegramBotProperties;
import com.telegram.messenger.domain.Client;
import com.telegram.messenger.repo.ClientRepository;
import com.telegram.messenger.telegram.dto.TelegramMessageDto;
import com.telegram.messenger.telegram.dto.TelegramUpdateDto;
import com.fasterxml.jackson.databind.ObjectMapper;

@ExtendWith(MockitoExtension.class)
class WebhookMessageTransactionServiceTest {

	private static final String NEW_USER_UPDATE = """
			{"update_id":1,"message":{"message_id":1,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"привет"}}\
			""";

	private static final String START_UPDATE = """
			{"update_id":2,"message":{"message_id":2,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"/start"}}\
			""";

	private static final String START_WITH_BOT_UPDATE = """
			{"update_id":3,"message":{"message_id":3,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"/start@vokals_bot"}}\
			""";

	private static final String MISSING_FROM_ID_UPDATE = """
			{"update_id":4,"message":{"message_id":4,"from":{"is_bot":false,"first_name":"X"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"hi"}}\
			""";

	@Mock
	private ClientRepository clientRepository;

	@Mock
	private LessonBookingService lessonBookingService;

	@Mock
	private ApplicationEventPublisher eventPublisher;

	private WebhookMessageTransactionService service;
	private ObjectMapper objectMapper;

	@BeforeEach
	void setUp() throws Exception {
		TelegramBotProperties props = new TelegramBotProperties();
		props.setWelcomeMessage("Здравствуйте, {name}!");
		SchoolScheduleProperties school = new SchoolScheduleProperties();
		school.setTimeZone("Europe/Moscow");
		service = new WebhookMessageTransactionService(
				clientRepository,
				props,
				school,
				lessonBookingService,
				eventPublisher);
		objectMapper = new ObjectMapper();
	}

	@Test
	void newClient_savesAndPublishesWelcomeIntent() throws Exception {
		TelegramMessageDto message = messageFrom(NEW_USER_UPDATE);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.empty());
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> {
			Client c = inv.getArgument(0);
			c.setId(1L);
			return c;
		});

		service.upsertClientAndMaybeWelcomeIntent(message);

		verify(clientRepository).save(any(Client.class));
		verify(eventPublisher).publishEvent(argThat((WelcomeSendIntent i) -> i.chatId() == 99L && i.text().contains("Иван")));
		verify(lessonBookingService, never()).handleBookCommand(any(), anyString(), any());
	}

	@Test
	void existingClient_privatePlainMessage_sendsWelcome() throws Exception {
		TelegramMessageDto message = messageFrom(NEW_USER_UPDATE);
		Client existing = new Client();
		existing.setId(10L);
		existing.setTelegramUserId(42L);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.of(existing));
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		service.upsertClientAndMaybeWelcomeIntent(message);

		verify(eventPublisher).publishEvent(argThat((WelcomeSendIntent i) -> i.chatId() == 99L && i.text().contains("Иван")));
		verify(lessonBookingService, never()).handleBookCommand(any(), anyString(), any());
	}

	private static final String GROUP_MESSAGE_UPDATE = """
			{"update_id":6,"message":{"message_id":6,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":-1001,"type":"group"},"date":1,"text":"привет"}}\
			""";

	@Test
	void existingClient_groupPlainMessage_noWelcome() throws Exception {
		TelegramMessageDto message = messageFrom(GROUP_MESSAGE_UPDATE);
		Client existing = new Client();
		existing.setId(10L);
		existing.setTelegramUserId(42L);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.of(existing));
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		service.upsertClientAndMaybeWelcomeIntent(message);

		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void existingClient_start_sendsWelcome() throws Exception {
		TelegramMessageDto message = messageFrom(START_UPDATE);
		Client existing = new Client();
		existing.setId(10L);
		existing.setTelegramUserId(42L);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.of(existing));
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		service.upsertClientAndMaybeWelcomeIntent(message);

		verify(eventPublisher).publishEvent(argThat((WelcomeSendIntent i) -> i.text().contains("Иван")));
		verify(lessonBookingService, never()).handleBookCommand(any(), anyString(), any());
	}

	@Test
	void existingClient_startWithBotUsername_sendsWelcome() throws Exception {
		TelegramMessageDto message = messageFrom(START_WITH_BOT_UPDATE);
		Client existing = new Client();
		existing.setId(10L);
		existing.setTelegramUserId(42L);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.of(existing));
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));

		service.upsertClientAndMaybeWelcomeIntent(message);

		verify(eventPublisher).publishEvent(argThat((WelcomeSendIntent i) -> i.text().contains("Иван")));
		verify(lessonBookingService, never()).handleBookCommand(any(), anyString(), any());
	}

	@Test
	void fromWithoutId_skipsDb() throws Exception {
		TelegramMessageDto message = messageFrom(MISSING_FROM_ID_UPDATE);

		service.upsertClientAndMaybeWelcomeIntent(message);

		verify(clientRepository, never()).findByTelegramUserId(any());
		verify(clientRepository, never()).save(any());
		verify(eventPublisher, never()).publishEvent(any());
	}

	@Test
	void isStartCommand_variants() {
		assertThat(WebhookMessageTransactionService.isStartCommand("/start")).isTrue();
		assertThat(WebhookMessageTransactionService.isStartCommand("/START")).isTrue();
		assertThat(WebhookMessageTransactionService.isStartCommand("/start@vokals_bot")).isTrue();
		assertThat(WebhookMessageTransactionService.isStartCommand("/start@vokals_bot args")).isTrue();
		assertThat(WebhookMessageTransactionService.isStartCommand("/start\tdeep-link-arg")).isTrue();
		assertThat(WebhookMessageTransactionService.isStartCommand("/startx")).isFalse();
		assertThat(WebhookMessageTransactionService.isStartCommand(" /start ")).isTrue();
	}

	@Test
	void shouldSendWelcome_privateOrNewOrStart() throws Exception {
		TelegramMessageDto existingPrivate = messageFrom(NEW_USER_UPDATE);
		assertThat(WebhookMessageTransactionService.shouldSendWelcome(existingPrivate, false)).isTrue();
		TelegramMessageDto groupMsg = messageFrom(GROUP_MESSAGE_UPDATE);
		assertThat(WebhookMessageTransactionService.shouldSendWelcome(groupMsg, false)).isFalse();
		assertThat(WebhookMessageTransactionService.shouldSendWelcome(groupMsg, true)).isTrue();
		TelegramMessageDto startInGroup = messageFrom("""
				{"update_id":7,"message":{"message_id":7,"from":{"id":1,"is_bot":false,"first_name":"U"},\
				"chat":{"id":-2,"type":"group"},"date":1,"text":"/start"}}\
				""");
		assertThat(WebhookMessageTransactionService.shouldSendWelcome(startInGroup, false)).isTrue();
	}

	@Test
	void isBookCommand_variants() {
		assertThat(WebhookMessageTransactionService.isBookCommand("/book")).isTrue();
		assertThat(WebhookMessageTransactionService.isBookCommand("/BOOK")).isTrue();
		assertThat(WebhookMessageTransactionService.isBookCommand("/book@vokals_bot")).isTrue();
		assertThat(WebhookMessageTransactionService.isBookCommand("/book 25.03.2026 18:00")).isTrue();
		assertThat(WebhookMessageTransactionService.isBookCommand("/bookx")).isFalse();
	}

	private static final String BOOK_UPDATE = """
			{"update_id":5,"message":{"message_id":5,"from":{"id":42,"is_bot":false,"first_name":"Иван"},\
			"chat":{"id":99,"type":"private"},"date":1,"text":"/book 2030-01-15 12:00"}}\
			""";

	@Test
	void bookCommand_invokesBookingService_andPublishesReply() throws Exception {
		TelegramMessageDto message = messageFrom(BOOK_UPDATE);
		Client existing = new Client();
		existing.setId(10L);
		existing.setTelegramUserId(42L);
		when(clientRepository.findByTelegramUserId(42L)).thenReturn(Optional.of(existing));
		when(clientRepository.save(any(Client.class))).thenAnswer(inv -> inv.getArgument(0));
		when(lessonBookingService.handleBookCommand(eq(existing), eq("/book 2030-01-15 12:00"), eq(ZoneId.of("Europe/Moscow"))))
				.thenReturn("Записано");

		service.upsertClientAndMaybeWelcomeIntent(message);

		verify(lessonBookingService).handleBookCommand(eq(existing), eq("/book 2030-01-15 12:00"), eq(ZoneId.of("Europe/Moscow")));
		verify(eventPublisher).publishEvent(argThat((WelcomeSendIntent i) -> i.chatId() == 99L && i.text().contains("Иван")));
		verify(eventPublisher).publishEvent(argThat((WelcomeSendIntent i) -> i.chatId() == 99L && "Записано".equals(i.text())));
	}

	private TelegramMessageDto messageFrom(String rawUpdate) throws Exception {
		TelegramUpdateDto update = objectMapper.readValue(rawUpdate, TelegramUpdateDto.class);
		return update.getMessage();
	}
}
