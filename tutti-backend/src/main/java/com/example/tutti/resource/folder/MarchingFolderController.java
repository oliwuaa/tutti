package com.example.tutti.resource.folder;

import com.example.tutti.resource.instrument.InstrumentType;
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
@RequestMapping("/marching-folders")
@RequiredArgsConstructor
@Tag(name = "Marching Folders", description = "Zarządzanie książkami z nutami")
public class MarchingFolderController {

    private final MarchingFolderService folderService;

    @Operation(
            summary = "Statystyki książek nut",
            description = "Zwraca statystyki książek nut dla orkiestry. " +
                    "Można filtrować po typie marszu, typie instrumentu i części. Wszystkie filtry są opcjonalne."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Statystyki pobrane pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @GetMapping("/orchestra/{orchestraId}/stats")
    @PreAuthorize("@ss.canManageScores(#orchestraId) or hasRole('ADMIN')")
    public List<MarchingFolderResponse> getStats(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId,
            @Parameter(description = "Opcjonalny filtr po typie marszu") @RequestParam(required = false) MarchingType marchingType,
            @Parameter(description = "Opcjonalny filtr po typie instrumentu") @RequestParam(required = false) InstrumentType instrumentType,
            @Parameter(description = "Opcjonalny filtr po części") @RequestParam(required = false) Integer part
    ) {
        return folderService.getStats(orchestraId, marchingType, instrumentType, part);
    }

    @Operation(summary = "Dodaj nową książkę nut", description = "Tworzy nową książkę nut (MarchingFolder) w danej orkiestrze.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Książka nut została dodana pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @PostMapping("/orchestra/{orchestraId}")
    @PreAuthorize("@ss.canManageScores(#orchestraId) or hasRole('ADMIN')")
    public MarchingFolderResponse addFolder(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId,
            @Parameter(description = "Obiekt książki nut do utworzenia") @RequestBody MarchingFolderRequest folder
    ) {
        return folderService.addFolder(orchestraId, folder);
    }

    @Operation(summary = "Usuń książkę nut", description = "Usuwa książkę nut z systemu na podstawie jej ID.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Książka nut została usunięta pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono książki nut")
    })
    @DeleteMapping("/{folderId}")
    @PreAuthorize("@ss.canManageFolder(#folderId) or hasRole('ADMIN')")
    public void deleteFolder(
            @Parameter(description = "ID książki nut") @PathVariable Long folderId
    ) {
        folderService.deleteFolder(folderId);
    }

    @Operation(summary = "Zwiększ liczbę książek nut", description = "Zwiększa ilość książek nut o 1.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liczba książek nut została zwiększona pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono książki nut")
    })
    @PatchMapping("/{folderId}/increment")
    @PreAuthorize("@ss.canManageFolder(#folderId) or hasRole('ADMIN')")
    public void incrementQuantity(
            @PathVariable Long folderId
    ) {
        folderService.incrementQuantity(folderId);
    }

    @Operation(summary = "Zmniejsz liczbę książek nut", description = "Zmniejsza ilość książek nut o 1.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Liczba książek nut została zmniejszona pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie udało się zmiejszyć liczby książek nut")
    })
    @PatchMapping("/{folderId}/decrement")
    @PreAuthorize("@ss.canManageFolder(#folderId) or hasRole('ADMIN')")
    public void decrementQuantity(
            @PathVariable Long folderId
    ) {
        folderService.decrementQuantity(folderId);
    }

}
