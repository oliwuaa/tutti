package com.example.tutti.orchestra;

import com.example.tutti.exception.BadRequestException;
import com.example.tutti.exception.NotFoundException;
import com.example.tutti.orchestra.membership.Membership;
import com.example.tutti.orchestra.membership.MembershipRepository;
import com.example.tutti.orchestra.membership.MembershipStatus;
import com.example.tutti.resource.instrument.InstrumentResponse;
import com.example.tutti.resource.instrument.InstrumentType;
import com.example.tutti.user.User;
import com.example.tutti.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class OrchestraServiceImpl implements OrchestraService {

    private final OrchestraRepository orchestraRepository;
    private final MembershipRepository membershipRepository;
    private final UserRepository userRepository;
    private final OrchestraMapper orchestraMapper;

    @Override
    @Transactional(readOnly = true)
    public List<OrchestraResponse> findAll() {
        return orchestraRepository.findAll().stream()
                .map(orchestraMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public OrchestraResponse findById(Long id) {
        return orchestraRepository.findById(id)
                .map(orchestraMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Orkiestra o ID " + id + " nie istnieje."));
    }

    @Override
    @Transactional(readOnly = true)
    public OrchestraResponse findByName(String name) {
        return orchestraRepository.findByName(name)
                .map(orchestraMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono orkiestry o nazwie: " + name));
    }

    @Override
    public OrchestraResponse createOrchestra(OrchestraRequest request) {
        String email = SecurityContextHolder.getContext().getAuthentication().getName();
        User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException("Nie znaleziono zalogowanego użytkownika"));

        if (orchestraRepository.existsByName(request.getName().toUpperCase())) {
            throw new BadRequestException("Orkiestra o nazwie " + request.getName() + " już istnieje!");
        }

        Orchestra orchestra = Orchestra.builder()
                .name(request.getName().toUpperCase())
                .address(request.getAddress())
                .owner(currentUser)
                .memberships(new HashSet<>())
                .instruments(new HashSet<>())
                .build();

        Orchestra saved = orchestraRepository.save(orchestra);

        // Automatyczne dodanie właściciela jako pierwszego członka
        Membership ownerMembership = Membership.builder()
                .orchestra(saved)
                .user(currentUser)
                .role(OrchestraRole.OWNER)
                .instrumentType(InstrumentType.NONE)
                .partNumber(0)
                .status(MembershipStatus.ACTIVE)
                .dateJoined(LocalDate.now())
                .build();

        membershipRepository.save(ownerMembership);

        return orchestraMapper.toResponse(saved);
    }

    @Override
    public OrchestraResponse updateOrchestra(Long id, OrchestraRequest request, User currentUser) {
        Orchestra orchestra = orchestraRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Orkiestra o ID " + id + " nie istnieje."));

        if (request.getName() != null && !request.getName().equalsIgnoreCase(orchestra.getName())) {
            if (orchestraRepository.existsByName(request.getName().toUpperCase())) {
                throw new BadRequestException("Orkiestra o nazwie " + request.getName() + " już istnieje!");
            }
            orchestra.setName(request.getName().toUpperCase());
        }

        if (request.getAddress() != null && !request.getAddress().isBlank()) {
            orchestra.setAddress(request.getAddress());
        }

        return orchestraMapper.toResponse(orchestraRepository.save(orchestra));
    }

    @Override
    public void deleteOrchestra(Long id) {
        Orchestra orchestra = orchestraRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Orkiestra nie istnieje."));

        boolean hasAssignedResources = orchestra.getInstruments().stream()
                .anyMatch(inst -> inst.getOwner() != null);

        if (hasAssignedResources) {
            throw new IllegalStateException("Nie można usunąć orkiestry: istnieją instrumenty, które nie zostały jeszcze zwrócone!");
        }

        orchestraRepository.delete(orchestra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<InstrumentResponse> getInstruments(Long orchestraId) {
        Orchestra orchestra = orchestraRepository.findById(orchestraId)
                .orElseThrow(() -> new NotFoundException("Orkiestra nie istnieje."));

        return orchestraMapper.toInstrumentResponseList(orchestra);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrchestraResponse> findAllByOwnerId(Long ownerId) {
        return orchestraRepository.findByOwnerId(ownerId).stream()
                .map(orchestraMapper::toResponse)
                .collect(Collectors.toList());
    }
}