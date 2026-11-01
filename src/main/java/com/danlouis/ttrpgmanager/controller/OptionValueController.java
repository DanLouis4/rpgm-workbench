package com.danlouis.ttrpgmanager.controller;

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

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.OptionValue;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.form.OptionValueForm;
import com.danlouis.ttrpgmanager.service.GameSystemService;
import com.danlouis.ttrpgmanager.service.OptionGroupService;
import com.danlouis.ttrpgmanager.service.OptionValueService;
import com.danlouis.ttrpgmanager.service.SystemTermService;

import jakarta.validation.Valid;

/**
 * Handles web requests for Game System Option Value management.
 *
 * <p>The controller allows authorized Game System owners to view,
 * create, edit, retire and restore Option Values belonging to an Option Group.
 * Option Values may optionally reference another Option Value in the
 * same Option Group as their parent.</p>
 *
 * @author Daniel Louis
 */
@Controller
@RequestMapping(
        "/systems/{gameSystemId}/terms/{systemTermId}"
        + "/groups/{optionGroupId}/values"
)
public class OptionValueController {

        private static final String UNAUTHORIZED_REDIRECT =
                "redirect:/systems?status=unauthorized";

        private static final String UNAVAILABLE_REDIRECT =
                "redirect:/systems?status=unavailable";

        private final OptionValueService optionValueService;
        private final OptionGroupService optionGroupService;
        private final SystemTermService systemTermService;
        private final GameSystemService gameSystemService;


        /**
         * Constructs the OptionValueController with its required services.
         *
         * @param optionValueService service used to manage Option Values
         * @param optionGroupService service used to manage Option Groups
         * @param systemTermService service used to manage System Terms
         * @param gameSystemService service used to manage Game Systems
         */
        public OptionValueController(
                OptionValueService optionValueService,
                OptionGroupService optionGroupService,
                SystemTermService systemTermService,
                GameSystemService gameSystemService) {

                this.optionValueService = optionValueService;
                this.optionGroupService = optionGroupService;
                this.systemTermService = systemTermService;
                this.gameSystemService = gameSystemService;
        }


                // -------------------- Option Values Page -------------------- //


                /**
                 * Displays the Option Value configuration page for an Option Group.
                 *
                 * @param gameSystemId identifier of the Game System
                 * @param systemTermId identifier of the System Term
                 * @param optionGroupId identifier of the Option Group
                 * @param returnUrl original workflow return destination
                 * @param authentication current authentication information
                 * @param model model used to supply page data
                 * @return Option Values page or redirect when unavailable
                 */
                @GetMapping
                public String showOptionValues(
                        @PathVariable Long gameSystemId,
                        @PathVariable Long systemTermId,
                        @PathVariable Long optionGroupId,
                        @RequestParam(required = false) String returnUrl,
                        Authentication authentication,
                        Model model) {

                try {

                        String username =
                                requireAuthenticatedUsername(
                                        authentication);

                        OptionValueForm optionValueForm =
                                new OptionValueForm();

                        populateOptionValuesModel(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username,
                                optionValueForm,
                                returnUrl,
                                model);

                        model.addAttribute("formMode", "create");

                        return "systems/options/values";

                } catch (SecurityException exception) {

                        return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                        return UNAVAILABLE_REDIRECT;
                }
        }


        // -------------------- Option Value Creation -------------------- //


        /**
         * Creates a new Option Value within the specified Option Group.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param optionValueForm submitted Option Value data
         * @param bindingResult result of form validation
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used when redisplaying validation errors
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to Option Values or page when validation fails
         */
        @PostMapping
        public String createOptionValue(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @Valid
                @ModelAttribute("optionValueForm")
                OptionValueForm optionValueForm,
                BindingResult bindingResult,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model,
                RedirectAttributes redirectAttributes) {

                try {

                        String username =
                                requireAuthenticatedUsername(
                                        authentication);

                        if (bindingResult.hasErrors()) {

                        populateOptionValuesModel(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username,
                                optionValueForm,
                                returnUrl,
                                model);

                        model.addAttribute("formMode", "create");

                        return "systems/options/values";
                        }

                        OptionValue optionValue = optionValueService
                                .createOptionValue(
                                        gameSystemId,
                                        systemTermId,
                                        optionGroupId,
                                        optionValueForm.getParentOptionValueId(),
                                        username,
                                        optionValueForm.getValueName(),
                                        optionValueForm.getDescription(),
                                        optionValueForm.getDisplayOrder());

                        redirectAttributes.addFlashAttribute(
                                "message",
                                "Option Value: \""
                                + optionValue.getValueName()
                                + "\" added successfully.");

                        redirectAttributes.addAttribute(
                                "returnUrl",
                                getSafeReturnUrl(returnUrl));

                        return redirectToOptionValues(
                                gameSystemId,
                                systemTermId,
                                optionGroupId);

                } catch (SecurityException exception) {

                        return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                        bindingResult.reject("optionValue", exception.getMessage());

                        String username =
                                requireAuthenticatedUsername(
                                        authentication);

                        populateOptionValuesModel(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username,
                                optionValueForm,
                                returnUrl,
                                model);

                        model.addAttribute("formMode", "create");

                        return "systems/options/values";
                }
        }


