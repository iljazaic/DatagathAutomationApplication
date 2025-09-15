package com.example.datagath.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.datagath.model.SessionToken;

public interface SessionTokenRepository extends JpaRepository<SessionToken, String> {
    

    Optional<SessionToken> findByUserId(Long userId);
    Optional<SessionToken> findByToken(String token);

}
