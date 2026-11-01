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

    @Size(max = 50, message = "Genre cannot exceed 50 characters.")
    private String genre;

    @Size(max = 100, message = "Ruleset family cannot exceed 100 characters.")
    private String rulesetFamily;

    @Size(max = 50, message = "Ruleset edition cannot exceed 50 characters.")
    private String rulesetEdition;

    private String description;

    private String systemOverview;

    private boolean isPublic = true;

    private boolean cloneable = false;

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
     * Returns the game system genre.
     *
     * @return the game system genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Sets the game system genre.
     *
     * @param genre the game system genre
     */
    public void setGenre(String genre) {
        this.genre = genre;
    }

    /**
     * Returns the ruleset family of the game system.
     *
     * @return the ruleset family
     */
    public String getRulesetFamily() {
        return rulesetFamily;
    }

    /**
     * Sets the ruleset family of the game system.
     *
     * @param rulesetFamily the ruleset family
     */
    public void setRulesetFamily(String rulesetFamily) {
        this.rulesetFamily = rulesetFamily;
    }

    /**
     * Returns the ruleset edition of the game system.
     *
     * @return the ruleset edition
     */
    public String getRulesetEdition() {
        return rulesetEdition;
    }

    /**
     * Sets the ruleset edition of the game system.
     *
     * @param rulesetEdition the ruleset edition
     */
    public void setRulesetEdition(String rulesetEdition) {
        this.rulesetEdition = rulesetEdition;
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
     * Returns the system overview of the game system.
     *
     * @return the system overview
     */
    public String getSystemOverview() {
        return systemOverview;
    }

    /**
     * Sets the system overview of the game system.
     *
     * @param systemOverview the system overview
     */
    public void setSystemOverview(String systemOverview) {
        this.systemOverview = systemOverview;
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

    /**
     * Indicates whether the game system may be cloned by other users.
     *
     * @return true when cloning is permitted; otherwise false
     */
    public boolean getCloneable() {
        return cloneable;
    }

    /**
     * Sets whether the game system may be cloned by other users.
     *
     * @param cloneable true to permit cloning
     */
    public void setCloneable(boolean cloneable) {
        this.cloneable = cloneable;
    }

}