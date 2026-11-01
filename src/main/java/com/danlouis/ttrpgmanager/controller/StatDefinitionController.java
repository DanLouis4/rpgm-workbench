package com.danlouis.ttrpgmanager.controller;

import java.util.List;

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

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.StatDefinition;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.form.StatDefinitionForm;
import com.danlouis.ttrpgmanager.service.GameSystemService;
import com.danlouis.ttrpgmanager.service.StatDefinitionService;
import com.danlouis.ttrpgmanager.service.SystemTermService;

import jakarta.validation.Valid;

/**
 * Handles web requests for Game System Stat Definition management.
 *
 * <p>The controller allows authorized Game System owners to view,
 * create, edit, retire, and restore Stat Definitions belonging to
 * a System Term.</p>
 *
 * <p>Stat Definitions may optionally belong beneath another Stat
 * Definition to support hierarchical statistical structures.</p>
 *
 * @author Daniel Louis
 */
@Controller
@RequestMapping(
        "/systems/{gameSystemId}/terms/{systemTermId}/stats"
)
public class StatDefinitionController {

    private static final String UNAUTHORIZED_REDIRECT =
            "redirect:/systems?status=unauthorized";

    private static final String UNAVAILABLE_REDIRECT =
            "redirect:/systems?status=unavailable";

    private final StatDefinitionService statDefinitionService;
    private final SystemTermService systemTermService;
    private final GameSystemService gameSystemService;


    /**
     * Constructs the StatDefinitionController with its required services.
     *
     * @param statDefinitionService service used to manage Stat Definitions
     * @param systemTermService service used to manage System Terms
     * @param gameSystemService service used to manage Game Systems
     */
    public StatDefinitionController(
            StatDefinitionService statDefinitionService,
            SystemTermService systemTermService,
            GameSystemService gameSystemService) {

        this.statDefinitionService = statDefinitionService;
        this.systemTermService = systemTermService;
        this.gameSystemService = gameSystemService;
    }


    // -------------------- Stat Definitions Page -------------------- //


    /**
     * Displays the Stat Definition configuration page for a System Term.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param returnUrl original workflow return destination
     * @param authentication current authentication information
     * @param model model used to supply page data
     * @return Stat Definitions page or redirect when unavailable
     */
    @GetMapping
    public String showStatDefinitions(
            @PathVariable Long gameSystemId,
            @PathVariable Long systemTermId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model) {

        try {

            String username = requireAuthenticatedUsername(authentication);

            StatDefinitionForm statDefinitionForm = new StatDefinitionForm();

            populateStatDefinitionsModel(
                    gameSystemId,
                    systemTermId,
                    username,
                    statDefinitionForm,
                    returnUrl,
                    model);

            model.addAttribute("formMode", "create");

            return "systems/stats/index";

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            return UNAVAILABLE_REDIRECT;
        }
    }


    // -------------------- Stat Definition Creation -------------------- //


    /**
     * Creates a new Stat Definition within the specified System Term.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionForm submitted Stat Definition data
     * @param bindingResult result of form validation
     * @param returnUrl original workflow return destination
     * @param authentication current authentication information
     * @param model model used when redisplaying validation errors
     * @param redirectAttributes attributes used for redirect feedback
     * @return redirect to Stats or page when validation fails
     */
    @PostMapping
    public String createStatDefinition(
            @PathVariable Long gameSystemId,
            @PathVariable Long systemTermId,
            @Valid
            @ModelAttribute("statDefinitionForm")
            StatDefinitionForm statDefinitionForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            String username = requireAuthenticatedUsername(authentication);

            if (bindingResult.hasErrors()) {

                populateStatDefinitionsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        statDefinitionForm,
                        returnUrl,
                        model);

                model.addAttribute("formMode", "create");

                return "systems/stats/index";
            }

