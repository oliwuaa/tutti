package com.example.tutti.auth;

import com.example.tutti.common.BaseEntity;
import com.example.tutti.user.User;
import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.time.Instant;

@Entity
@Data
@SuperBuilder
@AllArgsConstructor
@NoArgsConstructor
public class RefreshToken extends BaseEntity {

    @Column(nullable = false, unique = true)
    private String token;

    @OneToOne
    @JoinColumn(name = "user_id", referencedColumnName = "id", unique = true)
    private User user;

    @Column(nullable = false)
    private Instant expiryDate;
}