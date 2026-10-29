package we.rap.backend.services;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import we.rap.backend.enums.TokenType;
import we.rap.backend.models.User;
import we.rap.backend.models.UserTokens;
import we.rap.backend.repositories.TokenUserServiceRepository;
import we.rap.backend.shared.utils.HashUtils;
import we.rap.backend.shared.utils.TokenGenerator;

@Service 
public class TokenUserService {

    private static final Duration RESEND_COOLDOWN = Duration.ofMinutes(5);

    @Autowired
    TokenUserServiceRepository tokenUserServiceRepository;

    
    @Autowired 
    TokenGenerator tokenGenerator;
    
    boolean sentRecently(User user, TokenType type) {
        Instant limit = Instant.now().minus(RESEND_COOLDOWN);
        
        return tokenUserServiceRepository.getValidTokens(user.getId(), type)
                    .stream()
                    .anyMatch(t -> t.getCreatedAt().isAfter(limit));
    }


    @Transactional
    String createToken(User user, TokenType type) {
        tokenUserServiceRepository.getValidTokens(user.getId(), type)
                .forEach(t -> t.setRevoked(true));

        String token = tokenGenerator.generate();
        
        var newToken = UserTokens.builder()
                .tokenType(type)
                .hashToken(HashUtils.hash(token))
                .expiredAt(Instant.now().plus(15,ChronoUnit.MINUTES))
                .user(user)
                .build();
                
        tokenUserServiceRepository.save(newToken);
        return token;
    }


}
