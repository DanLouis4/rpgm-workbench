package com.danlouis.ttrpgmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danlouis.ttrpgmanager.entity.Role;

/**
 * Provides persistence operations for Role entities.
 *
 * The RoleRepository uses Spring Data JPA to perform database operations
 * against the roles table. It also provides lookup functionality used when
 * assigning application roles to registered users.
 * The parameters for JpaRepository are the entity being managed (Role) and the type of its primary key (Byte).
 */
public interface RoleRepository extends JpaRepository<Role, Byte> {

    /**
     * Finds a role by its unique role name.
     *
     * @param roleName the name of the role to locate
     * @return an Optional containing the matching role if one exists
     */
    Optional<Role> findByRoleName(String roleName);

    /**
     * Retrieves all roles except the ADMINISTRATOR role, ordered by role ID.
     *
     * @param roleName should be "ADMINISTRATOR" to exclude the administrator role from the results
     * @return a list of all Role entities except the excluded role
     */
    List<Role> findByRoleNameNotOrderByRoleIdAsc(String roleName);
}