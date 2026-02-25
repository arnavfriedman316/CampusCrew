package com.campuscrew.backend.controller;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.UserRepository;

@Controller
@RequestMapping("/admin")
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    // 1. Show the Admin Dashboard
    @GetMapping
    public String adminDashboard(Model model) {
        // Grab EVERY user in the database
        List<AppUser> allUsers = userRepository.findAll();
        model.addAttribute("users", allUsers);
        return "admin"; // This will point to the admin.html file we build next!
    }

    // 2. Change a user's role
    @PostMapping("/update-role")
    public String updateUserRole(@RequestParam Long userId, @RequestParam String newRole, RedirectAttributes redirectAttributes) {
        AppUser user = userRepository.findById(userId).orElse(null);

        if (user != null) {
            user.setRole(newRole);
            userRepository.save(user);
            redirectAttributes.addFlashAttribute("success", "Successfully updated " + user.getFullName() + " to " + newRole + "! 🛡️");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }

        return "redirect:/admin";
    }
}
