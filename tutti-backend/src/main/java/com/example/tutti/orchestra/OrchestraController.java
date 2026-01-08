package com.example.tutti.orchestra;

import com.example.tutti.resource.instrument.InstrumentResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/orchestras")
@RequiredArgsConstructor
@Tag(name = "Orchestras", description = "Zarządzanie orkiestrami")
public class OrchestraController {

    private final OrchestraService orchestraService;

    @Operation(summary = "Pobierz wszystkie orkiestry", description = "Zwraca listę wszystkich orkiestr w systemie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista orkiestr pobrana pomyślnie")
    })
    @GetMapping
    public ResponseEntity<List<OrchestraResponse>> getAllOrchestras() {
        return ResponseEntity.ok(orchestraService.findAll());
    }

    @Operation(summary = "Pobierz orkiestrę po ID", description = "Zwraca szczegóły orkiestry na podstawie jej ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orkiestra pobrana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrchestraResponse> getOrchestraById(
            @Parameter(description = "ID orkiestry") @PathVariable Long id
    ) {
        return ResponseEntity.ok(orchestraService.findById(id));
    }

    @Operation(summary = "Pobierz orkiestrę po nazwie", description = "Zwraca szczegóły orkiestry na podstawie nazwy.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orkiestra pobrana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @GetMapping("/name/{name}")
    public ResponseEntity<OrchestraResponse>getOrchestraByName(
            @Parameter(description = "Nazwa orkiestry") @PathVariable String name
    ) {
        return ResponseEntity.ok(orchestraService.findByName(name));
    }

    @Operation(summary = "Utwórz nową orkiestrę", description = "Tworzy nową orkiestrę przypisaną do użytkownika jako właściciela.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Orkiestra utworzona pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Orkiestra o takiej nazwie już istnieje")
    })
    @PostMapping
    public ResponseEntity<OrchestraResponse> createOrchestra(@RequestBody OrchestraRequest request) {
        return ResponseEntity.ok(orchestraService.createOrchestra(request));
    }
    @Operation(summary = "Aktualizuj orkiestrę", description = "Aktualizuje nazwę i/lub adres orkiestry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orkiestra zaktualizowana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @PutMapping("/{id}")
    @PreAuthorize("@ss.isManagement(#id) or hasRole('ADMIN')")
    public ResponseEntity<OrchestraResponse> updateOrchestra(
            @Parameter(description = "ID orkiestry") @PathVariable Long id,
            @RequestBody OrchestraRequest request
    ) {
        return ResponseEntity.ok(orchestraService.updateOrchestra(id, request, null));
    }

    @Operation(summary = "Usuń orkiestrę", description = "Usuwa orkiestrę jeśli użytkownik jest jej właścicielem lub adminem.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Orkiestra usunięta pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry"),
            @ApiResponse(responseCode = "403", description = "Brak uprawnień")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isOwner(#id) or hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrchestra(@PathVariable Long id) {
        orchestraService.deleteOrchestra(id);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "Pobierz instrumenty orkiestry", description = "Zwraca listę wszystkich instrumentów przypisanych do orkiestry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrumenty pobrane pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @GetMapping("/{orchestraId}/instruments")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public ResponseEntity<List<InstrumentResponse>> getInstruments(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId
    ) {
        try {
            return ResponseEntity.ok(orchestraService.getInstruments(orchestraId));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
    }


    @GetMapping("/owner/{ownerId}")
    @Operation(summary = "Pobierz orkiestry właściciela")
    public ResponseEntity<List<OrchestraResponse>> getOrchestrasByOwner(@PathVariable Long ownerId) {
        return ResponseEntity.ok(orchestraService.findAllByOwnerId(ownerId));
    }


}
