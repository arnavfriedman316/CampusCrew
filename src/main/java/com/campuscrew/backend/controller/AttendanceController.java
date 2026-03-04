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
            // Check if they already scanned the code today
            if (attendanceRepository.existsByEventIdAndUserId(id, user.getId())) {
                redirectAttributes.addFlashAttribute("error", "You have already checked in for " + event.getTitle() + "! ✅");
            } else {
                // Record the attendance!
                Attendance att = new Attendance();
                att.setEvent(event);
                att.setUser(user);
                att.setCheckInTime(LocalDateTime.now());
                attendanceRepository.save(att);

                redirectAttributes.addFlashAttribute("success", "Attendance marked for " + event.getTitle() + "! Welcome. 👋");
            }
        }
        return "redirect:/events";
    }

    // 📊 2. THE EXCEL/CSV EXPORT ENDPOINT
    @GetMapping("/events/{id}/attendance/export")
    public void exportAttendanceToCSV(@PathVariable Long id, HttpServletResponse response) {
        Events event = eventRepository.findById(id).orElse(null);

        if (event == null) {
            return;
        }

        // Set the response type so the browser knows to download a file
        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"attendance_" + event.getTitle().replaceAll("\\s+", "_") + ".csv\"");

        try (PrintWriter writer = response.getWriter()) {
            // Write the Excel Headers
            writer.println("Full Name,Email,Role,Check-In Time");

            // Fetch everyone who attended this specific event
            List<Attendance> attendees = attendanceRepository.findByEvent(event);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Loop through and write their data
            for (Attendance att : attendees) {
                String name = att.getUser().getFullName() != null ? att.getUser().getFullName() : "Unknown";
                String email = att.getUser().getEmail();
                String role = att.getUser().getRole();
                String time = att.getCheckInTime().format(formatter);

                // Write the row (Wrap text in quotes to prevent comma issues)
                writer.println(String.format("\"%s\",\"%s\",\"%s\",\"%s\"", name, email, role, time));
            }
        } catch (Exception e) {
            e.printStackTrace(); // Normally we'd log this, but this is safe for now
        }
    }
}
