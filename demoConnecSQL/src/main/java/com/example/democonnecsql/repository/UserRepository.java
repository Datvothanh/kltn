package com.example.democonnecsql.repository;


import com.example.democonnecsql.entity.User;
import com.example.democonnecsql.model.LoginModel;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    User findByEmailAddress(String emailAddress);

    boolean existsByUserNameAndEnabled(String username , Boolean enabled);

    boolean existsByEmailAddressAndEnabled(String email , Boolean enabled);


    Optional<User> findByUserNameAndEnabledOrEmailAddressAndEnabled(String username, boolean enabled_username, String emailAddress, boolean enabled_emailAddress);





}
