package com.example.tutti.resource.clothing;

import org.springframework.stereotype.Component;

@Component
public class ClothingMapper {

    public ClothingResponse mapToResponse(Clothing clothing) {
        if (clothing == null) return null;
        return ClothingResponse.builder()
                .id(clothing.getId())
                .type(clothing.getType())
                .size(clothing.getSize())
                .sex(clothing.getSex())
                .status(clothing.getStatus())
                .orchestraId(clothing.getOrchestra().getId())
                .membershipId(clothing.getMembership() != null ? clothing.getMembership().getId() : null)
                .build();
    }
}