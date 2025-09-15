package com.example.datagath.dto;

public class LoginForm {
    private String name;
    private String password;
    private String stayLoggedIn;

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public String getPassword() {
        return password;
    }
    public void setPassword(String password) {
        this.password = password;
    }
    public Boolean getStayLoggedIn() {

        return stayLoggedIn!=null&&stayLoggedIn.equals("on")?true:false;
    }

    public void setStayLoggedIn(String stayLoggedIn)  {
        this.stayLoggedIn = stayLoggedIn;
    }
}
