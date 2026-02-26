package com.campuscrew.backend.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
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
                // 1. Public pages
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/uploads/**").permitAll()
                // 2. 👑 THE VAULT: Only the Super Admin can access the admin dashboard
                .requestMatchers("/admin/**").hasAuthority("ROLE_SUPER_ADMIN")
                // 3. 📝 EVENT CREATION: Only Presidents and Super Admins can POST to /events
                .requestMatchers(HttpMethod.POST, "/events").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                // 4. Everything else requires the user to be logged in
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }
}
