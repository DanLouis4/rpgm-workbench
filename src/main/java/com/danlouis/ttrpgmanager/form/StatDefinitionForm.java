package com.danlouis.ttrpgmanager.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form object for capturing Stat Definition data from the user.
 *
 * <p>Stat Definitions identify the numbers, scores, ratings, ranks,
 * measurements, or other recorded values used by characters within
 * a Game System.</p>
 *
 * @author Daniel Louis
 */
public class StatDefinitionForm {

    private Long parentStatId;

    @NotBlank(message = "Stat name is required.")
    @Size(max = 100, message = "Stat name cannot exceed 100 characters.")
    private String statName;

    @Size(max = 20, message = "Abbreviation cannot exceed 20 characters.")
    private String abbreviation;

    private String description;

    @NotNull(message = "Display order is required.")
    @Min(value = 0, message = "Display order must be a non-negative integer.")
    private Integer displayOrder = 0;


    /**
     * Constructs an empty StatDefinitionForm.
     */
    public StatDefinitionForm() {
    }


    /**
     * Returns the ID of the parent Stat Definition.
     *
     * @return parent Stat Definition ID
     */
    public Long getParentStatId() {
        return parentStatId;
    }


    /**
     * Sets the ID of the parent Stat Definition.
     *
     * @param parentStatId parent Stat Definition ID
     */
    public void setParentStatId(Long parentStatId) {
        this.parentStatId = parentStatId;
    }


    /**
     * Returns the display name of the Stat Definition.
     *
     * @return Stat name
     */
    public String getStatName() {
        return statName;
    }


    /**
     * Sets the display name of the Stat Definition.
     *
     * @param statName Stat name
     */
    public void setStatName(String statName) {
        this.statName = statName;
    }


    /**
     * Returns the abbreviated Stat label.
     *
     * @return abbreviation
     */
    public String getAbbreviation() {
        return abbreviation;
    }


    /**
     * Sets the abbreviated Stat label.
     *
     * @param abbreviation abbreviation
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    /**
     * Returns the description of the Stat Definition.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Sets the description of the Stat Definition.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Returns the display order of the Stat Definition.
     *
     * @return display order
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }


    /**
     * Sets the display order of the Stat Definition.
     *
     * @param displayOrder display order
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}