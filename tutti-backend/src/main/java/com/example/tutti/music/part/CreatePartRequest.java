package com.example.tutti.music.part;

import com.example.tutti.resource.instrument.InstrumentType;

public record CreatePartRequest(
        Long scoreId,
        InstrumentType type,
        Integer partNumber,
        Integer pageStart,
        Integer pageEnd
) {}