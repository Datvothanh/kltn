package com.example.democonnecsql.event.listener;

import com.example.democonnecsql.entity.User;
import com.example.democonnecsql.event.EmailEvent;
import com.example.democonnecsql.event.RegistrationCompleteEvent;
import com.example.democonnecsql.service.EmailService;
import com.example.democonnecsql.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
public class EmailEventListener implements ApplicationListener<EmailEvent> {

    @Autowired
    private UserService userService;

    @Autowired
    private EmailService emailService;


    @Override
    public void onApplicationEvent(EmailEvent event){
        User user = event.getUser();
        String token = UUID.randomUUID().toString();
        userService.saveVerificationTokenForUser(token, user);

        String url = event.getApplicationUrl()
                + "api/auth/verifyRegistration?token="
                + token;
        String resetUrl = event.getApplicationUrl()
                + "api/auth/resendVerifyToken?token="
                + token;
        emailService.sendMail(user.getEmailAddress(), "Xác nhận tài khoản", "Link xác nhận:" + url + " , "+ "Link yêu cầu gửi lại: "+ resetUrl);
    }
}
