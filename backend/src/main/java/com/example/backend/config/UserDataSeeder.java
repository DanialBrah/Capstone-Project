package com.example.backend.config;

import com.example.backend.model.AppUser;
import com.example.backend.repository.AppUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/*
 * Seeds a couple of demo accounts on startup so you can log in immediately
 * without registering first. Safe to run every time the app starts - it
 * skips creating a user if that email already exists.
 */
@Configuration
public class UserDataSeeder {

    private static final Logger logger = LoggerFactory.getLogger(UserDataSeeder.class);

    @Bean
    CommandLineRunner seedUsers(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            createUserIfMissing(
                    appUserRepository,
                    passwordEncoder,
                    "Admin User",
                    "admin@example.com",
                    "Admin@12345",
                    "ADMIN"
            );

            createUserIfMissing(
                    appUserRepository,
                    passwordEncoder,
                    "Demo Student",
                    "student@example.com",
                    "Student@12345",
                    "STUDENT"
            );
        };
    }

    private void createUserIfMissing(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            String name,
            String email,
            String rawPassword,
            String role) {

        if (appUserRepository.existsByEmailIgnoreCase(email)) {
            logger.info("Seed user already exists: {}", email);
            return;
        }

        AppUser user = new AppUser(
                name,
                email.toLowerCase(),
                passwordEncoder.encode(rawPassword),
                role
        );

        appUserRepository.save(user);
        logger.info("Seeded user email={} role={}", user.getEmail(), user.getRole());
    }
}
