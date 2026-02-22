package com.campuscrew.backend.controller;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.repository.EventRepository;
import com.campuscrew.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.List;
import java.util.UUID;

@Controller
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. VIEW ALL EVENTS & SEARCH
    // ==========================================
    @GetMapping("/events")
    public String listEvents(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Events> events;
        // If the user typed something in the search bar, filter it!
        if (keyword != null && !keyword.isEmpty()) {
            events = eventRepository.findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(keyword, keyword);
            model.addAttribute("keyword", keyword);
        } else {
            // Otherwise, show everything
            events = eventRepository.findAll();
        }
        model.addAttribute("events", events);
        return "events";
    }

    // ==========================================
    // 2. POST A NEW EVENT
    // ==========================================
    @PostMapping("/events")
    public String createEvent(@ModelAttribute Events event) {
        eventRepository.save(event);
        return "redirect:/events";
    }

    // ==========================================
    // 3. DELETE AN EVENT
    // ==========================================
    @GetMapping("/delete-event/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/events";
    }

    // ==========================================
    // 4. REGISTER FOR AN EVENT
    // ==========================================
    @GetMapping("/register-event/{id}")
    public String registerForEvent(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email);
        Events event = eventRepository.findById(id).orElse(null);

        if (event != null) {
            // Check if the user is ALREADY registered to prevent duplicates
            if (user.getEvents().contains(event)) {
                redirectAttributes.addFlashAttribute("error", "You are already registered for this event!");
            } else {
                // If not, add them to the list!
                user.getEvents().add(event);
                userRepository.save(user);
                redirectAttributes.addFlashAttribute("success", "Successfully registered for " + event.getTitle() + "!");
            }
        }
        return "redirect:/events";
    }

    // ==========================================
    // 5. VIEW MY TICKETS (REGISTERED EVENTS)
    // ==========================================
    @GetMapping("/my-events")
    public String myEvents(Model model, Principal principal) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email);

        // Only send the events that this specific user is attending
        model.addAttribute("events", user.getEvents());
        return "my-events";
    }

    // ==========================================
    // 6. VIEW PROFILE PAGE
    // ==========================================
    @GetMapping("/profile")
    public String showProfile(Model model, Principal principal) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email);
        model.addAttribute("user", user);
        return "profile";
    }

    // ==========================================
    // 7. EDIT PROFILE (Name, Bio, and Image Upload)
    // ==========================================
    @PostMapping("/edit-profile")
    public String saveProfileChanges(
            @RequestParam("fullName") String fullName,
            @RequestParam("bio") String bio,
            @RequestParam(value = "currentPassword", required = false) String currentPassword,
            @RequestParam(value = "photo", required = false) MultipartFile photo,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email);

        // A. Password Verification for Name Change
        if (!user.getFullName().equals(fullName)) {
            if (currentPassword == null || currentPassword.isEmpty() || !passwordEncoder.matches(currentPassword, user.getPassword())) {
                redirectAttributes.addFlashAttribute("error", "Incorrect password! Name change denied.");
                return "redirect:/profile";
            }
            user.setFullName(fullName);
        }

        // B. Update the Bio
        user.setBio(bio);

        // C. Save the Physical Profile Photo to the Hard Drive
        if (photo != null && !photo.isEmpty()) {
            try {
                // Define the folder where we want to save images
                String uploadDir = "uploads/profile_photos/";
                Path uploadPath = Paths.get(uploadDir);

                // If the folder doesn't exist yet, create it!
                if (!Files.exists(uploadPath)) {
                    Files.createDirectories(uploadPath);
                }

                // Generate a random unique name for the file so users don't overwrite each other's photos
                String uniqueFileName = UUID.randomUUID().toString() + "_" + photo.getOriginalFilename();
                Path filePath = uploadPath.resolve(uniqueFileName);

                // Copy the file from the browser to your computer's folder
                Files.copy(photo.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

                // Save ONLY the URL path in the database
                user.setProfilePhotoUrl("/" + uploadDir + uniqueFileName);

            } catch (IOException e) {
                redirectAttributes.addFlashAttribute("error", "Failed to save the image to the server.");
                return "redirect:/profile";
            }
        }

        // D. Save to DB and return Success
        userRepository.save(user);
        redirectAttributes.addFlashAttribute("success", "Profile updated successfully! ✨");
        return "redirect:/profile";
    }
}
