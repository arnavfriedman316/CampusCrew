package com.campuscrew.backend.config;

import org.springframework.beans.factory.annotation.Autowired; 
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

import com.campuscrew.backend.service.CustomOAuth2UserService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomOAuth2UserService customOAuth2UserService;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                .requestMatchers("/register", "/login", "/css/**", "/js/**", "/uploads/**").permitAll()
                .requestMatchers("/admin/**").hasAuthority("ROLE_SUPER_ADMIN")
                .requestMatchers(HttpMethod.POST, "/events/*/register", "/events/*/cancel").authenticated()
                .requestMatchers(HttpMethod.POST, "/events/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                .requestMatchers(HttpMethod.POST, "/clubs/create").hasAuthority("ROLE_SUPER_ADMIN")
                .requestMatchers("/clubs/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                .anyRequest().authenticated()
                )
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/login") 
                .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService) 
                )
                .defaultSuccessUrl("/", true) 
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }
}
