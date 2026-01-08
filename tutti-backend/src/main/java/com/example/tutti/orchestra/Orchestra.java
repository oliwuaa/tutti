package com.example.tutti.orchestra;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.orchestra.membership.Membership;
import com.example.tutti.resource.instrument.Instrument;
import com.example.tutti.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.Set;

@Entity
@Table(name = "orchestras")
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Orchestra extends BaseEntity {

    @Column(unique = true)
    private String name;
    private String address;

    @ManyToOne
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @OneToMany(mappedBy = "orchestra", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Instrument> instruments;

    @OneToMany(mappedBy = "orchestra", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<Membership> memberships;

}