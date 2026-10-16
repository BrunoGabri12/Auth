package we.rap.backend.controllers;

import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import we.rap.backend.dto.AuthRequestDto;
import we.rap.backend.dto.AuthResponseDto;
import we.rap.backend.dto.GoogleLoginRequestDto;
import we.rap.backend.dto.RefreshRequestDto;
import we.rap.backend.services.AuthService;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    AuthService authService;

    @PostMapping("login")
    public AuthResponseDto login(@RequestBody @Valid AuthRequestDto login) {
        return authService.login(login);
    }

    @PostMapping("logout")
    public ResponseEntity<Void> logout(@RequestBody @Valid RefreshRequestDto request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.noContent().build();
    }

    @PostMapping("login/google")
    public AuthResponseDto loginWithGoogle(@RequestBody @Valid GoogleLoginRequestDto request) {
        return authService.loginWithGoogle(request.idToken());
    }

    @PostMapping("refresh")
    public AuthResponseDto refresh(@RequestBody @Valid RefreshRequestDto request) {
        return authService.refresh(request.refreshToken());
    }

    @PostMapping("revoke_login")
    public ResponseEntity<Void> revokeLogin(@RequestBody @Valid RefreshRequestDto request) {
        authService.logoutAll(request.refreshToken());
        return ResponseEntity.noContent().build();
    }
 
    //ADMIN
    @PostMapping("revoke_user_session/{userId}")
    public ResponseEntity<Void> revokeAll(@PathVariable UUID userId) {
        authService.revokeAllByUserId(userId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("revoke_all")
    public ResponseEntity<Void> revoke_all() {
        authService.revokeAllSessions();
        return ResponseEntity.noContent().build();
    }
    
}
