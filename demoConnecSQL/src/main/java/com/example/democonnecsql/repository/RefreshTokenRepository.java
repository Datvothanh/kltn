package com.example.democonnecsql.repository;

import com.example.democonnecsql.entity.RefreshToken;
import com.example.democonnecsql.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken,Integer> {
    Optional<RefreshToken> findByToken(String token);



    RefreshToken findByUser (User user);
}