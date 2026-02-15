package com.campuscrew.backend.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.campuscrew.backend.repository.EventRepository;

@Controller
public class EventController {

    @Autowired
    private EventRepository eventRepository;

    @GetMapping("/events")
    public String showEventsPage(Model model) {
        model.addAttribute("events", eventRepository.findAllByOrderByDateTimeAsc());
        return "events";
    }

    // ... post mapping ...
}
