package com.campuscrew.backend.controller;

import java.security.Principal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.repository.ClubRepository;
import com.campuscrew.backend.repository.UserRepository;

@Controller
public class AdminController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @org.springframework.beans.factory.annotation.Value("${app.admin.email:admin316@campuscrew.com}")
    private String SUPREME_ADMIN_EMAIL;
    @GetMapping("/admin")
    public String adminDashboard(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            model.addAttribute("users", userRepository.findByFullNameContainingIgnoreCaseOrEmailContainingIgnoreCase(keyword, keyword));
        } else {
            model.addAttribute("users", userRepository.findAll());
        }
        model.addAttribute("keyword", keyword);
        model.addAttribute("clubs", clubRepository.findAll());
        return "admin";
    }
    @PostMapping("/admin/update-role")
    public String updateUserRole(@RequestParam Long userId, @RequestParam String newRole, @RequestParam(required = false) Long managedClubId, Principal principal, RedirectAttributes redirectAttributes) {
        AppUser targetUser = userRepository.findById(userId).orElse(null);
        AppUser loggedInUser = userRepository.findByEmail(principal.getName());

        if (targetUser != null && loggedInUser != null) {

            if (targetUser.getEmail().equals(SUPREME_ADMIN_EMAIL)) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: The Supreme Admin account cannot be modified. 🛑");
                return "redirect:/admin";
            }
            if ("ROLE_SUPER_ADMIN".equals(targetUser.getRole()) && !loggedInUser.getEmail().equals(SUPREME_ADMIN_EMAIL)) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: Only the Supreme Admin can modify other Super Admins. 🛑");
                return "redirect:/admin";
            }

            if (loggedInUser.getRole().equals("ROLE_PRESIDENT") && "ROLE_SUPER_ADMIN".equals(newRole)) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: Only an Admin can forge another Super Admin. 🛑");
                return "redirect:/admin";
            }

            if (managedClubId != null) {
                targetUser.setManagedClub(clubRepository.findById(managedClubId).orElse(null));
            } else {
                targetUser.setManagedClub(null);
            }

            targetUser.setRole(newRole);
            userRepository.save(targetUser);

            redirectAttributes.addFlashAttribute("success", "Successfully updated " + targetUser.getFullName() + "'s role and club! 🛡️");
        }

        return "redirect:/admin";
    }

    @PostMapping("/admin/users/{id}/delete")
    public String deleteUser(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {

        AppUser targetUser = userRepository.findById(id).orElse(null);
        String loggedInEmail = principal.getName();

        if (targetUser != null) {

            if (targetUser.getEmail().equals(SUPREME_ADMIN_EMAIL)) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: The Supreme Admin cannot be deleted. 🛑");
                return "redirect:/admin";
            }

            if ("ROLE_SUPER_ADMIN".equals(targetUser.getRole()) && !loggedInEmail.equals(SUPREME_ADMIN_EMAIL)) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: Only the Supreme Admin can delete other Super Admins. 🛑");
                return "redirect:/admin";
            }

            if (targetUser.getEvents() != null) {
                for (Events event : targetUser.getEvents()) {
                    event.getAttendees().remove(targetUser);
                }
            }

            userRepository.delete(targetUser);
            redirectAttributes.addFlashAttribute("success", "User successfully deleted.");
        } else {
            redirectAttributes.addFlashAttribute("error", "User not found.");
        }

        return "redirect:/admin";
    }
}
