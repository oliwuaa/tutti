package com.example.tutti.user;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.time.ZonedDateTime;

@Component
@RequiredArgsConstructor
public class UserMapper {

    private final PasswordEncoder passwordEncoder;

    public UserResponse toResponse(User user) {
        if (user == null) return null;
        return UserResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .dateJoined(user.getDateJoined())
                .role(user.getRole())
                .isActive(user.isActive())
                .build();
    }

    public User toEntity(UserRequest request) {
        return User.builder()
                .email(request.getEmail().toLowerCase().trim())
                .password(passwordEncoder.encode(request.getPassword()))
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .dateJoined(ZonedDateTime.now())
                .role(request.getRole() != null ? request.getRole() : Role.USER)
                .isActive(true)
                .build();
    }
}