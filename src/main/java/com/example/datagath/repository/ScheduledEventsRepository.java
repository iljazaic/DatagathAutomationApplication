package com.example.datagath.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.datagath.model.ScheduledEvent;
import com.example.datagath.model.User;

public interface ScheduledEventsRepository extends JpaRepository<ScheduledEvent, Long> {
    

    List<ScheduledEvent> findByOwner(User user);
    List<ScheduledEvent> findByName(String token);
    Optional<ScheduledEvent> findById(Long id);


}
