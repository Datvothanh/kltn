package com.example.democonnecsql.service;


import com.example.democonnecsql.entity.JWTAuthResponse;
import com.example.democonnecsql.entity.RefreshToken;
import com.example.democonnecsql.entity.User;
import com.example.democonnecsql.entity.VerificationToken;
import com.example.democonnecsql.model.LoginModel;
import com.example.democonnecsql.model.UserModel;

import java.util.Optional;

public interface UserService {

    User registerUser(UserModel userModel);

    void saveVerificationTokenForUser(String token, User user);

    String validateVerificationToken(String token);

    VerificationToken generateNewVerificationToken(String oldToken);

    User findUserByEmail(String email);

    void createPasswordResetTokenForUser(User user, String token);

    boolean checkIfValidOldPassword(User user, String oldPassword);

    void changePassword(User user, String newPassword);

    String validatePasswordResetToken(String token);

    Optional<User> getUserByPasswordResetToken(String token);


    void deletePasswordResetToken(String token);

    String login(LoginModel loginModel);

    String refreshToken(String username);

    Boolean checkUser(String username);

    Boolean checkEmail(String email);

    JWTAuthResponse CreatJWTAuthResponse(String token , String refreshToken ,String usernameOrEmailAddress);

    User userInfoByToken(String token);
}
