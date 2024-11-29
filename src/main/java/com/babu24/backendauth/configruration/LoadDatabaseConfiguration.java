package com.babu24.backendauth.configruration;

import com.babu24.backendauth.features.authentication.model.AuthenticationUser;
import com.babu24.backendauth.features.authentication.repository.AuthenticationUserRepo;
import com.babu24.backendauth.features.authentication.utils.Encode;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LoadDatabaseConfiguration {

    private final Encode encoder;

    public LoadDatabaseConfiguration(Encode encoder) {
        this.encoder = encoder;
    }

    @Bean
    public CommandLineRunner initDatabase(AuthenticationUserRepo authenticationUserRepo) {
        return args -> {
            AuthenticationUser authenticationUser = new AuthenticationUser("babu@gmail.com", encoder.encode("root"));
           // AuthenticationUser authenticationUser2= new AuthenticationUser("khageswarnayak.com","admin");
            authenticationUserRepo.save(authenticationUser);
          //  authenticationUserRepo.save(authenticationUser2);
        };
    }
}
