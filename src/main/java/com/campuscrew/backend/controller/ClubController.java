package com.campuscrew.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;
import java.security.Principal;

import com.campuscrew.backend.entity.Club;
import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.repository.ClubRepository;
import com.campuscrew.backend.repository.UserRepository;

@Controller
@RequestMapping("/clubs")
public class ClubController {

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private UserRepository userRepository;

    // View the Club Management Page
    @GetMapping
    public String viewClubs(Model model, Principal principal) {
        if (principal != null) {
            AppUser user = userRepository.findByEmail(principal.getName());
            model.addAttribute("user", user);
        }
        model.addAttribute("clubs", clubRepository.findAll());
        return "clubs";
    }
    @PostMapping("/create")
    public String createClub(@RequestParam String name, @RequestParam String description, @RequestParam String themeColor, @RequestParam(required = false) String roleTagColor, @RequestParam(value = "logoImage", required = false) MultipartFile logoImage, RedirectAttributes redirectAttributes) {
        try {
            Club club = new Club();
            club.setName(name);
            club.setDescription(description);
            club.setThemeColor(themeColor);
            
            if (roleTagColor != null && !roleTagColor.isEmpty()) {
                club.setRoleTagColor(roleTagColor);
            } else {
                club.setRoleTagColor("#e2e8f0");
            }
            
            if (logoImage != null && !logoImage.isEmpty()) {
                club.setLogoData(logoImage.getBytes());
                club.setLogoType(logoImage.getContentType());
            }

            clubRepository.save(club);
            redirectAttributes.addFlashAttribute("success", "Club '" + name + "' forged successfully! 🏛️");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create club. That name might already exist!");
        }
        return "redirect:/clubs";
    }

    @GetMapping("/{id}/logo")
    public ResponseEntity<byte[]> getClubLogo(@PathVariable Long id) {
        Club club = clubRepository.findById(id).orElse(null);
        if (club != null && club.getLogoData() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(club.getLogoType()));
            return new ResponseEntity<>(club.getLogoData(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }
    @PostMapping("/edit/{id}")
    public String editClub(@PathVariable Long id, @RequestParam String description, @RequestParam String themeColor, @RequestParam(required = false) String roleTagColor, @RequestParam(value = "logoImage", required = false) MultipartFile logoImage, Principal principal, RedirectAttributes redirectAttributes) {
        Club club = clubRepository.findById(id).orElse(null);
        AppUser user = userRepository.findByEmail(principal.getName());
        
        if (club != null && user != null) {
             if (!"ROLE_SUPER_ADMIN".equals(user.getRole())) {
                 if (user.getManagedClub() == null || !club.getId().equals(user.getManagedClub().getId())) {
                     redirectAttributes.addFlashAttribute("error", "Access Denied: You cannot modify this club! 🛑");
                     return "redirect:/clubs";
                 }
             }

            club.setDescription(description);
            club.setThemeColor(themeColor);
            if (roleTagColor != null && !roleTagColor.isEmpty()) {
                club.setRoleTagColor(roleTagColor);
            }
            
            try {
                if (logoImage != null && !logoImage.isEmpty()) {
                    club.setLogoData(logoImage.getBytes());
                    club.setLogoType(logoImage.getContentType());
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload the new logo!");
                return "redirect:/clubs";
            }
            
            clubRepository.save(club);
            redirectAttributes.addFlashAttribute("success", "Club '" + club.getName() + "' updated successfully! 🎨");
        }
        return "redirect:/clubs";
    }

    @PostMapping("/{id}/delete")
    public String deleteClub(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Club club = clubRepository.findById(id).orElse(null);
        AppUser user = userRepository.findByEmail(principal.getName());
        
        if (club != null && user != null) {
             if (!"ROLE_SUPER_ADMIN".equals(user.getRole())) {
                 redirectAttributes.addFlashAttribute("error", "Access Denied: Only Super Admins can delete clubs! 🛑");
                 return "redirect:/clubs";
             }

            clubRepository.delete(club);
            redirectAttributes.addFlashAttribute("success", "Club deleted successfully! 🗑️");
        }
        return "redirect:/clubs";
    }

    @PostMapping("/{clubId}/update-role")
    public String updateRoleFromRoster(@PathVariable Long clubId, @RequestParam Long userId, @RequestParam String newRole, Principal principal, RedirectAttributes redirectAttributes) {
        AppUser loggedInUser = userRepository.findByEmail(principal.getName());
        AppUser targetUser = userRepository.findById(userId).orElse(null);
        Club club = clubRepository.findById(clubId).orElse(null);

        if (loggedInUser != null && targetUser != null && club != null) {
            
            if (!"ROLE_SUPER_ADMIN".equals(loggedInUser.getRole())) {
                if (loggedInUser.getManagedClub() == null || !loggedInUser.getManagedClub().getId().equals(club.getId())) {
                    redirectAttributes.addFlashAttribute("error", "Access Denied: You cannot manage members of another club! 🛑");
                    return "redirect:/clubs";
                }
            }
            
            if ("ROLE_SUPER_ADMIN".equals(targetUser.getRole())) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: Super Admin cannot be modified from the Roster! 🛑");
                return "redirect:/clubs";
            }
            
            if (!"ROLE_SUPER_ADMIN".equals(loggedInUser.getRole())) {
                if ("ROLE_SUPER_ADMIN".equals(newRole) || "ROLE_PRESIDENT".equals(newRole)) {
                     redirectAttributes.addFlashAttribute("error", "Access Denied: You can only promote members to Core Member. 🛑");
                     return "redirect:/clubs";
                }
            }
            
            targetUser.setRole(newRole);
            userRepository.save(targetUser);
            redirectAttributes.addFlashAttribute("success", "Successfully updated " + targetUser.getFullName() + "'s rank! 🎖️");
        }
        return "redirect:/clubs";
    }
}
