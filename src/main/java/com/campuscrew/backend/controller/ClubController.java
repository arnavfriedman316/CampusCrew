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

import com.campuscrew.backend.entity.Club;
import com.campuscrew.backend.repository.ClubRepository;

@Controller
@RequestMapping("/clubs")
public class ClubController {

    @Autowired
    private ClubRepository clubRepository;

    // View the Club Management Page
    @GetMapping
    public String viewClubs(Model model) {
        model.addAttribute("clubs", clubRepository.findAll());
        return "clubs";
    }

    // SUPER ADMIN ONLY: Create a new Club
    @PostMapping("/create")
    public String createClub(@RequestParam String name, @RequestParam String description, @RequestParam String themeColor, RedirectAttributes redirectAttributes) {
        try {
            Club club = new Club();
            club.setName(name);
            club.setDescription(description);
            club.setThemeColor(themeColor);
            clubRepository.save(club);
            redirectAttributes.addFlashAttribute("success", "Club '" + name + "' forged successfully! 🏛️");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to create club. That name might already exist!");
        }
        return "redirect:/clubs";
    }

    // PRESIDENTS & SUPER ADMINS: Edit an existing Club
    @PostMapping("/edit/{id}")
    public String editClub(@PathVariable Long id, @RequestParam String description, @RequestParam String themeColor, RedirectAttributes redirectAttributes) {
        Club club = clubRepository.findById(id).orElse(null);
        if (club != null) {
            club.setDescription(description);
            club.setThemeColor(themeColor);
            clubRepository.save(club);
            redirectAttributes.addFlashAttribute("success", "Club '" + club.getName() + "' updated successfully! 🎨");
        }
        return "redirect:/clubs";
    }
}
