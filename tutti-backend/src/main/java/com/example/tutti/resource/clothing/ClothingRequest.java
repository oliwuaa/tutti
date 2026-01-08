package com.example.tutti.resource.clothing;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClothingRequest {

    private ClothingType type;
    private ClothingSize size;
    private ClothingSex sex;
    private Long membershipId;
    private ClothingStatus status;
}
