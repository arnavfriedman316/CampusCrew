package com.campuscrew.backend.controller;

import java.security.Principal;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.entity.Club;
import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.repository.ClubRepository;
import com.campuscrew.backend.repository.EventRepository;
import com.campuscrew.backend.repository.UserRepository;

@Controller
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    // 📅 LOAD ALL EVENTS & SEARCH
    @GetMapping("/events")
    public String listEvents(@RequestParam(value = "keyword", required = false) String keyword, Model model) {
        if (keyword != null && !keyword.isEmpty()) {
            // Searches by title (Requires findByTitleContainingIgnoreCase in EventRepository)
            model.addAttribute("events", eventRepository.findByTitleContainingIgnoreCase(keyword));
        } else {
            model.addAttribute("events", eventRepository.findAll());
        }

        model.addAttribute("keyword", keyword);
        model.addAttribute("clubs", clubRepository.findAll()); // Populates the "Host Organization" dropdown

        return "events";
    }

    // ✨ POST A NEW EVENT
    @PostMapping("/events")
    public String createEvent(@RequestParam Long clubId,
            @RequestParam String title,
            @RequestParam String dateTime,
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam(required = false) String posterUrl,
            RedirectAttributes redirectAttributes) {

        Club club = clubRepository.findById(clubId).orElse(null);

        if (club != null) {
            Events event = new Events();
            event.setClub(club);
            event.setTitle(title);
            event.setDateTime(LocalDateTime.parse(dateTime));
            event.setLocation(location);
            event.setDescription(description);
            event.setPosterUrl(posterUrl);

            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("success", "Event created successfully! 🎉");
        }
        return "redirect:/events";
    }

    @PostMapping("/events/{id}/register")
    public String registerForEvent(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        Events event = eventRepository.findById(id).orElse(null);
        AppUser user = userRepository.findByEmail(principal.getName());

        if (event != null && user != null) {
            if (!event.getAttendees().contains(user)) {
                event.getAttendees().add(user);
                eventRepository.save(event);
                redirectAttributes.addFlashAttribute("success", "Successfully registered for " + event.getTitle() + "! 🎟️");
            }
        }
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
            if (event.getAttendees().contains(user)) {
                event.getAttendees().remove(user);
                eventRepository.save(event);
                redirectAttributes.addFlashAttribute("success", "Registration cancelled for " + event.getTitle() + ".");
            }
        }
        return "redirect:/events";
    }
    @PostMapping("/events/{id}/edit")
    public String editEvent(@PathVariable Long id,
            @RequestParam String title,
            @RequestParam String dateTime,
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam(required = false) String posterUrl,
            RedirectAttributes redirectAttributes) {

        Events event = eventRepository.findById(id).orElse(null);
        if (event != null) {
            event.setTitle(title);
            event.setDateTime(LocalDateTime.parse(dateTime));
            event.setLocation(location);
            event.setDescription(description);
            event.setPosterUrl(posterUrl);
            eventRepository.save(event);

            redirectAttributes.addFlashAttribute("success", "Event updated successfully! ✏️");
        }
        return "redirect:/events";
    }
    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Events event = eventRepository.findById(id).orElse(null);
        if (event != null) {
            eventRepository.delete(event);
            redirectAttributes.addFlashAttribute("success", "Event deleted successfully! 🗑️");
        }
        return "redirect:/events";
    }

    @GetMapping("/my-events")
    public String myEvents(Principal principal, Model model) {
        if (principal == null) {
            return "redirect:/login"; 
        }

        AppUser user = userRepository.findByEmail(principal.getName());

        if (user != null) {
            // fetching only the events this specific user registered for
            model.addAttribute("events", eventRepository.findByAttendeesContaining(user));
        }

        return "my-events";
    }
}
