package com.root.meetsync.controller;

import java.time.Year;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {
    
    @GetMapping("/")
    public String landingPage(Model model) {
        model.addAttribute("currentYear", Year.now().getValue());
        return "LandingPage";
    }
}
