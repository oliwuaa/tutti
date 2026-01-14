package com.example.tutti.orchestra.membership;

import com.example.tutti.orchestra.OrchestraRole;
import com.example.tutti.resource.instrument.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MembershipResponse {
    private Long id;
    private Long orchestraId;
    private String orchestraName;
    private Long userId;
    private String firstName;
    private String lastName;
    private InstrumentType instrumentType;
    private Integer partNumber;
    private OrchestraRole orchestraRole;
    private MembershipStatus status;
}