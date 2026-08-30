package com.danlouis.ttrpgmanager.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents user-submitted form data used to create or update a game system.
 *
 * The form contains only values that may be modified through the browser
 * interface. Ownership, administrative flags, identifiers, and audit
 * information are managed by the application rather than supplied by users.
 */
public class GameSystemForm {

    @NotBlank(message = "Game system name is required.")
    @Size(max = 100, message = "Game system name cannot exceed 100 characters.")
    private String name;

    private String description;

    private boolean isPublic = true;

    /**
     * Constructs an empty GameSystemForm for Spring form binding.
     */
    public GameSystemForm() {
    }

    /**
     * Returns the game system name.
     *
     * @return the game system name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the game system name.
     *
     * @param name the game system name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the game system description.
     *
     * @return the game system description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the game system description.
     *
     * @param description the game system description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Indicates whether the game system should be publicly viewable.
     *
     * @return true when the game system is public; otherwise false
     */
    public boolean getIsPublic() {
        return isPublic;
    }

    /**
     * Sets whether the game system should be publicly viewable.
     *
     * @param isPublic true to make the game system publicly viewable
     */
    public void setIsPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }
}