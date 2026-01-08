package com.example.tutti.music.part;

import com.example.tutti.event.EventType;

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