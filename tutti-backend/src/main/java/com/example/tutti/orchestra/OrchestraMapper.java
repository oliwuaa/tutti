package com.example.tutti.orchestra;

import com.example.tutti.orchestra.membership.MembershipRepository;
import com.example.tutti.resource.instrument.InstrumentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class OrchestraMapper {

    private final MembershipRepository membershipRepository;

    public OrchestraResponse toResponse(Orchestra orchestra) {
        if (orchestra == null) return null;

        int memberCount = membershipRepository.countByOrchestraId(orchestra.getId());

        return OrchestraResponse.builder()
                .id(orchestra.getId())
                .name(orchestra.getName())
                .address(orchestra.getAddress())
                .ownerName(orchestra.getOwner().getFirstName() + " " + orchestra.getOwner().getLastName())
                .membersCount(memberCount)
                .build();
    }

    public List<InstrumentResponse> toInstrumentResponseList(Orchestra orchestra) {
        return orchestra.getInstruments().stream()
                .map(inst -> new InstrumentResponse(
                        inst.getId(),
                        inst.getInstrumentType(),
                        inst.getBrand(),
                        inst.getNumber(),
                        inst.getDescription(),
                        inst.getOwner() != null ? inst.getOwner().getId() : null,
                        inst.getOwner() != null
                                ? inst.getOwner().getUser().getFirstName() + " " + inst.getOwner().getUser().getLastName()
                                : "brak"
                ))
                .collect(Collectors.toList());
    }
}