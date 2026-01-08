package com.example.tutti.resource.clothing;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ClothingResponse {

    private Long id;
    private ClothingType type;
    private ClothingSize size;
    private ClothingSex sex;
    private ClothingStatus status;
    private Long orchestraId;
    private Long membershipId;
}
