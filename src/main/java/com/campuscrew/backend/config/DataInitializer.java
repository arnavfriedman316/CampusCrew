package com.campuscrew.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.UserRepository;

@Configuration
public class DataInitializer {

    @org.springframework.beans.factory.annotation.Value("${app.admin.email:admin316@campuscrew.com}")
    private String adminEmail;

    @org.springframework.beans.factory.annotation.Value("${app.admin.password:admin@316}")
    private String adminPassword;

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // checks if our Master Admin exists by looking for the email
            if (userRepository.findByEmail(adminEmail) == null) {
                AppUser admin = new AppUser();
                admin.setFullName("Master Admin");
                admin.setEmail(adminEmail);
                admin.setPassword(passwordEncoder.encode(adminPassword));
                admin.setRole("ROLE_SUPER_ADMIN");
                admin.setBio("I am the creator of this universe.");

                userRepository.save(admin);
                System.out.println("Master Admin account created successfully!");
            } else {
                System.out.println("Master Admin already exists. Booting up...");
            }
        };
    }
}
