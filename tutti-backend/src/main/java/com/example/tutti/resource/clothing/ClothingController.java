package com.example.tutti.resource.clothing;

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
@RequestMapping("/clothes")
@RequiredArgsConstructor
@Tag(name = "Clothes", description = "Zarządzanie ubiorem orkiestrantów")
public class ClothingController {

    private final ClothingService clothingService;

    @Operation(summary = "Pobierz ubrania orkiestry", description = "Zwraca listę ubrań dla orkiestry z możliwością filtrowania po typie, rozmiarze i statusie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista ubrań pobrana pomyślnie")
    })
    @GetMapping("/orchestra/{orchestraId}")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public List<ClothingResponse> getClothes(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId,
            @Parameter(description = "Opcjonalny filtr po typie ubrania") @RequestParam(required = false) ClothingType type,
            @Parameter(description = "Opcjonalny filtr po rozmiarze ubrania") @RequestParam(required = false) ClothingSize size,
            @Parameter(description = "Opcjonalny filtr po statusie ubrania") @RequestParam(required = false) ClothingStatus status
    ) {
        return clothingService.getClothes(orchestraId, type, size, status);
    }

    @Operation(summary = "Pobierz ubrania po członkostwie", description = "Zwraca listę ubrań dla danego członkostwa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista ubrań pobrana pomyślnie")
    })
    @GetMapping("/membership/{membershipId}")
    @PreAuthorize("@ss.canAccessMembership(#membershipId) or hasRole('ADMIN')")
    public List<ClothingResponse> getClothes(
            @Parameter(description = "ID członkowstwa") @PathVariable Long membershipId
    ) {
        return clothingService.getClothesByMembership(membershipId);
    }

    @Operation(summary = "Dodaj nowe ubranie", description = "Tworzy nowe ubranie dla danej orkiestry.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubranie dodane pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono orkiestry")
    })
    @PostMapping("/orchestra/{orchestraId}")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public ClothingResponse addClothing(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId,
            @Parameter(description = "Dane ubrania do utworzenia") @RequestBody ClothingRequest request
    ) {
        return clothingService.addClothing(orchestraId, request);
    }

    @Operation(summary = "Przypisz ubranie do członka", description = "Przypisuje ubranie do członka orkiestry na podstawie ID członkostwa.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubranie przypisane pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ubrania lub członkostwa")
    })
    @PostMapping("/{clothingId}/assign/{membershipId}")
    @PreAuthorize("@ss.canAccessMembership(#membershipId) or hasRole('ADMIN')")
    public ClothingResponse assignClothing(
            @Parameter(description = "ID ubrania") @PathVariable Long clothingId,
            @Parameter(description = "ID członkostwa członka") @PathVariable Long membershipId
    ) {
        return clothingService.assignClothingToMember(clothingId, membershipId);
    }

    @Operation(summary = "Odbierz ubranie od członka", description = "Odbiera ubranie od członka i ustawia je jako dostępne.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubranie odebrane pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ubrania")
    })
    @PostMapping("/{clothingId}/unassign")
    @PreAuthorize("@ss.canManageClothing(#clothingId) or hasRole('ADMIN')")
    public ClothingResponse unassignClothing(
            @Parameter(description = "ID ubrania") @PathVariable Long clothingId
    ) {
        return clothingService.unassignClothingFromMember(clothingId);
    }

    @Operation(summary = "Usuń ubranie", description = "Usuwa ubranie, jeśli jest dostępne i nie jest przypisane do członka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Ubranie usunięte pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono ubrania")
    })
    @DeleteMapping("/{clothingId}")
    @PreAuthorize("@ss.canManageClothing(#clothingId) or hasRole('ADMIN')")
    public void removeClothing(
            @Parameter(description = "ID ubrania") @PathVariable Long clothingId
    ) {
        clothingService.removeClothing(clothingId);
    }
}
