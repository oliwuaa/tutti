package com.example.tutti.user;

public record ChangePasswordRequest(
        String oldPassword,
        String newPassword
) {}
