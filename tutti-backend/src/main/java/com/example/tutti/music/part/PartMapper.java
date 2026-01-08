package com.example.tutti.music.part;

import org.springframework.stereotype.Component;

@Component
public class PartMapper {

    public PartResponse toResponse(Part part) {
        if (part == null) return null;

        if (part.getScore() == null) {
            throw new IllegalStateException("Błąd danych: Głos o ID " + part.getId() + " nie posiada przypisanej partytury.");
        }

        return new PartResponse(
                part.getId(),
                part.getScore().getId(),
                part.getType(),
                part.getPartNumber(),
                part.getScore().getPdfPath(),
                part.getPages().getPageStart(),
                part.getPages().getPageEnd()
        );
    }

    public void updateEntityFromRequest(CreatePartRequest request, Part part) {
        part.setType(request.type());
        part.setPartNumber(request.partNumber());
        part.setPages(PageRange.of(request.pageStart(), request.pageEnd()));
    }
}