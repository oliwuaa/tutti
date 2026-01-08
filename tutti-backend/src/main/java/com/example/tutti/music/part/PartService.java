package com.example.tutti.music.part;

import com.example.tutti.resource.instrument.InstrumentType;
import java.util.List;

public interface PartService {
    PartResponse createPart(CreatePartRequest request);
    PartResponse updatePart(Long id, CreatePartRequest request);
    void deletePart(Long id);
    Part getPartEntity(Long id);
    PartResponse getPart(Long id);
    List<PartResponse> getPartsByScore(Long scoreId, Long orchestraId);
    List<PartResponse> getPartsByInstrumentType(InstrumentType instrument, Long orchestraId);
    PartResponse getPartByScoreAndInstrument(Long scoreId, InstrumentType type, Integer partNumber, Long orchestraId);
}