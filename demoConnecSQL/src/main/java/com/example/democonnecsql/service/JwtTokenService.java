package com.example.democonnecsql.service;

import com.example.democonnecsql.entity.JWTAuthResponse;
import com.example.democonnecsql.repository.JWTAuthResponseRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;


import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;


@Component
public class JwtTokenService {

    private static final Logger logger = LoggerFactory.getLogger(JwtTokenService.class);

    @Autowired
    private JWTAuthResponseRepository jwtAuthResponseRepository;

    @Value("${app.jwt-secret}")
    private String jwtSecret;

    @Value("${app-jwt-expiration-milliseconds}")
    private long jwtExpirationDate;

    // generate JWT token

    public String generateToken(Authentication authentication){
        return generateToken(new HashMap<>() , authentication);
    }

    public String generateTokenByName(String userName){
        Map<String,Object> claims=new HashMap<>();
        return createTokenByName(claims,userName);
    }

    private String createTokenByName(Map<String, Object> claims, String userName) {
        Date currentDate = new Date();

        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userName)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }
    public String generateToken(Map<String, Object> extraClaims , Authentication authentication){
        String usernameOrEmail = authentication.getName();

        Date currentDate = new Date();

        Date expireDate = new Date(currentDate.getTime() + jwtExpirationDate);

        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(usernameOrEmail)
                .setIssuedAt(new Date())
                .setExpiration(expireDate)
                .signWith(key(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Key key(){
        return Keys.hmacShaKeyFor(
                Decoders.BASE64.decode(jwtSecret)
        );
    }

    // get usernameOrEmail from Jwt token
    public String getUsernameOrEmail(String token){

        return extractClaim(token, Claims::getSubject);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver){
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    private Claims extractAllClaims(String token){
        return Jwts
                .parserBuilder()
                .setSigningKey(key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }


    // validate Jwt token
    public boolean validateToken(String token){
        try{
            Jwts.parserBuilder()
                    .setSigningKey(key())
                    .build()
                    .parse(token);

            return jwtAuthResponseRepository.findByAccessToken(token)
                    .map(t -> !t.isExpired() && !t.isRevoked())
                    .orElse(false);

        } catch (MalformedJwtException e) {
            logger.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            logger.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            logger.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            logger.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }


    public void expiredToken(String token){
      JWTAuthResponse jwtAuthResponse =  jwtAuthResponseRepository.findByAccessToken(token).orElseThrow(() -> new UsernameNotFoundException("Not Token"));
      jwtAuthResponse.setExpired(true);
      jwtAuthResponseRepository.save(jwtAuthResponse);
    }


}
