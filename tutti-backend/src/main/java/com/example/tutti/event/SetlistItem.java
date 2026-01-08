package com.example.tutti.event;

import com.example.tutti.music.score.Score;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "setlist_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SetlistItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false)
    private Event event;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "score_id", nullable = true)
    private Score score;

    private String customTitle;

    @Column(nullable = false)
    private Integer position;

    private String notes;
}
