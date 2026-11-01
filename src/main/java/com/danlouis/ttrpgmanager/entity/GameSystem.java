package com.danlouis.ttrpgmanager.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType; // For specifying fetch strategies for associations: LAZY (loaded on demand) or EAGER (loaded immediately)
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob; // For mapping large text fields like the game system description
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

/**
 * Represents a tabletop roleplaying game system managed within the
 * TTRPG Manager application.
 *
 * A game system defines the core mechanics used by campaigns and characters,
 * including its terminology, configurable options, and statistical
 * definitions. Each game system is owned by one user with appropriate
 * Game Designer permissions.
 */
@Entity
@Table(name = "game_systems")
public class GameSystem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "game_system_id", nullable = false, updatable = false)
    private Long gameSystemId;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "owner_user_id", nullable = false)
    private User owner;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "genre", length = 50)
    private String genre;

    @Column(name = "ruleset_family", length = 100)
    private String rulesetFamily;

    @Column(name = "ruleset_edition", length = 50)
    private String rulesetEdition;

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Lob
    @Column(name = "system_overview", columnDefinition = "LONGTEXT")
    private String systemOverview;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;
    
    @Column(name = "is_cloneable", nullable = false)
    private boolean cloneable = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cloned_from_game_system_id")
    private GameSystem clonedFromGameSystem;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "original_added_by_user_id")
    private User originalAddedBy;

    @Column(name = "is_flagged", nullable = false)
    private boolean isFlagged = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructs an empty GameSystem instance for JPA.
     */
    public GameSystem() {
    }

    /**
     * Constructs a game system with its ownership, identifying,
     * descriptive, and visibility information.
     *
     * @param owner the user who owns and manages the game system
     * @param name the display name of the game system
     * @param genre the genre of the game system
     * @param rulesetFamily the ruleset family of the game system
     * @param rulesetEdition the ruleset edition of the game system
     * @param description the descriptive information for the game system
     * @param systemOverview the overview of the game system
     * @param isPublic whether the game system is publicly viewable
     * @param isFlagged whether the game system is flagged for review
     */
    public GameSystem(
            User owner,
            String name,
            String genre,
            String rulesetFamily,
            String rulesetEdition,
            String description,
            String systemOverview,
            boolean isPublic,
            boolean isFlagged) {

        this.owner = owner;
        this.name = name;
        this.genre = genre;
        this.rulesetFamily = rulesetFamily;
        this.rulesetEdition = rulesetEdition;
        this.description = description;
        this.systemOverview = systemOverview;
        this.isPublic = isPublic;
        this.isFlagged = isFlagged;
    }

    /**
     * Returns the unique identifier for the game system.
     *
     * @return the game system ID
     */
    public Long getGameSystemId() {
        return gameSystemId;
    }

    /**
     * Sets the unique identifier for the game system.
     *
     * @param gameSystemId the game system ID
     */
    public void setGameSystemId(Long gameSystemId) {
        this.gameSystemId = gameSystemId;
    }

    /**
     * Returns the user who owns the game system.
     *
     * @return the owner
     */
    public User getOwner() {
        return owner;
    }

    /**
     * Sets the user who owns the game system.
     *
     * @param owner the owner
     */
    public void setOwner(User owner) {
        this.owner = owner;
    }

    /**
     * Returns the display name of the game system.
     *
     * @return the game system name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the game system.
     *
     * @param name the game system name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the genre of the game system.
     *
     * @return the game system genre
     */
    public String getGenre() {
        return genre;
    }

    /**
     * Sets the genre of the game system.
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
     * Returns the description of the game system.
     *
     * @return the game system description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the 'description' of the game system.
     *
     * @param description the game system description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns system overview of the game system.
     *
     * @return the game system overview
     */
    public String getSystemOverview() {
        return systemOverview;
    }
    
    /**
     * Sets the system overview of the game system.
     *
     * @param systemOverview the game system overview
     */
    public void setSystemOverview(String systemOverview) {
        this.systemOverview = systemOverview;
    }

    /**
     * Indicates whether the game system is publicly viewable.
     *
     * @return true when the game system is public; otherwise false
     */
    public boolean isPublic() {
        return isPublic;
    }

    /**
     * Sets whether the game system is publicly viewable.
     *
     * @param isPublic true to make the game system public
     */
    public void setPublic(boolean isPublic) {
        this.isPublic = isPublic;
    }

    /**
     * Indicates whether other users may clone the game system.
     *
     * @return true when cloning is permitted; otherwise false
     */
    public boolean isCloneable() {
        return cloneable;
    }

    /**
     * Sets whether other users may clone the game system.
     *
     * @param cloneable true to permit cloning
     */
    public void setCloneable(boolean cloneable) {
        this.cloneable = cloneable;
    }

    /**
     * Returns the Game System from which this system was directly cloned.
     *
     * @return source Game System, or null when this is an original system
     */
    public GameSystem getClonedFromGameSystem() {
        return clonedFromGameSystem;
    }

    /**
     * Sets the Game System from which this system was directly cloned.
     *
     * @param clonedFromGameSystem source Game System
     */
    public void setClonedFromGameSystem(
            GameSystem clonedFromGameSystem) {

        this.clonedFromGameSystem = clonedFromGameSystem;
    }

    /**
     * Returns the user who originally added the Game System
     * at the beginning of this clone lineage.
     *
     * @return original adding user, or null for an original system
     */
    public User getOriginalAddedBy() {
        return originalAddedBy;
    }

    /**
     * Sets the user who originally added the Game System
     * at the beginning of this clone lineage.
     *
     * @param originalAddedBy original adding user
     */
    public void setOriginalAddedBy(User originalAddedBy) {
        this.originalAddedBy = originalAddedBy;
    }

    /**
     * Indicates whether the game system has been flagged for administrative
     * review.
     *
     * @return true when the game system is flagged; otherwise false
     */
    public boolean isFlagged() {
        return isFlagged;
    }

    /**
     * Sets the administrative flag status of the game system.
     *
     * @param isFlagged true when the game system requires review
     */
    public void setFlagged(boolean isFlagged) {
        this.isFlagged = isFlagged;
    }

    /**
     * Returns the date and time the game system was created.
     *
     * @return the creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the game system creation timestamp.
     *
     * @param createdAt the creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the date and time the game system was last updated.
     *
     * @return the last update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the game system update timestamp.
     *
     * @param updatedAt the update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}