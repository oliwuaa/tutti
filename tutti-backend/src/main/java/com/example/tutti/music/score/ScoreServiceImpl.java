package com.example.tutti.music.score;

import com.example.tutti.exception.NotFoundException;
import com.example.tutti.music.part.CreatePartRequest;
import com.example.tutti.music.part.Part;
import com.example.tutti.music.part.PartRepository;
import com.example.tutti.music.part.PartService;
import com.example.tutti.orchestra.Orchestra;
import com.example.tutti.orchestra.OrchestraRepository;
import com.example.tutti.storage.FileStorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ScoreServiceImpl implements ScoreService {

    private final ScoreRepository scoreRepository;
    private final PartRepository partRepository; // Dodane do usuwania głosów przy edycji
    private final OrchestraRepository orchestraRepository;
    private final FileStorageService fileStorageService;
    private final ScoreMapper scoreMapper;
    private final PartService partService;

    @Override
    public ScoreResponse createFullScore(FullScoreRequest fullRequest, String pdfPath) {
        ScoreResponse savedScore = createScore(fullRequest.score(), pdfPath);

        try {
            for (CreatePartRequest partReq : fullRequest.parts()) {
                CreatePartRequest adjustedPart = new CreatePartRequest(
                        savedScore.getId(),
                        partReq.type(),
                        partReq.partNumber(),
                        partReq.pageStart(),
                        partReq.pageEnd()
                );
                partService.createPart(adjustedPart);
            }
        } catch (Exception e) {
            fileStorageService.deleteFile(pdfPath);
            throw e;
        }

        return savedScore;
    }

    @Override
    public ScoreResponse updateFullScore(Long id, FullScoreRequest fullRequest, String newPdfPath) {
        ScoreResponse updatedScore = updateScore(id, fullRequest.score(), newPdfPath);

        List<Part> oldParts = partRepository.findByScoreIdAndScoreOrchestraId(id, fullRequest.score().orchestraId());
        partRepository.deleteAll(oldParts);

        for (CreatePartRequest partReq : fullRequest.parts()) {
            CreatePartRequest adjustedPart = new CreatePartRequest(
                    id,
                    partReq.type(),
                    partReq.partNumber(),
                    partReq.pageStart(),
                    partReq.pageEnd()
            );
            partService.createPart(adjustedPart);
        }

        return updatedScore;
    }


    @Override
    public ScoreResponse createScore(CreateScoreRequest request, String pdfPath) {
        Orchestra orchestra = orchestraRepository.findById(request.orchestraId())
                .orElseThrow(() -> new NotFoundException("Orkiestra nie istnieje."));

        if (scoreRepository.existsByTitleAndComposerAndOrchestraId(
                request.title(), request.composer(), request.orchestraId())) {
            fileStorageService.deleteFile(pdfPath);
            throw new IllegalArgumentException("Ten utwór już znajduje się w bibliotece tej orkiestry.");
        }

        Score score = Score.builder()
                .orchestra(orchestra)
                .build();

        scoreMapper.updateEntityFromRequest(request, score, pdfPath);

        return scoreMapper.toResponse(scoreRepository.save(score));
    }

    @Override
    public ScoreResponse updateScore(Long id, CreateScoreRequest request, String newPdfPath) {
        Score existingScore = scoreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partytura nie istnieje."));

        boolean duplicateExists = scoreRepository.findFiltered(existingScore.getOrchestra().getId(), request.title(), request.composer())
                .stream()
                .anyMatch(s -> !s.getId().equals(id));

        if (duplicateExists) {
            if (newPdfPath != null && !newPdfPath.isBlank()) {
                fileStorageService.deleteFile(newPdfPath);
            }
            throw new IllegalArgumentException("Inny utwór o tym tytule i kompozytorze już istnieje w tej orkiestrze.");
        }

        if (newPdfPath != null && !newPdfPath.isBlank()) {
            fileStorageService.deleteFile(existingScore.getPdfPath());
        }

        scoreMapper.updateEntityFromRequest(request, existingScore, newPdfPath);

        return scoreMapper.toResponse(scoreRepository.save(existingScore));
    }

    @Override
    public void deleteScore(Long id) {
        Score score = scoreRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Partytura nie istnieje."));

        fileStorageService.deleteFile(score.getPdfPath());
        scoreRepository.delete(score);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ScoreResponse> getScores(Long orchestraId, String title, String composer) {
        return scoreRepository.findFiltered(orchestraId, title, composer)
                .stream()
                .map(scoreMapper::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ScoreResponse getScore(Long id) {
        return scoreRepository.findById(id)
                .map(scoreMapper::toResponse)
                .orElseThrow(() -> new NotFoundException("Partytura nie istnieje."));
    }
}