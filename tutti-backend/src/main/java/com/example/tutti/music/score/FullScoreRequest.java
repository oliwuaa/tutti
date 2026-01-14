package com.example.tutti.music.score;

import com.example.tutti.music.part.CreatePartRequest;

import java.util.List;

public record FullScoreRequest(CreateScoreRequest score, List<CreatePartRequest> parts) {
}
