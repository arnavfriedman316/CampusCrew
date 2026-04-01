package com.campuscrew.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.UserRepository;

@Controller
public class ProfileController {

    @Autowired
    private UserRepository userRepository;
    @GetMapping("/profile")
    public String viewProfile(Principal principal, Model model) {
        // finds who is currently logged in
        AppUser user = userRepository.findByEmail(principal.getName());
        if (user == null) {
            return "redirect:/login";
        }
        // hand their data to the profile.html page
        model.addAttribute("user", user);
        return "profile";
    }

    //this allows user to edit their profiles
    @GetMapping("/profile/edit")
    public String editProfilePage(Principal principal, Model model) {
        AppUser user = userRepository.findByEmail(principal.getName());

        if (user == null) {
            return "redirect:/login";
        }

        model.addAttribute("user", user);
        return "edit-profile"; 
    }

    @PostMapping("/profile/edit")
    public String saveProfileChanges(
            Principal principal,
            @RequestParam String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(required = false) String profilePhotoUrl,
            RedirectAttributes redirectAttributes) {

        AppUser user = userRepository.findByEmail(principal.getName());

        if (user != null) {
            // Update their details
            user.setFullName(fullName);

            if (bio != null) {
                user.setBio(bio);
            }
            if (profilePhotoUrl != null && !profilePhotoUrl.isEmpty()) {
                user.setProfilePhotoUrl(profilePhotoUrl);
            }

            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile updated successfully! ✨");
        }
        return "redirect:/profile";
    }
}
