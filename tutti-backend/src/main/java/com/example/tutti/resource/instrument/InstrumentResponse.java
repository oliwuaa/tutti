package com.example.tutti.resource.instrument;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class InstrumentResponse {
    private Long id;
    private InstrumentType instrumentType;
    private String brand;
    private Integer number;
    private String description;
    private Long ownerId;
    private String ownerFullName;
}