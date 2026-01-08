package com.example.tutti.music.part;

import com.example.tutti.resource.instrument.InstrumentType;

public record PartResponse(
        Long id,
        Long scoreId,
        InstrumentType type,
        Integer partNumber,
        String scorePdfPath,
        Integer pageStart,
        Integer pageEnd
) {}