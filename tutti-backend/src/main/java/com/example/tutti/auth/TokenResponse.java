package com.example.tutti.auth;

import java.time.Instant;

public record TokenResponse(
        String token,
        Instant expiryDate
) {}