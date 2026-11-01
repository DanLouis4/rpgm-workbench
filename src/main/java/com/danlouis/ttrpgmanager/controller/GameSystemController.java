package com.danlouis.ttrpgmanager.controller;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.util.UriComponentsBuilder;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.OptionValue;
import com.danlouis.ttrpgmanager.entity.StatDefinition;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.form.GameSystemForm;
import com.danlouis.ttrpgmanager.service.GameSystemService;
import com.danlouis.ttrpgmanager.service.OptionGroupService;
import com.danlouis.ttrpgmanager.service.OptionValueService;
import com.danlouis.ttrpgmanager.service.StatDefinitionService;
import com.danlouis.ttrpgmanager.service.SystemTermService;

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
@RequestMapping("/systems")
public class GameSystemController {

    // Redirect URL used when a game system is unavailable.
    private static final String UNAVAILABLE_REDIRECT = "redirect:/systems?status=unavailable";
    private static final String UNAUTHORIZED_REDIRECT = "redirect:/systems?status=unauthorized";

    private final GameSystemService gameSystemService;
    private final SystemTermService systemTermService;
    private final StatDefinitionService statDefinitionService;

    private final OptionGroupService optionGroupService;
    private final OptionValueService optionValueService;

    /**
     * Constructs the GameSystemController with its required service.
     *
     * @param gameSystemService service used to manage game systems
     * @param systemTermService service used to manage system terms
     */
    public GameSystemController(GameSystemService gameSystemService, SystemTermService systemTermService,
                                OptionGroupService optionGroupService, OptionValueService optionValueService,
                                StatDefinitionService statDefinitionService) {
                                    
        this.gameSystemService = gameSystemService;
        this.systemTermService = systemTermService;
        this.optionGroupService = optionGroupService;
        this.optionValueService = optionValueService;
        this.statDefinitionService = statDefinitionService;
    }

    // -------------------- Game System Browsing and Retrieval -------------------- //
    
