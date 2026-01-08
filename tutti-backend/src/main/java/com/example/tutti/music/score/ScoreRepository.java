package com.example.tutti.music.score;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ScoreRepository extends JpaRepository<Score, Long> {
    List<Score> findByOrchestraId(Long orchestraId);

    @Query("SELECT s FROM Score s WHERE s.orchestra.id = :orchId " +
            "AND (:title IS NULL OR LOWER(s.title) LIKE LOWER(CONCAT('%', :title, '%'))) " +
            "AND (:composer IS NULL OR LOWER(s.composer) LIKE LOWER(CONCAT('%', :composer, '%')))")
    List<Score> findFiltered(@Param("orchId") Long orchestraId,
                             @Param("title") String title,
                             @Param("composer") String composer);

    boolean existsByTitleAndComposerAndOrchestraId(String title, String composer, Long orchestraId);
}