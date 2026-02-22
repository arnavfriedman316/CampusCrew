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
                .csrf(csrf -> csrf.disable()) //this allows submissions of forms
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/register", "/css/**").permitAll() //these are the public pages, accessable by all
                .anyRequest().authenticated() //these will lock away the pages so that only the login-ed person can access this
                )
                .formLogin(login -> login
                .loginPage("/login") //use our custom file
                .defaultSuccessUrl("/", true)
                .permitAll() //this allows user to see the register and login pages
                )
                .logout(logout -> logout.permitAll());

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder();
    }
}
