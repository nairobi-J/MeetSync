package com.root.meetsync;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class WebController {
    @GetMapping("/event-create")
    public String showCreateEventPage(){
        return "create-event";
    }

}
