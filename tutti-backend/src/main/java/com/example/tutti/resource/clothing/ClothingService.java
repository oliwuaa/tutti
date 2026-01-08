package com.example.tutti.resource.clothing;

import java.util.List;

public interface ClothingService {
    ClothingResponse addClothing(Long orchestraId, ClothingRequest request);
    ClothingResponse assignClothingToMember(Long clothingId, Long membershipId);
    ClothingResponse unassignClothingFromMember(Long clothingId);
    void removeClothing(Long clothingId);
    List<ClothingResponse> getClothes(Long orchestraId, ClothingType type, ClothingSize size, ClothingStatus status);
    List<ClothingResponse> getClothesByMembership(Long membershipId);
}