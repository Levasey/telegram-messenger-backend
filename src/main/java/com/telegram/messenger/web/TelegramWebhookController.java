package com.telegram.messenger.web;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegram.messenger.service.WebhookUpdateService;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

	private final WebhookUpdateService webhookUpdateService;
	private final TelegramWebhookAuthenticator webhookAuthenticator;

	public TelegramWebhookController(
			WebhookUpdateService webhookUpdateService,
			TelegramWebhookAuthenticator webhookAuthenticator) {
		this.webhookUpdateService = webhookUpdateService;
		this.webhookAuthenticator = webhookAuthenticator;
	}

	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(
			@RequestHeader(value = TelegramWebhookAuthenticator.SECRET_HEADER, required = false) String secretToken,
			@RequestBody(required = false) String body) {
		if (!webhookAuthenticator.isAuthorized(secretToken)) {
			return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
		}
		webhookUpdateService.handleRawUpdate(body);
		return ResponseEntity.ok().build();
	}
}
