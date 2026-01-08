package com.example.tutti.music.part;

import com.example.tutti.resource.instrument.InstrumentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PartRepository extends JpaRepository<Part, Long> {
    List<Part> findByScoreId(Long scoreId);

    List<Part> findByType(InstrumentType type);

    List<Part> findByScoreIdAndScoreOrchestraId(Long scoreId, Long orchestraId);

    List<Part> findByTypeAndScoreOrchestraId(InstrumentType type, Long orchestraId);

    Optional<Part> findByScoreIdAndTypeAndPartNumber(Long scoreId, InstrumentType type, Integer partNumber);

    Optional<Part> findByScoreIdAndTypeAndPartNumberAndScoreOrchestraId(
            Long scoreId,
            InstrumentType type,
            Integer partNumber,
            Long orchestraId
    );
}
