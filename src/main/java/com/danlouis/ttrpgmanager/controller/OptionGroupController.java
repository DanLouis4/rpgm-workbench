package com.danlouis.ttrpgmanager.controller;

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

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.form.OptionGroupForm;
import com.danlouis.ttrpgmanager.service.GameSystemService;
import com.danlouis.ttrpgmanager.service.OptionGroupService;
import com.danlouis.ttrpgmanager.service.OptionValueService;
import com.danlouis.ttrpgmanager.service.SystemTermService;

import jakarta.validation.Valid;

/**
 * Handles web requests for Game System Option Group management.
 *
 * <p>The controller allows authorized Game System owners to view,
 * create, edit, retire, and restore Option Groups belonging to a System Term.</p>
 *
 * @author Daniel Louis
 */
@Controller
@RequestMapping(
        "/systems/{gameSystemId}/terms/{systemTermId}/groups"
)
public class OptionGroupController {

        private static final String UNAUTHORIZED_REDIRECT =
                "redirect:/systems?status=unauthorized";

        private static final String UNAVAILABLE_REDIRECT =
                "redirect:/systems?status=unavailable";

        private final OptionGroupService optionGroupService;
        private final SystemTermService systemTermService;
        private final GameSystemService gameSystemService;
        private final OptionValueService optionValueService;


        /**
         * Constructs the OptionGroupController with its required services.
         *
         * @param optionGroupService service used to manage Option Groups
         * @param systemTermService service used to manage System Terms
         * @param gameSystemService service used to manage Game Systems
         */
        public OptionGroupController(
                OptionGroupService optionGroupService,
                SystemTermService systemTermService,
                GameSystemService gameSystemService,
                OptionValueService optionValueService) {

        this.optionGroupService = optionGroupService;
        this.systemTermService = systemTermService;
        this.gameSystemService = gameSystemService;
        this.optionValueService = optionValueService;
        }


        // -------------------- Option Groups Page -------------------- //


