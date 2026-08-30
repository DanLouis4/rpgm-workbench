package com.danlouis.ttrpgmanager.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

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

    @Lob
    @Column(name = "description", columnDefinition = "LONGTEXT")
    private String description;

    @Column(name = "is_public", nullable = false)
    private boolean isPublic = true;

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
     * Constructs a game system with its required ownership and identifying
     * information.
     *
     * @param owner the user who owns and manages the game system
     * @param name the display name of the game system
     * @param description the descriptive information for the game system
     * @param isPublic whether the game system is publicly viewable
     */
    public GameSystem(
            User owner,
            String name,
            String description,
            boolean isPublic) {

        this.owner = owner;
        this.name = name;
        this.description = description;
        this.isPublic = isPublic;
        this.isFlagged = false;
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
     * Returns the description of the game system.
     *
     * @return the game system description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the game system.
     *
     * @param description the game system description
     */
    public void setDescription(String description) {
        this.description = description;
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