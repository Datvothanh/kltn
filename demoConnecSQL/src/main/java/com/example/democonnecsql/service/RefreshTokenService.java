package com.example.democonnecsql.service;

import com.example.democonnecsql.entity.RefreshToken;
import com.example.democonnecsql.entity.User;
import com.example.democonnecsql.repository.RefreshTokenRepository;
import com.example.democonnecsql.repository.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class RefreshTokenService {

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;
    @Autowired
    private UserRepository userRepository;



    public RefreshToken createRefreshToken(String usernameOrEmailAddress) {

        User user = userRepository.findByUserNameAndEnabledOrEmailAddressAndEnabled(usernameOrEmailAddress, true , usernameOrEmailAddress , true)
                .orElseThrow(() -> new UsernameNotFoundException("User not exists by Username or Email"));

        RefreshToken refreshToken = refreshTokenRepository.findByUser(user);

        if( refreshToken != null){
            refreshToken = verifyExpiration(refreshToken);
            if(refreshToken != null){
                return refreshToken;
            }else {
                RefreshToken  refreshTokenNew = RefreshToken.builder()
                        .user(user)
                        .token(UUID.randomUUID().toString())
                        .expiryDate(Instant.now().plusMillis(30000000))//10
                        .build();
                return refreshTokenRepository.save(refreshTokenNew);
            }

        }
        RefreshToken  refreshTokenNew = RefreshToken.builder()
                .user(user)
                .token(UUID.randomUUID().toString())
                .expiryDate(Instant.now().plusMillis(30000000))//10
                .build();

        return refreshTokenRepository.save(refreshTokenNew);
    }


    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }


    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpiryDate().compareTo(Instant.now()) < 0) {
            refreshTokenRepository.delete(token);
            throw new RuntimeException(token.getToken() + " Refresh token was expired. Please make a new signin request");
        }
        return token;
    }



}
