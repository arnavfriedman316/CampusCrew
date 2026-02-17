package com.campuscrew.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.repository.EventRepository;

@Controller
public class EventController {

    @Autowired
    private com.campuscrew.backend.repository.UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    // 1. GET Method: Shows the page
    @GetMapping("/events")
    public String showEventsPage(Model model) {
        // Fetch existing events to display
        model.addAttribute("events", eventRepository.findAllByOrderByDateTimeAsc());
        return "events";
    }

    // 2. POST Method: Saves the data
    // (Make sure this is separate from the method above!)
    @PostMapping("/events")
    public String createEvent(@ModelAttribute Events event) {
        eventRepository.save(event);
        return "redirect:/events";
    }

    @GetMapping("/delete-event/{id}")
    public String deleteEvent(@org.springframework.web.bind.annotation.PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/events";
    }

    @GetMapping("/register-event/{id}")
    public String registerForEvent(@org.springframework.web.bind.annotation.PathVariable Long id, java.security.Principal principal) {
        String email = principal.getName();
        com.campuscrew.backend.entity.AppUser user = userRepository.findByEmail(email);
        Events event = eventRepository.findById(id).orElse(null);
        if (event != null && user != null) {
            event.getAttendees().add(user);
            eventRepository.save(event);
        }
        return "redirect:/events";
    }

}
