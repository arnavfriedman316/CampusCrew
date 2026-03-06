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
import com.campuscrew.backend.repository.ClubRepository;
import com.campuscrew.backend.repository.UserRepository;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    // Load the Admin Dashboard
    // Load the Admin Dashboard (Now with Search!)
    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(value = "keyword", required = false) String keyword, Model model) {

        // If the admin typed something in the search bar, filter the users
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("users", userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword));
        } else {
            // Otherwise, show everyone
            model.addAttribute("users", userRepository.findAll());
        }

        model.addAttribute("keyword", keyword); // Saves their search term in the box
        model.addAttribute("clubs", clubRepository.findAll());

        return "admin";
    }

    // Handle the Role & Club Update Form
    @PostMapping("/admin/update-role")
    public String updateUserRole(@RequestParam Long userId, @RequestParam String newRole, @RequestParam(required = false) Long managedClubId, Principal principal, RedirectAttributes redirectAttributes) {
        AppUser targetUser = userRepository.findById(userId).orElse(null);
        AppUser loggedInUser = userRepository.findByEmail(principal.getName());

        if (targetUser != null && loggedInUser != null) {

            // SECURITY: Presidents cannot edit Super Admins
            if (loggedInUser.getRole().equals("ROLE_PRESIDENT") && "ROLE_SUPER_ADMIN".equals(targetUser.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: You cannot modify a Super Admin's account. 🛑");
                return "redirect:/admin";
            }
            // SECURITY: Presidents cannot create Super Admins
            if (loggedInUser.getRole().equals("ROLE_PRESIDENT") && "ROLE_SUPER_ADMIN".equals(newRole)) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: Only a Super Admin can forge another Super Admin. 🛑");
                return "redirect:/admin";
            }

            // --- THE NEW MAGIC: Assign the Club ---
            if (managedClubId != null) {
                targetUser.setManagedClub(clubRepository.findById(managedClubId).orElse(null));
            } else {
                targetUser.setManagedClub(null); // Clear it if "No Club" is selected
            }

            // Update the role
            targetUser.setRole(newRole);
            userRepository.save(targetUser);

            redirectAttributes.addFlashAttribute("success", "Successfully updated " + targetUser.getFullName() + "'s role and club! 🛡️");
        }

        return "redirect:/admin";
    }
}
