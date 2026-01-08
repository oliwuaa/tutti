package com.example.tutti.resource.folder;

import org.springframework.stereotype.Component;

@Component
public class MarchingFolderMapper {

    public MarchingFolderResponse toResponse(MarchingFolder folder) {
        if (folder == null) return null;
        return MarchingFolderResponse.builder()
                .id(folder.getId())
                .marchingType(folder.getMarchingType())
                .instrumentType(folder.getInstrumentType())
                .partNumber(folder.getPartNumber())
                .orchestraId(folder.getOrchestra().getId())
                .quantity(folder.getQuantity())
                .build();
    }
}