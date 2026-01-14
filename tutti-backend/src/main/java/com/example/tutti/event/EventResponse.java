package com.example.tutti.event;

import java.time.LocalDateTime;
import java.util.List;

public record EventResponse(
        Long id,
        Long orchestraId,
        String name,
        EventType type,
        LocalDateTime date,
        String address,
        String plan,
        List<SetlistItemResponse> setlist
) {}