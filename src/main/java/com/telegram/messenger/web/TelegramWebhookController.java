package com.telegram.messenger.web;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/telegram")
public class TelegramWebhookController {

	@PostMapping("/webhook")
	public ResponseEntity<Void> webhook(@RequestBody(required = false) String body) {
		return ResponseEntity.ok().build();
	}
}
