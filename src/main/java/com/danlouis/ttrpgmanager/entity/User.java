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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;


/**
 * Represents a registered user account within the TTRPG Manager.
 *
 * <p>Users are stored in the {@code users} database table and are assigned
 * an application role that determines the features and operations available
 * to them. User accounts also store authentication information, account
 * status, and record creation and modification timestamps.</p>
 *
 * @author Daniel Louis
 */
@Entity
@Table(name = "users")
public class User {

    /**
     * The unique identifier for the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id", nullable = false, updatable = false)
    private Long userId;

    /**
     * The application role assigned to the user.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "role_id", nullable = false)
    private Role role;

    /**
     * The unique username used to identify the user within the application.
     */
    @Column(name = "username", nullable = false, unique = true, length = 50)
    private String username;

    /**
     * The unique email address associated with the user account.
     */
    @Column(name = "email", nullable = false, unique = true, length = 255)
    private String email;

    /**
     * The securely encoded password hash used to authenticate the user.
     *
     * <p>Plain-text passwords must never be stored in this field.</p>
     */
    @Column(name = "password_hash", nullable = false, length = 255)
    private String passwordHash;

    /**
     * Indicates whether the user account is currently enabled.
     */
    @Column(name = "enabled", nullable = false)
    private boolean enabled = true;

    /**
     * The date and time when the user account was created.
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    /**
     * The date and time when the user account was most recently updated.
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    /**
     * Constructs an empty User instance.
     *
     * <p>This constructor is required by JPA for entity instantiation.</p>
     */
    public User() {
    }

    /**
     * Constructs a User instance with the specified role, username,
     * email address, and encoded password hash.
     *
     * <p>New users are enabled by default. The user identifier and
     * timestamps are generated automatically.</p>
     *
     * @param role         the application role assigned to the user
     * @param username     the unique username for the user
     * @param email        the unique email address for the user
     * @param passwordHash the securely encoded password hash
     */
    public User(Role role, String username, String email, String passwordHash) {
        this.role = role;
        this.username = username;
        this.email = email;
        this.passwordHash = passwordHash;
        this.enabled = true;
    }

    // Getters and Setters

    /**
     * Returns the unique identifier of the user.
     *
     * @return the user identifier
     */
    public Long getUserId() {
        return userId;
    }

    /**
     * Sets the unique identifier of the user.
     *
     * @param userId the user identifier
     */
    public void setUserId(Long userId) {
        this.userId = userId;
    }

    /**
     * Returns the application role assigned to the user.
     *
     * @return the assigned role
     */
    public Role getRole() {
        return role;
    }

    /**
     * Sets the application role assigned to the user.
     *
     * @param role the role to assign to the user
     */
    public void setRole(Role role) {
        this.role = role;
    }

    /**
     * Returns the username of the user.
     *
     * @return the username
     */
    public String getUsername() {
        return username;
    }

    /**
     * Sets the username of the user.
     *
     * @param username the username
     */
    public void setUsername(String username) {
        this.username = username;
    }

    /**
     * Returns the email address associated with the user.
     *
     * @return the email address
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address associated with the user.
     *
     * @param email the email address
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Returns the encoded password hash used for authentication.
     *
     * @return the encoded password hash
     */
    public String getPasswordHash() {
        return passwordHash;
    }

    /**
     * Sets the encoded password hash used for authentication.
     *
     * @param passwordHash the encoded password hash
     */
    public void setPasswordHash(String passwordHash) {
        this.passwordHash = passwordHash;
    }

    /**
     * Returns whether the user account is enabled.
     *
     * @return {@code true} if the account is enabled; otherwise {@code false}
     */
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether the user account is enabled.
     *
     * @param enabled {@code true} to enable the account; otherwise {@code false}
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Returns the date and time when the user account was created.
     *
     * @return the account creation timestamp
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Sets the date and time when the user account was created.
     *
     * @param createdAt the account creation timestamp
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Returns the date and time when the user account was most recently updated.
     *
     * @return the most recent update timestamp
     */
    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    /**
     * Sets the date and time when the user account was most recently updated.
     *
     * @param updatedAt the most recent update timestamp
     */
    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}