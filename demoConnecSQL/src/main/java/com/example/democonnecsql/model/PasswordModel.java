package com.example.democonnecsql.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
@Getter
@Setter
public class PasswordModel {
    private String emailAddress;
    private String newPassword;
    private String oldPassword;

}
