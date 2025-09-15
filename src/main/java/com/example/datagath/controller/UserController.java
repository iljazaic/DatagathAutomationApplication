package com.example.datagath.controller;

import java.util.List;

import org.springframework.http.HttpHeaders;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import com.example.datagath.dto.LoginForm;
import com.example.datagath.dto.SessionResponse;
import com.example.datagath.dto.SignupForm;
import com.example.datagath.model.CollectionTable;
import com.example.datagath.model.User;
import com.example.datagath.service.UserService;

import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
//jesus christ the abstractions
import org.springframework.web.bind.annotation.RequestBody;

@Controller
@RequestMapping("/")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    private User validateSessionToken(String sessionToken) {
        return (userService.validateSessionToken(sessionToken));
    }

    @GetMapping("/")
    public String getHomePage(@CookieValue(value = "sessionToken", required = false) String token, Model model) {

        User u = token != null ? validateSessionToken(token) : null;
        if(u!=null){
            model.addAttribute("user",u);
        }
        return "home";

    }

    @GetMapping("/login")
    public String getLoginPage(@CookieValue(value = "sessionToken", required = false) String token,
            Model model) {

        User u = token != null ? validateSessionToken(token) : null;
        if (u == null) {
            return "login";
        } else {
            model.addAttribute("user", u);
            return "redirect:/account";
        }
    }

    @GetMapping("/sign_out")
    public String logoutUser(@CookieValue(value = "sessionToken", required = false) String token,
            Model model) {
        System.out.println(token);
        userService.logoutUser(token);
        return "redirect:/login";
    }

    @GetMapping("/error")
    public String errorOccuring(@RequestBody String entity) {
        // TODO: process POST request

        return "error";
    }

    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody LoginForm loginForm) {
        SessionResponse sessionResponse = userService.loginUser(loginForm);
        if (sessionResponse.getSuccess()) {

            ResponseCookie cookie = ResponseCookie.from("sessionToken", sessionResponse.getSessionToken())
                    .httpOnly(true)
                    .secure(false)
                    .path("/")
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.status(HttpStatus.FOUND).header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .header(HttpHeaders.LOCATION, "/account").body("Login Successful");
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).header(HttpHeaders.LOCATION, "/error")
                    .body("Username or password wrong.");
        }
    }

    @GetMapping("/account")
    public String getAccountPage(@CookieValue(value = "sessionToken", required = false) String token, Model model) {
        User u = token != null ? validateSessionToken(token) : null;
        // System.out.println(u.getEmail());
        // System.out.println(u.getEmail());
        if (u != null) {
            model.addAttribute("user", u);
            return "account";
        }
        return "redirect:/login";
    }

    @GetMapping("/signup")
    public String getSignupPage(@CookieValue(value = "sessionToken", required = false) String token, Model model) {
 
        User u = token != null ? validateSessionToken(token) : null;
        if (u == null) {
            return "signup";
        } else {
            model.addAttribute("user", u);
            return "redirect:/login";
        }
    }

    @PostMapping("/signup")
    public ResponseEntity<?> newUser(@RequestBody SignupForm signupForm, Model model) {
        System.out.println(signupForm.getEmail());
        SessionResponse response = userService.createUser(signupForm.getName(), signupForm.getEmail(),
                signupForm.getPassword());
        if (response.getSuccess()) {
            ResponseCookie cookie = ResponseCookie.from("sessionToken", response.getSessionToken())
                    .httpOnly(true)
                    .secure(true)
                    .path("/")
                    .sameSite("Strict")
                    .build();
            return ResponseEntity.ok().header(HttpHeaders.SET_COOKIE, cookie.toString())
                    .header(HttpHeaders.LOCATION, "/account").body("SIGNUP SUCCESSFUL");
        } else {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("Username or Email Taken");
        }
    }
}
// not clean