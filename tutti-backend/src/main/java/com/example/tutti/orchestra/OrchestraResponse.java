package com.example.tutti.orchestra;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrchestraResponse {
    private Long id;
    private String name;
    private String address;
    private String ownerName;
    private int membersCount;
}
