package com.example.datagath.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.datagath.model.CollectionTable;
import com.example.datagath.model.User;  
import com.example.datagath.service.UserService;

@Controller
@RequestMapping("/manage")
public class ManagementController {

    private final UserService userService;

    public ManagementController(UserService userService) {
        this.userService = userService;
    }
    
    private User validateSessionToken(String sessionToken) {
        return (userService.validateSessionToken(sessionToken));
    }

    @GetMapping("/dashboard")
    public String getDashboard(@CookieValue(value = "sessionToken", required = false) String token,
            Model model) {
        User u = token != null ? validateSessionToken(token) : null;
        if (u == null) {
            return "redirect:/login";
        } else {

            //user is logged in
            //pull all the datasets the user has and make separate requests for them;
            List<CollectionTable> collectionTables = userService.GetAllDatasetsOfAUser(u);
            //List<Dataset> datasets = userService.GetAllUserDatasets(u);
            //List<ScheduledEvent> scheduleModule = userService.GetAllScheduleModulesOfAUser(u);
            //for (CollectionTable collectionTable : collectionTables) {
                
           // }
            return "dashboard";
        }
    }
}