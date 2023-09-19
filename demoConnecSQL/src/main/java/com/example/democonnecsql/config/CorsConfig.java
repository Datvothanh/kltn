package com.example.democonnecsql.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("http://localhost:3000") // Hoặc "*"
                .allowedMethods("GET", "POST" , "PUT", "DELETE") // Phương thức yêu cầu cho phép
                .allowedHeaders("Content-Type", "Authorization")
                .exposedHeaders("Authorization") // Cho phép truyền header Authorization trong phản hồi
                .allowCredentials(false)
                .maxAge(36000);// Các tiêu đề yêu cầu cho phép


    }
}
