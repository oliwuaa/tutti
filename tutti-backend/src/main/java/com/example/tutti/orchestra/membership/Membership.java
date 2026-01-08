package com.example.tutti.orchestra.membership;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.music.part.Part;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRole;
import com.example.tutti.resource.instrument.InstrumentType;
import com.example.tutti.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.LocalDate;

@Entity
@Table(name = "orchestra_memberships")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Membership extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "orchestra_id", nullable = false)
    private Orchestra orchestra;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    private OrchestraRole role;

    @Enumerated(EnumType.STRING)
    private InstrumentType instrumentType;
    private Integer partNumber;

    @ManyToOne
    private Part assignedPart;

    private LocalDate dateJoined;

    private boolean active;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MembershipStatus status;

}
