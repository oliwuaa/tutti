package com.example.tutti.user;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Zarządzanie użytkownikami")
public class UserController {
    private final UserService userService;

    @Operation(summary = "Pobiera wszystkich użytkowników", description = "Zwraca listę wszystkich użytkowników zarejestrowanych w systemie.")
    @ApiResponse(responseCode = "200", description = "Lista użytkowników została pobrana poprawnie", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public List<UserResponse> getAllUsers() {
        return userService.getAllUsers();
    }

    @Operation(summary = "Pobiera użytkownika po ID", description = "Zwraca dane użytkownika na podstawie jego unikalnego identyfikatora.")
    @ApiResponse(responseCode = "200", description = "Użytkownik został znaleziony", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "404", description = "Użytkownik o podanym ID nie istnieje")
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public UserResponse getUserById(@PathVariable Long id) {
        return userService.getUserById(id);
    }

    @Operation(summary = "Tworzy nowego użytkownika", description = "Dodaje nowego użytkownika do systemu na podstawie przesłanych danych.")
    @ApiResponse(responseCode = "201", description = "Użytkownik został pomyślnie utworzony", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "400", description = "Użytkownik z takim email już istnieje")
    @PostMapping
    public UserResponse createUser(@RequestBody UserRequest request) {
        return userService.createUser(request);
    }


    @Operation(summary = "Aktualizuje istniejącego użytkownika", description = "Aktualizuje dane użytkownika na podstawie ID oraz danych przesłanych w żądaniu.")
    @ApiResponse(responseCode = "200", description = "Użytkownik został zaktualizowany", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "404", description = "Użytkownik o podanym ID nie istnieje")
    @PutMapping("/{id}")
    @PreAuthorize("@ss.isSelf(#id) or hasRole('ADMIN')")
    public UserResponse updateUser(@PathVariable Long id, @RequestBody UserRequest request) {
        return userService.updateUser(id, request);
    }

    @Operation(summary = "Usuwa użytkownika", description = "Usuwa użytkownika z systemu na podstawie jego ID.")
    @ApiResponse(responseCode = "204", description = "Użytkownik został usunięty")
    @ApiResponse(responseCode = "404", description = "Użytkownik o podanym ID nie istnieje")
    @DeleteMapping("/{id}")
    @PreAuthorize("@ss.isSelf(#id) or hasRole('ADMIN')")
    public void deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
    }

    @Operation(summary = "Pobiera dane zalogowanego użytkownika", description = "Zwraca dane użytkownika zalogowanego.")
    @ApiResponse(responseCode = "200", description = "Użytkownik został znaleziony", content = @Content(schema = @Schema(implementation = UserResponse.class)))
    @ApiResponse(responseCode = "404", description = "Użytkownik nie jest zalogowany.")
    @GetMapping("/me")
    public UserResponse getCurrentUser() {
        String email = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication().getName();
        return userService.getUserByEmail(email);
    }

}