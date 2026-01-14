package com.example.tutti.music.score;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.music.part.Part;
import com.example.tutti.orchestra.Orchestra;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.*;

@Entity
@Table(name = "scores")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Score extends BaseEntity {

    private String title;
    private String composer;
    private String pdfPath;

    @OneToMany(mappedBy = "score", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Part> parts = new ArrayList<>();

    @ManyToOne
    @JoinColumn(name = "orchestra_id", nullable = false)
    private Orchestra orchestra;

}
