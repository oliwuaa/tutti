package com.example.tutti.config;

import com.example.tutti.user.Role;
import com.example.tutti.user.User;
import com.example.tutti.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.ZonedDateTime;

@Configuration
public class AdminInitializer {
    @Bean
    CommandLineRunner initAdmin(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@tutti.pl";
            if (userRepository.findByEmail(adminEmail).isEmpty()) {
                User admin = User.builder()
                        .email(adminEmail)
                        .password(passwordEncoder.encode("admin123"))
                        .firstName("System")
                        .lastName("Admin")
                        .role(Role.ADMIN)
                        .isActive(true)
                        .dateJoined(ZonedDateTime.now())
                        .build();

                userRepository.save(admin);
                System.out.println(">>> Utworzono domyślnego administratora: admin@tutti.pl / admin123");
            }
        };
    }
}
