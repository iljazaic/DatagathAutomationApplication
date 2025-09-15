package com.example.datagath.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.datagath.model.User;
import java.util.List;


public interface UserRepository extends JpaRepository<User, Long>{
    Optional<User> findByEmail(String email);
    Optional<User> findById(Long id);
    Optional<User> findByName(String name);

}