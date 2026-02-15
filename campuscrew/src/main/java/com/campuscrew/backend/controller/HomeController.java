package com.campuscrew.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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

    @GetMapping("/profile")
    public String showProfilePage(Model model, java.security.Principal principal) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email); //this will request the userprofile page
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/edit-profile")
    public String showEditProfilePage(org.springframework.ui.Model model, java.security.Principal principal) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email); //this will request for the edit profile page when cliciked
        model.addAttribute("user", user);
        return "edit-profile";
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

    @PostMapping("edit-profile")
    public String saveProfileChanges(@RequestParam String fullName, @RequestParam String bio, java.security.Principal principal) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email); //this will allow usee to enter the changes he/she wants to do in their profile.
        user.setFullName(fullName);
        user.setBio(bio);
        userRepository.save(user);
        return "redirect:/profile";
    }
}
