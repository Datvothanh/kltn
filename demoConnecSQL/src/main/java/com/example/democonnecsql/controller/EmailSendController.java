package com.example.democonnecsql.controller;


import com.example.democonnecsql.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;


@RestController
@RequestMapping("/api/auth/mail")
public class EmailSendController {
    @Autowired
    private EmailService emailService;

    @PostMapping("/send")
    public String sendMail(@RequestParam(required = false)  String to, String subject, String body) {
        return emailService.sendMail(to, subject, body);
    }

}