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
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/uploads/**").permitAll()
                .requestMatchers("/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                .requestMatchers(HttpMethod.POST, "/events/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                // 🏛️ NEW CLUB RULES: Super Admin creates, both can view/edit
                .requestMatchers(HttpMethod.POST, "/clubs/create").hasAuthority("ROLE_SUPER_ADMIN")
                .requestMatchers("/clubs/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
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
