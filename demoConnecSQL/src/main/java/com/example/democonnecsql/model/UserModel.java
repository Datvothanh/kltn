package com.example.democonnecsql.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserModel {
    private String givenName;
    private String surName;
    private String emailAddress;
    private String password;
    private String sex;
    private String userName;
    private Date dateOfBirth;
}
