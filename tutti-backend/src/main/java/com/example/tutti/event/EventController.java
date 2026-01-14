package com.example.tutti.event;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orchestras/{orchestraId}/events")
@RequiredArgsConstructor
@Tag(name = "Events", description = "Zarządzanie wydarzeniami konkretnej orkiestry")
public class EventController {

    private final EventService eventService;

    @Operation(summary = "Pobierz listę wydarzeń orkiestry")
    @GetMapping
    @PreAuthorize("@ss.isMemberOf(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<List<EventResponse>> getAllEvents(
            @PathVariable Long orchestraId,
            @RequestParam(required = false, defaultValue = "upcoming") String scope) {

        return switch (scope.toLowerCase()) {
            case "past" -> ResponseEntity.ok(eventService.getPastEvents(orchestraId));
            case "all" -> ResponseEntity.ok(eventService.getAllEvents(orchestraId));
            default -> ResponseEntity.ok(eventService.getUpcomingEvents(orchestraId));
        };
    }

    @Operation(summary = "Pobierz szczegóły wydarzenia")
    @GetMapping("/{id}")
    @PreAuthorize("@ss.isMemberOf(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> getOne(@PathVariable Long orchestraId, @PathVariable Long id) {
        return ResponseEntity.ok(eventService.getEvent(id, orchestraId));
    }

    @Operation(summary = "Utwórz nowe wydarzenie")
    @PostMapping
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> create(@PathVariable Long orchestraId, @RequestBody EventRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(orchestraId, request));
    }

    @Operation(summary = "Zaktualizuj wydarzenie")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<EventResponse> update(
            @PathVariable Long orchestraId,
            @PathVariable Long id,
            @RequestBody EventRequest request) {
        return ResponseEntity.ok(eventService.updateEvent(id, orchestraId, request));
    }

    @Operation(summary = "Usuń wydarzenie")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long orchestraId, @PathVariable Long id) {
        eventService.deleteEvent(id, orchestraId);
        return ResponseEntity.noContent().build();
    }
}