package com.example.tutti.event;

import com.example.tutti.exception.NotFoundException;
import com.example.tutti.music.score.Score;
import com.example.tutti.music.score.ScoreRepository;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final ScoreRepository scoreRepository;
    private final OrchestraRepository orchestraRepository;
    private final EventMapper eventMapper;

    @Override
    public EventResponse createEvent(Long orchestraId, EventRequest request) {
        Orchestra orchestra = orchestraRepository.findById(orchestraId)
                .orElseThrow(() -> new NotFoundException("Orkiestra nie istnieje"));

        Event event = new Event();
        event.setOrchestra(orchestra);
        mapRequestToEntity(request, event, orchestraId);

        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public EventResponse updateEvent(Long id, Long orchestraId, EventRequest request) {
        Event event = eventRepository.findByIdAndOrchestraId(id, orchestraId)
                .orElseThrow(() -> new NotFoundException("Wydarzenie nie istnieje w tej orkiestrze"));

        mapRequestToEntity(request, event, orchestraId);
        return eventMapper.toResponse(eventRepository.save(event));
    }

    @Override
    public void deleteEvent(Long id, Long orchestraId) {
        Event event = eventRepository.findByIdAndOrchestraId(id, orchestraId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono wydarzenia do usunięcia"));
        eventRepository.delete(event);
    }

    @Override
    public EventResponse getEvent(Long id, Long orchestraId) {
        return eventRepository.findByIdAndOrchestraId(id, orchestraId)
                .map(eventMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono wydarzenia"));
    }

    @Override
    public List<EventResponse> getAllEvents(Long orchestraId) {
        return eventRepository.findAllByOrchestraIdOrderByDateDesc(orchestraId)
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Override
    public List<EventResponse> getUpcomingEvents(Long orchestraId) {
        return eventRepository.findAllByOrchestraIdAndDateAfterOrderByDateAsc(orchestraId, LocalDateTime.now())
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    @Override
    public List<EventResponse> getPastEvents(Long orchestraId) {
        return eventRepository.findAllByOrchestraIdAndDateBeforeOrderByDateDesc(orchestraId, LocalDateTime.now())
                .stream()
                .map(eventMapper::toResponse)
                .toList();
    }

    private void mapRequestToEntity(EventRequest request, Event event, Long orchestraId) {
        event.setName(request.name());
        event.setType(request.type());
        event.setDate(request.date());
        event.setAddress(request.address());
        event.setPlan(request.plan());

        if (event.getSetlist() == null) {
            event.setSetlist(new ArrayList<>());
        } else {
            event.getSetlist().clear();
        }

        if (request.setlist() != null && !request.setlist().isEmpty()) {
            addItemsToEvent(event, request.setlist(), orchestraId);
        }
    }

    private void addItemsToEvent(Event event, List<SetlistItemRequest> setlistRequests, Long orchestraId) {
        for (SetlistItemRequest itemReq : setlistRequests) {
            SetlistItem item = new SetlistItem();
            if (itemReq.scoreId() != null) {
                Score score = scoreRepository.findById(itemReq.scoreId())
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono utworu o ID: " + itemReq.scoreId()));
                if (!score.getOrchestra().getId().equals(orchestraId)) {
                    throw new IllegalArgumentException("Utwór " + score.getTitle() + " nie należy do Twojej orkiestry!");
                }
                item.setScore(score);
            }
            item.setCustomTitle(itemReq.customTitle());
            item.setPosition(itemReq.position());
            item.setNotes(itemReq.notes());
            item.setEvent(event);
            event.getSetlist().add(item);
        }
    }
}