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
 * Represents a configurable option group used by a Game System.
 *
 * <p>Option groups organize selectable values associated with a System Term.
 * Examples may include Races, Classes, Combat Skills, Arcane Spells, or other
 * groups defined by the Game Designer.</p>
 *
 * <p>Each option group belongs to exactly one System Term and determines
 * whether a character may select one or multiple values from the group.</p>
 *
 * @author Daniel Louis
 */
@Entity
@Table( name = "option_groups",
        uniqueConstraints = {
            @UniqueConstraint(
                    name = "uk_option_groups_term_name",
                    columnNames = {"system_term_id", "group_name"}
            )
        }
)
public class OptionGroup {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "option_group_id", nullable = false, updatable = false)
    private Long optionGroupId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "system_term_id", nullable = false)
    private SystemTerm systemTerm;

    @Column(name = "group_name", nullable = false, length = 100)
    private String groupName;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "allow_multiple", nullable = false)
    private boolean allowMultiple = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active",nullable = false)
    private boolean active = true;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_option_group_id")
    private OptionGroup parentOptionGroup;

    /**
     * Constructs an empty OptionGroup instance for JPA.
     */
    public OptionGroup() {
    }

    /**
     * Constructs an option group with its System Term,
     * identifying information, and selection behavior.
     *
     * @param systemTerm System Term that owns the option group
     * @param groupName display name of the option group
     * @param description explanation of the option group
     * @param allowMultiple whether multiple values may be selected
     * @param displayOrder presentation order of the option group
     */
    public OptionGroup(
            SystemTerm systemTerm,
            String groupName,
            String description,
            boolean allowMultiple,
            Integer displayOrder,
            OptionGroup parentOptionGroup
        ) {

        this.systemTerm = systemTerm;
        this.groupName = groupName;
        this.description = description;
        this.allowMultiple = allowMultiple;
        this.displayOrder = displayOrder;
        this.parentOptionGroup = parentOptionGroup;
    }

    /**
     * Returns the unique identifier for the option group.
     *
     * @return the option group ID
     */
    public Long getOptionGroupId() {
        return optionGroupId;
    }

    /**
     * Sets the unique identifier for the option group.
     *
     * @param optionGroupId the option group ID
     */
    public void setOptionGroupId(Long optionGroupId) {
        this.optionGroupId = optionGroupId;
    }

    /**
     * Returns the System Term that owns the option group.
     *
     * @return the System Term
     */
    public SystemTerm getSystemTerm() {
        return systemTerm;
    }

    /**
     * Sets the System Term that owns the option group.
     *
     * @param systemTerm the System Term
     */
    public void setSystemTerm(SystemTerm systemTerm) {
        this.systemTerm = systemTerm;
    }
    /**
     * Returns the display name of the option group.
     *
     * @return the option group name
     */
    public String getGroupName() {
        return groupName;
    }

    /**
     * Sets the display name of the option group.
     *
     * @param groupName the option group name
     */
    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }


    /**
     * Returns the description of the option group.
     *
     * @return the option group description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the option group.
     *
     * @param description the option group description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Indicates whether multiple values may be selected
     * from the option group.
     *
     * @return true when multiple selections are allowed; otherwise false
     */
    public boolean isAllowMultiple() {
        return allowMultiple;
    }

    /**
     * Sets whether multiple values may be selected
     * from the option group.
     *
     * @param allowMultiple true to allow multiple selections
     */
    public void setAllowMultiple(boolean allowMultiple) {
        this.allowMultiple = allowMultiple;
    }


    /**
     * Returns the display order of the option group.
     *
     * @return the display order
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the display order of the option group.
     *
     * @param displayOrder the display order
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Returns whether the Option Group is active.
     *
     * @return true when active; otherwise false
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the Option Group is active.
     *
     * @param active whether the Option Group is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }

    /**
     * Returns the parent option group of this option group.
     *
     * @return the parent option group
     */
    public OptionGroup getParentOptionGroup() {
        return parentOptionGroup;
    }

    /**
     * Sets the parent option group of this option group.
     *
     * @param parentOptionGroup the parent option group
     */
    public void setParentOptionGroup(OptionGroup parentOptionGroup) {
        this.parentOptionGroup = parentOptionGroup;
    }
}