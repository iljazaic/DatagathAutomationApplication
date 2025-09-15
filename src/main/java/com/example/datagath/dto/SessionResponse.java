package com.example.datagath.dto;

import com.example.datagath.model.SessionToken;

public class SessionResponse {
    

    private Boolean success;
    private SessionToken sessionToken;


    public Boolean getSuccess(){
        return success;
    }

    public String getSessionToken(){
        return sessionToken.getToken();
    }

    public void setSuccess(Boolean success){
        this.success=success;
    }
    
    public void setSessionToken(SessionToken sessionToken){
        this.sessionToken = sessionToken;
    }

}
//clean