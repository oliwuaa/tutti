package com.example.tutti.resource.instrument;

import java.util.List;

public interface InstrumentService {
    InstrumentResponse addInstrument(InstrumentRequest request);
    InstrumentResponse updateInstrument(Long instrumentId, InstrumentRequest request);
    InstrumentResponse assignInstrumentToMember(Long instrumentId, Long membershipId);
    InstrumentResponse unassignInstrument(Long instrumentId);
    void deleteInstrument(Long instrumentId);
    List<InstrumentResponse> getInstruments(Long orchestraId, InstrumentType type, Boolean available);
    List<InstrumentResponse> getInstrumentsByMembership(Long membershipId);
}