package com.lifetracker.web;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class SpaForwardController {

    @GetMapping(value = {
            "/",
            "/app",
            "/app/**"
    })
    public String forward() {
        return "forward:/index.html";
    }
}
