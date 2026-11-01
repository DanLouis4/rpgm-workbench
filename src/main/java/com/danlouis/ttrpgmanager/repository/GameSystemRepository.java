package com.danlouis.ttrpgmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.User;

/**
 * Provides persistence operations for GameSystem entities.
 *
 * The repository supports standard CRUD operations along with queries
 * used to retrieve systems by ownership and visibility.
 */
public interface GameSystemRepository
        extends JpaRepository<GameSystem, Long> {

    /**
     * Returns all game systems owned by the specified user.
     *
     * @param owner the owning user
     * @return game systems owned by the user
     */
    @EntityGraph(attributePaths = "owner")
    List<GameSystem> findByOwnerOrderByUpdatedAtDesc(User owner);

    /**
     * Returns all publicly viewable game systems.
     *
     * @return public game systems ordered by name
     */
    @EntityGraph(attributePaths = "owner")
    List<GameSystem> findByIsPublicTrueOrderByNameAsc();

    /**
     * Returns all flagged game systems.
     *
     * @return flagged game systems
     */
    @EntityGraph(attributePaths = "owner")
    List<GameSystem> findByIsFlaggedTrueOrderByUpdatedAtDesc();

    /**
     * Returns a game system and its related entities by identifier.
     *
     * @param gameSystemId identifier of the game system
     * @return the matching game system, if one exists
     */
    @EntityGraph(attributePaths = {"owner", "originalAddedBy", "clonedFromGameSystem"})
    Optional<GameSystem> findByGameSystemId(Long gameSystemId);
}