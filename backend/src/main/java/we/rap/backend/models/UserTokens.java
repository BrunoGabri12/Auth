package we.rap.backend.models;


import java.time.Instant;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import we.rap.backend.enums.TokenType;

@Builder
@Entity 
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name="user_tokens")
public class UserTokens {
    
    @Id
    @GeneratedValue(strategy= GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name= "token_type", length=20, nullable=false)
    private TokenType tokenType; 

    @Column(name="hash_token", length=64, nullable=false)
    private String hashToken;

    @Builder.Default
    @Column(name="revoked")
    private boolean revoked = false; 

    @Column(name="expired_at", nullable = false)
    private Instant expiredAt;

    @CreationTimestamp
    @Column(name="created_at", nullable = false, updatable = false)
    private Instant createdAt;


    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="user_id", nullable = false)
    private User user;

}
