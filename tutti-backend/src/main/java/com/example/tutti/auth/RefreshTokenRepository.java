package com.example.tutti.auth;

import com.example.tutti.user.User;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {
    Optional<RefreshToken> findByToken(String token);

    @Modifying
    @Transactional
    @Query("DELETE FROM RefreshToken r WHERE r.user.email = :email")
    void deleteByUserEmail(String email);


    @Modifying
    @Transactional
    void deleteByUser(User user);

    Optional<RefreshToken> findByUser(User user);
}