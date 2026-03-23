package com.telegram.messenger;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class TelegramMessengerBackendApplication {

	public static void main(String[] args) {
		SpringApplication.run(TelegramMessengerBackendApplication.class, args);
	}
}
