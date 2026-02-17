package com.campuscrew.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.UserRepository;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Find user by email
        AppUser appUser = userRepository.findByEmail(email);
        
        if (appUser == null) {
            throw new UsernameNotFoundException("User not found");
        }

        // Return the user details to Spring Security
        return User.builder()
                .username(appUser.getEmail())
                .password(appUser.getPassword()) 
                .roles(appUser.getRole())
                .build();
    }
}