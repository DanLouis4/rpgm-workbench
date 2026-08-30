package com.danlouis.ttrpgmanager.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;


/**
 * Handles web requests for the Campaign Management module.
 *
 * <p>The CampaignController provides access to campaign browsing,
 * profile, creation, editing, and management functions according to the
 * permissions of the current user.</p>
 *
 * @author Daniel Louis
 */
@Controller
public class CampaignController {

    /**
     * Displays the Campaigns index page.
     *
     * <p>The index page serves as the primary public browse page for
     * available campaigns.</p>
     *
     * @return the name of the Campaigns index Thymeleaf template
     */
    @GetMapping("/campaigns")
    public String campaignsHome() {
        return "campaigns/index";
    }
}