package com.example.datagath.model;

import java.sql.Date;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;
import jakarta.persistence.Entity;

import jakarta.persistence.Id;

@Entity
public class CollectionTable {
    @Id
    private String id;
    private String name;
    private Boolean isPublic = false;
    private Long ownerId;
    private int remainingInputs;
    private int length;
    private String url;
    private Instant lastActive;

    public CollectionTable(String name, Long ownerId, String datatype) {
        this.id = UUID.randomUUID().toString();
        this.name = name;
        this.ownerId = ownerId;
        this.remainingInputs = -1;
        this.length = 0;

    };

    public CollectionTable() {
    };

    public void allowInput(String source, Date timestamp, String datatype, String content) {

    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Long getOwnerId() {
        return ownerId;
    }

    public void setOwnerId(Long ownerId) {
        this.ownerId = ownerId;
    }

    public int getRemainingInputs() {
        return remainingInputs;
    }

    public void setRemainingInputs(int remainingInputs) {
        this.remainingInputs = remainingInputs;
    }

    public int getLength() {
        return length;
    }

    public void setLength(int length) {
        this.length = length;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(Map<String, String> columns) {

        StringBuilder u = new StringBuilder("http://localhost:8080/tables/input/" + id + "?");
        columns.forEach((name, type) -> {
            switch (type) {
                case "TEXT":
                    u.append(name).append("='YOURTEXT'&");
                    break;
                case "DATETIME":
                    u.append(name).append("=YOURDATETIME&");

                    break;
                default:
                    u.append(name).append("=YOURNUMBER&");

                    break;
            }

        });
        if (u.charAt(u.length() - 1) == '&') {
            u.deleteCharAt(u.length() - 1);
        }
        this.url = u.toString();
    }

    public Boolean getIsPublic() {
        return isPublic;
    }

    public void setIsPublic(Boolean isPublic) {
        this.isPublic = isPublic;
    }

    public Instant getLastActive() {
        return lastActive;
    }

    public void setLastActive(Instant lastActive) {
        this.lastActive = lastActive;
    }

}
