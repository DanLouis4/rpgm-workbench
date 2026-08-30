package com.danlouis.ttrpgmanager.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;


/**
 * Represents an application role used to define user permissions
 * within the TTRPG Manager.
 *
 * <p>Roles are stored in the {@code roles} database table and include
 * values such as PLAYER, GAME_MASTER, GAME_DESIGNER, and ADMINISTRATOR.</p>
 *
 * @author Daniel Louis
 */
@Entity
@Table(name = "roles")
public class Role {

    /**
     * The unique identifier for the role.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id", nullable = false, updatable = false)
    private Byte roleId;

    /**
     * The name of the role, which must be unique and not null.
     */
    @Column(name = "role_name", nullable = false, unique = true, length = 30)
    private String roleName;

    /**
     * A brief description of the role's purpose and permissions.
     */
    @Column(name = "description", length = 255)
    private String description;

    /**
     * Constructs an empty Role instance.
     *
     * <p>This constructor is required by JPA for entity instantiation.</p>
     */
    public Role() {
    }

    /**
     * Constructs a Role instance with the specified name and description.
     *
     * @param roleName    the unique name of the role
     * @param description a brief description of the role
     */
    public Role(String roleName, String description) {
        this.roleName = roleName;
        this.description = description;
    }

    // Getters and Setters

    /**
     * Returns the unique identifier of the role.
     *
     * @return the role identifier
     */
    public Byte getRoleId() {
        return roleId;
    }

    /**
     * Sets the unique identifier of the role.
     *
     * @param roleId the role identifier
     */
    public void setRoleId(Byte roleId) {
        this.roleId = roleId;
    }
    
    /**
     * Returns the name of the role.
    *
     * @return the role name
     */
    public String getRoleName() {
        return roleName;
    }
    
    /**
     * Sets the name of the role.
    *
    * @param roleName the role name
    */
   public void setRoleName(String roleName) {
       this.roleName = roleName;
    }
    /**
     * Returns the description of the role.
    *
    * @return the role description
    */
   public String getDescription() {
       return description;
    }
    
    /**
     * Sets the description of the role.
     *
     * @param description the role description
     */
    public void setDescription(String description) {
        this.description = description;
    }
}
