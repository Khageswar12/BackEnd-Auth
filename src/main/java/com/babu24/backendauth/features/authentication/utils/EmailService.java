package com.babu24.backendauth.features.authentication.utils;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;

@Service
public class EmailService {


    private final JavaMailSender mailSender;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

   public void sendEmail(String email, String subject, String content) throws MessagingException , UnsupportedEncodingException {
       MimeMessage massage = mailSender.createMimeMessage();
       MimeMessageHelper helper = new MimeMessageHelper(massage);


       helper.setFrom("no-reply@linkedin.com","LinkedIn");
       helper.setTo(email);

       helper.setSubject(subject);
       helper.setText(content, true);

       mailSender.send(massage);
   }
}
