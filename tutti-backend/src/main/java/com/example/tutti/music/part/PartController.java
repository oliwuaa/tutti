package com.example.tutti.music.part;

import com.example.tutti.resource.instrument.InstrumentType;
import com.example.tutti.storage.PdfExtractionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/parts")
@RequiredArgsConstructor
@Tag(name = "Parts", description = "Zarządzanie głosami partytur w orkiestrze.")
public class PartController {

    private final PartService partService;
    private final PdfExtractionService pdfExtractionService;

    @Operation(summary = "Utwórz nową partię dla partytury")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Partia utworzona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Nieprawidłowe dane (np. duplikat partii)"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partytury")
    })
    @PostMapping
    @PreAuthorize("@ss.canManageScores(#request.orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<PartResponse> createPart(@RequestBody CreatePartRequest request) {
        PartResponse response = partService.createPart(request);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @Operation(summary = "Pobierz szczegóły partii")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Szczegóły partii pobrane pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partii")
    })
    @GetMapping("/{id}")
    public ResponseEntity<PartResponse> getPartById(@PathVariable Long id) {
        return ResponseEntity.ok(partService.getPart(id));
    }

    @Operation(summary = "Pobierz wszystkie partie dla danej partytury")
    @GetMapping("/score/{scoreId}/orchestra/{orchestraId}")
    @PreAuthorize("@ss.isMemberOf(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<List<PartResponse>> getPartsByScore(
            @PathVariable Long scoreId,
            @PathVariable Long orchestraId
    ) {
        return ResponseEntity.ok(partService.getPartsByScore(scoreId, orchestraId));
    }

    @Operation(summary = "Wyszukaj partie po instrumencie w danej orkiestrze")
    @GetMapping("/search/orchestra/{orchestraId}")
    @PreAuthorize("@ss.isMemberOf(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<List<PartResponse>> getPartsByInstrumentType(
            @PathVariable Long orchestraId,
            @RequestParam InstrumentType instrumentType
    ) {
        return ResponseEntity.ok(partService.getPartsByInstrumentType(instrumentType, orchestraId));
    }

    @Operation(summary = "Pobierz konkretną partię instrumentu dla partytury")
    @GetMapping("/score/{scoreId}/type/{type}/number/{partNumber}/orchestra/{orchestraId}")
    @PreAuthorize("@ss.isMemberOf(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<PartResponse> getPartByScoreAndInstrument(
            @PathVariable Long scoreId,
            @PathVariable InstrumentType type,
            @PathVariable Integer partNumber,
            @PathVariable Long orchestraId
    ) {
        return ResponseEntity.ok(partService.getPartByScoreAndInstrument(scoreId, type, partNumber, orchestraId));
    }

    @Operation(summary = "Zaktualizuj metadane partii")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Partia zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partii")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@ss.canManagePart(#id) or hasRole('ADMIN')")
    public ResponseEntity<PartResponse> updatePart(@PathVariable Long id, @RequestBody CreatePartRequest request) {
        PartResponse response = partService.updatePart(id, request);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "Usuń partię")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Partia usunięta pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partii")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.canManagePart(#id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deletePart(@PathVariable Long id) {
        partService.deletePart(id);
        return ResponseEntity.noContent().build();
    }

    @Operation(summary = "Pobierz wyciętą partię PDF",
            description = "Dynamicznie wycina zakres stron z głównego pliku Score i zwraca plik PDF.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Plik PDF zwrócony pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono partii/partytury"),
            @ApiResponse(responseCode = "500", description = "Błąd I/O lub błąd wycinania PDF")
    })
    @GetMapping("/{id}/pdf")
    public ResponseEntity<byte[]> getPartPdf(@PathVariable Long id) {
        try {
            Part part = partService.getPartEntity(id);
            byte[] pdfContent = pdfExtractionService.extractPartPdfBytes(part.getScore(), part);

            return ResponseEntity.ok()
                    .contentType(MediaType.APPLICATION_PDF)
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"part.pdf\"")
                    .body(pdfContent);
        } catch (IOException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}