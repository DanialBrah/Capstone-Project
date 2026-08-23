package com.example.backend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

@SpringBootApplication
public class BackendApplication {

	public static void main(String[] args) {
		loadDotEnv();
		SpringApplication.run(BackendApplication.class, args);
	}

	private static void loadDotEnv() {
		Path envFile = Path.of(".env");
		if (!Files.exists(envFile)) {
			return;
		}

		try {
			List<String> lines = Files.readAllLines(envFile);
			for (String line : lines) {
				String trimmed = line.trim();
				if (trimmed.isEmpty() || trimmed.startsWith("#") || !trimmed.contains("=")) {
					continue;
				}

				int separatorIndex = trimmed.indexOf('=');
				String key = trimmed.substring(0, separatorIndex).trim();
				String value = trimmed.substring(separatorIndex + 1).trim();
				System.setProperty(key, value);
			}
		} catch (IOException exception) {
			throw new IllegalStateException("Failed to read .env file", exception);
		}
	}

}
