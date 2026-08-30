package com.danlouis.ttrpgmanager.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.repository.UserRepository;

/**
 * Loads registered TTRPG Manager users for Spring Security authentication.
 *
 * <p>The CustomUserDetailsService retrieves user account information from
 * the application database and converts it into the UserDetails structure
 * required by Spring Security. Account role, encoded password, and enabled
 * status are included in the resulting security principal.</p>
 *
 * @author Daniel Louis
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * Creates a CustomUserDetailsService with the required user repository.
     *
     * @param userRepository repository used to retrieve registered users
     */
    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads a registered user by username for authentication.
     *
     * @param username the username submitted during authentication
     * @return the Spring Security user details for the matching account
     * @throws UsernameNotFoundException if no user exists with the supplied
     *         username
     */
    @Override                                                   // Implements the method from UserDetailsService to load user by username
    @Transactional(readOnly = true)                             // Ensures the database transaction is read-only for loading user details
    public UserDetails loadUserByUsername(String username)
            throws UsernameNotFoundException {

        com.danlouis.ttrpgmanager.entity.User user =
                userRepository.findByUsername(username)
                        .orElseThrow(() ->
                                new UsernameNotFoundException(
                                        "User not found: " + username));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPasswordHash())
                .roles(user.getRole().getRoleName())
                .disabled(!user.isEnabled())
                .build();
    }
}