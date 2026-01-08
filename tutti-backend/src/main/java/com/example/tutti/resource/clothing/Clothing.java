package com.example.tutti.resource.clothing;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.membership.Membership;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

@Entity
@Table(name = "clothes")
@Setter
@Getter
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class Clothing extends BaseEntity {

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClothingType type;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClothingSize size;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClothingSex sex;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClothingStatus status;

    @ManyToOne
    @JoinColumn(name = "orchestra_id", nullable = false)
    private Orchestra orchestra;

    @ManyToOne
    @JoinColumn(name = "membership_id")
    private Membership membership;

}