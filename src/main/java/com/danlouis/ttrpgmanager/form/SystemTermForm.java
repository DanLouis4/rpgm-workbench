package com.danlouis.ttrpgmanager.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Represents submitted form data for creating or editing a System Term.
 *
 * <p>System Terms define the user-facing terminology used by a Game System,
 * such as Race, Class, Skill, Spell, Ability, Attribute, or other concepts
 * defined by the Game Designer.</p>
 *
 * @author Daniel Louis
 */
public class SystemTermForm {

    @NotBlank(message = "Singular label is required.")
    @Size(max = 50, message = "Singular label cannot exceed 50 characters.")
    private String singularLabel;

    @Size(max = 50, message = "Plural label cannot exceed 50 characters.")
    private String pluralLabel;

    private String description;

    private boolean hasCharacterOptions = false;

    private boolean hasStats = false;

    @Min(value = 0, message = "Display order cannot be negative.")
    private Integer displayOrder = 0;


    /**
     * Constructs an empty SystemTermForm.
     */
    public SystemTermForm() {
    }

    /**
     * Returns the singular user-facing label.
     *
     * @return singular label
     */
    public String getSingularLabel() {
        return singularLabel;
    }


    /**
     * Sets the singular user-facing label.
     *
     * @param singularLabel singular label
     */
    public void setSingularLabel(String singularLabel) {
        this.singularLabel = singularLabel;
    }


    /**
     * Returns the plural user-facing label.
     *
     * @return plural label
     */
    public String getPluralLabel() {
        return pluralLabel;
    }


    /**
     * Sets the plural user-facing label.
     *
     * @param pluralLabel plural label
     */
    public void setPluralLabel(String pluralLabel) {
        this.pluralLabel = pluralLabel;
    }


    /**
     * Returns the description of the System Term.
     *
     * @return term description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Sets the description of the System Term.
     *
     * @param description term description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns whether the System Term has character options.
     *
     * @return true if the term has character options, false otherwise
     */
    public boolean isHasCharacterOptions() {
        return hasCharacterOptions;
    }

    /**
     * Sets whether the System Term has character options.
     *
     * @param hasCharacterOptions whether the term has character options
     */
    public void setHasCharacterOptions(boolean hasCharacterOptions) {
        this.hasCharacterOptions = hasCharacterOptions;
    }
    /**
     * Returns whether the System Term has stats.
     *
     * @return true if the term has stats, false otherwise
     */
    public boolean isHasStats() {
        return hasStats;
    }

    /**
     * Sets whether the System Term has stats.
     *
     * @param hasStats whether the term has stats
     */
    public void setHasStats(boolean hasStats) {
        this.hasStats = hasStats;
    }
    /**
     * Returns the configured display order.
     *
     * @return display order
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }


    /**
     * Sets the configured display order.
     *
     * @param displayOrder display order
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}