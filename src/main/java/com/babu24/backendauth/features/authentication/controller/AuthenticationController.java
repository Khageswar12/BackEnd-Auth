package com.babu24.backendauth.features.authentication.controller;

import com.babu24.backendauth.features.authentication.Service.AuthenticationService;
import com.babu24.backendauth.features.authentication.dto.AuthenticationRequestBody;
import com.babu24.backendauth.features.authentication.dto.AuthenticationResponceBody;
import com.babu24.backendauth.features.authentication.model.AuthenticationUser;
import jakarta.mail.MessagingException;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

import java.io.UnsupportedEncodingException;

@RestController
@RequestMapping("/api/v1/authentication")
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    public AuthenticationController(AuthenticationService authenticationService) {
        this.authenticationService = authenticationService;
    }

    @GetMapping("/user")
    public AuthenticationUser getUser(@RequestAttribute("authenticationUser") AuthenticationUser authenticationUser) {
        return authenticationUser;
    }

    @PostMapping("/login")
    public AuthenticationResponceBody loginPage(@Valid @RequestBody AuthenticationRequestBody loginRequestBody) {
        return authenticationService.login(loginRequestBody);

    }


    @PostMapping("/register")
    public AuthenticationResponceBody registerPage(@Valid @RequestBody AuthenticationRequestBody registerRequestBody) throws MessagingException, UnsupportedEncodingException {
        return authenticationService.register(registerRequestBody);
    }


    @PutMapping("/validate-email-verification-token")
    public String verifyEmail(@RequestParam String token,@RequestAttribute("authenticationUser") AuthenticationUser authenticationUser) {
        authenticationService.validateEmailVerificationToken(token,authenticationUser.getEmail());
        return "Email verified Successfully.";
    }


    @GetMapping("/send-email-verification-token")
    public String sendEmailVerificationToken(@RequestAttribute("authenticationUser") AuthenticationUser authenticationUser) {
        authenticationService.sendEmailVerificationToken(authenticationUser.getEmail());
        return "Email verification token sent Successfully.";
    }

       @PutMapping("/send-password-reset-token")
        public String sendPasswordResetToken(@RequestParam String email) {
        authenticationService.sendPasswordResetToken(email);
        return "Password reset token sent Successfully.";
        }

    @PutMapping("/reset-password")
    public String resetPassword(@RequestParam String newPassword, @RequestParam String token,@RequestParam String email) {
        authenticationService.resetPassword(newPassword,token,email);
        return "Password reset Successfully.";
    }
}
