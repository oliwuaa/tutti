package com.example.tutti.auth;

import org.springframework.stereotype.Component;

@Component
public class RefreshTokenMapper {

    public TokenResponse toResponse(RefreshToken token) {
        if (token == null) return null;
        return new TokenResponse(
                token.getToken(),
                token.getExpiryDate()
        );
    }
}