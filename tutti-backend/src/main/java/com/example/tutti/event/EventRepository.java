package com.example.tutti.event;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

    @EntityGraph(attributePaths = {"setlist", "setlist.score"})
    Optional<Event> findByIdAndOrchestraId(Long id, Long orchestraId);

    @EntityGraph(attributePaths = {"setlist"})
    List<Event> findAllByOrchestraIdAndDateAfterOrderByDateAsc(Long orchestraId, LocalDateTime date);

    List<Event> findAllByOrchestraIdAndDateBeforeOrderByDateDesc(Long orchestraId, LocalDateTime date);

    List<Event> findAllByOrchestraIdOrderByDateDesc(Long orchestraId);
}