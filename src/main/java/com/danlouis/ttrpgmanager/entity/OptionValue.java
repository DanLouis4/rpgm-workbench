package com.danlouis.ttrpgmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Represents a selectable option value within an Option Group.
 *
 * <p>Option values define the individual choices available within a
 * configurable Option Group. Examples may include Human, Elf, Fighter,
 * Fireball, Athletics, or other values defined by the Game Designer.</p>
 *
 * <p>Each option value belongs to exactly one Option Group.</p>
 *
 * @author Daniel Louis
 */
@Entity
@Table(name = "option_values",uniqueConstraints = {
            @UniqueConstraint(name = "uk_option_values_group_name", columnNames = {"option_group_id","value_name"})}
)
public class OptionValue {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_value_id", nullable = false, updatable = false)
    private Long optionValueId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "option_group_id", nullable = false)
    private OptionGroup optionGroup;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_option_value_id")
    private OptionValue parentOptionValue;

    @Column(name = "value_name", nullable = false, length = 100)
    private String valueName;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;


    /**
     * Constructs an empty OptionValue instance for JPA.
     */
    public OptionValue() {
    }


    /**
     * Constructs an option value with its owning Option Group,
     * identifying information, and display order.
     *
     * @param optionGroup Option Group that owns the option value
     * @param parentOptionValue parent option value, if any
     * @param valueName display name of the option value
     * @param description explanation of the option value
     * @param displayOrder presentation order of the option value
     */
    public OptionValue(
            OptionGroup optionGroup,
            OptionValue parentOptionValue,
            String valueName,
            String description,
            Integer displayOrder) {

        this.optionGroup = optionGroup;
        this.parentOptionValue = parentOptionValue;
        this.valueName = valueName;
        this.description = description;
        this.displayOrder = displayOrder;
    }

    /**
     * Returns the ID of the option value.
     *
     * @return option value ID
     */
    public Long getOptionValueId() {
        return optionValueId;
    }

    /**
     * Sets the ID of the option value.
     *
     * @param optionValueId option value ID
     */
    public void setOptionValueId(Long optionValueId) {
        this.optionValueId = optionValueId;
    }

    /**
     * Returns the parent option value of this option value.
     *
     * @return parent option value
     */
    public OptionValue getParentOptionValue() {
        return parentOptionValue;
    }

    /**
     * Sets the parent option value of this option value.
     *
     * @param parentOptionValue parent option value
     */
    public void setParentOptionValue(OptionValue parentOptionValue) {
        this.parentOptionValue = parentOptionValue;
    }

    /**
     * Returns the owning Option Group of the option value.
     *
     * @return option group
     */
    public OptionGroup getOptionGroup() {
        return optionGroup;
    }

    /**
     * Sets the owning Option Group of the option value.
     *
     * @param optionGroup option group
     */
    public void setOptionGroup(OptionGroup optionGroup) {
        this.optionGroup = optionGroup;
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

    /**
     * Returns whether the option value is active.
     *
     * @return active status
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the option value is active.
     *
     * @param active active status
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}