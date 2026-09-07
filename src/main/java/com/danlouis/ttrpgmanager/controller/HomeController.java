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
    
    /**
     * Displays the about page of the TTRPG Manager.
     *
     * @return the name of the about page Thymeleaf template
     */
    @GetMapping("/about")
    public String about() {
        return "about";
    }
    
    /**
     * Displays the privacy policy page of the TTRPG Manager.
     *
     * @return the name of the privacy policy Thymeleaf template
     */
    @GetMapping("/privacy-policy")
    public String privacyPolicy() {
        return "privacy-policy";
    }

    /**
     * Displays the terms of use page of the TTRPG Manager.
     *
     * @return the name of the terms of use Thymeleaf template
     */
    @GetMapping("/terms-of-use")
    public String termsOfUse() {
        return "terms-of-use";
    }
}