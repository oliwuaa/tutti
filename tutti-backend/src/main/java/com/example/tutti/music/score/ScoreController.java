package com.example.tutti.music.score;

import com.example.tutti.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/scores")
@RequiredArgsConstructor
@Tag(name = "Scores", description = "Zarządzanie partyturami w orkiestrze.")
public class ScoreController {

    private final ScoreService scoreService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "Utwórz nową partyturę z głosami", description = "Tworzy nową partyturę, zapisuje plik PDF oraz dodaje wszystkie zdefiniowane głosy w jednej transakcji.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partytura i głosy utworzone pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Błąd walidacji lub duplikacja danych"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @PostMapping(consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("@ss.canManageScores(#request.score().orchestraId()) or hasRole('ADMIN')")
    public ResponseEntity<ScoreResponse> createFullScore(
            @RequestPart("request") @Valid FullScoreRequest request,
            @RequestPart("file") MultipartFile file
    ) throws IOException {
        String pdfPath = fileStorageService.saveFile(file, "scores");

        ScoreResponse response = scoreService.createFullScore(request, pdfPath);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Aktualizuj partyturę i głosy", description = "Aktualizuje dane utworu, opcjonalnie podmienia plik PDF i synchronizuje listę głosów (usuwa stare, dodaje nowe).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partytura zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partytury")
    })
    @PutMapping(value = "/{id}", consumes = {MediaType.MULTIPART_FORM_DATA_VALUE})
    @PreAuthorize("@ss.canManageScores(#request.score().orchestraId()) or hasRole('ADMIN')")
    public ResponseEntity<ScoreResponse> updateFullScore(
            @PathVariable Long id,
            @RequestPart("request") @Valid FullScoreRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file
    ) throws IOException {
        String newPdfPath = null;
        if (file != null && !file.isEmpty()) {
            newPdfPath = fileStorageService.saveFile(file, "scores");
        }

        ScoreResponse response = scoreService.updateFullScore(id, request, newPdfPath);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Usuń partyturę", description = "Usuwa partyturę o podanym ID wraz z fizycznym plikiem i wszystkimi powiązanymi partiami.")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.candeleteScores(#id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScore(@PathVariable Long id) {
        scoreService.deleteScore(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    @PreAuthorize("@ss.canViewScore(#id) or hasRole('ADMIN')")
    public ResponseEntity<ScoreResponse> getScoreById(@PathVariable Long id) {
        return ResponseEntity.ok(scoreService.getScore(id));
    }

    @GetMapping
    @PreAuthorize("@ss.isMemberOf(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<List<ScoreResponse>> getScores(
            @RequestParam Long orchestraId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String composer
    ) {
        return ResponseEntity.ok(scoreService.getScores(orchestraId, title, composer));
    }

    @GetMapping("/{id}/file")
    @PreAuthorize("@ss.canViewScore(#id) or hasRole('ADMIN')")
    public ResponseEntity<byte[]> getScoreFile(@PathVariable Long id) throws IOException {
        ScoreResponse score = scoreService.getScore(id);
        byte[] pdfBytes = fileStorageService.loadFile(score.getPdfPath());

        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + score.getTitle() + ".pdf\"")
                .body(pdfBytes);
    }
}