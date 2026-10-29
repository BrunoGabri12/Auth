package we.rap.backend.controllers;

import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import jakarta.validation.Valid;
import we.rap.backend.dto.MessageResponseDto;
import we.rap.backend.dto.UserRequestDto;
import we.rap.backend.dto.UserResponseDto;
import we.rap.backend.models.User;
import we.rap.backend.services.UserService;

@RestController
@RequestMapping("/user")
public class UserController {

    @Autowired
    UserService userService;

    @GetMapping("/all")
    public List<UserResponseDto> getAllUsers() {
        return userService.findAllUsers();
    }

    @PostMapping("register")
    public ResponseEntity<MessageResponseDto> newUser(@RequestBody @Valid UserRequestDto newUser){
        userService.createNewUser(newUser);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new MessageResponseDto("Cadastro realizado! Enviamos um e-mail para confirmar sua conta."));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteUser(@PathVariable UUID id) {
        userService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("ping")    
    public String ping() {
        return "Pong";
    }

    @GetMapping("me")
    public UserResponseDto userDetails(@AuthenticationPrincipal User user){
        return userService.getUser(user.getId());
    }

}
