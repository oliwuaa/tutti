package com.example.tutti.orchestra.membership;

import org.springframework.stereotype.Component;

@Component
public class MembershipMapper {

    public MembershipResponse mapToResponse(Membership membership) {
        if (membership == null) return null;
        return MembershipResponse.builder()
                .id(membership.getId())
                .orchestraId(membership.getOrchestra().getId())
                .orchestraName(membership.getOrchestra().getName())
                .userId(membership.getUser().getId())
                .firstName(membership.getUser().getFirstName())
                .lastName(membership.getUser().getLastName())
                .instrumentType(membership.getInstrumentType())
                .partNumber(membership.getPartNumber())
                .orchestraRole(membership.getRole())
                .status(membership.getStatus())
                .build();
    }
}