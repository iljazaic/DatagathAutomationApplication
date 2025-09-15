package com.example.datagath.dto;


public class SchEvCreationResponse {
    

    private Boolean success;
    private Long eventId;
    
    public Boolean getSuccess(){
        return success;
    }

    public Long getEventId(){
        return eventId;
    }

    public void setSuccess(Boolean success){
        this.success=success;
    }
    
    public void setEventId(Long eventId){
        this.eventId = eventId;
    }

}
//clean