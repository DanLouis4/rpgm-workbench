package com.danlouis.ttrpgmanager.service;

import java.util.List;
import java.util.Locale;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.entity.Role;
import com.danlouis.ttrpgmanager.entity.User;
import com.danlouis.ttrpgmanager.repository.RoleRepository;
import com.danlouis.ttrpgmanager.repository.UserRepository;

/**
 * Provides business logic for user account management.
 *
 * <p>The UserService coordinates user persistence, role assignment,
 * password encoding, and account validation. Role information is retrieved
 * from the database and associated with user accounts through the configured
 * entity relationship.</p>
 *
 * @author Daniel Louis
 */
@Service
public class UserService {

    private static final String ADMINISTRATOR_ROLE = "ADMINISTRATOR";

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Creates a UserService with the required persistence and security
     * dependencies.
     *
     * @param userRepository repository used to manage user records
     * @param roleRepository repository used to retrieve application roles
     * @param passwordEncoder encoder used to securely hash user passwords
     */
    public UserService(
            UserRepository userRepository,
            RoleRepository roleRepository,
            PasswordEncoder passwordEncoder) {

        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Retrieves the roles available for public registration.
     *
     * @return the list of publicly selectable application roles
     */
    public List<Role> getRegistrationRoles() {
        return roleRepository
                .findByRoleNameNotOrderByRoleIdAsc(ADMINISTRATOR_ROLE);
    }

    /**
     * Registers a new user account using the selected database role.
     *
     * @param username the requested username
     * @param email the user's email address
     * @param password the user's plaintext password
     * @param roleId the identifier of the selected role
     * @return the newly persisted User entity
     * @throws IllegalArgumentException if account data or the selected role
     *         is invalid
     */
    @Transactional
    public User registerUser(
            String username,
            String email,
            String password,
            Byte roleId) {

        String normalizedUsername = username.trim();
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

        if (userRepository.existsByUsername(normalizedUsername)) {
            throw new IllegalArgumentException("Username is already in use.");
        }

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new IllegalArgumentException("Email address is already in use.");
        }

        Role selectedRole = roleRepository.findById(roleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Selected role does not exist."));

        if (ADMINISTRATOR_ROLE.equals(selectedRole.getRoleName())) {
            throw new IllegalArgumentException(
                    "Administrator cannot be selected during registration.");
        }

        String passwordHash = passwordEncoder.encode(password);

        User user = new User(
                selectedRole,
                normalizedUsername,
                normalizedEmail,
                passwordHash);

        return userRepository.save(user);
    }
}