        // -------------------- Option Value Editing -------------------- //


        /**
         * Displays the Option Values page with the selected Option Value loaded
         * for editing.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param optionValueId identifier of the Option Value
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used to supply page data
         * @return Option Values page or redirect when unavailable
         */
        @GetMapping("/{optionValueId}/edit")
        public String showEditOptionValue(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @PathVariable Long optionValueId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model) {

                try {

                        String username =
                                requireAuthenticatedUsername(
                                        authentication);

                        OptionGroup optionGroup = optionGroupService
                                .getOptionGroupById(
                                        gameSystemId,
                                        systemTermId,
                                        optionGroupId,
                                        username);

                        OptionValue optionValue =
                                optionValueService.getOptionValueById(
                                        optionValueId,
                                        optionGroup);

                        OptionValueForm optionValueForm =
                                new OptionValueForm();

                        optionValueForm.setValueName(optionValue.getValueName());
                        optionValueForm.setDescription(optionValue.getDescription());
                        optionValueForm.setDisplayOrder(optionValue.getDisplayOrder());

                        if (optionValue.getParentOptionValue() != null) {

                        optionValueForm
                                .setParentOptionValueId(
                                        optionValue
                                                .getParentOptionValue()
                                                .getOptionValueId());
                        }

                        populateOptionValuesModel(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username,
                                optionValueForm,
                                returnUrl,
                                model);

                        model.addAttribute("formMode", "edit");
                        model.addAttribute("optionValueId", optionValueId);

                        return "systems/options/values";

                } catch (SecurityException exception) {

                        return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                        return UNAVAILABLE_REDIRECT;
                }
        }


        /**
         * Processes changes to an existing Option Value.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param optionValueId identifier of the Option Value
         * @param optionValueForm submitted Option Value data
         * @param bindingResult result of form validation
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param model model used when redisplaying validation errors
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to Option Values or page when validation fails
         */
        @PostMapping("/{optionValueId}/edit")
        public String updateOptionValue(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @PathVariable Long optionValueId,
                @Valid
                @ModelAttribute("optionValueForm")
                OptionValueForm optionValueForm,
                BindingResult bindingResult,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                Model model,
                RedirectAttributes redirectAttributes) {

                try {

                        String username =
                                requireAuthenticatedUsername(
                                        authentication);

                        if (bindingResult.hasErrors()) {

                        populateOptionValuesModel(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username,
                                optionValueForm,
                                returnUrl,
                                model);

                        model.addAttribute("formMode", "edit");
                        model.addAttribute("optionValueId", optionValueId);

                        return "systems/options/values";
                        }

                        OptionValue optionValue = optionValueService
                                .updateOptionValue(
                                        gameSystemId,
                                        systemTermId,
                                        optionGroupId,
                                        optionValueId,
                                        optionValueForm.getParentOptionValueId(),
                                        username,
                                        optionValueForm.getValueName(),
                                        optionValueForm.getDescription(),
                                        optionValueForm.getDisplayOrder());

                        redirectAttributes.addFlashAttribute(
                                "message",
                                "Option Value: \""
                                + optionValue.getValueName()
                                + "\" updated successfully.");

                        redirectAttributes.addAttribute(
                                "returnUrl",
                                getSafeReturnUrl(returnUrl));

                        return redirectToOptionValues(
                                gameSystemId,
                                systemTermId,
                                optionGroupId);

                } catch (SecurityException exception) {

                        return UNAUTHORIZED_REDIRECT;

                } catch (IllegalArgumentException exception) {

                        bindingResult.reject("optionValue", exception.getMessage());

                        String username =
                                requireAuthenticatedUsername(
                                        authentication);

                        populateOptionValuesModel(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username,
                                optionValueForm,
                                returnUrl,
                                model);

                        model.addAttribute("formMode", "edit");
                        model.addAttribute("optionValueId", optionValueId);

                        return "systems/options/values";
                }
        }


        // -------------------- Option Value Retirement -------------------- //


        /**
         * Retires an active Option Value belonging to the specified Option Group.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param optionValueId identifier of the Option Value
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to the Option Values page
         */
        @PostMapping("/{optionValueId}/retire")
        public String retireOptionValue(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @PathVariable Long optionValueId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

        try {

                String username = requireAuthenticatedUsername(authentication);

                String retiredValueName =
                        optionValueService.retireOptionValue(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                optionValueId,
                                username);

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Option Value: \""
                        + retiredValueName
                        + "\" retired successfully.");

                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionValues(gameSystemId, systemTermId, optionGroupId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionValues(gameSystemId, systemTermId, optionGroupId);
        }
        }


