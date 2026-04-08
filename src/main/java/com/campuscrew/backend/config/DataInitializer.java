package com.campuscrew.backend.config;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.UserRepository;

@Configuration
public class DataInitializer {

    @Bean
    public CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // checks if our Master Admin exists by looking for the email
            if (userRepository.findByEmail("admin316@campuscrew.com") == null) {
                AppUser admin = new AppUser();
                admin.setFullName("Master Admin");
                admin.setEmail("admin316@campuscrew.com");
                //default password for the admin account is "admin123"
                admin.setPassword(passwordEncoder.encode("admin@316"));
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
