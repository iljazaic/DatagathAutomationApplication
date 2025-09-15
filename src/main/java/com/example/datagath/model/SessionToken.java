package com.example.datagath.model;

import java.util.UUID;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class SessionToken {

    @Id
    private String token;

    private Long userId;



    public int rejection;


    private Instant expiryDate;

    private Instant creationDate;

    private Instant lastUsedAt;


    public SessionToken(){}


    public SessionToken(Long userId, Instant expiryDate, Instant creationDate, Instant lastusedAt) {
        this.token = UUID.randomUUID().toString();
        this.creationDate = creationDate;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.lastUsedAt = lastusedAt;

    }   

    public SessionToken(int number){
       this.rejection = number;
    }
    //only getters cuz am lazy
    public String getToken(){
        return token;
    }
    public Long getUserId(){
        return userId;
    }

    public Instant getExpiryDate(){
        return expiryDate;
    }
    public Instant getCreationDate(){
        return creationDate;
    }
    public Instant getLastUsedAt(){
        return lastUsedAt;
    }
}
//clean