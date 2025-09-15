package com.example.datagath.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Bean;

@Configuration
public class SecConf {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())         // optional for development
                .authorizeHttpRequests(auth -> auth
                                .anyRequest().permitAll() // allow all requests without authentication
                );
        return http.build();
    }
}