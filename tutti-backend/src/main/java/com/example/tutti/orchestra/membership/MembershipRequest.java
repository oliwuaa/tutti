package com.example.tutti.orchestra.membership;

import com.example.tutti.orchestra.OrchestraRole;
import com.example.tutti.resource.instrument.InstrumentType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MembershipRequest {
    private Long orchestraId;
    private Long userId;
    private OrchestraRole orchestraRole;
    private InstrumentType instrumentType;
    private Integer partNumber;
}
