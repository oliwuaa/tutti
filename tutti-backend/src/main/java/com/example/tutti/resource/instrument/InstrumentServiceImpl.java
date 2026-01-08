package com.example.tutti.resource.instrument;

import com.example.tutti.exception.NotFoundException;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRepository;
import com.example.tutti.orchestra.membership.Membership;
import com.example.tutti.orchestra.membership.MembershipRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class InstrumentServiceImpl implements InstrumentService {

    private final InstrumentRepository instrumentRepository;
    private final MembershipRepository membershipRepository;
    private final OrchestraRepository orchestraRepository;
    private final InstrumentMapper instrumentMapper;

    @Override
    public InstrumentResponse addInstrument(InstrumentRequest request) {
        if (instrumentRepository.existsByNumberAndOrchestraId(request.getNumber(), request.getOrchestraId())) {
            throw new IllegalArgumentException("Instrument o numerze " + request.getNumber() + " już istnieje w tej orkiestrze.");
        }

        Orchestra orchestra = orchestraRepository.findById(request.getOrchestraId())
                .orElseThrow(() -> new NotFoundException("Orchestra not found"));

        Instrument instrument = Instrument.builder()
                .instrumentType(request.getInstrumentType())
                .brand(request.getBrand())
                .number(request.getNumber())
                .description(request.getDescription())
                .orchestra(orchestra)
                .owner(null)
                .build();

        return instrumentMapper.mapToResponse(instrumentRepository.save(instrument));
    }

    @Override
    public InstrumentResponse updateInstrument(Long instrumentId, InstrumentRequest request) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument not found"));

        instrument.setInstrumentType(request.getInstrumentType());
        instrument.setBrand(request.getBrand());
        instrument.setNumber(request.getNumber());
        instrument.setDescription(request.getDescription());

        if (request.getOrchestraId() != null) {
            Orchestra orchestra = orchestraRepository.findById(request.getOrchestraId())
                    .orElseThrow(() -> new NotFoundException("Orchestra not found"));
            instrument.setOrchestra(orchestra);
        }

        return instrumentMapper.mapToResponse(instrumentRepository.save(instrument));
    }

    @Override
    public InstrumentResponse assignInstrumentToMember(Long instrumentId, Long membershipId) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument nie istnieje"));

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Członek orkiestry nie istnieje"));

        if (!instrument.getOrchestra().getId().equals(membership.getOrchestra().getId())) {
            throw new IllegalArgumentException("Instrument i członek nie należą do tej samej orkiestry");
        }

        if (instrument.getOwner() != null) {
            throw new IllegalStateException("Instrument już jest przypisany.");
        }

        instrument.setOwner(membership);
        return instrumentMapper.mapToResponse(instrumentRepository.save(instrument));
    }

    @Override
    public InstrumentResponse unassignInstrument(Long instrumentId) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument nie istnieje"));

        if (instrument.getOwner() == null) {
            throw new IllegalStateException("Instrument nie jest przypisany.");
        }

        instrument.setOwner(null);
        return instrumentMapper.mapToResponse(instrumentRepository.save(instrument));
    }

    @Override
    public void deleteInstrument(Long instrumentId) {
        Instrument instrument = instrumentRepository.findById(instrumentId)
                .orElseThrow(() -> new NotFoundException("Instrument nie istnieje"));

        if (instrument.getOwner() != null) {
            throw new IllegalStateException("Nie można usunąć przypisanego instrumentu!");
        }

        instrumentRepository.delete(instrument);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentResponse> getInstruments(Long orchestraId, InstrumentType type, Boolean available) {
        List<Instrument> instruments = instrumentRepository.findByOrchestraId(orchestraId);

        return instruments.stream()
                .filter(i -> type == null || type.equals(i.getInstrumentType()))
                .filter(i -> available == null || (available ? i.getOwner() == null : i.getOwner() != null))
                .map(instrumentMapper::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentResponse> getInstrumentsByMembership(Long membershipId) {
        return instrumentRepository.findByOwnerId(membershipId).stream()
                .map(instrumentMapper::mapToResponse)
                .toList();
    }
}