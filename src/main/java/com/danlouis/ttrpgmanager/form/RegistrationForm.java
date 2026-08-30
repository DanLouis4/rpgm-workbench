package com.danlouis.ttrpgmanager.form;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Represents user-submitted registration form data.
 *
 * <p>The RegistrationForm defines the input and validation requirements
 * applied to public account registration before the submitted information
 * is passed to the user service.</p>
 *
 * @author Daniel Louis
 */
public class RegistrationForm {

    @NotBlank(message = "Username is required.")
    @Size(
        min = 3,
        max = 50,
        message = "Username must be between 3 and 50 characters."
    )
    private String username;

    @NotBlank(message = "Email address is required.")
    @Email(message = "Enter a valid email address.")
    @Size(
        max = 255,
        message = "Email address cannot exceed 255 characters."
    )
    private String email;

    @NotBlank(message = "Password is required.")
    @Size(
        min = 8,
        max = 72,
        message = "Password must be between 8 and 72 characters."
    )
    private String password;

    @NotBlank(message = "Password confirmation is required.")
    private String confirmPassword;

    @NotNull(message = "Select an account role.")
    private Byte roleId;

    /**
     * Creates an empty RegistrationForm for Spring form binding.
     */
    public RegistrationForm() {
    }

    /**
     * Gets the submitted username.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the submitted username.
     *
     * @param username the username to set
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Gets the submitted email address.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the submitted email address.
     *
     * @param email the email address to set
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Gets the submitted plaintext password.
     *
     * @return the password
     */
    public String getPassword() {
        return password;
    }

    /**
     * Sets the submitted plaintext password.
     *
     * @param password the password to set
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Gets the submitted password confirmation.
     *
     * @return the password confirmation
     */
    public String getConfirmPassword() {
        return confirmPassword;
    }

    /**
     * Sets the submitted password confirmation.
     *
     * @param confirmPassword the password confirmation to set
     */
    public void setConfirmPassword(String confirmPassword) {
        this.confirmPassword = confirmPassword;
    }

    /**
     * Gets the selected role identifier.
     *
     * @return the selected role ID
     */
    public Byte getRoleId() {
        return roleId;
    }

    /**
     * Sets the selected role identifier.
     *
     * @param roleId the role ID to set
     */
    public void setRoleId(Byte roleId) {
        this.roleId = roleId;
    }
}