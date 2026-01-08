package com.example.tutti.music.score;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateScoreRequest {
    private String title;
    private String composer;
    private Long orchestraId;
}
