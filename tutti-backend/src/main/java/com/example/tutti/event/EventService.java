package com.example.tutti.event;

import java.util.List;

public interface EventService {
    EventResponse createEvent(Long orchestraId, EventRequest request);
    EventResponse updateEvent(Long id, Long orchestraId, EventRequest request);
    void deleteEvent(Long id, Long orchestraId);
    EventResponse getEvent(Long id, Long orchestraId);
    List<EventResponse> getAllEvents(Long orchestraId);
    List<EventResponse> getUpcomingEvents(Long orchestraId);
    List<EventResponse> getPastEvents(Long orchestraId);
}