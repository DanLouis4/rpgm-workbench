package com.danlouis.ttrpgmanager.controller;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.form.GameSystemForm;
import com.danlouis.ttrpgmanager.service.GameSystemService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

/**
 * Handles web requests for the Game System Management module.
 *
 * The controller supports public game-system browsing and profile viewing,
 * along with authenticated Game Designer creation, editing, and deletion
 * workflows. Business rules and ownership validation are delegated to the
 * GameSystemService.
 *
 * @author Daniel Louis
 */
@Controller
public class GameSystemController {

    // Redirect URL used when a game system is unavailable.
    private static final String UNAVAILABLE_REDIRECT = "redirect:/systems?status=unavailable";
    private static final String UNAUTHORIZED_REDIRECT = "redirect:/systems?status=unauthorized";

    private final GameSystemService gameSystemService;

    /**
     * Constructs the GameSystemController with its required service.
     *
     * @param gameSystemService service used to manage game systems
     */
    public GameSystemController(GameSystemService gameSystemService) {
        this.gameSystemService = gameSystemService;
    }

    // -------------------- Game System Browsing -------------------- //
    /**
     * Displays the public Game Systems index page.
     *
     * @param status optional page-status value
     * @param model model used to supply game systems and feedback
     * @return the Game Systems index template
     */
    @GetMapping("/systems")
    public String showGameSystems(
            @RequestParam(required = false) String status,
            Model model) {

        model.addAttribute(
                "gameSystems",
                gameSystemService.getPublicGameSystems());

        if ("unavailable".equals(status)) {
            model.addAttribute(
                    "message",
                    "Game System Unavailable.");
        } else if ("unauthorized".equals(status)) {
            model.addAttribute(
                    "message",
                    "You are not authorized to modify this Game System.");
        }

        return "systems/index";
    }

    /**
     * Displays a game system when it is public or owned by the authenticated
     * user. It also provides the return URL for navigation back to the previous
     * page.
     *
     * @param gameSystemId identifier of the requested game system
     * @param returnUrl the URL to return to after viewing the game system
     * @param authentication current authentication information
     * @param request the HTTP servlet request
     * @param model model used to supply the game system
     * @return the profile page or Game Systems index redirect
     */
    @GetMapping("/systems/{gameSystemId}")
    public String showGameSystem(
            @PathVariable Long gameSystemId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            HttpServletRequest request,
            Model model) {

        try {

            GameSystem gameSystem
                    = gameSystemService.getGameSystemById(gameSystemId);

            String username
                    = getAuthenticatedUsername(authentication);

            boolean isOwner
                    = gameSystemService.isOwner(
                            gameSystem,
                            username);

            if (!gameSystem.isPublic() && !isOwner) {
                return UNAVAILABLE_REDIRECT;
            }

            model.addAttribute(
                    "gameSystem",
                    gameSystem);

            model.addAttribute(
                    "canManage",
                    isOwner);

            String safeReturnUrl
                    = isValidReturnUrl(returnUrl)
                    ? returnUrl
                    : getReturnUrl(request);

            String currentUrl
                    = UriComponentsBuilder
                            .fromPath("/systems/{gameSystemId}")
                            .queryParam("returnUrl", safeReturnUrl)
                            .buildAndExpand(gameSystemId)
                            .encode()
                            .toUriString();

            model.addAttribute(
                    "returnUrl",
                    safeReturnUrl);

            model.addAttribute(
                    "currentUrl",
                    currentUrl);

            return "systems/profile";

        } catch (IllegalArgumentException exception) {

            return UNAVAILABLE_REDIRECT;
        }
    }

    // -------------------- Game System Creation -------------------- //
    /**
     * Displays the form used to create a new game system.
     *
     * @param model model used to supply the form object
     * @return the Game System form template
     */
    @GetMapping("/systems/create")
    public String showCreateForm(HttpServletRequest request, Model model) {

        model.addAttribute(
                "gameSystemForm",
                new GameSystemForm());

        model.addAttribute(
                "formMode",
                "create");

        model.addAttribute(
                "returnUrl",
                getReturnUrl(request));

        return "systems/form";
    }

