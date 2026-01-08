package com.example.tutti.resource.clothing;

import com.example.tutti.orchestra.membership.Membership;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ClothingRepository extends JpaRepository<Clothing, Long> {
    List<Clothing> findByOrchestraId(Long orchestraId);

    List<Clothing> findByOrchestraIdAndType(Long orchestraId, ClothingType type);

    List<Clothing> findByOrchestraIdAndStatus(Long orchestraId, ClothingStatus status);

    List<Clothing> findByOrchestraIdAndSize(Long orchestraId, ClothingSize size);

    List<Clothing> findByOrchestraIdAndTypeAndSize(Long orchestraId, ClothingType type, ClothingSize size);

    List<Clothing> findByMembershipId(Long membershipId);

    List<Clothing> findByMembership(Membership membership);
}
