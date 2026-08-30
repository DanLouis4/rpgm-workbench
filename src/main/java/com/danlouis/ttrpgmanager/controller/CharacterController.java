package com.danlouis.ttrpgmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Handles web requests for the Character Management module.
 *
 * <p>The CharacterController provides access to character browsing,
 * profile, creation, editing, and management functions according to the
 * permissions of the current user.</p>
 *
 * @author Daniel Louis
 */
@Controller
public class CharacterController {

    /**
     * Displays the Characters index page.
     *
     * <p>The index page serves as the primary public browse page for
     * available characters.</p>
     *
     * @return the name of the Characters index Thymeleaf template
     */
    @GetMapping("/characters")
    public String charactersHome() {
        return "characters/index";
    }
}