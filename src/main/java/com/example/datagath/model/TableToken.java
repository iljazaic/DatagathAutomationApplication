package com.example.datagath.model;

import java.util.UUID;
import java.time.Instant;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;

@Entity
public class TableToken {

    @Id
    private String token;

    private Long userId;



    public int rejection;
    private String tableName;

    private Instant expiryDate;

    private Instant creationDate;

    private Instant lastUsedAt;


    public TableToken(){}


    public TableToken(Long userId, Instant expiryDate, Instant creationDate, Instant lastusedAt, String TableName) {
        this.token = UUID.randomUUID().toString();
        this.creationDate = creationDate;
        this.userId = userId;
        this.expiryDate = expiryDate;
        this.lastUsedAt = lastusedAt;
        this.tableName = tableName;
    }   

    public TableToken(int number){
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

    public String getTableName(){
        return tableName;
    };
}
//clean