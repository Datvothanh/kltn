package com.example.democonnecsql.service.impl;

import com.example.democonnecsql.entity.*;
import com.example.democonnecsql.model.LoginModel;
import com.example.democonnecsql.model.UserModel;
import com.example.democonnecsql.repository.JWTAuthResponseRepository;
import com.example.democonnecsql.repository.PasswordResetTokenRepository;
import com.example.democonnecsql.repository.UserRepository;
import com.example.democonnecsql.repository.VerificationTokenRepository;
import com.example.democonnecsql.service.JwtTokenService;
import com.example.democonnecsql.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Calendar;
import java.util.Optional;
import java.util.UUID;
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private VerificationTokenRepository verificationTokenRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JWTAuthResponseRepository jwtAuthResponseRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtTokenService jwtTokenProvider;


    public UserServiceImpl(
            JwtTokenService jwtTokenProvider,
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            VerificationTokenRepository verificationTokenRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            JWTAuthResponseRepository jwtAuthResponseRepository) {
        this.authenticationManager = authenticationManager;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtTokenProvider = jwtTokenProvider;
        this.verificationTokenRepository = verificationTokenRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.jwtAuthResponseRepository = jwtAuthResponseRepository;
    }

    @Override
    public User registerUser(UserModel userModel) { //lưu tài khoản vào DB
        User user = new User();
        user.setEmailAddress(userModel.getEmailAddress());
        user.setSurName(userModel.getSurName());
        user.setGivenName(userModel.getGivenName());
        user.setDateOfBirth(userModel.getDateOfBirth());
        user.setSex(userModel.getSex());
        user.setRole("USER");
        user.setUserName(userModel.getUserName());
        user.setPassword(passwordEncoder.encode(userModel.getPassword()));
        userRepository.save(user);
        return user;
    }



    @Override
    public void saveVerificationTokenForUser(String token, User user) { // Lưu token
        VerificationToken verificationToken = new VerificationToken(user,token);
        verificationTokenRepository.save(verificationToken);

    }

    @Override
    public String validateVerificationToken(String token) { // Kiểm tra token
        VerificationToken verificationToken = verificationTokenRepository.findByToken(token);
        if(verificationToken == null){
            return "invalid";
        }

        User user = verificationToken.getUser();
        Calendar cal = Calendar.getInstance();

        if((verificationToken.getExpirationTime().getTime() - cal.getTime().getTime()) <= 0){
            verificationTokenRepository.delete(verificationToken);
            return "expired";
        }
        user.setEnabled(true);
        userRepository.save(user);
        return "valid";
    }

    @Override
    public VerificationToken generateNewVerificationToken(String oldToken) { //Thay đổi token cũ thành mới
        VerificationToken verificationToken = verificationTokenRepository.findByToken(oldToken);
        verificationToken.setToken(UUID.randomUUID().toString());
        verificationTokenRepository.save(verificationToken);
        return verificationToken;
    }

    @Override
    public User findUserByEmail(String emailAdress) {
        return userRepository.findByEmailAddress(emailAdress);
    }



    @Override
    public void createPasswordResetTokenForUser(User user, String token) {
        PasswordResetToken passwordResetToken = new PasswordResetToken(user,token);
        passwordResetTokenRepository.save(passwordResetToken);
    }

    @Override
    public boolean checkIfValidOldPassword(User user, String oldPassword) {
        return passwordEncoder.matches(oldPassword, user.getPassword());
    }

    @Override
    public void changePassword(User user, String newPassword) {
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    @Override
    public String validatePasswordResetToken(String token) {
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);

        if(passwordResetToken == null){
            return "invalid";
        }

        User user = passwordResetToken.getUser();
        Calendar cal = Calendar.getInstance();

        if((passwordResetToken.getExpirationTime().getTime() - cal.getTime().getTime()) <= 0){
            passwordResetTokenRepository.delete(passwordResetToken);
            return "expired";
        }

        return "valid";
    }

    @Override
    public Optional<User> getUserByPasswordResetToken(String token) {
        return Optional.ofNullable(passwordResetTokenRepository.findByToken(token).getUser());
    }

    @Override
    public void deletePasswordResetToken(String token ){
        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token);
        passwordResetTokenRepository.delete(passwordResetToken);
    }


    @Override
    public String login(LoginModel loginModel) {
        Authentication authentication = authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(
                loginModel.getUsernameOrEmailAddress(), loginModel.getPassword()));
        SecurityContextHolder.getContext().setAuthentication(authentication);
        return jwtTokenProvider.generateToken(authentication);

    }

    @Override
    public String refreshToken(String username) {
        return jwtTokenProvider.generateTokenByName(username);

    }

    @Override
    public Boolean checkUser(String username) {
       return userRepository.existsByUserNameAndEnabled(username , true);
    }

    @Override
    public Boolean checkEmail(String email) {
        return userRepository.existsByEmailAddressAndEnabled(email , true);
    }

    @Override
    public JWTAuthResponse CreatJWTAuthResponse (String token , String refreshToken , String usernameOrEmailAddress){
        User user = userRepository.findByUserNameAndEnabledOrEmailAddressAndEnabled(usernameOrEmailAddress, true , usernameOrEmailAddress , true)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));
        JWTAuthResponse jwtAuthResponse = new JWTAuthResponse();
        jwtAuthResponse.setAccessToken(token);
        jwtAuthResponse.setToken(refreshToken);
        jwtAuthResponse.setUser(user);
        jwtAuthResponse.setRevoked(false);
        jwtAuthResponse.setExpired(false);
        jwtAuthResponseRepository.save(jwtAuthResponse);
        return  jwtAuthResponse;
    }


    @Override
    public User userInfoByToken (String token){
        String usernameOrEmailAddress = jwtTokenProvider.getUsernameOrEmail(token);
        return userRepository.findByUserNameAndEnabledOrEmailAddressAndEnabled(usernameOrEmailAddress, true , usernameOrEmailAddress , true)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));
    }




}
