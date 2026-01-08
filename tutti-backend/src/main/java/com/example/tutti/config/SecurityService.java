package com.example.tutti.config;

import com.example.tutti.music.part.PartRepository;
import com.example.tutti.music.score.ScoreRepository;
import com.example.tutti.orchestra.membership.MembershipRepository;
import com.example.tutti.orchestra.OrchestraRole;
import com.example.tutti.resource.clothing.ClothingRepository;
import com.example.tutti.resource.folder.MarchingFolderRepository;
import com.example.tutti.resource.instrument.InstrumentRepository;
import com.example.tutti.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service("ss")
@RequiredArgsConstructor
public class SecurityService {

    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final MarchingFolderRepository folderRepository;
    private final InstrumentRepository instrumentRepository;
    private final ClothingRepository clothingRepository;
    private final ScoreRepository scoreRepository;
    private final PartRepository partRepository;

    public boolean isConductor(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraIdAndRole(
                email, orchestraId, OrchestraRole.CONDUCTOR
        );
    }

    public boolean isLibrarian(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraIdAndRole(
                email, orchestraId, OrchestraRole.LIBRARIAN
        );
    }

    public boolean isOwner(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraIdAndRole(
                email, orchestraId, OrchestraRole.OWNER
        );
    }

    public boolean isMusician(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraIdAndRole(
                email, orchestraId, OrchestraRole.MUSICIAN
        );
    }

    public boolean isAdmin(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraIdAndRole(
                email, orchestraId, OrchestraRole.ADMIN
        );
    }

    public boolean isMemberOf(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraId(email, orchestraId);
    }

    public boolean isSelf(Long userId) {
        String currentUserEmail = SecurityContextHolder.getContext().getAuthentication().getName();
        return userRepository.findById(userId)
                .map(user -> user.getEmail().equals(currentUserEmail))
                .orElse(false);
    }

    public boolean isManagement(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraIdAndRoleIn(
                email, orchestraId,
                java.util.List.of(OrchestraRole.ADMIN, OrchestraRole.OWNER, OrchestraRole.CONDUCTOR)
        );
    }

    public boolean canManageScores(Long orchestraId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.existsByUserEmailAndOrchestraIdAndRoleIn(
                email, orchestraId,
                java.util.List.of(OrchestraRole.ADMIN, OrchestraRole.OWNER, OrchestraRole.CONDUCTOR, OrchestraRole.LIBRARIAN)
        );
    }

    public boolean canManageMembership(Long membershipId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();

        return membershipRepository.findById(membershipId).map(m -> {
            Long orchestraId = m.getOrchestra().getId();
            return isManagement(orchestraId);
        }).orElse(false);
    }

    public boolean isMembershipOwner(Long membershipId) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        return membershipRepository.findById(membershipId)
                .map(m -> m.getUser().getEmail().equals(email))
                .orElse(false);
    }

    public boolean canAccessMembership(Long membershipId) {
        return canManageMembership(membershipId) || isMembershipOwner(membershipId);
    }

    public boolean canManageFolder(Long folderId) {
        return folderRepository.findById(folderId)
                .map(folder -> {
                    Long orchestraId = folder.getOrchestra().getId();
                    return canManageScores(orchestraId);
                })
                .orElse(false);
    }

    public boolean canManageInstrument(Long instrumentId) {
        return instrumentRepository.findById(instrumentId)
                .map(instrument -> {
                    Long orchestraId = instrument.getOrchestra().getId();
                    return isManagement(orchestraId);
                })
                .orElse(false);
    }

    public boolean canManageClothing(Long clothingId) {
        return clothingRepository.findById(clothingId)
                .map(clothing -> {
                    Long orchestraId = clothing.getOrchestra().getId();
                    return isManagement(orchestraId);
                })
                .orElse(false);
    }

    public boolean canViewScore(Long scoreId) {
        return scoreRepository.findById(scoreId)
                .map(score -> {
                    Long orchestraId = score.getOrchestra().getId();
                    return isMemberOf(orchestraId);
                })
                .orElse(false);
    }

    public boolean canDeleteScore(Long scoreId) {
        return scoreRepository.findById(scoreId)
                .map(score -> {
                    Long orchestraId =score.getOrchestra().getId();
                    return canManageScores(orchestraId);
                })
                .orElse(false);
    }

    public boolean canManagePart(Long partId) {
        return partRepository.findById(partId)
                .map(part -> {
                    Long orchestraId = part.getScore().getOrchestra().getId();
                    return canManageScores(orchestraId);
                })
                .orElse(false);
    }

}