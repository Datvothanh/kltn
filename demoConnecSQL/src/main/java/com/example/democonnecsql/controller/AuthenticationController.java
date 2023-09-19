package com.example.democonnecsql.controller;


import com.example.democonnecsql.entity.JWTAuthResponse;
import com.example.democonnecsql.entity.RefreshToken;
import com.example.democonnecsql.entity.User;
import com.example.democonnecsql.entity.VerificationToken;
import com.example.democonnecsql.event.EmailEvent;
import com.example.democonnecsql.event.RegistrationCompleteEvent;
import com.example.democonnecsql.model.*;
import com.example.democonnecsql.repository.JWTAuthResponseRepository;
import com.example.democonnecsql.service.JwtTokenService;
import com.example.democonnecsql.service.RefreshTokenService;
import com.example.democonnecsql.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import org.springframework.web.bind.annotation.*;

import java.util.Optional;
import java.util.UUID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/auth")
public class AuthenticationController {

    @Autowired
    private UserService userService;

    @Autowired
    private ApplicationEventPublisher publisher;

    @Autowired
    private RefreshTokenService refreshTokenService;

    @Autowired
    private JwtTokenService jwtTokenService;


    @PostMapping(value ="/register" ) // đăng ký tài khoản
    public String registerUser(@RequestBody UserModel userModel, final HttpServletRequest request) {
        User user = userService.registerUser(userModel);
        publisher.publishEvent(new RegistrationCompleteEvent(user, applicationUrl(request))); // Gọi sự kiện xác nhận tài khoản
        return "Success";
    }

    // Build Login REST API
    @PostMapping("/login")
    public  JWTAuthResponse authenticate(@RequestBody LoginModel loginModel){
        String token = userService.login(loginModel);
        RefreshToken refreshToken = refreshTokenService.createRefreshToken(loginModel.getUsernameOrEmailAddress());
        return userService.CreatJWTAuthResponse(token , refreshToken.getToken() , loginModel.getUsernameOrEmailAddress());
    }

    @PostMapping("/expiredToken")
    public  String expiredToken(@RequestParam("token") String token){
        jwtTokenService.expiredToken(token);
      return "expiredToken";
    }

    @GetMapping("/verifyRegistration")// Link xác nhận tài khoản
    public String verifyRegistration(@RequestParam("token") String token) {
        String result = userService.validateVerificationToken(token);
        if (result.equalsIgnoreCase("valid")) {
            return "User Verifies Successfully";
        }
        return "Bad User";
    }

    @GetMapping("/resendVerifyToken") // Yêu cầu gửi lại link xác nhận
    public String resendVerificationToken(@RequestParam("token") String oldToken, HttpServletRequest request) {
        VerificationToken verificationToken = userService.generateNewVerificationToken(oldToken);
        User user = verificationToken.getUser();
        publisher.publishEvent(new EmailEvent(user, applicationUrl(request) ));
//        resendVerificationTokenMail(user, applicationUrl(request), verificationToken);
        return "Verification Link Sent";

    }

    @PostMapping("/refreshToken")
    public JWTAuthResponse refreshToken(@RequestBody RefreshTokenModel refreshTokenModel) {
        JWTAuthResponse jwtAuthResponse = new JWTAuthResponse();
        log.info(refreshTokenModel.getToken());
         return refreshTokenService.findByToken(refreshTokenModel.getToken())
                .map(refreshTokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    String accessToken = userService.refreshToken(user.getUserName());
                    jwtAuthResponse.setAccessToken(accessToken);
                    jwtAuthResponse.setToken(refreshTokenModel.getToken());
                    userService.CreatJWTAuthResponse(accessToken , refreshTokenModel.getToken() , user.getUserName());
                    return jwtAuthResponse;
                }).orElseThrow(() -> new RuntimeException(
                        "Refresh token is not in database!"));
    }



    @PostMapping("/resetPassword")// Đổi lại mật khẩu
    public String resetPassword(@RequestBody PasswordModel passwordModel, HttpServletRequest request) {

        User user = userService.findUserByEmail(passwordModel.getEmailAddress());
        String url = "";
        if(user!=null) {
            String token = UUID.randomUUID().toString();
            userService.createPasswordResetTokenForUser(user,token);
            url = passwordResetTokenMail(user,applicationUrl(request), token);
        }
        return url;
    }

    @GetMapping("/checkUser")
    public Boolean checkUser(@RequestParam("username") String username) {
        return userService.checkUser(username);
    }

    @GetMapping("/checkEmail")
    public Boolean checkEmail(@RequestParam("email") String email) {
        return userService.checkEmail(email);
    }

    private String passwordResetTokenMail(User user, String applicationUrl, String token) {
        String url = applicationUrl + "api/auth/savePassword?token=" + token;

        log.info("Click the link to reset your password: {}", url);

        return url;
    }

    @PostMapping("/savePassword")// Lưu mật khẩu mới
    public String savePassword(@RequestParam("token") String token,
                               @RequestBody PasswordModel passwordModel) {
        String result = userService.validatePasswordResetToken(token);
        if(!result.equalsIgnoreCase("valid")) {
            return "Invalid Token";
        }
        Optional<User> user = userService.getUserByPasswordResetToken(token);
        if(user.isPresent()) {
            userService.changePassword(user.get(), passwordModel.getNewPassword());
            userService.deletePasswordResetToken(token);
            return "Password Reset Successfully";
        } else {
            return "Invalid Token";
        }
    }

    @PostMapping("/changePassword")//Thay đổi mật khẩu
    public String changePassword(@RequestBody PasswordModel passwordModel){
        User user = userService.findUserByEmail(passwordModel.getEmailAddress());
        if(!userService.checkIfValidOldPassword(user,passwordModel.getOldPassword())) {
            return "Invalid Old Password";
        }
        //Save New Password
        userService.changePassword(user,passwordModel.getNewPassword());
        return "Password Changed Successfully";
    }


    private String applicationUrl(HttpServletRequest request) { // đường dẫn
        return "http://" + request.getServerName() + ":" + request.getServerPort() + "/" + request.getContextPath();
    }


}