    /**
     * Processes creation of a new game system.
     *
     * @param gameSystemForm validated form data
     * @param bindingResult result of form validation
     * @param returnUrl the URL to return to after processing the form
     * @param authentication current authentication information
     * @param model model used when redisplaying validation errors
     * @param redirectAttributes attributes used for redirect scenarios
     * @return redirect to the created system or the form
     */
    @PostMapping("/systems/create")
    public String createGameSystem(
            @Valid
            @ModelAttribute("gameSystemForm") GameSystemForm gameSystemForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "formMode",
                    "create");

            model.addAttribute(
                    "returnUrl",
                    returnUrl);

            return "systems/form";
        }

        try {

            GameSystem gameSystem
                    = gameSystemService.createGameSystem(
                            requireAuthenticatedUsername(authentication),
                            gameSystemForm.getName(),
                            gameSystemForm.getDescription(),
                            gameSystemForm.getIsPublic());

            String safeReturnUrl
                    = isValidReturnUrl(returnUrl)
                    ? returnUrl
                    : "/systems";

            redirectAttributes.addAttribute(
                    "returnUrl",
                    safeReturnUrl);

            return "redirect:/systems/"
                    + gameSystem.getGameSystemId();

        } catch (IllegalArgumentException exception) {

            bindingResult.reject(
                    "gameSystem",
                    exception.getMessage());

            model.addAttribute(
                    "formMode",
                    "create");

            model.addAttribute(
                    "returnUrl",
                    returnUrl);

            return "systems/form";
        }
    }

    // -------------------- Game System Editing -------------------- //
    /**
     * Displays the edit form when the authenticated user owns the requested
     * game system.
     *
     * @param gameSystemId identifier of the game system
     * @@param returnUrl page to return to when leaving the edit form
     * @param authentication current authentication information
     * @param request current HTTP request
     * @param model model used to supply the populated form
     * @return the edit form or Game Systems index redirect
     * @see #getReturnUrl(HttpServletRequest)
     */
    @GetMapping("/systems/{gameSystemId}/edit")
    public String showEditForm(
            @PathVariable Long gameSystemId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            HttpServletRequest request,
            Model model) {

        try {

            GameSystem gameSystem
                    = gameSystemService.getGameSystemById(gameSystemId);

            String username
                    = requireAuthenticatedUsername(authentication);

            if (!gameSystemService.isOwner(
                    gameSystem,
                    username)) {

                return UNAUTHORIZED_REDIRECT;
            }

            GameSystemForm gameSystemForm
                    = new GameSystemForm();

            gameSystemForm.setName(
                    gameSystem.getName());

            gameSystemForm.setDescription(
                    gameSystem.getDescription());

            gameSystemForm.setIsPublic(
                    gameSystem.isPublic());

            model.addAttribute(
                    "gameSystemForm",
                    gameSystemForm);

            model.addAttribute(
                    "gameSystemId",
                    gameSystemId);

            model.addAttribute(
                    "formMode",
                    "edit");

            model.addAttribute(
                    "returnUrl",
                    isValidReturnUrl(returnUrl)
                    ? returnUrl
                    : getReturnUrl(request));

            return "systems/form";

        } catch (IllegalArgumentException
                | SecurityException exception) {

            return UNAVAILABLE_REDIRECT;
        }
    }

    /**
     * Processes changes to an existing game system.
     *
     * @param gameSystemId identifier of the game system
     * @param gameSystemForm validated form data
     * @param bindingResult result of form validation
     * @param returnUrl page to return to after the update
     * @param authentication current authentication information
     * @param model model used when redisplaying validation errors
     * @return redirect to the referring page or the form when validation fails
     */
    @PostMapping("/systems/{gameSystemId}/edit")
    public String updateGameSystem(
            @PathVariable Long gameSystemId,
            @Valid
            @ModelAttribute("gameSystemForm") GameSystemForm gameSystemForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute(
                    "gameSystemId",
                    gameSystemId);

            model.addAttribute(
                    "formMode",
                    "edit");

            model.addAttribute(
                    "returnUrl",
                    returnUrl);

            return "systems/form";
        }

        try {

            gameSystemService.updateGameSystem(
                    gameSystemId,
                    requireAuthenticatedUsername(authentication),
                    gameSystemForm.getName(),
                    gameSystemForm.getDescription(),
                    gameSystemForm.getIsPublic());

            String safeReturnUrl
                    = isValidReturnUrl(returnUrl)
                    ? returnUrl
                    : "/systems/" + gameSystemId;

            return "redirect:" + safeReturnUrl;

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            bindingResult.reject(
                    "gameSystem",
                    exception.getMessage());

            model.addAttribute(
                    "gameSystemId",
                    gameSystemId);

            model.addAttribute(
                    "formMode",
                    "edit");

            model.addAttribute(
                    "returnUrl",
                    returnUrl);

            return "systems/form";
        }
    }

    // -------------------- Game System Deletion -------------------- //

    /**
     * Deletes a game system owned by the authenticated Game Designer.
     *
     * @param gameSystemId identifier of the game system
     * @param returnUrl optional page to return to after deletion
     * @param authentication current authentication information
     * @param request current HTTP request
     * @param redirectAttributes attributes used for redirect feedback
     * @return redirect to the referring page
     */
    @PostMapping("/systems/{gameSystemId}/delete")
    public String deleteGameSystem(
            @PathVariable Long gameSystemId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {

            gameSystemService.deleteGameSystem(
                    gameSystemId,
                    requireAuthenticatedUsername(authentication));

            String safeReturnUrl
                    = isValidReturnUrl(returnUrl)
                    ? returnUrl
                    : getReturnUrl(request);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Game System deleted successfully.");

            return "redirect:" + safeReturnUrl;

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            return UNAVAILABLE_REDIRECT;
        }
    }

    // -------------------- Authentication Helpers -------------------- //
    
    /**
     * Returns the username of the authenticated user.
     *
     * @param authentication current authentication information
     * @return the username or null for an unauthenticated user
     */
    private String getAuthenticatedUsername(
            Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            return null;
        }

        return authentication.getName();
    }

    /**
     * Returns the username of the authenticated user for a protected management
     * operation.
     *
     * @param authentication current authentication information
     * @return the authenticated username
     * @throws SecurityException if authentication is unavailable
     */
    private String requireAuthenticatedUsername(
            Authentication authentication) {

        String username
                = getAuthenticatedUsername(authentication);

        if (username == null) {
            throw new SecurityException(
                    "Authentication is required.");
        }

        return username;
    }

    // -------------------- More Helpers -------------------- //
    /**
     * Verifies that a return URL refers to a local application path.
     *
     * @param returnUrl return URL to evaluate
     * @return true when the URL is a valid local path
     */
    private boolean isValidReturnUrl(String returnUrl) {

        return returnUrl != null
                && !returnUrl.isBlank()
                && returnUrl.startsWith("/")
                && !returnUrl.startsWith("//");
    }

    /**
     * Determines the application page that referred the user to the current
     * request.
     *
     * When no valid referring page is available, the Game Systems index is used
     * as the default destination.
     *
     * @param request current HTTP request
     * @return local URL used by the profile's return link
     */
    private String getReturnUrl(HttpServletRequest request) {

        String referer = request.getHeader("Referer");

        if (referer == null || referer.isBlank()) {
            return "/systems";
        }

        String applicationUrl
                = request.getScheme()
                + "://"
                + request.getServerName()
                + ":"
                + request.getServerPort();

        if (!referer.startsWith(applicationUrl)) {
            return "/systems";
        }

        return referer.substring(applicationUrl.length());

    }
}
