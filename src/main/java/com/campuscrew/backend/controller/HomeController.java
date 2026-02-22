package com.campuscrew.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.UserRepository;

@Controller
public class HomeController {

    @Autowired
    private UserRepository userRepository;  //this will access the userRepository

    @Autowired
    private PasswordEncoder passwordEncoder; //password endcoding part

    @GetMapping("/")
    public String home() { //this is the first thing that was made while making this project
        return "index";
    }

    @GetMapping("/login")
    public String showLoginPage() { //this is to request the login page
        return "login";
    }

    @GetMapping("/register")
    public String showRegisterPage() { //this will request register page
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @RequestParam String fullName,
            @RequestParam String email, //this will allow data entering into the database. when a user registers this is what will allow it to register
            @RequestParam String password) {

        AppUser newUser = new AppUser();
        newUser.setFullName(fullName);
        newUser.setEmail(email);

        //the following is going to encrypt the user's password
        newUser.setPassword(passwordEncoder.encode(password));

        userRepository.save(newUser);

        return "redirect:/login";
    }
}
