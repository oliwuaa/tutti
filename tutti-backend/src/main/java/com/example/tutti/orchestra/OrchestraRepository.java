package com.example.tutti.orchestra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrchestraRepository extends JpaRepository<Orchestra, Long> {
    Optional<Orchestra> findByName(String name);
    boolean existsByName(String name);
    Optional<Orchestra> findById(Long id);
    List<Orchestra> findAll();
    List<Orchestra> findByOwnerId(Long ownerId);
}
