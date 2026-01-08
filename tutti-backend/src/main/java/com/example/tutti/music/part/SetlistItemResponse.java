package com.example.tutti.music.part;

public record SetlistItemResponse(
        Long id,
        Long scoreId,
        String scoreTitle,
        String customTitle,
        Integer position,
        String notes) {}