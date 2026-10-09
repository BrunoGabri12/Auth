package we.rap.backend.config;

import java.time.LocalDateTime;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import we.rap.backend.enums.Roles;
import we.rap.backend.models.User;
import we.rap.backend.repositories.UserRepository;

@Component 
public class AdminSeeder implements ApplicationRunner {
    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Value("${app.admin.email:}")
    String adminEmail;

    @Value("${app.admin.username:admin}")
    String adminUsername;

    @Value("${app.admin.password:}")
    String adminPassword;

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (adminEmail.isBlank() || adminPassword.isBlank()) {
            log.warn("ADMIN_EMAIL/ADMIN_PASSWORD not defined");
            return;
        }

        String email = adminEmail.trim().toLowerCase();

        if (userRepository.existsByEmail(email)) {
            return;
        }

        User admin = User.builder()
            .email(email)
            .username(adminUsername)
            .emailVerified(true)
            .isActive(true)
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .passwordHash(passwordEncoder.encode(adminPassword))
            .role(Roles.ADMIN)
            .build();

        userRepository.save(admin);
        log.info("Admin user created: {}", email);
    }
}
