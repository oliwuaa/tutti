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
    private Long id;              // ID członkostwa
    private Long orchestraId;     // ID orkiestry
    private String orchestraName; // Nazwa orkiestry
    private Long userId;          // ID użytkownika
    private String firstName;
    private String lastName;
    private InstrumentType instrumentType;
    private Integer partNumber;
    private OrchestraRole orchestraRole;
    private MembershipStatus status;
}