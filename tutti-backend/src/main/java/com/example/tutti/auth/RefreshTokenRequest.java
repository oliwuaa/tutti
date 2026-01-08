package com.example.tutti.auth;

import lombok.Data;

@Data
public class RefreshTokenRequest {
    private String token;
}