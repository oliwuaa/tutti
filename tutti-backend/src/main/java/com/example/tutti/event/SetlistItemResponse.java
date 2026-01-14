package com.example.tutti.event;

public record SetlistItemResponse(
        Long id,
        Long scoreId,
        String scoreTitle,
        String customTitle,
        Integer position,
        String notes) {}