    /**
     * Displays the public Game Systems index page.
     *
     * @param status optional page-status value
     * @param model model used to supply game systems and feedback
     * @return the Game Systems index template
     */
    @GetMapping
    public String showGameSystems(@RequestParam(required = false) String status, Model model) {

        model.addAttribute("gameSystems", gameSystemService.getPublicGameSystems());

        if ("unavailable".equals(status)) {
            model.addAttribute("message", "Game System Unavailable.");
        } else if ("unauthorized".equals(status)) {
            model.addAttribute("message", "You are not authorized to modify this Game System.");
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
    @GetMapping("/{gameSystemId}")
    public String showGameSystem(
            @PathVariable Long gameSystemId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            HttpServletRequest request,
            Model model) {

        try {

            GameSystem gameSystem = gameSystemService.getGameSystemById(gameSystemId);

            String username = getAuthenticatedUsername(authentication);

            boolean isOwner = gameSystemService.isOwner(gameSystem, username);

            boolean canClone = gameSystemService.canClone(gameSystem, username);

            if (!gameSystem.isPublic() && !isOwner) {
                return UNAVAILABLE_REDIRECT;
            }

            model.addAttribute("gameSystem", gameSystem);

            // Retrieve system terms for the game system
            List<SystemTerm> systemTerms = systemTermService.getSystemTermsForProfile(gameSystem);

            model.addAttribute("systemTerms", systemTerms);

            // Initialize maps for option groups and values by term and group respectively
            Map<Long, List<OptionGroup>> optionGroupsByTerm = new LinkedHashMap<>();

            // Map to store option values by their corresponding option group
            Map<Long, List<OptionValue>> optionValuesByGroup = new LinkedHashMap<>();

            // Map to store child option groups by their parent option group
            Map<Long, List<OptionGroup>> childGroupsByParentGroup = new LinkedHashMap<>();

            // Map to store child option values by their parent option value
            Map<Long, Map<Long, List<OptionValue>>> childValuesByParentValue = new LinkedHashMap<>();

            // Map to store stat definitions by their corresponding game system
            Map<Long, List<StatDefinition>> statDefinitionsByTerm = new LinkedHashMap<>();

            boolean hasConfiguredOptions = false;

            // Iterate over each system term to retrieve associated option groups and values
            for (SystemTerm systemTerm : systemTerms) {

                List<OptionGroup> optionGroups =optionGroupService.getOptionGroupsForProfile(systemTerm);

                optionGroupsByTerm.put(systemTerm.getSystemTermId(), optionGroups);

                if (!optionGroups.isEmpty()) {
                    hasConfiguredOptions = true;
                }

                // Iterate over each option group to retrieve associated option values
                for (OptionGroup optionGroup : optionGroups) {

                    List<OptionValue> optionValues = optionValueService.getActiveOptionValuesForGroup(optionGroup);

                    if (optionGroup.getParentOptionGroup() != null) {

                        Long parentGroupId =
                                optionGroup.getParentOptionGroup().getOptionGroupId();

                        childGroupsByParentGroup
                                .computeIfAbsent(parentGroupId, id -> new ArrayList<>())
                                .add(optionGroup);
                    }

                    optionValuesByGroup.put(optionGroup.getOptionGroupId(), optionValues);

                    for (OptionValue optionValue : optionValues) {

                        if (optionValue.getParentOptionValue() != null) {

                            Long parentValueId =
                                    optionValue.getParentOptionValue().getOptionValueId();

                            childValuesByParentValue
                                    .computeIfAbsent(parentValueId, id -> new LinkedHashMap<>())
                                    .computeIfAbsent(optionGroup.getOptionGroupId(), id -> new ArrayList<>())
                                    .add(optionValue);
                        }
                    }

                }
                
                // Retrieve stat definitions for the system term if it has stats
                if (systemTerm.isHasStats()) {

                    List<StatDefinition> statDefinitions =
                            statDefinitionService.getActiveStatDefinitions(systemTerm);

                    statDefinitionsByTerm.put(systemTerm.getSystemTermId(), statDefinitions);
                }
            }
            
            // Add option groups and values to the model
            model.addAttribute("optionGroupsByTerm", optionGroupsByTerm);
            model.addAttribute("optionValuesByGroup", optionValuesByGroup);

            // Add child option groups and values to the model
            model.addAttribute("childGroupsByParentGroup", childGroupsByParentGroup);
            model.addAttribute("childValuesByParentValue", childValuesByParentValue);
            
            // Add stat definitions to the model for the entire system
            model.addAttribute("statDefinitionsBySystem", statDefinitionsByTerm);
            
            // Add stat definitions to the model for each term
            model.addAttribute("statDefinitionsByTerm", statDefinitionsByTerm);
            
            // Add flag indicating if there are configured options to the model
            model.addAttribute("hasConfiguredOptions", hasConfiguredOptions);            

            // Add ownership flag to the model
            model.addAttribute("canManage", isOwner);

            // Add clone permission flag to the model
            model.addAttribute("canClone", canClone);

            // Add suggested clone name to the model
            model.addAttribute("suggestedCloneName", buildSuggestedCloneName(gameSystem.getName()));

            String safeReturnUrl = isValidReturnUrl(returnUrl) ? returnUrl : getReturnUrl(request);

            String currentUrl = UriComponentsBuilder.fromPath("/systems/{gameSystemId}")
                    .queryParam("returnUrl", safeReturnUrl)
                    .buildAndExpand(gameSystemId)
                    .encode()
                    .toUriString();

            model.addAttribute("returnUrl", safeReturnUrl);

            model.addAttribute("currentUrl", currentUrl);

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
    @GetMapping("/create")
    public String showCreateForm(HttpServletRequest request, Model model) {

        model.addAttribute("gameSystemForm", new GameSystemForm());

        model.addAttribute("formMode", "create");

        model.addAttribute("returnUrl", getReturnUrl(request));

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
    @PostMapping("/create")
    public String createGameSystem(
            @Valid @ModelAttribute("gameSystemForm") GameSystemForm gameSystemForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("formMode", "create");

            model.addAttribute("returnUrl", returnUrl);

            return "systems/form";
        }

        try {

            GameSystem gameSystem = gameSystemService.createGameSystem(
                    requireAuthenticatedUsername(authentication),
                    gameSystemForm.getName(),
                    gameSystemForm.getGenre(),
                    gameSystemForm.getRulesetFamily(),
                    gameSystemForm.getRulesetEdition(),
                    gameSystemForm.getDescription(),
                    gameSystemForm.getSystemOverview(),
                    gameSystemForm.getIsPublic(),
                    gameSystemForm.getCloneable());

            String safeReturnUrl = isValidReturnUrl(returnUrl) ? returnUrl : "/systems";

            redirectAttributes.addAttribute("returnUrl", safeReturnUrl);

            return "redirect:/systems/" + gameSystem.getGameSystemId();

        } catch (IllegalArgumentException exception) {

            bindingResult.reject("gameSystem", exception.getMessage());

            model.addAttribute("formMode", "create");

            model.addAttribute("returnUrl", returnUrl);

            return "systems/form";
        }
    }

    // -------------------- Game System Editing -------------------- //
    /**
     * Displays the edit form when the authenticated user owns the requested
     * game system.
     *
     * @param gameSystemId identifier of the game system
     * @param returnUrl page to return to when leaving the edit form
     * @param authentication current authentication information
     * @param request current HTTP request
     * @param model model used to supply the populated form
     * @return the edit form or Game Systems index redirect
     * @see #getReturnUrl(HttpServletRequest)
     */
    @GetMapping("/{gameSystemId}/edit")
    public String showEditForm(
            @PathVariable Long gameSystemId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            HttpServletRequest request,
            Model model) {

        try {

            GameSystem gameSystem = gameSystemService.getGameSystemById(gameSystemId);

            String username = requireAuthenticatedUsername(authentication);

            if (!gameSystemService.isOwner(gameSystem, username)) {

                return UNAUTHORIZED_REDIRECT;
            }

            GameSystemForm gameSystemForm = new GameSystemForm();

            gameSystemForm.setName(gameSystem.getName());

            gameSystemForm.setGenre(gameSystem.getGenre());

            gameSystemForm.setRulesetFamily(gameSystem.getRulesetFamily());

            gameSystemForm.setRulesetEdition(gameSystem.getRulesetEdition());

            gameSystemForm.setDescription(gameSystem.getDescription());

            gameSystemForm.setSystemOverview(gameSystem.getSystemOverview());

            gameSystemForm.setIsPublic(gameSystem.isPublic());

            gameSystemForm.setCloneable(gameSystem.isCloneable());

            model.addAttribute("gameSystemForm", gameSystemForm);

            model.addAttribute("gameSystemId", gameSystemId);

            model.addAttribute("formMode", "edit");

            model.addAttribute("returnUrl", isValidReturnUrl(returnUrl) ? returnUrl : getReturnUrl(request));

            return "systems/form";

        } catch (IllegalArgumentException | SecurityException exception) {

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
    @PostMapping("/{gameSystemId}/edit")
    public String updateGameSystem(
            @PathVariable Long gameSystemId,
            @Valid @ModelAttribute("gameSystemForm") GameSystemForm gameSystemForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model) {

        if (bindingResult.hasErrors()) {

            model.addAttribute("gameSystemId", gameSystemId);

            model.addAttribute("formMode", "edit");

            model.addAttribute("returnUrl", returnUrl);

            return "systems/form";
        }

        try {

            gameSystemService.updateGameSystem(
                    gameSystemId,
                    requireAuthenticatedUsername(authentication),
                    gameSystemForm.getName(),
                    gameSystemForm.getGenre(),
                    gameSystemForm.getRulesetFamily(),
                    gameSystemForm.getRulesetEdition(),
                    gameSystemForm.getDescription(),
                    gameSystemForm.getSystemOverview(),
                    gameSystemForm.getIsPublic(),
                    gameSystemForm.getCloneable()
                );

            String safeReturnUrl = isValidReturnUrl(returnUrl) ? returnUrl : "/systems/" + gameSystemId;

            return "redirect:" + safeReturnUrl;

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            bindingResult.reject("gameSystem", exception.getMessage());

            model.addAttribute("gameSystemId", gameSystemId);

            model.addAttribute("formMode", "edit");

            model.addAttribute("returnUrl", returnUrl);

            return "systems/form";
        }
    }

    // -------------------- Game System Deletion -------------------- //

    /**
     * Deletes a game system owned by the authenticated user when the
     * user has Game System management permissions.
     * 
     * @param gameSystemId identifier of the game system
     * @param returnUrl optional page to return to after deletion
     * @param authentication current authentication information
     * @param request current HTTP request
     * @param redirectAttributes attributes used for redirect feedback
     * @return redirect to the referring page
     */
    @PostMapping("/{gameSystemId}/delete")
    public String deleteGameSystem(
            @PathVariable Long gameSystemId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            HttpServletRequest request,
            RedirectAttributes redirectAttributes) {

        try {

            String deletedSystemName = gameSystemService.deleteGameSystem(gameSystemId, requireAuthenticatedUsername(authentication));

            String safeReturnUrl = isValidReturnUrl(returnUrl) ? returnUrl : getReturnUrl(request);

            redirectAttributes.addFlashAttribute("deletedSystemName", deletedSystemName);
            
            return "redirect:" + safeReturnUrl;

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            return UNAVAILABLE_REDIRECT;
        }
    }

    // -------------------- Game System Cloning -------------------- //

    /**
     * Creates an independent copy of a cloneable Game System for the
     * authenticated Game Designer.
     *
     * @param gameSystemId identifier of the source Game System
     * @param cloneName name assigned to the new Game System
     * @param returnUrl original workflow return destination
     * @param authentication current authentication information
     * @param redirectAttributes attributes used for redirect feedback
     * @return redirect to the newly cloned Game System or source profile
     */
    @PostMapping("/{gameSystemId}/clone")
    public String cloneGameSystem(
            @PathVariable Long gameSystemId,
            @RequestParam String cloneName,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            GameSystem clonedSystem =
                    gameSystemService.cloneGameSystem(
                            gameSystemId,
                            requireAuthenticatedUsername(authentication),
                            cloneName);

            GameSystem sourceSystem = gameSystemService.getGameSystemById(gameSystemId);

            redirectAttributes.addFlashAttribute("message",
                    sourceSystem.getName() + " Successfully Cloned. "
                    + clonedSystem.getName() + " Successfully Created.");

            return "redirect:/dashboard";

        } catch (SecurityException | IllegalArgumentException | IllegalStateException exception) {

            redirectAttributes.addFlashAttribute("cloneError", exception.getMessage());

            redirectAttributes.addFlashAttribute("cloneName", cloneName);

            redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

            return "redirect:/systems/" + gameSystemId;

        }
    }

    // -------------------- Authentication Helpers -------------------- //

    /**
     * Returns the username of the authenticated user.
     *
     * @param authentication current authentication information
     * @return the username or null for an unauthenticated user
     */
    private String getAuthenticatedUsername(Authentication authentication) {

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
    private String requireAuthenticatedUsername(Authentication authentication) {

        String username = getAuthenticatedUsername(authentication);

        if (username == null) {
            throw new SecurityException("Authentication is required.");
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

        return returnUrl != null && !returnUrl.isBlank() && returnUrl.startsWith("/") && !returnUrl.startsWith("//");
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

        try {

            URI refererUri = URI.create(referer);

            if (!request.getScheme().equalsIgnoreCase(refererUri.getScheme())
                    || !request.getServerName().equalsIgnoreCase(refererUri.getHost())) {
                return "/systems";
            }

            String returnUrl = refererUri.getRawPath();

            if (refererUri.getRawQuery() != null) {
                returnUrl += "?" + refererUri.getRawQuery();
            }

            return isValidReturnUrl(returnUrl) ? returnUrl : "/systems";

        } catch (IllegalArgumentException exception) {
            return "/systems";
        }
    }

    /**
     * Builds the default name displayed when cloning a Game System.
     *
     * @param sourceName source Game System name
     * @return clone name that does not exceed the 100-character limit
     */
    private String buildSuggestedCloneName(String sourceName) {

        final String suffix = " - Copy";
        final int maximumNameLength = 100;

        int availableLength =
                maximumNameLength - suffix.length();

        String baseName =
                sourceName.length() > availableLength
                        ? sourceName.substring(
                                0,
                                availableLength).trim()
                        : sourceName;

        return baseName + suffix;
    }

    /**
     * Returns a safe local workflow return destination.
     *
     * @param returnUrl requested return destination
     * @return safe local application path
     */
    private String getSafeReturnUrl(String returnUrl) {

        return isValidReturnUrl(returnUrl)
                ? returnUrl
                : "/systems";
    }


}
