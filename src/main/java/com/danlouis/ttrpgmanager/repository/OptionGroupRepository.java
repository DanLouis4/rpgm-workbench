package com.danlouis.ttrpgmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.SystemTerm;

/**
 * Provides persistence operations for OptionGroup entities.
 *
 * <p>The repository supports standard CRUD operations along with queries
 * used to retrieve and validate option groups belonging to a specific
 * System Term.</p>
 *
 * @author Daniel Louis
 */
public interface OptionGroupRepository extends JpaRepository<OptionGroup, Long> {


    // ---------------------------- Retrieval Methods ----------------------------- //


    /**
     * Returns all option groups belonging to the specified System Term.
     *
     * <p>Groups are ordered first by their configured display order and then
     * alphabetically by their group name.</p>
     *
     * @param systemTerm System Term whose option groups are being retrieved
     * @return ordered list of option groups
     */
    List<OptionGroup> findBySystemTermOrderByDisplayOrderAscGroupNameAsc(SystemTerm systemTerm);


    /**
     * Returns an option group by identifier when it belongs to the specified
     * System Term.
     *
     * @param optionGroupId identifier of the option group
     * @param systemTerm System Term that must own the option group
     * @return matching option group, if one exists
     */
    Optional<OptionGroup> findByOptionGroupIdAndSystemTerm(Long optionGroupId, SystemTerm systemTerm);

    /**
     * Returns all option groups that do not have a parent option group
     * for the specified System Term.
     *
     * @param systemTerm System Term whose top-level option groups are being retrieved
     * @return list of top-level option groups
     */
    List<OptionGroup> findBySystemTermAndParentOptionGroupIsNullOrderByDisplayOrderAscGroupNameAsc(SystemTerm systemTerm);
                
    /**
     * Returns all option groups that have the specified parent option group.
     *
     * @param parentOptionGroup parent option group
     * @return list of child option groups
     */
    List<OptionGroup> findByParentOptionGroupOrderByDisplayOrderAscGroupNameAsc(OptionGroup parentOptionGroup);

    

    // -------------------------- Existence Checks --------------------- //



    /**
     * Determines whether the specified System Term already contains the
     * supplied group name.
     *
     * @param systemTerm System Term being evaluated
     * @param groupName option group name being checked
     * @return true when the group name already exists for the System Term
     */
    boolean existsBySystemTermAndGroupNameIgnoreCase(SystemTerm systemTerm, String groupName);

    /**
     * Determines whether the specified System Term already contains the
     * supplied group name, excluding a specific option group by its identifier.
     *
     * @param systemTerm System Term being evaluated
     * @param groupName option group name being checked
     * @param optionGroupId identifier of the option group to exclude from the check
     * @return true when the group name already exists for the System Term, excluding the specified option group
     */
    boolean existsBySystemTermAndGroupNameIgnoreCaseAndOptionGroupIdNot(
            SystemTerm systemTerm,
            String groupName,
            Long optionGroupId);
    
    /**
     * Determines whether the specified option group has any child option groups.
     * To prevent deletion of a parent option group that still has child option groups,
     * this method can be used to check for their existence.
     *
     * @param parentOptionGroup parent option group being evaluated
     * @return true when the parent option group has child option groups
     */
    boolean existsByParentOptionGroup(OptionGroup parentOptionGroup);

    /**
     * Determines whether the specified System Term contains any Option Groups.
     *
     * @param systemTerm System Term being evaluated
     * @return true when at least one Option Group exists for the System Term
     */
    boolean existsBySystemTerm(SystemTerm systemTerm);



    // --------------------- Active Option Groups --------------------- //



    /**
     * Returns all active Option Groups belonging to the specified System Term.
     *
     * @param systemTerm System Term whose Option Groups are being retrieved
     * @return ordered list of active Option Groups
     */
    List<OptionGroup> findBySystemTermAndActiveTrueOrderByDisplayOrderAscGroupNameAsc(
            SystemTerm systemTerm);


    /**
     * Returns all active root Option Groups belonging to the specified System Term.
     *
     * @param systemTerm System Term whose root Option Groups are being retrieved
     * @return ordered list of active root Option Groups
     */
    List<OptionGroup> findBySystemTermAndParentOptionGroupIsNullAndActiveTrueOrderByDisplayOrderAscGroupNameAsc(
            SystemTerm systemTerm);


    /**
     * Returns all active child Option Groups belonging to the specified parent.
     *
     * @param parentOptionGroup parent Option Group
     * @return ordered list of active child Option Groups
     */
    List<OptionGroup> findByParentOptionGroupAndActiveTrueOrderByDisplayOrderAscGroupNameAsc(
            OptionGroup parentOptionGroup);


    /**
     * Determines whether the specified Option Group has active child groups.
     *
     * @param parentOptionGroup parent Option Group being evaluated
     * @return true when at least one active child Option Group exists
     */
    boolean existsByParentOptionGroupAndActiveTrue(
            OptionGroup parentOptionGroup);


    /**
     * Determines whether the specified System Term contains active Option Groups.
     *
     * @param systemTerm System Term being evaluated
     * @return true when at least one active Option Group exists
     */
    boolean existsBySystemTermAndActiveTrue(
            SystemTerm systemTerm);

}