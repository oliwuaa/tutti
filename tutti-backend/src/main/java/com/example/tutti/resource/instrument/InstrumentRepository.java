package com.example.tutti.resource.instrument;

import com.example.tutti.orchestra.membership.Membership;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface InstrumentRepository extends JpaRepository<Instrument, Long> {
    List<Instrument> findByOrchestraId(Long orchestraId);
    List<Instrument> findByOwnerId(Long membershipId);

    boolean existsByNumberAndOrchestraId(Integer number, Long orchestraId);
    List<Instrument> findByOwner(Membership owner);
}
