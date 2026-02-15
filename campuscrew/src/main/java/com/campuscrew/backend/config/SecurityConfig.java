package com.campuscrew.backend.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.campuscrew.backend.service.CustomUserDetailsService;

@Configuration
public class SecurityConfig {

    @Autowired
    private CustomUserDetailsService customUserDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable()) // Allow form submissions
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/css/**").permitAll() // PUBLIC PAGES
                .anyRequest().authenticated() // LOCK EVERYTHING ELSE
                )
                .formLogin(login -> login
                .loginPage("/login") // Use our custom file
                .defaultSuccessUrl("/", true)
                .permitAll() // Allow everyone to see the login page!
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
