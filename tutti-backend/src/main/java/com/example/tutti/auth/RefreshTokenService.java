package com.example.tutti.auth;

public interface RefreshTokenService {
    RefreshToken createRefreshToken(String email);
    RefreshToken verifyExpiration(RefreshToken token);
    void deleteByUserId(String email);
}