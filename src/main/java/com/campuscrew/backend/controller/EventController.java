package com.campuscrew.backend.controller;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Principal;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.repository.EventRepository;
import com.campuscrew.backend.repository.UserRepository;

@Controller
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private com.campuscrew.backend.repository.ClubRepository clubRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ==========================================
    // 1. VIEW ALL EVENTS & SEARCH
    // ==========================================
    @GetMapping("/events")
    public String listEvents(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        if (keyword != null) {
            model.addAttribute("events", eventRepository.findByTitleContainingIgnoreCase(keyword));
        } else {
            model.addAttribute("events", eventRepository.findAll());
        }
        model.addAttribute("keyword", keyword);

        // 🌟 NEW LINE: Send the clubs to the frontend dropdown
        model.addAttribute("clubs", clubRepository.findAll());

        return "events";
    }

    // ==========================================
    // 2. POST A NEW EVENT
    // ==========================================
    @PostMapping("/events")
    public String createEvent(@RequestParam String title, @RequestParam String dateTime, @RequestParam String location, @RequestParam String description, @RequestParam Long clubId, RedirectAttributes redirectAttributes) {
        Events event = new Events();
        event.setTitle(title);

        // Spring handles converting the HTML datetime string into a LocalDateTime
        event.setDateTime(java.time.LocalDateTime.parse(dateTime));
        event.setLocation(location);
        event.setDescription(description);

        // 🌟 FIND THE CLUB AND ATTACH IT TO THE EVENT!
        com.campuscrew.backend.entity.Club hostingClub = clubRepository.findById(clubId).orElse(null);
        event.setClub(hostingClub);

        eventRepository.save(event);
        redirectAttributes.addFlashAttribute("success", "Event published successfully! 🎉");
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

    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        // 1. Safety check: Is the user logged in?
        if (principal == null) {
            return "redirect:/login";
        }
        // 2. Find the event they clicked on
        Events event = eventRepository.findById(id).orElse(null);
        if (event == null) {
            redirectAttributes.addFlashAttribute("error", "Oops! That event doesn't exist anymore.");
            return "redirect:/events";
        }

        // 3. Find the user who clicked the button
        AppUser user = userRepository.findByEmail(principal.getName());

        // 4. Check if they are ALREADY registered to prevent duplicates!
        if (event.getAttendees().contains(user)) {
            redirectAttributes.addFlashAttribute("error", "You are already registered for " + event.getTitle() + "! 😉");
            return "redirect:/events";
        }

        // 5. Add them to the list and save to the database
        event.addAttendee(user);
        eventRepository.save(event);

        redirectAttributes.addFlashAttribute("success", "Successfully registered for " + event.getTitle() + "! 🎉 Check 'My Tickets'!");
        return "redirect:/events";
    }

    @PostMapping("/events/{id}/cancel")
    public String cancelRegistration(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Events event = eventRepository.findById(id).orElse(null);
        AppUser user = userRepository.findByEmail(principal.getName());

        if (event != null && user != null) {
            // Safely look through the attendees and remove the one that matches this user's ID
            boolean removed = event.getAttendees().removeIf(attendee -> attendee.getId().equals(user.getId()));

            if (removed) {
                eventRepository.save(event);
                redirectAttributes.addFlashAttribute("success", "Registration canceled for " + event.getTitle() + ". We'll catch you at the next one! 👋");
            } else {
                redirectAttributes.addFlashAttribute("error", "You weren't registered for this event.");
            }
        }

        return "redirect:/events";
    }
}
