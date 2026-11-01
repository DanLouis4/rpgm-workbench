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
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.form.SystemTermForm;
import com.danlouis.ttrpgmanager.service.GameSystemService;
import com.danlouis.ttrpgmanager.service.SystemTermService;

import jakarta.validation.Valid;

/**
 * Handles web requests for Game System Terminology management.
 *
 * <p>The controller allows authorized Game System owners to view, create,
 * edit, retire and restore System Terms belonging to a Game System.</p>
 *
 * @author Daniel Louis
 */
@Controller
@RequestMapping("/systems/{gameSystemId}/terms")
public class SystemTermController {

        private static final String UNAUTHORIZED_REDIRECT =
                "redirect:/systems?access=unauthorized";

        private static final String UNAVAILABLE_REDIRECT =
                "redirect:/systems?status=unavailable";

        private final SystemTermService systemTermService;
        private final GameSystemService gameSystemService;


        /**
         * Constructs the SystemTermController with its required services.
         *
         * @param systemTermService service used to manage System Terms
         * @param gameSystemService service used to manage Game Systems
         */
        public SystemTermController(
                SystemTermService systemTermService,
                GameSystemService gameSystemService) {

                this.systemTermService = systemTermService;
                this.gameSystemService = gameSystemService;
        }


        // -------------------- Terminology Page -------------------- //


