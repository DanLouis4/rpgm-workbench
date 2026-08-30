package com.danlouis.ttrpgmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.User;
import com.danlouis.ttrpgmanager.repository.GameSystemRepository;
import com.danlouis.ttrpgmanager.repository.UserRepository;

/**
 * Provides business logic for Game System management.
 *
 * The service manages game system retrieval, creation, modification,
 * deletion, ownership validation, and input normalization.
 */
@Service
public class GameSystemService {

    private static final String GAME_DESIGNER_ROLE = "GAME_DESIGNER";

    private final GameSystemRepository gameSystemRepository;
    private final UserRepository userRepository;

    /**
     * Constructs the GameSystemService with its required repositories.
     *
     * @param gameSystemRepository repository used to manage game systems
     * @param userRepository repository used to retrieve application users
     */
    public GameSystemService(
            GameSystemRepository gameSystemRepository,
            UserRepository userRepository) {

        this.gameSystemRepository = gameSystemRepository;
        this.userRepository = userRepository;
    }


    // -------------------- Game System Retrieval -------------------- //


    /**
     * Returns all publicly viewable game systems.
     *
     * @return public game systems ordered by name
     */
    @Transactional(readOnly = true)
    public List<GameSystem> getPublicGameSystems() {

        return gameSystemRepository
                .findByIsPublicTrueOrderByNameAsc();
    }

    /**
     * Returns all game systems owned by the specified user.
     *
     * @param username username of the system owner
     * @return game systems owned by the user
     */
    @Transactional(readOnly = true)
    public List<GameSystem> getOwnedGameSystems(String username) {

        User owner = getUserByUsername(username);

        return gameSystemRepository
                .findByOwnerOrderByUpdatedAtDesc(owner);
    }

    /**
     * Returns a game system by its identifier.
     *
     * @param gameSystemId identifier of the game system
     * @return the matching game system
     * @throws IllegalArgumentException if the game system does not exist
     */
    @Transactional(readOnly = true)
    public GameSystem getGameSystemById(Long gameSystemId) {

        return gameSystemRepository
                .findByGameSystemId(gameSystemId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Game system unavailable."));
    }


    // -------------------- Ownership -------------------- //


    /**
     * Determines whether the specified user owns the game system.
     *
     * Guests and unauthenticated users are never considered owners.
     *
     * @param gameSystem game system being evaluated
     * @param username username of the authenticated user
     * @return true when the user owns the game system; otherwise false
     */
    @Transactional(readOnly = true)
    public boolean isOwner(
            GameSystem gameSystem,
            String username) {

        if (username == null || username.isBlank()) {
            return false;
        }

        User user = getUserByUsername(username);

        return gameSystem.getOwner()
                .getUserId()
                .equals(user.getUserId());
    }


    // -------------------- Creation and Modification -------------------- //


    /**
     * Creates a new game system owned by the specified Game Designer.
     *
     * @param username username of the authenticated user
     * @param name display name of the game system
     * @param description descriptive information for the game system
     * @param isPublic whether the game system is publicly viewable
     * @return the newly created game system
     * @throws SecurityException if the user is not a Game Designer
     */
    @Transactional
    public GameSystem createGameSystem(
            String username,
            String name,
            String description,
            boolean isPublic) {

        User owner = getUserByUsername(username);

        requireGameDesigner(owner);

        GameSystem gameSystem = new GameSystem(
                owner,
                normalizeName(name),
                normalizeDescription(description),
                isPublic);

        return gameSystemRepository.save(gameSystem);
    }

    /**
     * Updates a game system owned by the specified Game Designer.
     *
     * @param gameSystemId identifier of the game system
     * @param username username of the authenticated user
     * @param name updated display name
     * @param description updated description
     * @param isPublic updated visibility setting
     * @return the updated game system
     * @throws SecurityException if the user does not own the game system
     */
    @Transactional
    public GameSystem updateGameSystem(
            Long gameSystemId,
            String username,
            String name,
            String description,
            boolean isPublic) {

        User user = getUserByUsername(username);

        requireGameDesigner(user);

        GameSystem gameSystem =
                getGameSystemById(gameSystemId);

        requireOwner(gameSystem, user);

        gameSystem.setName(normalizeName(name));
        gameSystem.setDescription(
                normalizeDescription(description));
        gameSystem.setPublic(isPublic);

        return gameSystemRepository.save(gameSystem);
    }

    /**
     * Deletes a game system owned by the specified Game Designer.
     *
     * @param gameSystemId identifier of the game system
     * @param username username of the authenticated user
     * @throws SecurityException if the user does not own the game system
     */
    @Transactional
    public void deleteGameSystem(
            Long gameSystemId,
            String username) {

        User user = getUserByUsername(username);

        requireGameDesigner(user);

        GameSystem gameSystem =
                getGameSystemById(gameSystemId);

        requireOwner(gameSystem, user);

        gameSystemRepository.delete(gameSystem);
    }


    // -------------------- User and Permission Checks -------------------- //


    /**
     * Retrieves an application user by username.
     *
     * @param username username to locate
     * @return the matching user
     * @throws IllegalArgumentException if the user does not exist
     */
    private User getUserByUsername(String username) {

        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException(
                        "User not found."));
    }

    /**
     * Verifies that the specified user has Game Designer permissions.
     *
     * @param user user whose role is evaluated
     * @throws SecurityException if the user is not a Game Designer
     */
    private void requireGameDesigner(User user) {

        if (!GAME_DESIGNER_ROLE.equals(
                user.getRole().getRoleName())) {

            throw new SecurityException(
                    "Game Designer permissions are required.");
        }
    }

    /**
     * Verifies that the specified user owns the game system.
     *
     * @param gameSystem game system being modified
     * @param user authenticated user
     * @throws SecurityException if the user does not own the game system
     */
    private void requireOwner(
            GameSystem gameSystem,
            User user) {

        if (!gameSystem.getOwner()
                .getUserId()
                .equals(user.getUserId())) {

            throw new SecurityException(
                    "You are not authorized to modify this Game System.");
        }
    }


    // -------------------- Input Normalization -------------------- //


    /**
     * Normalizes and validates a game system name.
     *
     * @param value name to normalize
     * @return normalized name
     * @throws IllegalArgumentException if the name is invalid
     */
    private String normalizeName(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Game system name is required.");
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > 100) {
            throw new IllegalArgumentException(
                    "Game system name cannot exceed 100 characters.");
        }

        return normalizedValue;
    }

    /**
     * Normalizes an optional game system description.
     *
     * @param value description to normalize
     * @return normalized description or null
     */
    private String normalizeDescription(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}