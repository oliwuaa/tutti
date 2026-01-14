package com.example.tutti.event;

import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class EventMapper {

    public EventResponse toResponse(Event event) {
        if (event == null) return null;

        List<SetlistItemResponse> setlistResponse = event.getSetlist().stream()
                .map(item -> new SetlistItemResponse(
                        item.getId(),
                        item.getScore() != null ? item.getScore().getId() : null,
                        item.getScore() != null ? item.getScore().getTitle() : null,
                        item.getCustomTitle(),
                        item.getPosition(),
                        item.getNotes()
                )).toList();

        return new EventResponse(
                event.getId(),
                event.getOrchestra().getId(),
                event.getName(),
                event.getType(),
                event.getDate(),
                event.getAddress(),
                event.getPlan(),
                setlistResponse
        );
    }
}