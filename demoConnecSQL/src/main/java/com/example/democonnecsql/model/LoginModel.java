package com.example.democonnecsql.model;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginModel {
    private String usernameOrEmailAddress;
    private String password;
}
