package com.example.tutti.music.score;

import com.example.tutti.storage.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
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
public class ScoreController {

    private final ScoreService scoreService;
    private final FileStorageService fileStorageService;

    @Operation(summary = "Utwórz nową partyturę", description = "Tworzy nową partyturę w systemie i przypisuje do orkiestry.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partytura utworzona pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @PostMapping(consumes = {"multipart/form-data"})
    @PreAuthorize("@ss.canManageScores(#request.orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<ScoreResponse> createScore(
            @RequestPart("request") @Valid CreateScoreRequest request,
            @RequestPart("file") MultipartFile file
    ) throws IOException {

        String pdfPath = fileStorageService.saveFile(file, "scores");

        ScoreResponse response = scoreService.createScore(request, pdfPath);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @Operation(summary = "Aktualizuj partyturę", description = "Aktualizuje istniejącą partyturę.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partytura zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partytury")
    })
    @PutMapping(value = "/{id}", consumes = {"multipart/form-data"})
    @PreAuthorize("@ss.canManageScores(#request.orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<ScoreResponse> updateScore(
            @PathVariable Long id,
            @RequestPart("request") CreateScoreRequest request,
            @RequestPart(value = "file", required = false) MultipartFile file // Plik opcjonalny
    ) throws IOException {
        String newPdfPath = null;

        if (file != null && !file.isEmpty()) {
            newPdfPath = fileStorageService.saveFile(file, "scores");
        }

        ScoreResponse response = scoreService.updateScore(id, request, newPdfPath);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Usuń partyturę", description = "Usuwa partyturę o podanym ID wraz ze wszystkimi powiązanymi partiami.")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Partytura usunięta pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partytury")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.candeleteScores(#id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteScore(
            @Parameter(description = "ID partytury do usunięcia") @PathVariable Long id
    ) {
        scoreService.deleteScore(id);
        return ResponseEntity.noContent().build(); // Standardowy kod 204 No Content dla DELETE
    }

    @Operation(summary = "Pobierz partyturę po ID", description = "Zwraca szczegóły partytury na podstawie jej ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partytura pobrana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partytury")
    })
    @GetMapping("/{id}")
    @PreAuthorize("@ss.canViewScore(#id) or hasRole('ADMIN')")
    public ResponseEntity<ScoreResponse> getScoreById(
            @Parameter(description = "ID partytury") @PathVariable Long id
    ) {
        return ResponseEntity.ok(scoreService.getScore(id));
    }

    @Operation(summary = "Pobierz partytury według orkiestry", description = "Zwraca listę partytur dla danej orkiestry. Opcjonalnie można filtrować po tytule i kompozytorze.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista partytur pobrana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @GetMapping
    @PreAuthorize("@ss.isMemberOf(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<List<ScoreResponse>> getScores(
            @RequestParam Long orchestraId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String composer
    ) {
        return ResponseEntity.ok(scoreService.getScores(orchestraId, title, composer));
    }

    @Operation(summary = "Pobierz plik PDF partytury", description = "Zwraca surowy plik PDF do wyświetlenia w przeglądarce.")
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
