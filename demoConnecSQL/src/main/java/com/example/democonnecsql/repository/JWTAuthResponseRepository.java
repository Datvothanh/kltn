package com.example.democonnecsql.repository;


import com.example.democonnecsql.entity.JWTAuthResponse;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


import java.util.Optional;

@Repository
public interface JWTAuthResponseRepository extends JpaRepository<JWTAuthResponse, Long> {

    Optional<JWTAuthResponse> findByAccessToken(String Token);



}
