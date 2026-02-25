package com.campuscrew.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                // 1. Public pages (anyone can access these)
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/uploads/**").permitAll()
                // 2. 👑 THE VAULT: Only the Super Admin can access anything that starts with /admin
                .requestMatchers("/admin/**").hasAuthority("ROLE_SUPER_ADMIN")
                // 3. Everything else requires the user to be logged in
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout") // <--- This replaces the AntPathRequestMatcher!
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }
}
