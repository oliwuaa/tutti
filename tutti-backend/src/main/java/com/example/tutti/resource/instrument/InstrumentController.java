package com.example.tutti.resource.instrument;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/instruments")
@RequiredArgsConstructor
@Tag(name = "Instruemnts", description = "Zarządzanie instrumentami orkiestr")
public class InstrumentController {

    private final InstrumentService instrumentService;

    @Operation(summary = "Pobierz instrumenty orkiestry", description = "Zwraca wszystkie instrumenty dla danej orkiestry. Można opcjonalnie filtrować po typie instrumentu i dostępności.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista instrumentów pobrana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @GetMapping("/orchestra/{orchestraId}")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public List<InstrumentResponse> getInstruments(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId,
            @Parameter(description = "Opcjonalny filtr po typie instrumentu") @RequestParam(required = false) InstrumentType type,
            @Parameter(description = "Opcjonalny filtr po dostępności (true = dostępny, false = przypisany)") @RequestParam(required = false) Boolean available
    ) {
        return instrumentService.getInstruments(orchestraId, type, available);
    }

    @Operation(summary = "Pobierz instrumenty po członkoswie", description = "Zwraca wszystkie instrumenty dla danego członkostwa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista instrumentów pobrana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono członkostwa")
    })
    @GetMapping("membership/{membershipId}")
    @PreAuthorize("@ss.canAccessMembership(#membershipId) or hasRole('ADMIN')")
    public List<InstrumentResponse> getInstruments(
            @Parameter(description = "ID członkostwa") @PathVariable Long membershipId
    ) {
        return instrumentService.getInstrumentsByMembership(membershipId);
    }

    @Operation(summary = "Dodaj nowy instrument", description = "Tworzy nowy instrument w systemie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrument został dodany pomyślnie"),
            @ApiResponse(responseCode = "400", description = "Niepoprawne dane wejściowe")
    })
    @PostMapping
    @PreAuthorize("@ss.isManagement(#instrumentRequest.orchestraId) or hasRole('ADMIN')")
    public InstrumentResponse createInstrument(
            @Parameter(description = "Obiekt instrumentu do utworzenia") @RequestBody InstrumentRequest instrumentRequest
    ) {
        return instrumentService.addInstrument(instrumentRequest);
    }

    @Operation(summary = "Aktualizuj instrument", description = "Aktualizuje dane instrumentu. Może to zrobić tylko zarząd orkiestry.")
    @PutMapping("/{instrumentId}")
    @PreAuthorize("@ss.canManageInstrument(#id) or hasRole('ADMIN')")
    public InstrumentResponse updateInstrument(
            @PathVariable Long instrumentId,
            @RequestBody InstrumentRequest instrumentRequest
    ) {
        return instrumentService.updateInstrument(instrumentId, instrumentRequest);
    }

    @Operation(summary = "Przypisz instrument do członka", description = "Przypisuje konkretny instrument do członka na podstawie jego ID członkostwa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrument przypisany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono instrumentu lub członka")
    })
    @PostMapping("/{instrumentId}/member/{membershipId}/assign")
    @PreAuthorize("@ss.canAccessMembership(#membershipId) or hasRole('ADMIN')")
    public InstrumentResponse assignInstrument(
            @Parameter(description = "ID instrumentu") @PathVariable Long instrumentId,
            @Parameter(description = "ID członkostwa członka") @PathVariable Long membershipId
    ) {
        return instrumentService.assignInstrumentToMember(instrumentId, membershipId);
    }

    @Operation(summary = "Odbierz instrument", description = "Usuwa obecne przypisanie instrumentu od członka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrument odebrany pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono instrumentu")
    })
    @PostMapping("/{instrumentId}/unassign")
    @PreAuthorize("@ss.canManageInstrument(#instrumentId) or hasRole('ADMIN')")
    public InstrumentResponse unassignInstrument(
            @Parameter(description = "ID instrumentu") @PathVariable Long instrumentId
    ) {
        return instrumentService.unassignInstrument(instrumentId);
    }

    @Operation(summary = "Usuń instrument", description = "Usuwa instrument z systemu na podstawie jego ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrument usunięty pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono instrumentu")
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.canManageInstrument(#instrumentId) or hasRole('ADMIN')")
    public void deleteInstrument(
            @Parameter(description = "ID instrumentu") @PathVariable Long id
    ) {
        instrumentService.deleteInstrument(id);
    }

}