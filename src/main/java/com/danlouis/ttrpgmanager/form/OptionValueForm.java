package com.danlouis.ttrpgmanager.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * Form object for capturing option value data from the user.
 *
 * @author Daniel Louis
 */
public class OptionValueForm {

    private Long parentOptionValueId;

    @NotBlank(message = "Option value name is required.")
    @Size(max = 100, message = "Option value name cannot exceed 100 characters.")
    private String valueName;

    private String description;

    @NotNull(message = "Display order is required.")
    @Min(value = 0, message = "Display order must be a non-negative integer.")
    private Integer displayOrder = 0;

    /**
     * Constructs an empty OptionValueForm.
     */
    public OptionValueForm() {
    }
    
    /**
     * Returns the ID of the parent option value.
     *
     * @return parent option value ID
     */
    public Long getParentOptionValueId() {
        return parentOptionValueId;
    }

    /**
     * Sets the ID of the parent option value.
     *
     * @param parentOptionValueId parent option value ID
     */
    public void setParentOptionValueId(Long parentOptionValueId) {
        this.parentOptionValueId = parentOptionValueId;
    }

    /**
     * Returns the display name of the option value.
     *
     * @return value name
     */
    public String getValueName() {
        return valueName;
    }

    /**
     * Sets the display name of the option value.
     *
     * @param valueName value name
     */
    public void setValueName(String valueName) {
        this.valueName = valueName;
    }


    /**
     * Returns the description of the option value.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the option value.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Returns the display order of the option value.
     *
     * @return display order
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the display order of the option value.
     *
     * @param displayOrder display order
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
}