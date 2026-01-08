package com.example.tutti.music.part;

import com.example.tutti.exception.NotFoundException;
import com.example.tutti.music.score.Score;
import com.example.tutti.music.score.ScoreRepository;
import com.example.tutti.resource.instrument.InstrumentType;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class PartServiceImpl implements PartService {

    private final PartRepository partRepository;
    private final ScoreRepository scoreRepository;
    private final PartMapper partMapper;

    @Override
    public PartResponse createPart(CreatePartRequest request) {
        Score score = scoreRepository.findById(request.scoreId())
                .orElseThrow(() -> new NotFoundException("Partytura o ID " + request.scoreId() + " nie istnieje."));

        validatePartUniqueness(request.scoreId(), request.type(), request.partNumber(), null);

        Part part = new Part();
        part.setScore(score);
        partMapper.updateEntityFromRequest(request, part);

        return partMapper.toResponse(partRepository.save(part));
    }

    @Override
    public PartResponse updatePart(Long id, CreatePartRequest request) {
        Part existingPart = partRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Głos o ID " + id + " nie istnieje."));

        validatePartUniqueness(existingPart.getScore().getId(), request.type(), request.partNumber(), id);

        partMapper.updateEntityFromRequest(request, existingPart);

        return partMapper.toResponse(partRepository.save(existingPart));
    }

    @Override
    public void deletePart(Long id) {
        if (!partRepository.existsById(id)) {
            throw new NotFoundException("Nie można usunąć. Głos o ID " + id + " nie istnieje.");
        }
        partRepository.deleteById(id);
    }

    @Override
    public Part getPartEntity(Long id) {
        return partRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Głos o ID " + id + " nie istnieje."));
    }

    @Override
    public PartResponse getPart(Long id) {
        return partMapper.toResponse(getPartEntity(id));
    }

    @Override
    public List<PartResponse> getPartsByScore(Long scoreId, Long orchestraId) {
        return partRepository.findByScoreIdAndScoreOrchestraId(scoreId, orchestraId)
                .stream()
                .map(partMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public List<PartResponse> getPartsByInstrumentType(InstrumentType instrument, Long orchestraId) {
        return partRepository.findByTypeAndScoreOrchestraId(instrument, orchestraId)
                .stream()
                .map(partMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public PartResponse getPartByScoreAndInstrument(Long scoreId, InstrumentType type, Integer partNumber, Long orchestraId) {
        Part part = partRepository.findByScoreIdAndTypeAndPartNumberAndScoreOrchestraId(scoreId, type, partNumber, orchestraId)
                .orElseThrow(() -> new NotFoundException(
                        "Nie znaleziono głosu " + type.name() + " " + partNumber + " w partyturze o ID " + scoreId)
                );
        return partMapper.toResponse(part);
    }

    private void validatePartUniqueness(Long scoreId, InstrumentType type, Integer partNumber, Long existingPartId) {
        Optional<Part> existing = partRepository.findByScoreIdAndTypeAndPartNumber(scoreId, type, partNumber);
        if (existing.isPresent() && !existing.get().getId().equals(existingPartId)) {
            throw new IllegalArgumentException(
                    "Partia o typie " + type.name() + " i numerze " + partNumber + " już istnieje w tej partyturze."
            );
        }
    }
}