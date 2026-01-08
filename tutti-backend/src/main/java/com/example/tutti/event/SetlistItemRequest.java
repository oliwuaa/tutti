package com.example.tutti.event;

public record SetlistItemRequest(
        Long scoreId,
        String customTitle,
        Integer position,
        String notes
) {}
