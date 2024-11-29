package com.babu24.backendauth.features.authentication.repository;

import com.babu24.backendauth.features.authentication.model.AuthenticationUser;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AuthenticationUserRepo extends JpaRepository<AuthenticationUser,Long> {

    Optional<AuthenticationUser> findByEmail(String email);
}
