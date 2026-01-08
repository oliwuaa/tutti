package com.example.tutti.event;

import java.time.LocalDateTime;
import java.util.List;

public record EventRequest(
        String name,
        EventType type,
        LocalDateTime date,
        String address,
        String plan,
        List<SetlistItemRequest> setlist
) {}