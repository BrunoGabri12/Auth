package we.rap.backend.services;

import java.time.Duration;
import java.time.Instant;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTCreator;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTCreationException;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;

import we.rap.backend.dto.AccessTokenClaims;
import we.rap.backend.exceptions.auth.InvalidAccessTokenException;
import we.rap.backend.exceptions.auth.InvalidRefreshTokenException;
import we.rap.backend.models.User;

@Service
public class TokenService {

    // Id da sessão (tabela auth) à qual o access token pertence
    private static final String SESSION_CLAIM = "sid";

    @Value("${api.security.token.secret}")
    private String jwtSecret;

    @Value("${api.security.refresh.token.secret}")
    private String jwtRefreshSecret;

    @Value("${api.security.token.expiration}")
    private Duration tokenExpiration;

    @Value("${api.security.refresh.token.expiration}")
    private Duration refreshTokenExpiration;

    public String generateAccessToken(User user, UUID sessionId) {
        return sign(baseToken(user, tokenExpiration).withClaim(SESSION_CLAIM, sessionId.toString()), jwtSecret);
    }

    public String generateRefreshToken(User user) {
        return sign(baseToken(user, refreshTokenExpiration), jwtRefreshSecret);
    }

    public UUID validateRefreshToken(String refreshToken) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtRefreshSecret))
                    .build()
                    .verify(refreshToken);
            return UUID.fromString(jwt.getSubject());
        } catch (JWTVerificationException | IllegalArgumentException exception) {
            throw new InvalidRefreshTokenException();
        }
    }

    public AccessTokenClaims validateAccessToken(String accessToken) {
        try {
            DecodedJWT jwt = JWT.require(Algorithm.HMAC256(jwtSecret))
                    .withClaimPresence(SESSION_CLAIM)
                    .build()
                    .verify(accessToken);
            return new AccessTokenClaims(
                UUID.fromString(jwt.getSubject()),
                UUID.fromString(jwt.getClaim(SESSION_CLAIM).asString())
            );
        } catch (JWTVerificationException | IllegalArgumentException | NullPointerException exception) {
            throw new InvalidAccessTokenException();
        }
    }

    public Instant getExpiration(String token) {
        return JWT.decode(token).getExpiresAtAsInstant();
    }

    private JWTCreator.Builder baseToken(User user, Duration expiration) {
        return JWT.create()
                .withSubject(user.getId().toString())
                .withJWTId(UUID.randomUUID().toString())
                .withExpiresAt(Instant.now().plus(expiration));
    }

    private String sign(JWTCreator.Builder token, String secret) {
        try {
            return token.sign(Algorithm.HMAC256(secret));
        } catch (JWTCreationException exception) {
            throw new IllegalStateException("Erro ao gerar token", exception);
        }
    }
}