        // -------------------- Option Value Restoration -------------------- //


        /**
         * Restores a retired Option Value belonging to the specified Option Group.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param optionValueId identifier of the Option Value
         * @param returnUrl original workflow return destination
         * @param authentication current authentication information
         * @param redirectAttributes attributes used for redirect feedback
         * @return redirect to the Option Values page
         */
        @PostMapping("/{optionValueId}/restore")
        public String restoreOptionValue(
                @PathVariable Long gameSystemId,
                @PathVariable Long systemTermId,
                @PathVariable Long optionGroupId,
                @PathVariable Long optionValueId,
                @RequestParam(required = false) String returnUrl,
                Authentication authentication,
                RedirectAttributes redirectAttributes) {

        try {

                String username = requireAuthenticatedUsername(authentication);

                String restoredValueName =
                        optionValueService.restoreOptionValue(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                optionValueId,
                                username);

                redirectAttributes.addFlashAttribute(
                        "message",
                        "Option Value: \""
                        + restoredValueName
                        + "\" restored successfully.");

                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionValues(gameSystemId, systemTermId, optionGroupId);

        } catch (SecurityException exception) {

                return UNAUTHORIZED_REDIRECT;

        } catch (IllegalArgumentException exception) {

                redirectAttributes.addFlashAttribute("error", exception.getMessage());
                redirectAttributes.addAttribute("returnUrl", getSafeReturnUrl(returnUrl));

                return redirectToOptionValues(gameSystemId, systemTermId, optionGroupId);
        }
        }


        // -------------------- Model Helpers -------------------- //


        /**
         * Populates the model used by the Option Value configuration page.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param username username of the authenticated user
         * @param optionValueForm form displayed on the page
         * @param returnUrl original workflow return destination
         * @param model model to populate
         */
        private void populateOptionValuesModel(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                String username,
                OptionValueForm optionValueForm,
                String returnUrl,
                Model model) {

                GameSystem gameSystem = gameSystemService.getManageableGameSystemById(gameSystemId,username);

                SystemTerm systemTerm = systemTermService.getSystemTermById(gameSystemId, systemTermId, username);

                OptionGroup optionGroup = optionGroupService.getOptionGroupById(gameSystemId, systemTermId, optionGroupId, username);

                List<OptionValue> optionValues = optionValueService.getOptionValuesForGroup(optionGroup);

                OptionGroup parentOptionGroup = optionGroup.getParentOptionGroup();

                List<OptionValue> parentOptionValues = List.of();

                List<OptionValue> parentOptionValuesForDisplay =
                        List.of();

                Map<Long, List<OptionValue>> optionValuesByParentValue =
                        new LinkedHashMap<>();

                String parentValueLabel = null;


                if (parentOptionGroup != null) {

                        parentOptionValues = optionValueService.getActiveOptionValuesForGroup(parentOptionGroup);

                        parentOptionValuesForDisplay = optionValueService.getOptionValuesForGroup(parentOptionGroup);

                        parentValueLabel = "Parent " + systemTerm.getSingularLabel();


                        for (OptionValue optionValue : optionValues) {

                                OptionValue parentValue = optionValue.getParentOptionValue();

                                if (parentValue != null) {

                                optionValuesByParentValue
                                        .computeIfAbsent(
                                                parentValue.getOptionValueId(),
                                                id -> new ArrayList<>())
                                        .add(optionValue);
                                }
                        }
                }

                model.addAttribute("gameSystem", gameSystem);
                model.addAttribute("systemTerm", systemTerm);
                model.addAttribute("optionGroup", optionGroup);
                model.addAttribute("optionValues", optionValues);
                model.addAttribute("parentOptionGroup", parentOptionGroup);
                model.addAttribute("parentOptionValues", parentOptionValues);
                model.addAttribute("optionValuesByParentValue", optionValuesByParentValue);
                model.addAttribute("parentOptionValuesForDisplay", parentOptionValuesForDisplay);
                model.addAttribute("parentValueLabel", parentValueLabel);
                model.addAttribute("optionValueForm", optionValueForm);
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
        private String requireAuthenticatedUsername(
                Authentication authentication) {

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {

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
         * Builds a redirect to the Option Value configuration page.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @return redirect URL
         */
        private String redirectToOptionValues(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId) {

        return "redirect:/systems/"
                + gameSystemId
                + "/terms/"
                + systemTermId
                + "/groups/"
                + optionGroupId
                + "/values";
        }
}