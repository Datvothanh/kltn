package com.example.democonnecsql.config;


import com.example.democonnecsql.entity.JWTAuthResponse;

import com.example.democonnecsql.repository.JWTAuthResponseRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.LogoutHandler;
import org.springframework.stereotype.Service;
@Service
@RequiredArgsConstructor
@Slf4j
public class LogoutService implements LogoutHandler {


    @Autowired
    private JWTAuthResponseRepository jwtAuthResponseRepository;

    @Override
    public void logout(
            HttpServletRequest request,
            HttpServletResponse response,
            Authentication authentication
    ) {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        if (authHeader == null ||!authHeader.startsWith("Bearer ")) {
            return;
        }
        jwt = authHeader.substring(7);
        var storedToken = jwtAuthResponseRepository.findByAccessToken(jwt)
                .orElse(null);
        if (storedToken != null) {
                storedToken.setExpired(true);
            storedToken.setRevoked(true);
            jwtAuthResponseRepository.save(storedToken);
            SecurityContextHolder.clearContext();
        }
    }
}
