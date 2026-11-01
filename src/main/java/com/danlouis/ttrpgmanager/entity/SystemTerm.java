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
 * Represents a configurable Terminology term used by a Game System.
 *
 * <p>System terms allow each Game System to define the terminology used
 * throughout its rules and character options. Examples may include Race,
 * Class, Skill, Spell, Ability, Attribute, Trait, or other concepts defined
 * by the Game Designer.</p>
 *
 * @author Daniel Louis
 */
@Entity
@Table(
    name = "system_terms",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_system_terms_system_key",columnNames = {"game_system_id", "singular_label"}
        )
    }
)
public class SystemTerm {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "system_term_id", nullable = false, updatable = false)
    private Long systemTermId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "game_system_id", nullable = false)

    private GameSystem gameSystem;

    @Column(name = "singular_label", nullable = false, length = 50)
    private String singularLabel;

    @Column(name = "plural_label", length = 50)
    private String pluralLabel;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "has_character_options", nullable = false)
    private boolean hasCharacterOptions = false;

    @Column(name = "has_stats", nullable = false)
    private boolean hasStats = false;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder = 0;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    /**
     * Constructs an empty SystemTerm instance for JPA.
     */
    public SystemTerm() {
    }


    /**
     * Constructs a SystemTerm belonging to the specified Game System.
     *
     * @param gameSystem Game System that defines the term
     * @param singularLabel singular user-facing label
     * @param pluralLabel plural user-facing label
     * @param description description of the term
     * @param hasCharacterOptions whether the term has character options
     * @param hasStats whether the term has stats
     * @param displayOrder display position of the term
     */
    public SystemTerm(
            GameSystem gameSystem,
            String singularLabel,
            String pluralLabel,
            String description,
            boolean hasCharacterOptions,
            boolean hasStats,
            Integer displayOrder) {

        this.gameSystem = gameSystem;
        this.singularLabel = singularLabel;
        this.pluralLabel = pluralLabel;
        this.description = description;
        this.hasCharacterOptions = hasCharacterOptions;
        this.hasStats = hasStats;
        this.displayOrder = displayOrder;
        this.active = true;

    }


    /**
     * Returns the unique identifier of the system term.
     *
     * @return system term ID
     */
    public Long getSystemTermId() {
        return systemTermId;
    }


    /**
     * Sets the unique identifier of the system term.
     *
     * @param systemTermId system term ID
     */
    public void setSystemTermId(Long systemTermId) {
        this.systemTermId = systemTermId;
    }


    /**
     * Returns the Game System that defines this term.
     *
     * @return associated Game System
     */
    public GameSystem getGameSystem() {
        return gameSystem;
    }


    /**
     * Sets the Game System that defines this term.
     *
     * @param gameSystem associated Game System
     */
    public void setGameSystem(GameSystem gameSystem) {
        this.gameSystem = gameSystem;
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
     * Returns the description of the term.
     *
     * @return term description
     */
    public String getDescription() {
        return description;
    }


    /**
     * Sets the description of the term.
     *
     * @param description term description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns whether the term has character options.
     *
     * @return true if the term has character options, false otherwise
     */
    public boolean isHasCharacterOptions() {
        return hasCharacterOptions;
    }

    /**
     * Sets whether the term has character options.
     *
     * @param hasCharacterOptions true if the term has character options, false otherwise
     */
    public void setHasCharacterOptions(boolean hasCharacterOptions) {
        this.hasCharacterOptions = hasCharacterOptions;
    }

    /**
     * Returns whether the term has stats.
     *
     * @return true if the term has stats, false otherwise
     */
    public boolean isHasStats() {
        return hasStats;
    }

    /**
     * Sets whether the term has stats.
     *
     * @param hasStats true if the term has stats, false otherwise
     */
    public void setHasStats(boolean hasStats) {
        this.hasStats = hasStats;
    }

    /**
     * Returns the display order of the term.
     *
     * @return display order
     */
    public Integer getDisplayOrder() {
        return displayOrder;
    }

    /**
     * Sets the display order of the term.
     *
     * @param displayOrder display order
     */
    public void setDisplayOrder(Integer displayOrder) {
        this.displayOrder = displayOrder;
    }

    /**
     * Returns whether the term is active.
     *
     * @return true if the term is active, false otherwise
     */
    public boolean isActive() {
        return active;
    }

    /**
     * Sets whether the term is active.
     *
     * @param active true if the term is active, false otherwise
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}