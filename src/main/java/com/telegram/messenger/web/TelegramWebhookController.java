package com.telegram.messenger.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.telegram.messenger.service.WebhookUpdateService;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

	private final WebhookUpdateService webhookUpdateService;

	public TelegramWebhookController(WebhookUpdateService webhookUpdateService) {
		this.webhookUpdateService = webhookUpdateService;
	}

	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(@RequestBody(required = false) String body) {
		webhookUpdateService.handleRawUpdate(body);
		return ResponseEntity.ok().build();
	}
}
