package we.rap.backend.services;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken.Payload;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

import we.rap.backend.dto.AccessTokenClaims;
import we.rap.backend.dto.AuthRequestDto;
import we.rap.backend.dto.AuthResponseDto;
import we.rap.backend.dto.NewPassword;
import we.rap.backend.dto.ValidateToken;
import we.rap.backend.enums.Roles;
import we.rap.backend.enums.TokenType;
import we.rap.backend.exceptions.auth.InvalidAccessTokenException;
import we.rap.backend.exceptions.auth.InvalidCredentialsException;
import we.rap.backend.exceptions.auth.InvalidEmailValidationException;
import we.rap.backend.exceptions.auth.InvalidRefreshTokenException;
import we.rap.backend.exceptions.auth.InvalidResetPasswordException;
import we.rap.backend.exceptions.auth.InvalidUserTokenException;
import we.rap.backend.exceptions.user.EmailAlreadyInUseException;
import we.rap.backend.models.Auth;
import we.rap.backend.models.User;
import we.rap.backend.models.UserTokens;
import we.rap.backend.repositories.AuthRepository;
import we.rap.backend.repositories.TokenUserServiceRepository;
import we.rap.backend.repositories.UserRepository;
import we.rap.backend.shared.utils.HashUtils;

@Service
public class AuthService {

    private final MailService mailService;

    private final GoogleIdTokenVerifier googleIdTokenVerifier;

    @Autowired
    UserRepository userRepository;

    @Autowired
    AuthRepository authRepository;

    @Autowired
    TokenService tokenService;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired 
    TokenUserServiceRepository tokenUserServiceRepository;


    AuthService(GoogleIdTokenVerifier googleIdTokenVerifier, MailService mailService) {
        this.googleIdTokenVerifier = googleIdTokenVerifier;
        this.mailService = mailService;
    }

    @Transactional(noRollbackFor = InvalidEmailValidationException.class)
    public AuthResponseDto login(AuthRequestDto request) {
        String email = request.email().trim().toLowerCase();

        User user = userRepository.findByEmail(email)
                .filter(u -> u.isActive())
                .filter(u -> u.getPasswordHash() != null
                        && passwordEncoder.matches(request.password(), u.getPasswordHash()))
                .orElseThrow(InvalidCredentialsException::new);

        if(!user.isEmailVerified()) {
            mailService.validateEmail(user);
            throw new InvalidEmailValidationException();
        }

        user.setLastLoginAt(LocalDateTime.now());

        return issueTokens(user);
    }

    @Transactional
    public AuthResponseDto loginWithGoogle(String idTokenString) {
        Payload payload = verifyGoogleIdToken(idTokenString);

        String googleId = payload.getSubject();
        String email = payload.getEmail() == null ? null : payload.getEmail().toLowerCase().trim();
        boolean emailVerified = Boolean.valueOf(payload.getEmailVerified());

        User user = userRepository.findByGoogleId(googleId)
                .orElseGet(() -> createGoogleUser(googleId, email, emailVerified, payload));

        if (!user.isActive()) {
            throw new InvalidCredentialsException();
        }

        user.setLastLoginAt(LocalDateTime.now());

        return issueTokens(user);
    }


    @Transactional
    public void validateUserEmail(ValidateToken request) {
        UserTokens issued = tokenUserServiceRepository.getTokenByHashToken(HashUtils.hash(request.token()))
                .filter(t -> t.getTokenType() == TokenType.VALIDATE_USER)
                .filter(t -> !t.isRevoked())
                .filter(t -> t.getExpiredAt().isAfter(Instant.now()))
                .orElseThrow(InvalidUserTokenException::new);

        issued.setRevoked(true);
        issued.getUser().setEmailVerified(true);
    }

    private Payload verifyGoogleIdToken(String idTokenString) {
        try {
            GoogleIdToken idToken = googleIdTokenVerifier.verify(idTokenString);
            if (idToken == null) {
                throw new InvalidCredentialsException();
            }
            return idToken.getPayload();
        } catch (GeneralSecurityException | IOException | IllegalArgumentException exception) {
            throw new InvalidCredentialsException();
        }
    }

    private User createGoogleUser(String googleId, String email, boolean emailVerified, Payload payload) {
        if (email == null || !emailVerified) {
            throw new InvalidCredentialsException();
        }

        if (userRepository.existsByEmail(email)) {
            throw new EmailAlreadyInUseException(email);
        }

        return userRepository.save(User.builder()
                .email(email)
                .username(resolveUsername(payload, email))
                .googleId(googleId)
                .emailVerified(true)
                .isActive(true)
                .role(Roles.USER)
                .build());
    }

    private String resolveUsername(Payload payload, String email) {
        Object name = payload.get("name");
        if (name instanceof String value && !value.isBlank()) {
            return value;
        }
        return email.substring(0, email.indexOf('@'));
    }

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
        authRepository.revokeAllExceptRole(Roles.ADMIN);
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

    @Transactional()
    public void resetPassword(NewPassword request) {

       UserTokens issued = tokenUserServiceRepository.getTokenByHashToken(HashUtils.hash(request.token()))
            .filter(t -> t.getTokenType() == TokenType.FORGOT_PASSWORD)
            .filter(t -> !t.isRevoked())
            .filter(t -> t.getExpiredAt().isAfter(Instant.now()))
            .filter(t -> t.getUser().isActive())
            .orElseThrow(InvalidResetPasswordException::new);

    issued.setRevoked(true);
    User user = issued.getUser();
    user.setPasswordHash(passwordEncoder.encode(request.password()));
    revokeAllByUserId(user.getId());
        
    }

    private AuthResponseDto issueTokens(User user) {
        String refreshToken = tokenService.generateRefreshToken(user);

        Auth session = new Auth();
        session.setUser(user);
        session.setTokenHash(HashUtils.hash(refreshToken));
        session.setExpiresAt(toLocalDateTime(tokenService.getExpiration(refreshToken)));
        authRepository.save(session);

        // Gerado após o save: o access token carrega o id da sessão
        String accessToken = tokenService.generateAccessToken(user, session.getId());

        return new AuthResponseDto(
                accessToken,
                refreshToken,
                secondsUntil(tokenService.getExpiration(accessToken)));
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

        return authRepository.findByTokenHash(HashUtils.hash(refreshToken))
                .filter(session -> session.getUser().getId().equals(userId))
                .filter(session -> session.getUser().isActive())
                .orElseThrow(InvalidRefreshTokenException::new);
    }

    private void ensureNotExpired(Auth session) {
        if (!session.getExpiresAt().isAfter(LocalDateTime.now())) {
            throw new InvalidRefreshTokenException();
        }
    }


    private long secondsUntil(Instant instant) {
        return Math.max(0, Duration.between(Instant.now(), instant).getSeconds());
    }

    private LocalDateTime toLocalDateTime(Instant instant) {
        return LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }
}