        /**
         * Displays the Terminology configuration page for a Game System.
         *
         * @param gameSystemId identifier of the Game System
         * @param returnUrl original page to return to after leaving the workflow
         * @param authentication current authentication information
         * @param model model used to supply page data
         * @return Terminology page or redirect when unavailable
         */
        @GetMapping
        public String showCoreConcepts(
                @PathVariable Long gameSystemId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model) {

                try {

                String username =
                        requireAuthenticatedUsername(authentication);

                populateCoreConceptsModel(
                        gameSystemId,
                        username,
                        new SystemTermForm(),
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "create");

                return "systems/terms/index";

                } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                return UNAVAILABLE_REDIRECT;
                }
        }


        // -------------------- System Term Creation -------------------- //


        /**
         * Creates a new Terminology entry for the specified Game System.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermForm submitted Terminology data
         * @param bindingResult result of form validation
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used when redisplaying validation errors
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to Terminology or page when validation fails
         */
        @PostMapping
        public String createSystemTerm(
                @PathVariable Long gameSystemId,
                @Valid
                @ModelAttribute("systemTermForm")
                SystemTermForm systemTermForm,
                BindingResult bindingResult,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model,
                RedirectAttributes redirectAttributes) {

                String username =
                        requireAuthenticatedUsername(authentication);

                if (bindingResult.hasErrors()) {

                populateCoreConceptsModel(
                        gameSystemId,
                        username,
                        systemTermForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "create");

                return "systems/terms/index";
                }

                try {

                SystemTerm systemTerm =
                        systemTermService.createSystemTerm(
                                gameSystemId,
                                username,
                                systemTermForm.getSingularLabel(),
                                systemTermForm.getPluralLabel(),
                                systemTermForm.getDescription(),
                                systemTermForm.isHasCharacterOptions(),
                                systemTermForm.isHasStats(),
                                systemTermForm.getDisplayOrder());

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Terminology: \""
                        + systemTerm.getSingularLabel()
                        + "\" added successfully.");

                redirectAttributes.addAttribute(
                        "returnUrl",
                        getSafeReturnUrl(returnUrl));

                return redirectToCoreConcepts(gameSystemId);

                } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                bindingResult.reject(
                        "systemTerm",
                        exception.getMessage());

                populateCoreConceptsModel(
                        gameSystemId,
                        username,
                        systemTermForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "create");

                return "systems/terms/index";
                }
        }


        // -------------------- System Term Editing -------------------- //


        /**
         * Displays the Terminology page with the selected term loaded for editing.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used to supply page data
         * @return Terminology page or redirect when unavailable
         */
        @GetMapping("/{systemTermId}/edit")
        public String showEditSystemTerm(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model) {

                try {

                String username =
                        requireAuthenticatedUsername(authentication);

                SystemTerm systemTerm =
                        systemTermService.getSystemTermById(
                                gameSystemId,
                                systemTermId,
                                username);

                SystemTermForm systemTermForm =
                        new SystemTermForm();

                systemTermForm.setSingularLabel(
                        systemTerm.getSingularLabel());

                systemTermForm.setPluralLabel(
                        systemTerm.getPluralLabel());

                systemTermForm.setDescription(
                        systemTerm.getDescription());

                systemTermForm.setHasCharacterOptions(
                        systemTerm.isHasCharacterOptions());

                systemTermForm.setHasStats(
                        systemTerm.isHasStats());

                systemTermForm.setDisplayOrder(
                        systemTerm.getDisplayOrder());

                populateCoreConceptsModel(
                        gameSystemId,
                        username,
                        systemTermForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "edit");

                model.addAttribute(
                        "systemTermId",
                        systemTermId);

                return "systems/terms/index";

                } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                return UNAVAILABLE_REDIRECT;
                }
        }


        /**
         * Processes changes to an existing Terminology.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param systemTermForm submitted Terminology data
         * @param bindingResult result of form validation
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used when redisplaying validation errors
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to Terminology or page when validation fails
         */
        @PostMapping("/{systemTermId}/edit")
        public String updateSystemTerm(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @Valid
                @ModelAttribute("systemTermForm")
                SystemTermForm systemTermForm,
                BindingResult bindingResult,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model,
                RedirectAttributes redirectAttributes) {

                String username =
                        requireAuthenticatedUsername(authentication);

                if (bindingResult.hasErrors()) {

                populateCoreConceptsModel(
                        gameSystemId,
                        username,
                        systemTermForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "edit");

                model.addAttribute(
                        "systemTermId",
                        systemTermId);

                return "systems/terms/index";
                }

                try {

                SystemTerm systemTerm =
                        systemTermService.updateSystemTerm(
                                gameSystemId,
                                systemTermId,
                                username,
                                systemTermForm.getSingularLabel(),
                                systemTermForm.getPluralLabel(),
                                systemTermForm.getDescription(),
                                systemTermForm.isHasCharacterOptions(),
                                systemTermForm.isHasStats(),
                                systemTermForm.getDisplayOrder());

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Terminology: \""
                        + systemTerm.getSingularLabel()
                        + "\" updated successfully.");

                redirectAttributes.addAttribute(
                        "returnUrl",
                        getSafeReturnUrl(returnUrl));

                return redirectToCoreConcepts(gameSystemId);

                } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                bindingResult.reject(
                        "systemTerm",
                        exception.getMessage());

                populateCoreConceptsModel(
                        gameSystemId,
                        username,
                        systemTermForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "edit");

                model.addAttribute(
                        "systemTermId",
                        systemTermId);

                return "systems/terms/index";
                }
        }


        // -------------------- System Term Retirement -------------------- //


        /**
         * Retires an active Terminology belonging to the specified Game System.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to the Terminology page
         */
        @PostMapping("/{systemTermId}/retire")
        public String retireSystemTerm(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

        try {

                String username = requireAuthenticatedUsername(authentication);

                String retiredTermName =
                        systemTermService.retireSystemTerm(
                                gameSystemId,
                                systemTermId,
                                username);

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Terminology: \""
                        + retiredTermName
                        + "\" retired successfully.");

                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToCoreConcepts(gameSystemId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToCoreConcepts(gameSystemId);
        }
        }


        // -------------------- System Term Restoration -------------------- //


        /**
         * Restores a retired Terminology belonging to the specified Game System.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to the Terminology page
         */
        @PostMapping("/{systemTermId}/restore")
        public String restoreSystemTerm(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

        try {

                String username = requireAuthenticatedUsername(authentication);

                String restoredTermName =
                        systemTermService.restoreSystemTerm(
                                gameSystemId,
                                systemTermId,
                                username);

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Terminology: \""
                        + restoredTermName
                        + "\" restored successfully.");

                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToCoreConcepts(gameSystemId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToCoreConcepts(gameSystemId);
        }
        }

        // -------------------- Model Helpers -------------------- //


        /**
         * Populates the model used by the Terminology configuration page.
         *
         * @param gameSystemId identifier of the Game System
         * @param username username of the authenticated user
         * @param systemTermForm form displayed on the page
         * @param returnUrl original workflow return destination
         * @param model model being populated
         */
        private void populateCoreConceptsModel(
                Long gameSystemId,
                String username,
                SystemTermForm systemTermForm,
                String returnUrl,
                Model model) {

                GameSystem gameSystem =
                        gameSystemService.getManageableGameSystemById(
                                gameSystemId,
                                username);

                model.addAttribute(
                        "gameSystem",
                        gameSystem);

                model.addAttribute(
                        "systemTerms",
                        systemTermService.getSystemTerms(
                                gameSystemId,
                                username));

                model.addAttribute(
                        "systemTermForm",
                        systemTermForm);

                model.addAttribute(
                        "returnUrl",
                        getSafeReturnUrl(returnUrl));
        }


        // -------------------- Authentication Helpers -------------------- //


        /**
         * Returns the authenticated username for a protected management request.
         *
         * @param authentication current authentication information
         * @return authenticated username
         * @throws SecurityException if no registered user is authenticated
         */
        private String requireAuthenticatedUsername(
                Authentication authentication) {

                if (authentication == null
                        || !authentication.isAuthenticated()
                        || authentication
                        instanceof AnonymousAuthenticationToken) {

                throw new SecurityException(
                        "Authentication is required.");
                }

                return authentication.getName();
        }


        // -------------------- Navigation Helpers -------------------- //


        /**
         * Determines whether the supplied return URL refers to a local
         * application path.
         *
         * @param returnUrl return URL being evaluated
         * @return true when the URL is a valid local path
         */
        private boolean isValidReturnUrl(
                String returnUrl) {

                return returnUrl != null
                        && !returnUrl.isBlank()
                        && returnUrl.startsWith("/")
                        && !returnUrl.startsWith("//");
        }


        /**
         * Returns a safe workflow return destination.
         *
         * @param returnUrl requested return destination
         * @return validated local return URL or Game Systems index
         */
        private String getSafeReturnUrl(
                String returnUrl) {

                return isValidReturnUrl(returnUrl)
                        ? returnUrl
                        : "/systems";
        }


        /**
         * Builds a redirect to the Terminology configuration page.
         *
         * @param gameSystemId identifier of the Game System
         * @return Terminology redirect
         */
        private String redirectToCoreConcepts(
                Long gameSystemId) {

                return "redirect:/systems/"
                        + gameSystemId
                        + "/terms";
        }
}