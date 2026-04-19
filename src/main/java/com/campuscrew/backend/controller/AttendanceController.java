package com.campuscrew.backend.controller;

import java.io.PrintWriter;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.campuscrew.backend.entity.AppUser;
import com.campuscrew.backend.entity.Attendance;
import com.campuscrew.backend.entity.Events;
import com.campuscrew.backend.repository.AttendanceRepository;
import com.campuscrew.backend.repository.EventRepository;
import com.campuscrew.backend.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;

@Controller
public class AttendanceController {

    @Autowired
    private AttendanceRepository attendanceRepository;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    // 📱 1. THE QR SCANNER ENDPOINT
    @GetMapping("/events/{id}/checkin")
    public String checkIn(@PathVariable Long id, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login"; // Must be logged in to scan!
        }

        Events event = eventRepository.findById(id).orElse(null);
        AppUser user = userRepository.findByEmail(principal.getName());

        if (event != null && user != null) {
            if (attendanceRepository.existsByEventIdAndUserId(id, user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You have already checked in for " + event.getTitle() + "! ✅");
            } else {
                Attendance att = new Attendance();
                att.setEvent(event);
                att.setUser(user);
                att.setCheckInTime(LocalDateTime.now());
                attendanceRepository.save(att);

                redirectAttributes.addFlashAttribute("success", "Attendance marked for " + event.getTitle() + "! Welcome. ");
            }
        }
        return "redirect:/events";
    }
    @GetMapping("/events/{id}/attendance/export")
    public void exportAttendanceToCSV(@PathVariable Long id, HttpServletResponse response) {
        Events event = eventRepository.findById(id).orElse(null);

        if (event == null) {
            return;
        }
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_" + event.getTitle().replaceAll("\\s+", "_") + ".csv\"");

        try (PrintWriter writer = response.getWriter()) {
            writer.println("Full Name,Email,Role,Check-In Time");
            List<Attendance> attendees = attendanceRepository.findByEvent(event);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            for (Attendance att : attendees) {
                String name = att.getUser().getFullName() != null ? att.getUser().getFullName() : "Unknown";
                String email = att.getUser().getEmail();
                String role = att.getUser().getRole();
                String time = att.getCheckInTime().format(formatter);
                writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\"", name, email, role, time));
            }
        } catch (Exception e) {
            e.printStackTrace(); 
        }
    }

    // 📱 2. TICKET SCANNER FOR STAFF
    @PostMapping("/events/scan-ticket")
    public String scanTicket(@RequestParam String qrData, Principal principal, RedirectAttributes redirectAttributes) {
        if (principal == null) {
            return "redirect:/login";
        }

        try {
            String[] parts = qrData.split(",");
            String emailPart = parts[0].split(":")[1];
            Long eventId = Long.parseLong(parts[1].split(":")[1]);

            Events event = eventRepository.findById(eventId).orElse(null);
            AppUser attendee = userRepository.findByEmail(emailPart);

            if (event == null || attendee == null) {
                redirectAttributes.addFlashAttribute("error", "Invalid Ticket! User or Event not found. ❌");
                return "redirect:/events";
            }

            if (!event.getAttendees().contains(attendee)) {
                redirectAttributes.addFlashAttribute("error", attendee.getFullName() + " is not registered for " + event.getTitle() + "! 🛑");
                return "redirect:/events";
            }

            if (attendanceRepository.existsByEventIdAndUserId(eventId, attendee.getId())) {
                redirectAttributes.addFlashAttribute("error", attendee.getFullName() + " has already been checked in! ⚠️");
                return "redirect:/events";
            }

            Attendance att = new Attendance();
            att.setEvent(event);
            att.setUser(attendee);
            att.setCheckInTime(LocalDateTime.now());
            attendanceRepository.save(att);

            redirectAttributes.addFlashAttribute("success", attendee.getFullName() + " successfully checked in! ✅");

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Failed to parse ticket data! Invalid QR format. ❌");
        }

        return "redirect:/events";
    }
}
