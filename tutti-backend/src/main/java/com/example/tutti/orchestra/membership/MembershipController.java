package com.example.tutti.orchestra.membership;

import com.example.tutti.resource.instrument.InstrumentType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/memberships")
@RequiredArgsConstructor
@Tag(name = "Memberships", description = "Zarządzanie członkostwami w orkiestrach")
public class MembershipController {

    private final MembershipService membershipService;

    @Operation(summary = "Dodaj członkostwo do orkiestry", description = "Tworzy nowe członkostwo dla użytkownika w danej orkiestrze (status PENDING).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Członkostwo dodane pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono użytkownika lub orkiestry")
    })
    @PostMapping
    public MembershipResponse addMembership(
            @Parameter(description = "Dane nowego członkostwa") @RequestBody MembershipRequest request
    ) {
        return membershipService.addMembership(request);
    }

    @Operation(summary = "Zatwierdź członkostwo", description = "Zmienia status członkostwa na ACTIVE i ustawia datę dołączenia.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Członkostwo zatwierdzone pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono członkostwa")
    })
    @PostMapping("/{membershipId}/approve")
    @PreAuthorize("@ss.canManageMembership(#membershipId) or hasRole('ADMIN')")
    public MembershipResponse approveMembership(
            @Parameter(description = "ID członkostwa") @PathVariable Long membershipId
    ) {
        return membershipService.approveMembership(membershipId);
    }

    @Operation(summary = "Odrzuć członkostwo", description = "Zmienia status członkostwa na REJECTED.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Członkostwo odrzucone pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono członkostwa")
    })
    @PostMapping("/{membershipId}/reject")
    @PreAuthorize("@ss.canManageMembership(#membershipId) or hasRole('ADMIN')")
    public MembershipResponse rejectMembership(
            @Parameter(description = "ID członkostwa") @PathVariable Long membershipId
    ) {
        return membershipService.rejectMembership(membershipId);
    }

    @Operation(summary = "Usuń członkostwo", description = "Zmienia status członkostwa na LEFT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Członkostwo zakończone pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono członkostwa")
    })
    @DeleteMapping("/{membershipId}")
    @PreAuthorize("@ss.canAccessMembership(#membershipId) or hasRole('ADMIN')")
    public void removeMembership(
            @Parameter(description = "ID członkostwa") @PathVariable Long membershipId
    ) {
        membershipService.removeMembership(membershipId);
    }

    @Operation(summary = "Użytkownik opuszcza orkiestrę", description = "Zmienia status członkostwa użytkownika na LEFT.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Członek opuszcza orkiestrę"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono członkostwa")
    })
    @PostMapping("/orchestra/{orchestraId}/user/{userId}/leave")
    @PreAuthorize("@ss.canAccessMembership(#membershipId) or hasRole('ADMIN')")
    public void leaveOrchestra(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId,
            @Parameter(description = "ID użytkownika") @PathVariable Long userId
    ) {
        membershipService.leaveOrchestra(orchestraId, userId);
    }

    @Operation(summary = "Zmień rolę członka orkiestry", description = "Aktualizuje rolę członka w orkiestrze.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Rola zmieniona pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono członkostwa")
    })
    @PutMapping("{id}/change-role")
    @PreAuthorize("@ss.canAccessMembership(#id) or hasRole('ADMIN')")
    public MembershipResponse changeRole(
            @Parameter(description = "ID członkostwa") @PathVariable Long id,
            @Parameter(description = "Dane członkostwa do aktualizacji") @RequestBody MembershipRequest request
    ) {
        return membershipService.changeRole(id, request);
    }

    @Operation(summary = "Zmień instrument członka orkiestry", description = "Aktualizuje instrument i numer partii członka.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Instrument zmieniony pomyślnie"),
            @ApiResponse(responseCode = "404", description = "Nie znaleziono członkostwa")
    })
    @PutMapping("/{id}/change-instrument")
    @PreAuthorize("@ss.canAccessMembership(#id) or hasRole('ADMIN')")
    public MembershipResponse changeInstrument(
            @PathVariable Long id,
            @RequestBody MembershipRequest request
    ) {
        // Przekazujemy ID i dane osobno do serwisu
        return membershipService.changeInstrument(id, request);
    }

    @Operation(summary = "Pobierz aktywnych członków orkiestry", description = "Zwraca listę członków o statusie ACTIVE.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista aktywnych członków pobrana pomyślnie")
    })
    @GetMapping("/orchestra/{orchestraId}/active")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public List<MembershipResponse> getActiveMembersForOrchestra(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId
    ) {
        return membershipService.getActiveMembersForOrchestra(orchestraId);
    }

    @Operation(summary = "Pobierz członków według instrumentu", description = "Zwraca listę aktywnych członków grających na określonym instrumencie.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista członków pobrana pomyślnie")
    })
    @GetMapping("/orchestra/{orchestraId}/instrument/{instrumentType}")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public List<MembershipResponse> getMembersByInstrument(
            @Parameter(description = "ID orkiestry") @PathVariable Long orchestraId,
            @Parameter(description = "Typ instrumentu") @PathVariable InstrumentType instrumentType
    ) {
        return membershipService.getMembersByInstrument(orchestraId, instrumentType);
    }

    @Operation(
            summary = "Pobierz członków według statusu",
            description = "Zwraca listę członków orkiestry o określonym statusie (PENDING, ACTIVE, REJECTED, LEFT)."
    )
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lista członków pobrana pomyślnie")
    })
    @GetMapping("/orchestra/{orchestraId}/status/{status}")
    @PreAuthorize("@ss.isManagement(#orchestraId) or hasRole('ADMIN')")
    public List<MembershipResponse> getMembersByStatus(
            @PathVariable Long orchestraId,
            @PathVariable MembershipStatus status) {
        return membershipService.getMembersByStatus(orchestraId, status);
    }

    @Operation(summary = "Pobierz członkostwa zalogowanego użytkownika", description = "Zwraca listę wszystkich orkiestr, do których należy użytkownik.")
    @GetMapping("/my-memberships")
    public List<MembershipResponse> getMyMemberships() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        return membershipService.getUserMemberships(currentUserName);
    }

    @Operation(summary = "Pobierz członkostwa zalogowanego użytkownika", description = "Zwraca listę wszystkich orkiestr, do których należy użytkownik.")
    @GetMapping("/my-memberships/active")
    public List<MembershipResponse> getMyActiveMemberships() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String currentUserName = authentication.getName();
        return membershipService.getUserActiveMemberships(currentUserName);
    }

    @GetMapping("/user/{userId}/active-memberships")
    @Operation(summary = "Pobierz orkiestry, do których użytkownik należy (jako członek)")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MembershipResponse>> getUserMemberships(@PathVariable Long userId) {
        return ResponseEntity.ok(membershipService.findAllByUserId(userId));
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pobierz wszystkie członkostwa")
    public List<MembershipResponse> getAllMemberships() {
        return membershipService.findAll();
    }

    @GetMapping("/active")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Pobierz wszystkie członkostwa")
    public List<MembershipResponse> getAllActiveMemberships() {
        return membershipService.findAllActive();
    }
}