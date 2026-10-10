package we.rap.backend.services;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HexFormat;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import we.rap.backend.dto.AccessTokenClaims;
import we.rap.backend.dto.AuthRequestDto;
import we.rap.backend.dto.AuthResponseDto;
import we.rap.backend.enums.Roles;
import we.rap.backend.exceptions.auth.InvalidAccessTokenException;
import we.rap.backend.exceptions.auth.InvalidCredentialsException;
import we.rap.backend.exceptions.auth.InvalidRefreshTokenException;
import we.rap.backend.models.Auth;
import we.rap.backend.models.User;
import we.rap.backend.repositories.AuthRepository;
import we.rap.backend.repositories.UserRepository;

@Service
public class AuthService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthRepository authRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Transactional
    public AuthResponseDto login(AuthRequestDto request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email) //TODO: O repository deve ser exposto somente pela camada de service
                .filter(u -> u.isActive())
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        user.setLastLoginAt(LocalDateTime.now());

        return issueTokens(user);
    }

    // noRollbackFor: a revogação feita ao detectar reuso precisa ser persistida mesmo com a exceção
    @Transactional(noRollbackFor = InvalidRefreshTokenException.class)
    public AuthResponseDto refresh(String refreshToken) {
        Auth session = findSession(refreshToken);

        if (session.isRevoked()) {
            revokeAllByUserId(session.getUser().getId());
            throw new InvalidRefreshTokenException();
        }
        ensureNotExpired(session);
        session.setRevoked(true);

        return issueTokens(session.getUser());
    }

    @Transactional
    public void logout(String refreshToken) {
        findValidSession(refreshToken).setRevoked(true);
    }

    @Transactional
    public void logoutAll(String refreshToken) {
        revokeAllByUserId(findValidSession(refreshToken).getUser().getId());
    }

    @Transactional
    public void revokeAllByUserId(UUID userId) {
        authRepository.findAllByUserId(userId)
                .forEach(session -> session.setRevoked(true));
    }

    @Transactional
    public void revokeAllSessions() {
        authRepository.revokeAllExceptRole();
    }

    @Transactional(readOnly = true)
    public User authenticate(String accessToken) {
        AccessTokenClaims claims = tokenService.validateAccessToken(accessToken);

        return authRepository.findById(claims.sessionId())
                .filter(session -> !session.isRevoked())
                .filter(session -> session.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(Auth::getUser)
                .filter(user -> user.getId().equals(claims.userId()))
                .filter(User::isActive)
                .orElseThrow(InvalidAccessTokenException::new);
    }

    private AuthResponseDto issueTokens(User user) {
        String refreshToken = tokenService.generateRefreshToken(user);

        Auth session = new Auth();
        session.setUser(user);
        session.setTokenHash(hash(refreshToken));
        session.setExpiresAt(toLocalDateTime(tokenService.getExpiration(refreshToken)));
        authRepository.save(session);

        // Gerado após o save: o access token carrega o id da sessão
        String accessToken = tokenService.generateAccessToken(user, session.getId());

        return new AuthResponseDto(
            accessToken,
            refreshToken,
            secondsUntil(tokenService.getExpiration(accessToken))
        );
    }

    private Auth findValidSession(String refreshToken) {
        Auth session = findSession(refreshToken);
        if (session.isRevoked()) {
            throw new InvalidRefreshTokenException();
        }
        ensureNotExpired(session);
        return session;
    }

    private Auth findSession(String refreshToken) {
        UUID userId = tokenService.validateRefreshToken(refreshToken);

        return authRepository.findByTokenHash(hash(refreshToken))
                .filter(session -> session.getUser().getId().equals(userId))
                .filter(session -> session.getUser().isActive())
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private void ensureNotExpired(Auth session) {
        if (!session.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException();
        }
    }

    private String hash(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 not available", exception);
        }
    }

    private long secondsUntil(Instant instant) {
        return Math.max(0, Duration.between(Instant.now(), instant).getSeconds());
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
