package com.example.tutti.music.score;

import java.util.List;

public interface ScoreService {
    ScoreResponse createScore(CreateScoreRequest request, String pdfPath);
    ScoreResponse updateScore(Long id, CreateScoreRequest request, String newPdfPath);
    void deleteScore(Long id);
    List<ScoreResponse> getScores(Long orchestraId, String title, String composer);
    ScoreResponse getScore(Long id);
    ScoreResponse updateFullScore(Long id, FullScoreRequest fullRequest, String newPdfPath);
    ScoreResponse createFullScore(FullScoreRequest fullRequest, String pdfPath);
}