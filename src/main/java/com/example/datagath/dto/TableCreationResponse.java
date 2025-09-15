package com.example.datagath.dto;

import com.example.datagath.model.TableToken;

public class TableCreationResponse {
    

    private Boolean success;
    private TableToken tableToken;
    private String body;

    public Boolean getSuccess(){
        return success;
    }

    public String getTableToken(){
        return tableToken.getToken();
    }

    public void setSuccess(Boolean success){
        this.success=success;
    }
    
    public void setTableToken(TableToken sessionToken){
        this.tableToken = sessionToken;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

}
//clean