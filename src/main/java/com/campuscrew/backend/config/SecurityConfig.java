package com.campuscrew.backend.config;

import org.springframework.beans.factory.annotation.Autowired; // Make sure to import the new file!
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
                .requestMatchers("/admin/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                .requestMatchers(HttpMethod.POST, "/events/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                // 🏛️ NEW CLUB RULES: Super Admin creates, both can view/edit
                .requestMatchers(HttpMethod.POST, "/clubs/create").hasAuthority("ROLE_SUPER_ADMIN")
                .requestMatchers("/clubs/**").hasAnyAuthority("ROLE_SUPER_ADMIN", "ROLE_PRESIDENT")
                .anyRequest().authenticated()
                )
                // 📧 TRADITIONAL EMAIL/PASSWORD LOGIN
                .formLogin(form -> form
                .loginPage("/login")
                .defaultSuccessUrl("/", true)
                .permitAll()
                )
                // 🌐 NEW GOOGLE OAUTH2 LOGIN
                .oauth2Login(oauth2 -> oauth2
                .loginPage("/login") // Use the exact same login page UI
                .userInfoEndpoint(userInfo -> userInfo
                .userService(customOAuth2UserService) // Intercept Google data to save to Neon!
                )
                .defaultSuccessUrl("/", true) // Send to dashboard on success
                )
                .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout")
                .permitAll()
                );

        return http.build();
    }
}
