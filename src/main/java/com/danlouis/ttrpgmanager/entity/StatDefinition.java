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

/**
 * Represents a statistic definition used by a Game System.
 *
 * <p>Stat definitions identify the numbers, scores, ratings, ranks,
 * measurements, or other recorded values used by characters in a
 * Game System. Examples may include Strength, Armor Class, Hit Points,
 * Initiative, Sanity, Basic Speed, or other values defined by the
 * Game Designer.</p>
 *
 * <p>RPGM Workbench treats statistic values as descriptive value pairs
 * rather than enforcing system-specific calculations, ranges, formulas,
 * or datatypes. The rules governing each statistic are documented by
 * the Game Designer within the Game System.</p>
 *
 * <p>A Stat Definition may optionally belong beneath another Stat
 * Definition to support related or hierarchical values such as
 * Current Hit Points and Maximum Hit Points.</p>
 *
 * @author Daniel Louis
 */
@Entity
@Table(name = "stat_definitions")
public class StatDefinition {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(
            name = "stat_definition_id",
            nullable = false,
            updatable = false
    )
    private Long statDefinitionId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "system_term_id",
            nullable = false
    )
    private SystemTerm systemTerm;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_stat_id")
    private StatDefinition parentStat;

    @Column(
            name = "stat_name",
            nullable = false,
            length = 100
    )
    private String statName;

    @Column(
            name = "abbreviation",
            length = 20
    )
    private String abbreviation;

    @Lob
    @Column(
            name = "description",
            columnDefinition = "LONGTEXT"
    )
    private String description;

    @Column(
            name = "display_order",
            nullable = false
    )
    private Integer displayOrder = 0;

    @Column(
            name = "is_active",
            nullable = false
    )
    private boolean active = true;


    /**
     * Constructs an empty StatDefinition instance for JPA.
     */
    public StatDefinition() {
    }


    /**
     * Constructs a Stat Definition belonging to the specified System Term.
     *
     * @param systemTerm System Term that owns the Stat Definition
     * @param parentStat parent Stat Definition, if any
     * @param statName display name of the statistic
     * @param abbreviation optional abbreviated statistic label
     * @param description explanation of the statistic
     * @param displayOrder presentation order of the statistic
     */
    public StatDefinition(
            SystemTerm systemTerm,
            StatDefinition parentStat,
            String statName,
            String abbreviation,
            String description,
            Integer displayOrder) {

        this.systemTerm = systemTerm;
        this.parentStat = parentStat;
        this.statName = statName;
        this.abbreviation = abbreviation;
        this.description = description;
        this.displayOrder = displayOrder;
        this.active = true;
    }


    /**
     * Returns the unique identifier of the Stat Definition.
     *
     * @return Stat Definition ID
     */
    public Long getStatDefinitionId() {
        return statDefinitionId;
    }


    /**
     * Sets the unique identifier of the Stat Definition.
     *
     * @param statDefinitionId Stat Definition ID
     */
    public void setStatDefinitionId(Long statDefinitionId) {
        this.statDefinitionId = statDefinitionId;
    }


    /**
     * Returns the System Term that owns the Stat Definition.
     *
     * @return associated System Term
     */
    public SystemTerm getSystemTerm() {
        return systemTerm;
    }


    /**
     * Sets the System Term that owns the Stat Definition.
     *
     * @param systemTerm associated System Term
     */
    public void setSystemTerm(SystemTerm systemTerm) {
        this.systemTerm = systemTerm;
    }


    /**
     * Returns the parent Stat Definition.
     *
     * @return parent Stat Definition, or null when none exists
     */
    public StatDefinition getParentStat() {
        return parentStat;
    }


    /**
     * Sets the parent Stat Definition.
     *
     * @param parentStat parent Stat Definition
     */
    public void setParentStat(StatDefinition parentStat) {
        this.parentStat = parentStat;
    }


    /**
     * Returns the display name of the statistic.
     *
     * @return statistic name
     */
    public String getStatName() {
        return statName;
    }


    /**
     * Sets the display name of the statistic.
     *
     * @param statName statistic name
     */
    public void setStatName(String statName) {
        this.statName = statName;
    }


    /**
     * Returns the abbreviated statistic label.
     *
     * @return abbreviation, or null when none is defined
     */
    public String getAbbreviation() {
        return abbreviation;
    }


    /**
     * Sets the abbreviated statistic label.
     *
     * @param abbreviation abbreviation
     */
    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }


    /**
     * Returns the description of the statistic.
     *
     * @return statistic description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Sets the description of the statistic.
     *
     * @param description statistic description
     */
    public void setDescription(String description) {
        this.description = description;
    }


    /**
     * Returns the display order of the statistic.
     *
     * @return display order
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }


    /**
     * Sets the display order of the statistic.
     *
     * @param displayOrder display order
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }


    /**
     * Returns whether the Stat Definition is active.
     *
     * <p>Retired definitions remain available to existing Character
     * records but should not be offered for new selections.</p>
     *
     * @return true when active; otherwise false
     */
    public boolean isActive() {
        return active;
    }


    /**
     * Sets whether the Stat Definition is active.
     *
     * @param active whether the Stat Definition is active
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}