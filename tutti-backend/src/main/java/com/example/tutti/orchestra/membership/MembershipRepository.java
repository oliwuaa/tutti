package com.example.tutti.orchestra.membership;

import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRole;
import com.example.tutti.user.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface MembershipRepository extends JpaRepository<Membership, Long> {
    Optional<Membership> findByOrchestraAndUser(Orchestra orchestra, User user);
    Optional<Membership> findById(Long membershipId);
    List<Membership> findByUserId(Long userId);
    Boolean existsByUserEmailAndOrchestraId(String email, Long orchestraId);
    Boolean existsByUserEmailAndOrchestraIdAndRole(String email, Long orchestraId, OrchestraRole orchestraRole);
    boolean existsByUserEmailAndOrchestraIdAndRoleIn(String email, Long orchestraId, Collection<OrchestraRole> roles);
    List<Membership> findAllByUserEmail(String email);
    List<Membership> findAllByStatus(MembershipStatus status);
    List<Membership> findAllByUserEmailAndStatus(String email, MembershipStatus status);
    Optional<Membership> findByOrchestraIdAndUserId(Long orchestraId, Long userId);
    Optional<Membership> findByOrchestraIdAndUserIdAndStatusIn(Long orchestraId, Long userId,  Collection<MembershipStatus> status);
    List<Membership> findByOrchestraId(Long orchestraId);
    List<Membership> findByOrchestraIdAndStatus(Long orchestraId, MembershipStatus status);
    int countByOrchestraId(Long orchestraId);

}