        /**
         * Displays the Option Group configuration page for a System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used to supply page data
         * @return Option Groups page or redirect when unavailable
         */
        @GetMapping
        public String showOptionGroups(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model) {

        try {

                String username =
                        requireAuthenticatedUsername(authentication);

                OptionGroupForm optionGroupForm =
                        new OptionGroupForm();

                optionGroupForm.setSystemTermId(systemTermId);

                populateOptionGroupsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        optionGroupForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "create");

                return "systems/options/index";

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                return UNAVAILABLE_REDIRECT;
        }
        }


        // -------------------- Option Group Creation -------------------- //


        /**
         * Creates a new Option Group for the specified System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupForm submitted Option Group data
         * @param bindingResult result of form validation
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used when redisplaying validation errors
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to Option Groups or page when validation fails
         */
        @PostMapping
        public String createOptionGroup(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @Valid
                @ModelAttribute("optionGroupForm")
                OptionGroupForm optionGroupForm,
                BindingResult bindingResult,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model,
                RedirectAttributes redirectAttributes) {

        String username =
                requireAuthenticatedUsername(authentication);

        if (bindingResult.hasErrors()) {

                populateOptionGroupsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        optionGroupForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "create");

                return "systems/options/index";
        }

        try {

                OptionGroup optionGroup =
                        optionGroupService.createOptionGroup(
                                gameSystemId,
                                systemTermId,
                                username,
                                optionGroupForm.getGroupName(),
                                optionGroupForm.getDescription(),
                                optionGroupForm.isAllowMultiple(),
                                optionGroupForm.getDisplayOrder(),
                                optionGroupForm.getParentOptionGroupId());

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Option Group: \""
                        + optionGroup.getGroupName()
                        + "\" added successfully.");

                redirectAttributes.addAttribute(
                        "returnUrl",
                        getSafeReturnUrl(returnUrl));

                return redirectToOptionGroups(
                        gameSystemId,
                        systemTermId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                bindingResult.reject(
                        "optionGroup",
                        exception.getMessage());

                populateOptionGroupsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        optionGroupForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "create");

                return "systems/options/index";
        }
        }


        // -------------------- Option Group Editing -------------------- //


        /**
         * Displays the Option Groups page with the selected group loaded for editing.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used to supply page data
         * @return Option Groups page or redirect when unavailable
         */
        @GetMapping("/{optionGroupId}/edit")
        public String showEditOptionGroup(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model) {

        try {

                String username =
                        requireAuthenticatedUsername(authentication);

                OptionGroup optionGroup =
                        optionGroupService.getOptionGroupById(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username);

                OptionGroupForm optionGroupForm =
                        new OptionGroupForm();

                optionGroupForm.setSystemTermId(systemTermId);

                optionGroupForm.setGroupName(
                        optionGroup.getGroupName());

                optionGroupForm.setDescription(
                        optionGroup.getDescription());

                optionGroupForm.setAllowMultiple(
                        optionGroup.isAllowMultiple());

                optionGroupForm.setDisplayOrder(
                        optionGroup.getDisplayOrder());

                if (optionGroup.getParentOptionGroup() != null) {
                optionGroupForm.setParentOptionGroupId(
                        optionGroup.getParentOptionGroup().getOptionGroupId());
                }

                populateOptionGroupsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        optionGroupForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "edit");

                model.addAttribute(
                        "optionGroupId",
                        optionGroupId);

                return "systems/options/index";

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                return UNAVAILABLE_REDIRECT;
        }
        }


        /**
         * Processes changes to an existing Option Group.
         */
        @PostMapping("/{optionGroupId}/edit")
        public String updateOptionGroup(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @Valid
                @ModelAttribute("optionGroupForm")
                OptionGroupForm optionGroupForm,
                BindingResult bindingResult,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model,
                RedirectAttributes redirectAttributes) {

        String username =
                requireAuthenticatedUsername(authentication);

        if (bindingResult.hasErrors()) {

                populateOptionGroupsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        optionGroupForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "edit");

                model.addAttribute(
                        "optionGroupId",
                        optionGroupId);

                return "systems/options/index";
        }

        try {

                OptionGroup optionGroup =
                        optionGroupService.updateOptionGroup(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username,
                                optionGroupForm.getGroupName(),
                                optionGroupForm.getDescription(),
                                optionGroupForm.isAllowMultiple(),
                                optionGroupForm.getDisplayOrder(),
                                optionGroupForm.getParentOptionGroupId());

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Option Group: \""
                        + optionGroup.getGroupName()
                        + "\" updated successfully.");

                redirectAttributes.addAttribute(
                        "returnUrl",
                        getSafeReturnUrl(returnUrl));

                return redirectToOptionGroups(
                        gameSystemId,
                        systemTermId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                bindingResult.reject(
                        "optionGroup",
                        exception.getMessage());

                populateOptionGroupsModel(
                        gameSystemId,
                        systemTermId,
                        username,
                        optionGroupForm,
                        returnUrl,
                        model);

                model.addAttribute(
                        "formMode",
                        "edit");

                model.addAttribute(
                        "optionGroupId",
                        optionGroupId);

                return "systems/options/index";
        }
        }


        // -------------------- Option Group Retirement -------------------- //


        /**
         * Retires an active Option Group belonging to the specified System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to the Option Groups page
         */
        @PostMapping("/{optionGroupId}/retire")
        public String retireOptionGroup(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

        try {

                String username = requireAuthenticatedUsername(authentication);

                String retiredGroupName =
                        optionGroupService.retireOptionGroup(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username);

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Option Group: \""
                        + retiredGroupName
                        + "\" retired successfully.");

                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionGroups(gameSystemId, systemTermId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionGroups(gameSystemId, systemTermId);
        }
        }


        // -------------------- Option Group Restoration -------------------- //


        /**
         * Restores a retired Option Group belonging to the specified System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to the Option Groups page
         */
        @PostMapping("/{optionGroupId}/restore")
        public String restoreOptionGroup(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

        try {

                String username = requireAuthenticatedUsername(authentication);

                String restoredGroupName =
                        optionGroupService.restoreOptionGroup(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username);

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Option Group: \""
                        + restoredGroupName
                        + "\" restored successfully.");

                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionGroups(gameSystemId, systemTermId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionGroups(gameSystemId, systemTermId);
        }
        }

        // -------------------- Model Helpers -------------------- //


        /**
         * Populates the model used by the Option Group configuration page.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param username username of the authenticated user
         * @param optionGroupForm form displayed on the page
         * @param returnUrl original workflow return destination
         * @param model model to populate
         */
        private void populateOptionGroupsModel(
                Long gameSystemId,
                Long systemTermId,
                String username,
                OptionGroupForm optionGroupForm,
                String returnUrl,
                Model model) {

        GameSystem gameSystem = gameSystemService.getManageableGameSystemById(gameSystemId, username);

        SystemTerm systemTerm = systemTermService.getSystemTermById(gameSystemId, systemTermId, username);

        List<OptionGroup> optionGroups = optionGroupService.getOptionGroups(gameSystemId, systemTermId, username);

        List<OptionGroup> parentOptionGroups = optionGroupService.getActiveOptionGroupsForTerm(systemTerm);

        Map<Long, Long> valueCountsByGroup = new LinkedHashMap<>();

        for (OptionGroup optionGroup : optionGroups) {

                long valueCount = optionValueService.countActiveOptionValues(optionGroup);

                valueCountsByGroup.put(optionGroup.getOptionGroupId(), valueCount);
        }

        model.addAttribute("valueCountsByGroup", valueCountsByGroup);

        optionGroupForm.setSystemTermId(systemTermId);

        model.addAttribute("gameSystem", gameSystem);
        model.addAttribute("systemTerm", systemTerm);
        model.addAttribute("optionGroups", optionGroups);
        model.addAttribute("parentOptionGroups", parentOptionGroups);
        model.addAttribute("optionGroupForm", optionGroupForm);
        model.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));
        }


        // -------------------- Authentication Helpers -------------------- //


        /**
         * Returns the authenticated username for a protected management request.
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
         */
        private String getSafeReturnUrl(
                String returnUrl) {

        return isValidReturnUrl(returnUrl)
                ? returnUrl
                : "/systems";
        }


        /**
         * Builds a redirect to the Option Group configuration page.
         */
        private String redirectToOptionGroups(
                Long gameSystemId,
                Long systemTermId) {

        return "redirect:/systems/"
                + gameSystemId
                + "/terms/"
                + systemTermId
                + "/groups";
        }
}