package com.danlouis.ttrpgmanager.controller;

import java.util.Collections;
import java.util.Locale;
import java.util.Objects;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.danlouis.ttrpgmanager.form.RegistrationForm;
import com.danlouis.ttrpgmanager.service.GameSystemService;
import com.danlouis.ttrpgmanager.service.UserService;

import jakarta.validation.Valid;

/**
 * Handles web requests for user account functionality.
 *
 * <p>The UserController provides access to user-facing account operations
 * including registration, authentication pages, dashboard access, and account
 * management functions. User-related business logic is delegated to the
 * UserService.</p>
 *
 * @author Daniel Louis
 */
@Controller
public class UserController {

    private final UserService userService;
    private final GameSystemService gameSystemService;

    /**
     * Creates a UserController with the required user service.
     *
     * @param userService service used to perform user account operations
     */
    public UserController(UserService userService, GameSystemService gameSystemService) {
        this.userService = userService;
        this.gameSystemService = gameSystemService;
    }

    /**
     * Displays the user registration form.
     *
     * <p>Application roles available for public registration are retrieved
     * from the database and supplied to the registration template.</p>
     *
     * @param model model used to provide registration data to the view
     * @return the registration template
     */
    @GetMapping("/register")
    public String showRegistrationForm(Model model) {

        model.addAttribute(
                "registrationForm",
                new RegistrationForm());

        addRegistrationRoles(model);

        return "users/register";
    }

    /**
     * Processes a user registration request.
     *
     * <p>The submitted form is validated before account information is
     * passed to the UserService. Validation and registration errors are
     * returned to the registration form for display to the user.</p>
     *
     * @param registrationForm submitted registration form data
     * @param bindingResult result of Spring form validation
     * @param model model used to provide registration data to the view
     * @return a redirect to login after successful registration or the
     *         registration template when validation fails
     */
    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("registrationForm")
            RegistrationForm registrationForm,
            BindingResult bindingResult,
            Model model) {

        // Check if the password and confirm password match
        if (!Objects.equals(
                registrationForm.getPassword(),
                registrationForm.getConfirmPassword())) {

            bindingResult.rejectValue(
                    "confirmPassword",
                    "password.mismatch",
                    "Passwords do not match.");
        }

        if (bindingResult.hasErrors()) {
            addRegistrationRoles(model);
            return "users/register";
        }

        try {

            userService.registerUser(
                    registrationForm.getUsername(),
                    registrationForm.getEmail(),
                    registrationForm.getPassword(),
                    registrationForm.getRoleId());

            return "redirect:/login?status=registered";

        } catch (IllegalArgumentException exception) {

            bindingResult.reject(
                    "registration.error",
                    exception.getMessage());

            addRegistrationRoles(model);

            return "users/register";
        }
    }

    /**
     * Displays the user login page.
     *
     * <p>When a login status is supplied, the corresponding message and
     * presentation state are provided to the login template.</p>
     *
     * @param status optional login-page status identifier
     * @param model model used to provide login status information
     * @return the login template
     */
    @GetMapping("/login")
    public String showLoginForm(
            @RequestParam(required = false) String status,
            Model model) {

        LoginStatus loginStatus = LoginStatus.from(status);

        if (loginStatus != null) {
            model.addAttribute("loginStatus", loginStatus);
        }

        return "users/login";
    }

    /**
     * Displays the authenticated user's dashboard.
     *
     * <p>The dashboard provides the primary authenticated entry point to
     * Game System, Campaign, and Character content associated with the
     * current user.</p>
     *
     * @param authentication current authentication information
     * @param model model used to provide dashboard content to the view
     * @return the user dashboard template
     */
    @GetMapping("/dashboard")
    public String showDashboard(
            Authentication authentication,
            Model model) {

        boolean isGameDesigner =
                authentication.getAuthorities()
                        .stream()
                        .anyMatch(authority ->
                                authority.getAuthority()
                                        .equals("ROLE_GAME_DESIGNER"));

        if (isGameDesigner) {

            model.addAttribute(
                    "gameSystems",
                    gameSystemService.getOwnedGameSystems(
                            authentication.getName()));

        } else {

            model.addAttribute(
                    "gameSystems",
                    gameSystemService.getPublicGameSystems());
        }

        model.addAttribute(
                "campaigns",
                Collections.emptyList());

        model.addAttribute(
                "characters",
                Collections.emptyList());

        return "users/dashboard";
    }

    /**
     * Defines status messages displayed by the login page.
     */
    public enum LoginStatus {

        REGISTERED(
                "Your account was created successfully. Sign in with your new username and password.",
                "alert-success"),

        ERROR(
                "Invalid username or password.",
                "alert-danger"),

        LOGOUT(
                "You have been signed out successfully.",
                "alert-success");

        private final String message;
        private final String alertClass;

        /**
         * Creates a login status with its display message and Bootstrap
         * alert class.
         *
         * @param message message displayed to the user
         * @param alertClass Bootstrap alert class used to display the message
         */
        LoginStatus(String message, String alertClass) {
            this.message = message;
            this.alertClass = alertClass;
        }

        /**
         * Returns the display message associated with the status.
         *
         * @return the login status message
         */
        public String getMessage() {
            return message;
        }

        /**
         * Returns the Bootstrap alert class associated with the status.
         *
         * @return the Bootstrap alert class
         */
        public String getAlertClass() {
            return alertClass;
        }

        /**
         * Converts a request parameter into a recognized login status.
         *
         * @param status login status request parameter
         * @return the matching login status or null when the value is absent
         *         or unrecognized
         */
        public static LoginStatus from(String status) {

            if (status == null || status.isBlank()) {
                return null;
            }

            try {
                return LoginStatus.valueOf(
                        status.trim().toUpperCase(Locale.ROOT));

            } catch (IllegalArgumentException exception) {
                return null;
            }
        }
    }

        /**
     * Adds the application roles available for public registration to the
     * supplied model.
     *
     * @param model model used to provide role data to the registration view
     */
    private void addRegistrationRoles(Model model) {

        model.addAttribute(
                "roles",
                userService.getRegistrationRoles());
    }
}