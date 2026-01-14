package com.example.tutti.music.score;

import org.springframework.stereotype.Component;

@Component
public class ScoreMapper {

    public ScoreResponse toResponse(Score score) {
        if (score == null) return null;
        return ScoreResponse.builder()
                .id(score.getId())
                .title(score.getTitle())
                .composer(score.getComposer())
                .orchestraId(score.getOrchestra().getId())
                .pdfPath(score.getPdfPath())
                .build();
    }

    public void updateEntityFromRequest(CreateScoreRequest request, Score score, String pdfPath) {
        score.setTitle(request.title());
        score.setComposer(request.composer());
        if (pdfPath != null && !pdfPath.isBlank()) {
            score.setPdfPath(pdfPath);
        }
    }
}