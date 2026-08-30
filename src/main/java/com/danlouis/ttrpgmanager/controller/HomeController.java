package com.danlouis.ttrpgmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Handles requests for the primary public pages of the TTRPG Manager.
 *
 * <p>The HomeController provides the main entry point to the application
 * and directs users to the public areas used to browse game systems,
 * campaigns, and characters.</p>
 *
 * @author Daniel Louis
 */
@Controller
public class HomeController {

    /**
     * Displays the public home page of the TTRPG Manager.
     *
     * @return the name of the home page Thymeleaf template
     */
    @GetMapping("/")
    public String home() {
        return "home";
    }
}