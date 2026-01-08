package com.example.tutti.resource.clothing;

import com.example.tutti.exception.NotFoundException;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRepository;
import com.example.tutti.orchestra.membership.Membership;
import com.example.tutti.orchestra.membership.MembershipRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class ClothingServiceImpl implements ClothingService {

    private final ClothingRepository clothingRepository;
    private final OrchestraRepository orchestraRepository;
    private final MembershipRepository membershipRepository;
    private final ClothingMapper clothingMapper;

    @Override
    public ClothingResponse addClothing(Long orchestraId, ClothingRequest request) {
        Orchestra orchestra = orchestraRepository.findById(orchestraId)
                .orElseThrow(() -> new NotFoundException("Orkiestra nie istnieje"));

        Clothing clothing = Clothing.builder()
                .orchestra(orchestra)
                .type(request.getType())
                .size(request.getSize())
                .sex(request.getSex())
                .status(ClothingStatus.AVAILABLE)
                .membership(null)
                .build();

        return clothingMapper.mapToResponse(clothingRepository.save(clothing));
    }

    @Override
    public ClothingResponse assignClothingToMember(Long clothingId, Long membershipId) {
        Clothing clothing = clothingRepository.findById(clothingId)
                .orElseThrow(() -> new NotFoundException("Ubranie nie istnieje"));

        Membership membership = membershipRepository.findById(membershipId)
                .orElseThrow(() -> new NotFoundException("Członkostwo nie istnieje"));

        if (clothing.getStatus() == ClothingStatus.ASSIGNED || clothing.getMembership() != null) {
            throw new IllegalStateException("Ubranie jest już przypisane do innego członka.");
        }

        if (!clothing.getOrchestra().getId().equals(membership.getOrchestra().getId())) {
            throw new IllegalArgumentException("Ubranie i członek nie należą do tej samej orkiestry");
        }

        clothing.setMembership(membership);
        clothing.setStatus(ClothingStatus.ASSIGNED);

        return clothingMapper.mapToResponse(clothingRepository.save(clothing));
    }

    @Override
    public ClothingResponse unassignClothingFromMember(Long clothingId) {
        Clothing clothing = clothingRepository.findById(clothingId)
                .orElseThrow(() -> new NotFoundException("Ubranie nie istnieje"));

        if (clothing.getStatus().equals(ClothingStatus.AVAILABLE)) {
            throw new IllegalArgumentException("Ubranie nie zostało przydzielone do członka orkiestry.");
        }

        clothing.setMembership(null);
        clothing.setStatus(ClothingStatus.AVAILABLE);

        return clothingMapper.mapToResponse(clothingRepository.save(clothing));
    }

    @Override
    public void removeClothing(Long clothingId) {
        Clothing clothing = clothingRepository.findById(clothingId)
                .orElseThrow(() -> new NotFoundException("Ubranie nie istnieje"));

        if (!clothing.getStatus().equals(ClothingStatus.AVAILABLE)) {
            throw new IllegalArgumentException("Ubranie jest przydzielone do członka orkiestry - nie można usunąć.");
        }

        clothingRepository.delete(clothing);
    }

    @Override
    public List<ClothingResponse> getClothes(Long orchestraId, ClothingType type, ClothingSize size, ClothingStatus status) {
        List<Clothing> clothes = clothingRepository.findByOrchestraId(orchestraId);

        return clothes.stream()
                .filter(c -> type == null || c.getType() == type)
                .filter(c -> size == null || c.getSize() == size)
                .filter(c -> status == null || c.getStatus() == status)
                .map(clothingMapper::mapToResponse)
                .toList();
    }

    @Override
    public List<ClothingResponse> getClothesByMembership(Long membershipId) {
        return clothingRepository.findByMembershipId(membershipId).stream()
                .map(clothingMapper::mapToResponse)
                .toList();
    }
}