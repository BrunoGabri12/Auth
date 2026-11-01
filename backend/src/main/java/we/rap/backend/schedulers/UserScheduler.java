package we.rap.backend.schedulers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import jakarta.transaction.Transactional;
import we.rap.backend.repositories.AuthRepository;
import we.rap.backend.repositories.TokenUserServiceRepository;

@Component 
public class UserScheduler {
    
    @Autowired 
    TokenUserServiceRepository tokenUserServiceRepository;


    @Autowired 
    AuthRepository authRepository;


    @Scheduled(cron = "0 0 0 * * *", zone = "America/Sao_Paulo")
    @Transactional
    public void deleteTokens(){
        System.out.println("Executando scheduler");
        tokenUserServiceRepository.deleteUsedTokens();
        authRepository.deleteUsedTokens();

    }

}
