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
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.entity.Club;
import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.entity.TeamMember;
import com.campuscrew.backend.entity.TeamRegistration;
import com.campuscrew.backend.repository.ClubRepository;
import com.campuscrew.backend.repository.EventRepository;
import com.campuscrew.backend.repository.TeamRegistrationRepository;
import com.campuscrew.backend.repository.UserRepository;

@Controller
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClubRepository clubRepository;

    @Autowired
    private TeamRegistrationRepository teamRegistrationRepository;

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

    @GetMapping("/events/{id}/poster")
    public ResponseEntity<byte[]> getEventPoster(@PathVariable Long id) {
        Events event = eventRepository.findById(id).orElse(null);
        if (event != null && event.getPosterData() != null) {
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.parseMediaType(event.getPosterType()));
            return new ResponseEntity<>(event.getPosterData(), headers, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // ✨ POST A NEW EVENT
    @PostMapping("/events")
    public String createEvent(@RequestParam(required = false) Long clubId,
            @RequestParam String title,
            @RequestParam String dateTime,
            @RequestParam String location,
            @RequestParam String description,
            @RequestParam(required = false) Boolean isTeamEvent,
            @RequestParam(required = false) Integer minTeamSize,
            @RequestParam(required = false) Integer maxTeamSize,
            @RequestParam(required = false) String posterUrl,
            @RequestParam(value = "posterImage", required = false) MultipartFile posterImage,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        AppUser user = userRepository.findByEmail(principal.getName());
        Long assignedClubId = clubId;

        if (!"ROLE_SUPER_ADMIN".equals(user.getRole())) {
            if (user.getManagedClub() == null) {
                redirectAttributes.addFlashAttribute("error", "Access Denied: You do not manage any clubs!");
                return "redirect:/events";
            }
            assignedClubId = user.getManagedClub().getId();
        }

        Club club = assignedClubId != null ? clubRepository.findById(assignedClubId).orElse(null) : null;

        if (club != null) {
            Events event = new Events();
            event.setClub(club);
            event.setTitle(title);
            event.setDateTime(LocalDateTime.parse(dateTime));
            event.setLocation(location);
            event.setDescription(description);
            event.setIsTeamEvent(isTeamEvent != null ? isTeamEvent : false);
            event.setMinTeamSize(minTeamSize);
            event.setMaxTeamSize(maxTeamSize);
            event.setPosterUrl(posterUrl);
            
            try {
                if (posterImage != null && !posterImage.isEmpty()) {
                    event.setPosterData(posterImage.getBytes());
                    event.setPosterType(posterImage.getContentType());
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload poster image!");
                return "redirect:/events";
            }

            eventRepository.save(event);
            redirectAttributes.addFlashAttribute("success", "Event created successfully! 🎉");
        } else {
            redirectAttributes.addFlashAttribute("error", "Error: Club not found or missing ID.");
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

    @PostMapping("/events/{id}/register-team")
    public String registerTeamForEvent(@PathVariable Long id, 
            @RequestParam String teamName,
            @RequestParam(value="memberName[]", required=false) String[] memberNames,
            @RequestParam(value="memberPhone[]", required=false) String[] memberPhones,
            @RequestParam(value="memberCampus[]", required=false) String[] memberCampuses,
            @RequestParam(value="memberEmail[]", required=false) String[] memberEmails,
            Principal principal, RedirectAttributes redirectAttributes) {
        
        if (principal == null) {
            return "redirect:/login";
        }

        Events event = eventRepository.findById(id).orElse(null);
        AppUser leader = userRepository.findByEmail(principal.getName());

        if (event != null && leader != null && Boolean.TRUE.equals(event.getIsTeamEvent())) {
            
            if (!event.getAttendees().contains(leader)) {
                
                TeamRegistration team = new TeamRegistration();
                team.setEvent(event);
                team.setLeader(leader);
                team.setTeamName(teamName);
                
                if (memberNames != null) {
                    for (int i = 0; i < memberNames.length; i++) {
                        if (!memberNames[i].trim().isEmpty()) {
                            TeamMember member = new TeamMember();
                            member.setName(memberNames[i]);
                            member.setPhoneNumber(memberPhones != null && i < memberPhones.length ? memberPhones[i] : "");
                            member.setCampus(memberCampuses != null && i < memberCampuses.length ? memberCampuses[i] : "");
                            member.setEmail(memberEmails != null && i < memberEmails.length ? memberEmails[i] : "");
                            team.addMember(member);
                        }
                    }
                }
                
                teamRegistrationRepository.save(team);

                event.getAttendees().add(leader);
                eventRepository.save(event);
                redirectAttributes.addFlashAttribute("success", "Successfully registered Team " + teamName + " for " + event.getTitle() + "! 🎟️");
            } else {
                redirectAttributes.addFlashAttribute("error", "You have already registered for this event!");
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
                
                TeamRegistration team = teamRegistrationRepository.findByEventAndLeader(event, user);
                if (team != null) {
                    teamRegistrationRepository.delete(team);
                }

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
            @RequestParam(required = false) Boolean isTeamEvent,
            @RequestParam(required = false) Integer minTeamSize,
            @RequestParam(required = false) Integer maxTeamSize,
            @RequestParam(required = false) String posterUrl,
            @RequestParam(value = "posterImage", required = false) MultipartFile posterImage,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        Events event = eventRepository.findById(id).orElse(null);
        AppUser user = userRepository.findByEmail(principal.getName());

        if (event != null && user != null) {
             if (!"ROLE_SUPER_ADMIN".equals(user.getRole())) {
                 if (user.getManagedClub() == null || !event.getClub().getId().equals(user.getManagedClub().getId())) {
                     redirectAttributes.addFlashAttribute("error", "Access Denied: You cannot modify this event! 🛑");
                     return "redirect:/events";
                 }
             }

            event.setTitle(title);
            event.setDateTime(LocalDateTime.parse(dateTime));
            event.setLocation(location);
            event.setDescription(description);
            event.setIsTeamEvent(isTeamEvent != null ? isTeamEvent : false);
            event.setMinTeamSize(minTeamSize);
            event.setMaxTeamSize(maxTeamSize);
            event.setPosterUrl(posterUrl);
            
            try {
                if (posterImage != null && !posterImage.isEmpty()) {
                    event.setPosterData(posterImage.getBytes());
                    event.setPosterType(posterImage.getContentType());
                }
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("error", "Failed to upload the new poster image!");
                return "redirect:/events";
            }
            
            eventRepository.save(event);

            redirectAttributes.addFlashAttribute("success", "Event updated successfully! ✏️");
        }
        return "redirect:/events";
    }
    @PostMapping("/events/{id}/delete")
    public String deleteEvent(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        Events event = eventRepository.findById(id).orElse(null);
        AppUser user = userRepository.findByEmail(principal.getName());

        if (event != null && user != null) {
             if (!"ROLE_SUPER_ADMIN".equals(user.getRole())) {
                 if (user.getManagedClub() == null || !event.getClub().getId().equals(user.getManagedClub().getId())) {
                     redirectAttributes.addFlashAttribute("error", "Access Denied: You cannot delete this event! 🛑");
                     return "redirect:/events";
                 }
             }

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
