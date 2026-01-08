package com.example.tutti.user;

import com.example.tutti.orchestra.membership.MembershipResponse;
import com.example.tutti.orchestra.OrchestraResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZonedDateTime;
import java.util.Set;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserResponse {
    private Long id;
    private String email;
    private String firstName;
    private String lastName;
    private ZonedDateTime dateJoined;
    private Role role;
    private boolean isActive;

    private Set<MembershipResponse> memberships;
    private Set<OrchestraResponse> ownedOrchestras;
}
