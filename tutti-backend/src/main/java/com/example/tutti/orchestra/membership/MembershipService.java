package com.example.tutti.orchestra.membership;

import com.example.tutti.resource.instrument.InstrumentType;
import java.util.List;

public interface MembershipService {
    MembershipResponse addMembership(MembershipRequest request);
    MembershipResponse approveMembership(Long membershipId);
    MembershipResponse rejectMembership(Long membershipId);
    void releaseResources(Membership membership);
    void removeMembership(Long membershipId);
    void leaveOrchestra(Long orchestraId, Long userId);
    MembershipResponse changeRole(Long id, MembershipRequest request);
    MembershipResponse changeInstrument(Long id, MembershipRequest request);
    List<MembershipResponse> getActiveMembersForOrchestra(Long orchestraId);
    List<MembershipResponse> getUserActiveMemberships(String email);
    List<MembershipResponse> getMembersByInstrument(Long orchestraId, InstrumentType instrumentType);
    List<MembershipResponse> getMembersByStatus(Long orchestraId, MembershipStatus status);
    List<MembershipResponse> findAllByUserId(Long userId);
    List<MembershipResponse> findAll();
    List<MembershipResponse> findAllActive();
    List<MembershipResponse> getUserMemberships(String email);
}