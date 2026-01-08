package com.example.tutti.resource.instrument;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.membership.Membership;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;


@Entity
@Table(name = "instruments")
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Instrument extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InstrumentType instrumentType;
    private String brand;
    private Integer number;
    private String description;

    @ManyToOne
    @JoinColumn(name = "membership_id")
    private Membership owner;

    @ManyToOne
    @JoinColumn(name = "orchestra_id", nullable = false)
    private Orchestra orchestra;
}