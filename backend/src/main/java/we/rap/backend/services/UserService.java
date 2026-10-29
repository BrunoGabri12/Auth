package we.rap.backend.services;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import we.rap.backend.dto.UserRequestDto;
import we.rap.backend.dto.UserResponseDto;
import we.rap.backend.enums.Roles;
import we.rap.backend.enums.TokenType;
import we.rap.backend.exceptions.user.EmailAlreadyInUseException;
import we.rap.backend.exceptions.user.UserNotFoundException;
import we.rap.backend.exceptions.user.UserPermissionException;
import we.rap.backend.models.User;
import we.rap.backend.repositories.UserRepository;

@Service
public class UserService {

    private final MailService mailService;

    @Autowired
    UserRepository userRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Autowired
    AuthService authService;

    @Autowired 
    TokenUserService tokenUserService;

    UserService(MailService mailService) {
        this.mailService = mailService;
    }

    @Transactional
    public void createNewUser(UserRequestDto request) {
        String email = request.email().trim().toLowerCase();

        Optional<User> availableUser = userRepository.findByEmail(email);

        if(availableUser.isPresent()) {
            User existingUser = availableUser.get();

            if (existingUser.getGoogleId() != null) {
                throw new EmailAlreadyInUseException(email);
            }

            boolean hasTokensAvailabels = tokenUserService.sentRecently(existingUser, TokenType.VALIDATE_USER);

            if(hasTokensAvailabels) return;

            mailService.validateEmail(existingUser);

            return;
        }


        User user = User.builder()
                .email(email)
                .username(request.username())
                .emailVerified(false)
                .isActive(true)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(Roles.USER)
                .build();

        User saved = userRepository.save(user);

        mailService.validateEmail(saved);
    }


    @Transactional(readOnly = true)
    public List<UserResponseDto> findAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toResponseDto)
                .toList();
    }

    @Transactional 
    public User getUserById(UUID userId) {
        return userRepository.findById(userId)
                .filter(User::isActive)
                .orElseThrow(() -> new UserNotFoundException(userId));
    }

    @Transactional 
    public UserResponseDto getUser(UUID userUuid) {
        return toResponseDto(getUserById(userUuid));
    }

    @Transactional
    public void deleteUser(UUID userId) {
        User user = getUserById(userId);

        if(user.getRole() == Roles.ADMIN) {
            throw new UserPermissionException(user.getId());
        }

        user.setActive(false);
        authService.revokeAllByUserId(user.getId());
    }


    private UserResponseDto toResponseDto(User user) {
        return new UserResponseDto(
                user.getId(),
                user.getEmail(),
                user.getUsername(),
                user.isEmailVerified(),
                user.getCreatedAt(),
                user.getRole()
        );}

}
