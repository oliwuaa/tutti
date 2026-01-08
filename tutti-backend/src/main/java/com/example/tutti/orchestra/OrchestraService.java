package com.example.tutti.orchestra;

import com.example.tutti.resource.instrument.InstrumentResponse;
import com.example.tutti.user.User;
import java.util.List;

public interface OrchestraService {
    List<OrchestraResponse> findAll();
    OrchestraResponse findById(Long id);
    OrchestraResponse findByName(String name);
    OrchestraResponse createOrchestra(OrchestraRequest request);
    OrchestraResponse updateOrchestra(Long id, OrchestraRequest request, User currentUser);
    void deleteOrchestra(Long id);
    List<InstrumentResponse> getInstruments(Long orchestraId);
    List<OrchestraResponse> findAllByOwnerId(Long ownerId);
}