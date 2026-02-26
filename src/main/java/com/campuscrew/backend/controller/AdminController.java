package com.campuscrew.backend.controller;

import java.security.Principal;
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
    public String updateUserRole(@RequestParam Long userId, @RequestParam String newRole, Principal principal, RedirectAttributes redirectAttributes) {
        AppUser targetUser = userRepository.findById(userId).orElse(null);
        AppUser loggedInUser = userRepository.findByEmail(principal.getName());

        if (targetUser != null && loggedInUser != null) {

            // 🛑 SECURITY RULE 1: Presidents cannot edit an existing Super Admin
            if (loggedInUser.getRole().equals("ROLE_PRESIDENT") && "ROLE_SUPER_ADMIN".equals(targetUser.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: You cannot modify a Super Admin's account. 🛑");
                return "redirect:/admin";
            }

            // 🛑 SECURITY RULE 2: Presidents cannot promote someone TO Super Admin
            if (loggedInUser.getRole().equals("ROLE_PRESIDENT") && "ROLE_SUPER_ADMIN".equals(newRole)) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: Only a Super Admin can forge another Super Admin. 🛑");
                return "redirect:/admin";
            }

            // If they pass the security checks, update the role!
            targetUser.setRole(newRole);
            userRepository.save(targetUser);
            redirectAttributes.addFlashAttribute("success", "Successfully updated " + targetUser.getFullName() + " to " + newRole + "! 🛡️");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found!");
        }

        return "redirect:/admin";
    }

}
