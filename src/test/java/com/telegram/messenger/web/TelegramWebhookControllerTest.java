package com.telegram.messenger.web;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import com.telegram.messenger.config.TelegramBotProperties;
import com.telegram.messenger.service.WebhookUpdateService;

class TelegramWebhookControllerTest {

	@Nested
	@WebMvcTest(controllers = TelegramWebhookController.class)
	@EnableConfigurationProperties(TelegramBotProperties.class)
	@Import(TelegramWebhookAuthenticator.class)
	class WithoutSecret {

		@Autowired
		private MockMvc mockMvc;

		@MockBean
		private WebhookUpdateService webhookUpdateService;

		@Test
		void acceptsRequestWithoutHeader() throws Exception {
			mockMvc.perform(post("/api/telegram/webhook")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isOk());

			verify(webhookUpdateService).handleRawUpdate(any());
		}
	}

	@Nested
	@WebMvcTest(controllers = TelegramWebhookController.class)
	@EnableConfigurationProperties(TelegramBotProperties.class)
	@Import(TelegramWebhookAuthenticator.class)
	@TestPropertySource(properties = "telegram.bot.webhook-secret-token=test-secret")
	class WithSecret {

		@Autowired
		private MockMvc mockMvc;

		@MockBean
		private WebhookUpdateService webhookUpdateService;

		@Test
		void rejectsWithoutHeader() throws Exception {
			mockMvc.perform(post("/api/telegram/webhook")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isForbidden());

			verify(webhookUpdateService, never()).handleRawUpdate(any());
		}

		@Test
		void acceptsWithValidHeader() throws Exception {
			mockMvc.perform(post("/api/telegram/webhook")
							.header(TelegramWebhookAuthenticator.SECRET_HEADER, "test-secret")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isOk());

			verify(webhookUpdateService).handleRawUpdate(any());
		}

		@Test
		void rejectsWithWrongHeader() throws Exception {
			mockMvc.perform(post("/api/telegram/webhook")
							.header(TelegramWebhookAuthenticator.SECRET_HEADER, "wrong")
							.contentType(MediaType.APPLICATION_JSON)
							.content("{}"))
					.andExpect(status().isForbidden());

			verify(webhookUpdateService, never()).handleRawUpdate(any());
		}
	}
}
