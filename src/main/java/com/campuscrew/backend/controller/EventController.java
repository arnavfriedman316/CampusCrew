package com.campuscrew.backend.controller;

import java.security.Principal;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
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

    // 1. GET Method: Shows the events page (with Search functionality)
    @GetMapping("/events")
    public String showEventsPage(Model model, @RequestParam(value = "keyword", required = false) String keyword) {
        List<Events> events;

        if (keyword != null && !keyword.isEmpty()) {
            // Search by title or location (Requires the custom method in EventRepository)
            events = eventRepository.findByTitleContainingIgnoreCaseOrLocationContainingIgnoreCase(keyword, keyword);
        } else {
            // Show all events sorted by date
            events = eventRepository.findAllByOrderByDateTimeAsc();
        }

        model.addAttribute("events", events);
        model.addAttribute("keyword", keyword); // Keeps the search word in the box
        return "events";
    }

    // 2. POST Method: Saves a new event
    @PostMapping("/events")
    public String createEvent(@ModelAttribute Events event) {
        eventRepository.save(event);
        return "redirect:/events";
    }

    // 3. DELETE Method: Removes an event
    @GetMapping("/delete-event/{id}")
    public String deleteEvent(@PathVariable Long id) {
        eventRepository.deleteById(id);
        return "redirect:/events";
    }

    // 4. REGISTER Method: User joins an event (With duplicate check & flash messages)
    @GetMapping("/register-event/{id}")
    public String registerForEvent(@PathVariable Long id,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        // Find Logged-in User
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email);

        // Find Event clicked
        Events event = eventRepository.findById(id).orElse(null);

        if (event != null && user != null) {

            // Check if user is already in the attendees list
            boolean alreadyRegistered = false;
            for (AppUser attendee : event.getAttendees()) {
                if (attendee.getId().equals(user.getId())) {
                    alreadyRegistered = true;
                    break;
                }
            }

            if (alreadyRegistered) {
                // Reject duplicate
                redirectAttributes.addFlashAttribute("error", "You are already registered for this event! 🚫");
            } else {
                // Add and save
                event.getAttendees().add(user);
                eventRepository.save(event);
                redirectAttributes.addFlashAttribute("success", "Successfully registered! See you there. 🎉");
            }
        } else {
            redirectAttributes.addFlashAttribute("error", "Event or User not found!");
        }

        return "redirect:/events";
    }

    // 5. MY EVENTS: Show only events the logged-in user joined
    @GetMapping("/my-events")
    public String showMyEvents(Model model, Principal principal) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email);

        List<Events> myEvents = user.getEvents();

        model.addAttribute("events", myEvents);
        model.addAttribute("isMyEventsPage", true);
        return "my-events";
    }

    // 6. UNREGISTER: User leaves an event
    @GetMapping("/unregister/{id}")
    public String unregisterFromEvent(@PathVariable Long id, Principal principal) {
        String email = principal.getName();
        AppUser user = userRepository.findByEmail(email);
        Events event = eventRepository.findById(id).orElse(null);

        if (event != null && user != null) {
            // Remove the user from the event's attendee list
            event.getAttendees().remove(user);
            eventRepository.save(event);
        }

        return "redirect:/my-events";
    }
}
