package com.example.tutti.music.score;

public record CreateScoreRequest (
    String title,
    String composer,
    Long orchestraId
) {}
