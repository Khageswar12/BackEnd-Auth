package com.babu24.backendauth.features.authentication.Service;

import com.babu24.backendauth.features.authentication.dto.AuthenticationRequestBody;
import com.babu24.backendauth.features.authentication.dto.AuthenticationResponceBody;
import com.babu24.backendauth.features.authentication.model.AuthenticationUser;
import com.babu24.backendauth.features.authentication.repository.AuthenticationUserRepo;
import com.babu24.backendauth.features.authentication.utils.EmailService;
import com.babu24.backendauth.features.authentication.utils.Encode;
import com.babu24.backendauth.features.authentication.utils.JsonWebToken;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthenticationService {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationService.class);


    private final JsonWebToken jsonWebToken;
    private final AuthenticationUserRepo authenticationUserRepo;
    private final Encode encode;
    private final EmailService emailService;
    private final int durationInMinutes=1;

    public AuthenticationService(JsonWebToken jsonWebToken, AuthenticationUserRepo authenticationUserRepo, Encode encode, EmailService emailService) {
        this.jsonWebToken = jsonWebToken;
        this.authenticationUserRepo = authenticationUserRepo;
        this.encode = encode;
        this.emailService = emailService;
    }

    public static String generateEmailVerificationToken(){
        SecureRandom random=new SecureRandom();
        StringBuilder token=new StringBuilder(5);
        for(int i=0;i<5;i++){
            token.append(random.nextInt(10));// appending random digit from 0 to 9

        }
        return token.toString();
    }

    public void sendEmailVerificationToken(String email){
        Optional<AuthenticationUser> user=authenticationUserRepo.findByEmail(email);
        if(user.isPresent() && !user.get().getEmailVerified()){
            String emailVerificationToken=generateEmailVerificationToken();
            String hashedToken=encode.encode(emailVerificationToken);
            user.get().setEmailVerificationToken(hashedToken);
            user.get().setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusMinutes(durationInMinutes));
            authenticationUserRepo.save(user.get());
            String subject= "Email verification";
            String body=String.format("Only one step to take full advantage of LinkedIn.\n\n"
            + "Enter this code to verify your email: " + "%s\n\n" + "The code will expire in " + "%s" + "minutes.",emailVerificationToken,durationInMinutes);

            try {
                emailService.sendEmail(email,subject,body);
            }catch (Exception e){
                logger.info("Error while sending email: {}", e.getMessage());
            }
        }else {
            throw new IllegalArgumentException("Email verification token failed, or email is already verified.");

        }
    }
    public void validateEmailVerificationToken(String token, String email) {
        Optional<AuthenticationUser> user = authenticationUserRepo.findByEmail(email);
        if (user.isPresent() && encode.matches(token, user.get().getEmailVerificationToken()) && !user.get().getEmailVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            user.get().setEmailVerified(true);
            user.get().setEmailVerificationToken(null);
            user.get().setEmailVerificationTokenExpiryDate(null);
            authenticationUserRepo.save(user.get());
        } else if (user.isPresent() && encode.matches(token, user.get().getEmailVerificationToken()) && user.get().getEmailVerificationTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Email verification token expired.");
        } else {
            throw new IllegalArgumentException("Email verification token failed.");
        }
    }



    public AuthenticationUser getUser(String email) {
        return authenticationUserRepo.findByEmail(email).orElseThrow(() -> new IllegalArgumentException("user not found"));
    }

    public AuthenticationResponceBody register(AuthenticationRequestBody registerRequestBody) {
        AuthenticationUser user=authenticationUserRepo.save(new AuthenticationUser(registerRequestBody.getEmail(),encode.encode(registerRequestBody.getPassword())));

        String emailVerificationToken=generateEmailVerificationToken();
        String hashedToken=encode.encode(emailVerificationToken);
        user.setEmailVerificationToken(hashedToken);
        user.setEmailVerificationTokenExpiryDate(LocalDateTime.now().plusMinutes(durationInMinutes));
        authenticationUserRepo.save(user);
        String subject = "Email Verification";
        String body = String.format("""
                        Only one step to take full advantage of LinkedIn.
                        
                        Enter this code to verify your email: %s. The code will expire in %s minutes.""",
                emailVerificationToken, durationInMinutes);
        try {
            emailService.sendEmail(registerRequestBody.getEmail(),subject,body);
        }catch (Exception e){
            logger.info("Error while sending email: {}", e.getMessage());
        }
        String authToken = jsonWebToken.generateToken(registerRequestBody.getEmail());
        return new AuthenticationResponceBody(authToken, "User registered successfully.");
    }

    //Password reset logic

    public void sendPasswordResetToken(String email) {
        Optional<AuthenticationUser> user = authenticationUserRepo.findByEmail(email);
        if(user.isPresent()){
            String passwordResetToken=generateEmailVerificationToken();
            String hashedToken=encode.encode(passwordResetToken);
            user.get().setPasswordResetToken(hashedToken);
            user.get().setPasswordResetTokenExpiryDate(LocalDateTime.now().plusMinutes(durationInMinutes));
            authenticationUserRepo.save(user.get());
            String subject = "Password Reset";
            String body = String.format("""
                    You requested a password reset.
                    
                    Enter this code to reset your password: %s. The code will expire in %s minutes.""",
                    passwordResetToken, durationInMinutes);
            try {
                emailService.sendEmail(email,subject,body);
            }catch (Exception e){
                logger.info("Error while sending email: {}", e.getMessage());
            }

        }else {
            throw new IllegalArgumentException("User not found.");
        }
    }

    public void resetPassword(String email,String newPassword,String token) {
        Optional<AuthenticationUser> user = authenticationUserRepo.findByEmail(email);
        if(user.isPresent() && encode.matches(token, user.get().getPasswordResetToken()) && !user.get().getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())){
            user.get().setPasswordResetToken(null);
            user.get().setPasswordResetTokenExpiryDate(null);
            user.get().setPassword(encode.encode(newPassword));
            authenticationUserRepo.save(user.get());


        }else if (user.isPresent() && encode.matches(token,user.get().getPasswordResetToken()) && user.get().getPasswordResetTokenExpiryDate().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Password reset token expired.");
        }else {
            throw new IllegalArgumentException("Password reset token failed.");
        }
    }

    public AuthenticationResponceBody login(AuthenticationRequestBody loginRequestBody) {
        AuthenticationUser user = authenticationUserRepo.
                findByEmail(loginRequestBody.getEmail()).orElseThrow(() -> new IllegalArgumentException("user not found"));

        if (!encode.matches(loginRequestBody.getPassword(), user.getPassword())) {
            throw new IllegalArgumentException("wrong password");
        }
        String token = jsonWebToken.generateToken(loginRequestBody.getEmail());
        return new AuthenticationResponceBody(token, "Authentication successful.");
    }
}
