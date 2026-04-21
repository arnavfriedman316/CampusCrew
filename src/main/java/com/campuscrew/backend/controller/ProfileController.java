package com.campuscrew.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

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
    @GetMapping("/profile/edit")
    public String redirectEditToProfile() {
        return "redirect:/profile";
    }
    @PostMapping("/profile/edit")
    public String saveProfileChanges(
            Principal principal,
            @RequestParam String fullName,
            @RequestParam(required = false) String bio,
            @RequestParam(value = "profileImage", required = false) MultipartFile profileImage,
            RedirectAttributes redirectAttributes) {
        AppUser user = userRepository.findByEmail(principal.getName());

        if (user != null) {
            user.setFullName(fullName);
            if (bio != null) {
                user.setBio(bio);
            }
            try {
                if (profileImage != null && !profileImage.isEmpty()) {
                    String contentType = profileImage.getContentType();
                    if (contentType == null || !(contentType.equals("image/jpeg") || contentType.equals("image/png") || contentType.equals("image/webp") || contentType.equals("image/gif"))) {
                        redirectAttributes.addFlashAttribute("error", "Security Alert: Invalid file type. Only JPG, PNG, WEBP, and GIF are allowed.");
                        return "redirect:/profile";
                    }
                    user.setProfilePhotoData(profileImage.getBytes());
                    user.setProfilePhotoType(contentType);
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failure mapping image chunk bytes to memory.");
                return "redirect:/profile";
            }
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Profile configuration synchronized! ✨");
        }
        return "redirect:/profile";
    }

    @GetMapping("/users/{id}/avatar")
    public ResponseEntity<byte[]> getUserAvatar(@PathVariable Long id) {
        AppUser user = userRepository.findById(id).orElse(null);
        if (user != null && user.getProfilePhotoData() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(user.getProfilePhotoType()));
            return new ResponseEntity<>(user.getProfilePhotoData(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
}
