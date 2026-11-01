package com.danlouis.ttrpgmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.SystemTerm;

/**
 * Provides persistence operations for SystemTerm entities.
 *
 * <p>The repository supports standard CRUD operations along with queries
 * used to retrieve and validate terms, singular labels and active status belonging to a specific Game System.</p>
 *
 * @author Daniel Louis
 */
public interface SystemTermRepository
        extends JpaRepository<SystemTerm, Long> {


        // ------------------------- Game System Queries ------------------------- //


        /**
         * Returns all system terms belonging to the specified Game System.
         *
         * <p>Terms are ordered first by their configured display order and then
         * alphabetically by their singular label.</p>
         *
         * @param gameSystem Game System whose terms are being retrieved
         * @return ordered list of system terms
         */
        List<SystemTerm>
                findByGameSystemOrderByDisplayOrderAscSingularLabelAsc(
                        GameSystem gameSystem);


        /**
         * Returns a system term by identifier when it belongs to the specified
         * Game System.
         *
         * @param systemTermId identifier of the system term
         * @param gameSystem Game System that must own the term
         * @return matching system term, if one exists
         */
        Optional<SystemTerm>
                findBySystemTermIdAndGameSystem(
                        Long systemTermId,
                        GameSystem gameSystem);



        // ------------------------- Singular Label Validation ------------------------- //
        


        /**
         * Determines whether the specified Game System already contains the
         * supplied singular label.
         *
         * @param gameSystem Game System being evaluated
         * @param singularLabel singular label being checked
         * @return true when the term key already exists for the Game System
         */
        boolean existsByGameSystemAndSingularLabelIgnoreCase(
                GameSystem gameSystem,
                String singularLabel);

        /**
         * Determines whether the specified Game System already contains the
         * supplied singular label, excluding the system term with the given ID.
         *
         * @param gameSystem Game System being evaluated
         * @param singularLabel singular label being checked
         * @param systemTermId ID of the system term to exclude from the check
         * @return true when the term key already exists for the Game System
         */
        boolean existsByGameSystemAndSingularLabelIgnoreCaseAndSystemTermIdNot(
                GameSystem gameSystem,
                String singularLabel,
                Long systemTermId);



        // ------------------------- Active System Terms ------------------------- //



        /**
         * Returns all active System Terms belonging to the specified Game System.
         *
         * @param gameSystem Game System whose terms are being retrieved
         * @return ordered list of active System Terms
         */
        List<SystemTerm> findByGameSystemAndActiveTrueOrderByDisplayOrderAscSingularLabelAsc(
                GameSystem gameSystem);


        /**
         * Determines whether the specified Game System contains active System Terms.
         *
         * @param gameSystem Game System being evaluated
         * @return true when at least one active System Term exists
         */
        boolean existsByGameSystemAndActiveTrue(GameSystem gameSystem);

}