            StatDefinition statDefinition =
                    statDefinitionService.createStatDefinition(
                            gameSystemId,
                            systemTermId,
                            statDefinitionForm.getParentStatId(),
                            username,
                            statDefinitionForm.getStatName(),
                            statDefinitionForm.getAbbreviation(),
                            statDefinitionForm.getDescription(),
                            statDefinitionForm.getDisplayOrder());

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Stat Definition: \""
                    + statDefinition.getStatName()
                    + "\" added successfully.");

            redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

            return redirectToStatDefinitions(gameSystemId, systemTermId);

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            bindingResult.reject("statDefinition", exception.getMessage());

            String username = requireAuthenticatedUsername(authentication);

            populateStatDefinitionsModel(
                    gameSystemId,
                    systemTermId,
                    username,
                    statDefinitionForm,
                    returnUrl,
                    model);

            model.addAttribute("formMode", "create");

            return "systems/stats/index";
        }
    }


    // -------------------- Stat Definition Editing -------------------- //


    /**
     * Displays the Stats page with the selected Stat Definition loaded
     * for editing.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionId identifier of the Stat Definition
     * @param returnUrl original workflow return destination
     * @param authentication current authentication information
     * @param model model used to supply page data
     * @return Stats page or redirect when unavailable
     */
    @GetMapping("/{statDefinitionId}/edit")
    public String showEditStatDefinition(
            @PathVariable Long gameSystemId,
            @PathVariable Long systemTermId,
            @PathVariable Long statDefinitionId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model) {

        try {

            String username = requireAuthenticatedUsername(authentication);

            SystemTerm systemTerm =
                    systemTermService.getSystemTermById(
                            gameSystemId,
                            systemTermId,
                            username);

            StatDefinition statDefinition =
                    statDefinitionService.getStatDefinitionById(
                            statDefinitionId,
                            systemTerm);

            StatDefinitionForm statDefinitionForm = new StatDefinitionForm();

            statDefinitionForm.setStatName(statDefinition.getStatName());

            statDefinitionForm.setAbbreviation(statDefinition.getAbbreviation());

            statDefinitionForm.setDescription(statDefinition.getDescription());

            statDefinitionForm.setDisplayOrder(statDefinition.getDisplayOrder());

            if (statDefinition.getParentStat() != null) {

                statDefinitionForm.setParentStatId(
                    statDefinition
                            .getParentStat()
                            .getStatDefinitionId());
            }

            populateStatDefinitionsModel(
                    gameSystemId,
                    systemTermId,
                    username,
                    statDefinitionForm,
                    returnUrl,
                    model);

            model.addAttribute("formMode", "edit");

            model.addAttribute("statDefinitionId", statDefinitionId);

            return "systems/stats/index";

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            return UNAVAILABLE_REDIRECT;
        }
    }


    /**
     * Processes changes to an existing Stat Definition.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionId identifier of the Stat Definition
     * @param statDefinitionForm submitted Stat Definition data
     * @param bindingResult result of form validation
     * @param returnUrl original workflow return destination
     * @param authentication current authentication information
     * @param model model used when redisplaying validation errors
     * @param redirectAttributes attributes used for redirect feedback
     * @return redirect to Stats or page when validation fails
     */
    @PostMapping("/{statDefinitionId}/edit")
    public String updateStatDefinition(
            @PathVariable Long gameSystemId,
            @PathVariable Long systemTermId,
            @PathVariable Long statDefinitionId,
            @Valid
            @ModelAttribute("statDefinitionForm")
            StatDefinitionForm statDefinitionForm,
            BindingResult bindingResult,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            Model model,
            RedirectAttributes redirectAttributes) {

        try {

            String username = requireAuthenticatedUsername(authentication);

            if (bindingResult.hasErrors()) {

                populateStatDefinitionsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        statDefinitionForm,
                        returnUrl,
                        model);

                model.addAttribute("formMode", "edit");

                model.addAttribute("statDefinitionId", statDefinitionId);

                return "systems/stats/index";
            }

            StatDefinition statDefinition =
                    statDefinitionService.updateStatDefinition(
                            gameSystemId,
                            systemTermId,
                            statDefinitionId,
                            statDefinitionForm.getParentStatId(),
                            username,
                            statDefinitionForm.getStatName(),
                            statDefinitionForm.getAbbreviation(),
                            statDefinitionForm.getDescription(),
                            statDefinitionForm.getDisplayOrder());

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Stat Definition: \""
                    + statDefinition.getStatName()
                    + "\" updated successfully.");

            redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

            return redirectToStatDefinitions(gameSystemId, systemTermId);

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            bindingResult.reject("statDefinition", exception.getMessage());

            String username = requireAuthenticatedUsername(authentication);

            populateStatDefinitionsModel(
                    gameSystemId,
                    systemTermId,
                    username,
                    statDefinitionForm,
                    returnUrl,
                    model);

            model.addAttribute("formMode", "edit");

            model.addAttribute("statDefinitionId", statDefinitionId);

            return "systems/stats/index";
        }
    }


    // -------------------- Stat Definition Retirement -------------------- //


    /**
     * Retires an active Stat Definition.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionId identifier of the Stat Definition
     * @param returnUrl original workflow return destination
     * @param authentication current authentication information
     * @param redirectAttributes attributes used for redirect feedback
     * @return redirect to the Stats configuration page
     */
    @PostMapping("/{statDefinitionId}/retire")
    public String retireStatDefinition(
            @PathVariable Long gameSystemId,
            @PathVariable Long systemTermId,
            @PathVariable Long statDefinitionId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            String username = requireAuthenticatedUsername(authentication);

            String retiredStatName =
                    statDefinitionService.retireStatDefinition(
                            gameSystemId,
                            systemTermId,
                            statDefinitionId,
                            username);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Stat Definition: \""
                    + retiredStatName
                    + "\" retired successfully.");

            redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

            return redirectToStatDefinitions(gameSystemId, systemTermId);

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute("error", exception.getMessage());

            redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

            return redirectToStatDefinitions(gameSystemId, systemTermId);
        }
    }


    // -------------------- Stat Definition Restoration -------------------- //


    /**
     * Restores a retired Stat Definition.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionId identifier of the Stat Definition
     * @param returnUrl original workflow return destination
     * @param authentication current authentication information
     * @param redirectAttributes attributes used for redirect feedback
     * @return redirect to the Stats configuration page
     */
    @PostMapping("/{statDefinitionId}/restore")
    public String restoreStatDefinition(
            @PathVariable Long gameSystemId,
            @PathVariable Long systemTermId,
            @PathVariable Long statDefinitionId,
            @RequestParam(required = false) String returnUrl,
            Authentication authentication,
            RedirectAttributes redirectAttributes) {

        try {

            String username = requireAuthenticatedUsername(authentication);

            String restoredStatName =
                    statDefinitionService.restoreStatDefinition(
                            gameSystemId,
                            systemTermId,
                            statDefinitionId,
                            username);

            redirectAttributes.addFlashAttribute(
                    "message",
                    "Stat Definition: \""
                    + restoredStatName
                    + "\" restored successfully.");

            redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

            return redirectToStatDefinitions(gameSystemId, systemTermId);

        } catch (SecurityException exception) {

            return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

            redirectAttributes.addFlashAttribute("error", exception.getMessage());

            redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

            return redirectToStatDefinitions(gameSystemId, systemTermId);
        }
    }


    // -------------------- Model Helpers -------------------- //


    /**
     * Populates the model used by the Stat Definition configuration page.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param username username of the authenticated user
     * @param statDefinitionForm form displayed on the page
     * @param returnUrl original workflow return destination
     * @param model model to populate
     */
    private void populateStatDefinitionsModel(
            Long gameSystemId,
            Long systemTermId,
            String username,
            StatDefinitionForm statDefinitionForm,
            String returnUrl,
            Model model) {

        GameSystem gameSystem = gameSystemService.getManageableGameSystemById(gameSystemId, username);

        SystemTerm systemTerm = systemTermService.getSystemTermById(
                        gameSystemId,
                        systemTermId,
                        username);

        if (!systemTerm.isHasStats()) {
            throw new IllegalArgumentException(
                    "Stats are not enabled for this Terminology.");
        }

        List<StatDefinition> statDefinitions =
                statDefinitionService.getStatDefinitions(
                        gameSystemId,
                        systemTermId,
                        username);

        List<StatDefinition> parentStats = statDefinitionService.getActiveStatDefinitions(systemTerm);

        model.addAttribute("gameSystem", gameSystem);
        model.addAttribute("systemTerm", systemTerm);
        model.addAttribute("statDefinitions", statDefinitions);
        model.addAttribute("parentStats", parentStats);
        model.addAttribute("statDefinitionForm", statDefinitionForm);

        model.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));
    }


    // -------------------- Authentication Helpers -------------------- //


    /**
     * Returns the authenticated username for a protected management request.
     *
     * @param authentication current authentication information
     * @return authenticated username
     * @throws SecurityException if authentication is unavailable
     */
    private String requireAuthenticatedUsername(Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

            throw new SecurityException("Authentication is required.");
        }

        return authentication.getName();
    }


    // -------------------- Navigation Helpers -------------------- //


    /**
     * Determines whether the supplied return URL refers to a local
     * application path.
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
     * Returns a safe workflow return destination.
     *
     * @param returnUrl requested return destination
     * @return safe local return URL
     */
    private String getSafeReturnUrl(String returnUrl) {

        return isValidReturnUrl(returnUrl)
                ? returnUrl
                : "/systems";
    }


    /**
     * Builds a redirect to the Stat Definition configuration page.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @return redirect URL
     */
    private String redirectToStatDefinitions(Long gameSystemId, Long systemTermId) {

        return "redirect:/systems/"
                + gameSystemId
                + "/terms/"
                + systemTermId
                + "/stats";
    }
}