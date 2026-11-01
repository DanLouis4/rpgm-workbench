package com.danlouis.ttrpgmanager.form;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Form object for capturing option group data from the user.
 * Contains fields for system term ID, group name, description,
 * allow multiple flag, and display order.
 * 
 * <p>This form is typically used in web controllers to bind user input
 * to an OptionGroup entity.</p>
 *
 * @author Daniel Louis
 */

public class OptionGroupForm {

    private Long systemTermId;

    @NotBlank(message = "Option group name is required.")
    @Size(max = 100, message = "Option group name cannot exceed 100 characters.")
    private String groupName;

    private String description;

    private boolean allowMultiple = false;

    @Min(value = 0, message = "Display order must be a non-negative integer.")
    private Integer displayOrder = 0;

    private Long parentOptionGroupId;


    /**
     * Constructs an empty OptionGroupForm.
     */
    public OptionGroupForm() {
    }

    /**
     * Returns the name of the option group.
     *
     * @return group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the name of the option group.
     *
     * @param groupName group name
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    /**
     * Returns the ID of the associated system term.
     *
     * @return system term ID
     */
    public Long getSystemTermId() {
        return systemTermId;
    }

    /**
     * Sets the ID of the associated system term.
     *
     * @param systemTermId system term ID
     */
    public void setSystemTermId(Long systemTermId) {
        this.systemTermId = systemTermId;
    }

    /**
     * Returns the description of the option group.
     *
     * @return description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the option group.
     *
     * @param description description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns whether multiple options are allowed.
     *
     * @return allow multiple flag
     */
    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    /**
     * Sets whether multiple options are allowed.
     *
     * @param allowMultiple allow multiple flag
     */
    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }

    /**
     * Returns the display order of the option group.
     *
     * @return display order
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the display order of the option group.
     *
     * @param displayOrder display order
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }
    /**
     * Returns the ID of the parent option group.
     *
     * @return parent option group ID
     */
    public Long getParentOptionGroupId() {
        return parentOptionGroupId;
    }

    /**
     * Sets the ID of the parent option group.
     *
     * @param parentOptionGroupId parent option group ID
     */
    public void setParentOptionGroupId(Long parentOptionGroupId) {
        this.parentOptionGroupId = parentOptionGroupId;
    }
}
