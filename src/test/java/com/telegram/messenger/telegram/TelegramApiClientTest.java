package com.telegram.messenger.telegram;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.http.HttpMethod.POST;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import com.telegram.messenger.config.TelegramBotProperties;

class TelegramApiClientTest {

	private MockRestServiceServer server;
	private TelegramApiClient client;

	@BeforeEach
	void setUp() {
		TelegramBotProperties props = new TelegramBotProperties();
		props.setToken("test-token");
		RestClient.Builder builder = RestClient.builder();
		server = MockRestServiceServer.bindTo(builder).build();
		RestClient restClient = builder.baseUrl("https://localhost").build();
		client = new TelegramApiClient(props, restClient);
	}

	@AfterEach
	void tearDown() {
		server.verify();
	}

	@Test
	void sendMessage_returnsFalse_whenHttp200_andOkFalse() {
		server.expect(requestTo("https://localhost/bottest-token/sendMessage"))
				.andExpect(method(POST))
				.andRespond(withSuccess(
						"{\"ok\":false,\"error_code\":400,\"description\":\"chat not found\"}",
						MediaType.APPLICATION_JSON));

		assertFalse(client.sendMessage(1L, "hello"));
	}

	@Test
	void sendMessage_returnsTrue_whenOkTrue() {
		server.expect(requestTo("https://localhost/bottest-token/sendMessage"))
				.andExpect(method(POST))
				.andRespond(withSuccess("{\"ok\":true}", MediaType.APPLICATION_JSON));

		assertTrue(client.sendMessage(1L, "hello"));
	}
}
