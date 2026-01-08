package com.example.tutti.event;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.orchestra.Orchestra;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "events")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Event extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventType type;

    private LocalDateTime date;

    private String address;

    @Column(columnDefinition = "TEXT")
    private String plan;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "orchestra_id", nullable = false)
    private Orchestra orchestra;

    @Builder.Default
    @OneToMany(mappedBy = "event", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("position ASC")
    private List<SetlistItem> setlist = new ArrayList<>();

}
