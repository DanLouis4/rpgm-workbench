package com.danlouis.ttrpgmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danlouis.ttrpgmanager.entity.StatDefinition;
import com.danlouis.ttrpgmanager.entity.SystemTerm;

/**
 * Repository for accessing and managing Stat Definition records.
 *
 * <p>Stat Definitions belong to a System Term and may optionally
 * reference another Stat Definition as their parent.</p>
 *
 * @author Daniel Louis
 */
public interface StatDefinitionRepository
        extends JpaRepository<StatDefinition, Long> {

    /**
     * Returns all Stat Definitions belonging to the specified System Term
     * ordered by display order and Stat name.
     *
     * @param systemTerm owning System Term
     * @return ordered list of Stat Definitions
     */
    List<StatDefinition> findBySystemTermOrderByDisplayOrderAscStatNameAsc(
            SystemTerm systemTerm);


    /**
     * Returns all active Stat Definitions belonging to the specified
     * System Term ordered by display order and Stat name.
     *
     * @param systemTerm owning System Term
     * @return ordered list of active Stat Definitions
     */
    List<StatDefinition> findBySystemTermAndActiveTrueOrderByDisplayOrderAscStatNameAsc(
            SystemTerm systemTerm);


    /**
     * Returns the specified Stat Definition when it belongs to the
     * supplied System Term.
     *
     * @param statDefinitionId Stat Definition ID
     * @param systemTerm owning System Term
     * @return matching Stat Definition when found
     */
    Optional<StatDefinition> findByStatDefinitionIdAndSystemTerm(
            Long statDefinitionId,
            SystemTerm systemTerm);


    /**
     * Determines whether the supplied System Term already contains
     * a Stat Definition with the specified name.
     *
     * @param systemTerm owning System Term
     * @param statName Stat name being evaluated
     * @return true when the Stat name already exists
     */
    boolean existsBySystemTermAndStatNameIgnoreCase(
            SystemTerm systemTerm,
            String statName);


    /**
     * Determines whether another Stat Definition within the supplied
     * System Term already uses the specified name.
     *
     * @param systemTerm owning System Term
     * @param statName Stat name being evaluated
     * @param statDefinitionId Stat Definition excluded from the check
     * @return true when another Stat Definition uses the name
     */
    boolean existsBySystemTermAndStatNameIgnoreCaseAndStatDefinitionIdNot(
            SystemTerm systemTerm,
            String statName,
            Long statDefinitionId);


    /**
     * Determines whether the specified Stat Definition has any
     * child Stat Definitions.
     *
     * @param parentStat parent Stat Definition
     * @return true when child Stat Definitions exist
     */
    boolean existsByParentStat(
            StatDefinition parentStat);


    /**
     * Determines whether the specified Stat Definition has any
     * active child Stat Definitions.
     *
     * @param parentStat parent Stat Definition
     * @return true when active child Stat Definitions exist
     */
    boolean existsByParentStatAndActiveTrue(
            StatDefinition parentStat);


    /**
     * Determines whether the supplied System Term contains any
     * Stat Definitions.
     *
     * @param systemTerm System Term being evaluated
     * @return true when at least one Stat Definition exists
     */
    boolean existsBySystemTerm(
            SystemTerm systemTerm);


    /**
     * Determines whether the supplied System Term contains any
     * active Stat Definitions.
     *
     * @param systemTerm System Term being evaluated
     * @return true when at least one active Stat Definition exists
     */
    boolean existsBySystemTermAndActiveTrue(
            SystemTerm systemTerm);
}