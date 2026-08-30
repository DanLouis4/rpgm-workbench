package com.danlouis.ttrpgmanager.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danlouis.ttrpgmanager.entity.User;

/**
 * Provides persistence operations for User entities.
 *
 * The UserRepository uses Spring Data JPA to perform database operations
 * against the users table. It also provides lookup functionality used when
 * managing application users.
 * The parameters for JpaRepository are the entity being managed (User) and the type of its primary key (Long).
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their unique username.
     *
     * @param username the username of the user to locate
     * @return an Optional containing the matching user if one exists
     */
    Optional<User> findByUsername(String username);

    /**
     * Finds a user by their unique email address.
     *
     * @param email the email address of the user to locate
     * @return an Optional containing the matching user if one exists
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks if a user exists with the given username.
     *
     * @param username the username to check for existence
     * @return true if a user with the given username exists, false otherwise
     */
    boolean existsByUsername(String username);

    /**
     * Checks if a user exists with the given email address.
     *
     * @param email the email address to check for existence
     * @return true if a user with the given email exists, false otherwise
     */
    boolean existsByEmail(String email);    
}