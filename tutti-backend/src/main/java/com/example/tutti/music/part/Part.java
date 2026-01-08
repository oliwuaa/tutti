package com.example.tutti.music.part;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.music.score.Score;
import com.example.tutti.resource.instrument.InstrumentType;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "parts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Part extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentType type;
    private Integer partNumber;

    @Embedded
    private PageRange pages;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "score_id")
    private Score score;
}
