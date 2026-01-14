package com.example.tutti.orchestra.membership;

import com.example.tutti.exception.NotFoundException;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRepository;
import com.example.tutti.orchestra.OrchestraRole;
import com.example.tutti.resource.clothing.Clothing;
import com.example.tutti.resource.clothing.ClothingRepository;
import com.example.tutti.resource.clothing.ClothingStatus;
import com.example.tutti.resource.instrument.Instrument;
import com.example.tutti.resource.instrument.InstrumentRepository;
import com.example.tutti.resource.instrument.InstrumentType;
import com.example.tutti.user.User;
import com.example.tutti.user.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class MembershipServiceImpl implements MembershipService {

    private final OrchestraRepository orchestraRepository;
    private final MembershipRepository membershipRepository;
    private final InstrumentRepository instrumentRepository;
    private final ClothingRepository clothingRepository;
    private final UserRepository userRepository;
    private final MembershipMapper membershipMapper; // Wzorzec Mapper

    @Override
    public MembershipResponse addMembership(MembershipRequest request) {
        List<MembershipStatus> activeOrPending = List.of(MembershipStatus.ACTIVE, MembershipStatus.PENDING);
        Orchestra orchestra = orchestraRepository.findById(request.getOrchestraId())
                .orElseThrow(() -> new NotFoundException("Orkiestra nie istnieje"));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new NotFoundException("Użytkownik nie istnieje"));

        membershipRepository.findByOrchestraIdAndUserIdAndStatusIn(orchestra.getId(), user.getId(), activeOrPending)
                .ifPresent(m -> {
                    throw new IllegalStateException("Masz już aktywne lub oczekujące członkostwo");
                });

        boolean isAdmin = SecurityContextHolder.getContext()
                .getAuthentication()
                .getAuthorities()
                .stream()
                .anyMatch(a -> a.getAuthority().equals("ADMIN"));

        Membership membership = Membership.builder()
                .orchestra(orchestra)
                .user(user)
                .role(request.getOrchestraRole())
                .instrumentType(request.getInstrumentType())
                .partNumber(request.getPartNumber())
                .status(isAdmin ? MembershipStatus.ACTIVE : MembershipStatus.PENDING)
                .dateJoined(isAdmin ? LocalDate.now() : null)
                .build();

        return membershipMapper.mapToResponse(membershipRepository.save(membership));
    }

    @Override
    public MembershipResponse approveMembership(Long membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Członek nie istnieje"));

        membership.setStatus(MembershipStatus.ACTIVE);
        membership.setDateJoined(LocalDate.now());

        return membershipMapper.mapToResponse(membershipRepository.save(membership));
    }

    @Override
    public MembershipResponse rejectMembership(Long membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Członek nie istnieje"));

        membership.setStatus(MembershipStatus.REJECTED);
        return membershipMapper.mapToResponse(membershipRepository.save(membership));
    }

    @Override
    public void removeMembership(Long membershipId) {
        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Członek nie istnieje"));

        if (membership.getOrchestra().getOwner().equals(membership.getUser())) {
            throw new IllegalStateException("Właściciel nie może opuścić własnej orkiestry.");
        }

        releaseResources(membership);
        membership.setStatus(MembershipStatus.LEFT);
        membership.setActive(false);
        membershipRepository.save(membership);
    }

    public void releaseResources(Membership membership) {
        List<Clothing> clothes = clothingRepository.findByMembership(membership);
        clothes.forEach(c -> {
            c.setMembership(null);
            c.setStatus(ClothingStatus.AVAILABLE);
        });
        clothingRepository.saveAll(clothes);

        List<Instrument> instruments = instrumentRepository.findByOwner(membership);
        instruments.forEach(i -> i.setOwner(null));
        instrumentRepository.saveAll(instruments);

        clothingRepository.flush();
        instrumentRepository.flush();
    }

    @Override
    public void leaveOrchestra(Long orchestraId, Long userId) {
        Membership membership = membershipRepository.findByOrchestraIdAndUserId(orchestraId, userId)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono członkostwa."));
        membership.setStatus(MembershipStatus.LEFT);
        membershipRepository.save(membership);
    }

    @Override
    @Transactional
    public MembershipResponse changeRole(Long id, MembershipRequest request) {
        Membership targetMembership = membershipRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Członek nie istnieje"));

        Orchestra orchestra = targetMembership.getOrchestra();

        if (request.getOrchestraRole() == OrchestraRole.OWNER) {
            User oldOwner = orchestra.getOwner();
            User newOwner = targetMembership.getUser();

            if (!oldOwner.getId().equals(newOwner.getId())) {

                Membership oldOwnerMembership = membershipRepository
                        .findByOrchestraIdAndUserId(orchestra.getId(), oldOwner.getId())
                        .orElseThrow(() -> new NotFoundException("Nie znaleziono członkostwa starego właściciela"));

                oldOwnerMembership.setRole(OrchestraRole.MUSICIAN);
                membershipRepository.save(oldOwnerMembership);

                orchestra.setOwner(newOwner);
                orchestraRepository.save(orchestra);
            }
        }
        targetMembership.setRole(request.getOrchestraRole());

        return membershipMapper.mapToResponse(membershipRepository.save(targetMembership));
    }

    @Override
    public MembershipResponse changeInstrument(Long id, MembershipRequest request) {
        Membership membership = membershipRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono członkostwa."));
        membership.setInstrumentType(request.getInstrumentType());
        membership.setPartNumber(request.getPartNumber());
        return membershipMapper.mapToResponse(membershipRepository.save(membership));
    }

    @Override
    public List<MembershipResponse> getActiveMembersForOrchestra(Long orchestraId) {
        return membershipRepository.findByOrchestraIdAndStatus(orchestraId, MembershipStatus.ACTIVE).stream()
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> getUserActiveMemberships(String email) {
        return membershipRepository.findAllByUserEmailAndStatus(email, MembershipStatus.ACTIVE).stream()
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> getMembersByInstrument(Long orchestraId, InstrumentType instrumentType) {
        return membershipRepository.findByOrchestraIdAndStatus(orchestraId, MembershipStatus.ACTIVE).stream()
                .filter(m -> m.getInstrumentType() == instrumentType)
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> getMembersByStatus(Long orchestraId, MembershipStatus status) {
        return membershipRepository.findByOrchestraIdAndStatus(orchestraId, status).stream()
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> findAllByUserId(Long userId) {
        return membershipRepository.findByUserId(userId).stream()
                .filter(m -> !m.getRole().name().equals("OWNER"))
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> findAll() {
        return membershipRepository.findAll().stream()
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> findAllActive() {
        return membershipRepository.findAllByStatus(MembershipStatus.ACTIVE).stream()
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<MembershipResponse> getUserMemberships(String email) {
        return membershipRepository.findAllByUserEmail(email).stream()
                .map(membershipMapper::mapToResponse)
                .collect(Collectors.toList());
    }
}