package com.example.datagath.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.datagath.model.CollectionTable;

@Repository
public interface CollectionTableRepository extends JpaRepository<CollectionTable, String>{
    Optional<CollectionTable> findById(String id);
    Optional<CollectionTable> findByName(String name);
    List<CollectionTable> findByOwnerId(Long ownerId);
}