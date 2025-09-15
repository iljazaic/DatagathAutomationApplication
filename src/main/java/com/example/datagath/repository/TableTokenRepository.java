package com.example.datagath.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.datagath.model.SessionToken;
import com.example.datagath.model.TableToken;

public interface TableTokenRepository extends JpaRepository<TableToken, String> {
    

    Optional<TableToken> findByUserId(Long userId);
    Optional<TableToken> findByToken(String token);

}
