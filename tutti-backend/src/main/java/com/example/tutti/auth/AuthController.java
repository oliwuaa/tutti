package com.example.tutti.auth;

import com.example.tutti.config.JwtService;
import com.example.tutti.user.User;
import com.example.tutti.user.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpointy do logowania, odświeżania tokenów i wylogowywania")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Operation(summary = "Logowanie użytkownika", description = "Zwraca Access Token (JWT) oraz Refresh Token")
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );

        var user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("Nie znaleziono użytkownika"));

        var jwtToken = jwtService.generateToken(user);
        var refreshToken = refreshTokenService.createRefreshToken(user.getEmail());

        return ResponseEntity.ok(AuthResponse.builder()
                .accessToken(jwtToken)
                .refreshToken(refreshToken.getToken())
                .email(user.getEmail())
                .role(user.getRole().name())
                .build());
    }

    @Operation(summary = "Odświeżenie sesji", description = "Użyj Refresh Tokena, aby uzyskać nowy Access Token")
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@RequestBody RefreshTokenRequest request) {
        return refreshTokenRepository.findByToken(request.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = jwtService.generateToken(user);
                    return ResponseEntity.ok(AuthResponse.builder()
                            .accessToken(accessToken)
                            .refreshToken(request.getToken())
                            .email(user.getEmail())
                            .role(user.getRole().name())
                            .build());
                })
                .orElseThrow(() -> new RuntimeException("Refresh token nie istnieje w bazie danych!"));
    }

    @Operation(summary = "Wylogowanie", description = "Unieważnia Refresh Token w bazie danych. Wymaga nagłówka Authorization.")
    @PostMapping("/logout")
    public ResponseEntity<?> logout() {
        var authentication = org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();

        refreshTokenService.deleteByUserId(email);

        return ResponseEntity.ok("Wylogowano pomyślnie i unieważniono sesję.");
    }
}