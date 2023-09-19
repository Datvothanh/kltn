package com.example.democonnecsql.controller;

import com.example.democonnecsql.entity.User;
import com.example.democonnecsql.model.UserModel;
import com.example.democonnecsql.service.JwtTokenService;
import com.example.democonnecsql.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
@RequiredArgsConstructor
    @RequestMapping("/api/v1/home")
public class HomeController {

    @Autowired
    private UserService userService;


    @GetMapping("/userInfo") // Yêu cầu gửi lại link xác nhận
    public User userInfo(@RequestParam("token") String token ) {
        return userService.userInfoByToken(token);
    }
}
