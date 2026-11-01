package com.danlouis.ttrpgmanager.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.OptionValue;

/**
 * Provides persistence operations for OptionValue entities.
 *
 * <p>The repository supports standard CRUD operations along with queries
 * used to retrieve and validate option values belonging to a specific
 * Option Group.</p>
 *
 * @author Daniel Louis
 */
public interface OptionValueRepository
        extends JpaRepository<OptionValue, Long> {

        /**
         * Returns all option values belonging to the specified Option Group.
         *
         * <p>Values are ordered first by their configured display order and then
         * alphabetically by their value name.</p>
         *
         * @param optionGroup Option Group whose values are being retrieved
         * @return ordered list of option values
         */
        List<OptionValue>
                findByOptionGroupOrderByDisplayOrderAscValueNameAsc(
                        OptionGroup optionGroup);


        /**
         * Returns an option value by identifier when it belongs to the specified
         * Option Group.
         *
         * @param optionValueId identifier of the option value
         * @param optionGroup Option Group that must own the option value
         * @return matching option value, if one exists
         */
        Optional<OptionValue>
                findByOptionValueIdAndOptionGroup(
                        Long optionValueId,
                        OptionGroup optionGroup);
                        

        /**
         * Returns all active option values belonging to the specified Option Group.
         *
         * @param optionGroup Option Group whose values are being retrieved
         * @return ordered list of active option values
         */
        List<OptionValue> findByOptionGroupAndActiveTrueOrderByDisplayOrderAscValueNameAsc(
                OptionGroup optionGroup);


        /**
         * Determines whether the specified Option Value has active child values.
         *
         * @param parentOptionValue parent value being evaluated
         * @return true when one or more active values reference it as their parent
         */
        boolean existsByParentOptionValueAndActiveTrue(
                OptionValue parentOptionValue);


        /**
         * Determines whether the specified Option Group contains active Option Values.
         *
         * @param optionGroup Option Group being evaluated
         * @return true when at least one active Option Value exists
         */
        boolean existsByOptionGroupAndActiveTrue(
                OptionGroup optionGroup);


        /**
         * Determines whether the specified Option Group already contains the
         * supplied value name.
         *
         * @param optionGroup Option Group being evaluated
         * @param valueName option value name being checked
         * @return true when the value name already exists for the Option Group
         */
        boolean existsByOptionGroupAndValueNameIgnoreCase(
                OptionGroup optionGroup,
                String valueName);

        /**
         * Determines whether the specified Option Group already contains the
         * supplied value name, excluding a specific option value by its identifier.
         *
         * @param optionGroup Option Group being evaluated
         * @param valueName option value name being checked
         * @param optionValueId identifier of the option value to exclude from the check
         * @return true when the value name already exists for the Option Group, excluding the specified option value
         */
        boolean existsByOptionGroupAndValueNameIgnoreCaseAndOptionValueIdNot(
                OptionGroup optionGroup,
                String valueName,
                Long optionValueId);

        /**
         * Determines whether the specified Option Value has child values.
         *
         * @param parentOptionValue parent value being evaluated
         * @return true when one or more values reference it as their parent
         */
        boolean existsByParentOptionValue(
                OptionValue parentOptionValue);

        /**
         * Counts the active Option Values belonging to the specified Option Group.
         *
         * @param optionGroup Option Group being evaluated
         * @return number of active Option Values
         */
        long countByOptionGroupAndActiveTrue(OptionGroup optionGroup);
}