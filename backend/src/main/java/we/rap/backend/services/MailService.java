package we.rap.backend.services;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import we.rap.backend.dto.ForgotPassword;
import we.rap.backend.enums.TokenType;
import we.rap.backend.exceptions.mail.ErrorToSendException;
import we.rap.backend.models.User;
import we.rap.backend.repositories.UserRepository;

@Service 
public class MailService {
    
    @Value("${mail.origin}")
    private String origin;

    @Autowired 
    private TokenUserService tokenUserService; 

    @Autowired 
    private UserRepository userRepository;

    @Autowired 
    private JavaMailSender mailSender;

    @Transactional 
    public void sendForgotPassword(ForgotPassword request) {
        String email = request.email().trim().toLowerCase();
        User endUser = userRepository.findByEmail(email)
            .filter(User::isActive)
            .filter(User::isEmailVerified)
            .filter(u -> u.getGoogleId() == null || u.getGoogleId().isEmpty())
            .orElseThrow(ErrorToSendException::new);
            
        if (tokenUserService.sentRecently(endUser, TokenType.FORGOT_PASSWORD)) {
            return;
        }

        String token = tokenUserService.createToken(endUser, TokenType.FORGOT_PASSWORD);
        String resetURL = origin + "/reset_password?token=" + token;
        String text = "Clique no link para resetar sua senha: " + resetURL;

        createMailMessage(email,"Esqueci a senha", text);

    }

    @Transactional 
    public void validateEmail(User user) {
        // O link enviado há pouco continua válido, então não reenvia
        if (tokenUserService.sentRecently(user, TokenType.VALIDATE_USER)) {
            return;
        }

        String token = tokenUserService.createToken(user, TokenType.VALIDATE_USER);
        String validateURL = origin + "/validate_user?token=" + token;
        String text = "Clique no link para validar seu usuário: " + validateURL;

        createMailMessage(user.getEmail(), "Validação do e-mail", text);
    }


    private void createMailMessage(String email, String subject, String text) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject(subject);
        message.setText(text);

        try {
            mailSender.send(message);
        } catch(MailException ex) { 
            throw new ErrorToSendException();
        }
        
    }
}
