package com.example.tutti.resource.instrument;

import org.springframework.stereotype.Component;

@Component
public class InstrumentMapper {

    public InstrumentResponse mapToResponse(Instrument instrument) {
        if (instrument == null) return null;

        Long ownerId = null;
        String ownerName = "W magazynie";

        if (instrument.getOwner() != null && instrument.getOwner().getUser() != null) {
            ownerId = instrument.getOwner().getId();
            ownerName = instrument.getOwner().getUser().getFirstName() + " " + instrument.getOwner().getUser().getLastName();
        }

        return InstrumentResponse.builder()
                .id(instrument.getId())
                .instrumentType(instrument.getInstrumentType())
                .brand(instrument.getBrand())
                .number(instrument.getNumber())
                .description(instrument.getDescription())
                .ownerId(ownerId)
                .ownerFullName(ownerName)
                .build();
    }
}