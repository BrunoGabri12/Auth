package we.rap.backend.repositories;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import we.rap.backend.enums.TokenType;
import we.rap.backend.models.UserTokens;

public interface TokenUserServiceRepository extends JpaRepository<UserTokens, Long> {
    
    Optional<UserTokens> getTokenByHashToken(String hashToken);

    List<UserTokens> getTokensByUserIdAndTokenTypeAndRevokedFalse(UUID userId, TokenType tokenType);

    @Modifying
    @Query("""
        DELETE 
        FROM UserTokens t 
        WHERE t.revoked = true 
            AND t.expiredAt < CURRENT_TIMESTAMP
        """)
    int deleteUsedTokens();

    @Query("""
        SELECT t
        FROM UserTokens t 
        WHERE t.user.id = :userId 
            AND t.revoked = false 
            AND t.expiredAt > CURRENT_TIMESTAMP
            AND t.tokenType = :tokenType
        """)
    List<UserTokens> getValidTokens(
        @Param("userId") UUID userId,
        @Param("tokenType") TokenType tokenType);

}
