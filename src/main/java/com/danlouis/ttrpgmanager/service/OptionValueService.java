package com.danlouis.ttrpgmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.OptionValue;
import com.danlouis.ttrpgmanager.repository.OptionValueRepository;

/**
 * Provides business logic for Option Value management.
 *
 * <p>The service manages option value retrieval, creation, modification,
 * deletion, parent validation, duplicate prevention, and input
 * normalization.</p>
 *
 * @author Daniel Louis
 */
@Service
public class OptionValueService {

        private final OptionValueRepository optionValueRepository;
        private final OptionGroupService optionGroupService;


        /**
         * Constructs the OptionValueService with its required dependencies.
         *
         * @param optionValueRepository repository used to manage option values
         * @param optionGroupService service used to retrieve and validate option groups
         */
        public OptionValueService(
                OptionValueRepository optionValueRepository,
                OptionGroupService optionGroupService) {

        this.optionValueRepository = optionValueRepository;
        this.optionGroupService = optionGroupService;
        }


        // -------------------- Option Value Retrieval -------------------- //


        /**
         * Returns all option values belonging to the specified Option Group.
         *
         * @param optionGroup Option Group whose values are being retrieved
         * @return ordered list of option values
         */
        @Transactional(readOnly = true)
        public List<OptionValue> getOptionValuesForGroup(
                OptionGroup optionGroup) {

        return optionValueRepository
                .findByOptionGroupOrderByDisplayOrderAscValueNameAsc(optionGroup);
        }


        /**
         * Returns an option value belonging to the specified Option Group.
         *
         * @param optionValueId identifier of the option value
         * @param optionGroup Option Group that must own the option value
         * @return matching option value
         * @throws IllegalArgumentException if the option value does not exist
         */
        @Transactional(readOnly = true)
        public OptionValue getOptionValueById(
                Long optionValueId,
                OptionGroup optionGroup) {

        return optionValueRepository
                .findByOptionValueIdAndOptionGroup(
                        optionValueId,
                        optionGroup)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Option value unavailable."));
        }


        // -------------------- Creation and Modification -------------------- //


        /**
         * Creates a new Option Value within an Option Group owned by the
         * authenticated user's Game System.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the parent Option Group
         * @param parentOptionValueId identifier of the parent option value (optional)
         * @param username username of the authenticated user
         * @param valueName display name of the option value
         * @param description optional description
         * @param displayOrder presentation order
         * @return newly created option value
         */
        @Transactional
        public OptionValue createOptionValue(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                Long parentOptionValueId,
                String username,
                String valueName,
                String description,
                Integer displayOrder) {

        OptionGroup optionGroup =
                optionGroupService.getOptionGroupById(
                        gameSystemId,
                        systemTermId,
                        optionGroupId,
                        username);

        if (!optionGroup.isActive()) {
                throw new IllegalArgumentException(
                        "Option Values cannot be added to a retired Option Group.");
                }

        String normalizedValueName = normalizeValueName(valueName);

        if (optionValueRepository
                .existsByOptionGroupAndValueNameIgnoreCase(
                        optionGroup,
                        normalizedValueName)) {

                throw new IllegalArgumentException(
                        "An option value with this name already exists "
                        + "in the selected Option Group.");
        }

        OptionValue parentOptionValue = resolveParentOptionValue(optionGroup, parentOptionValueId);

        OptionValue optionValue = new OptionValue(
                optionGroup,
                parentOptionValue,
                normalizedValueName,
                normalizeDescription(description),
                normalizeDisplayOrder(displayOrder));

        return optionValueRepository.save(optionValue);
        }

        /**
         * Updates an existing Option Value.
         *
         * @param gameSystemId identifier of the game system
         * @param systemTermId identifier of the system term
         * @param optionGroupId identifier of the parent Option Group
         * @param optionValueId identifier of the option value
         * @param parentOptionValueId identifier of the parent option value (optional)
         * @param username username of the authenticated user
         * @param valueName updated display name
         * @param description updated description
         * @param displayOrder updated presentation order
         * @return updated option value
         */
        @Transactional
        public OptionValue updateOptionValue(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                Long optionValueId,
                Long parentOptionValueId,
                String username,
                String valueName,
                String description,
                Integer displayOrder) {

        OptionGroup optionGroup =
                optionGroupService.getOptionGroupById(
                        gameSystemId,
                        systemTermId,
                        optionGroupId,
                        username);
        
        if (!optionGroup.isActive()) {
                throw new IllegalArgumentException(
                        "Option Values cannot be updated in a retired Option Group.");
        }

        OptionValue optionValue = getOptionValueById(optionValueId, optionGroup);

        String normalizedValueName = normalizeValueName(valueName);

        if (optionValueRepository
                .existsByOptionGroupAndValueNameIgnoreCaseAndOptionValueIdNot(
                        optionGroup,
                        normalizedValueName,
                        optionValueId)) {

                throw new IllegalArgumentException(
                        "An option value with this name already exists "
                        + "in the selected Option Group.");
        }

        OptionValue parentOptionValue = resolveParentOptionValue(optionGroup, parentOptionValueId);

        optionValue.setParentOptionValue(parentOptionValue);
        optionValue.setValueName(normalizedValueName);
        optionValue.setDescription(normalizeDescription(description));
        optionValue.setDisplayOrder(normalizeDisplayOrder(displayOrder));

        return optionValueRepository.save(optionValue);
        }

        // -------------------- Retirement and Restoration -------------------- //


        /**
         * Retires an active Option Value.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param optionValueId identifier of the Option Value
         * @param username username of the authenticated user
         * @return name of the retired Option Value
         */
        @Transactional
        public String retireOptionValue(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                Long optionValueId,
                String username) {

                OptionGroup optionGroup =
                        optionGroupService.getOptionGroupById(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username);

                OptionValue optionValue = getOptionValueById(optionValueId, optionGroup);

                if (!optionValue.isActive()) {
                        throw new IllegalArgumentException(
                                "This Option Value is already retired.");
                }

                if (optionValueRepository
                        .existsByParentOptionValueAndActiveTrue(optionValue)) {

                        throw new IllegalArgumentException(
                                "This Option Value cannot be retired while it contains active child values.");
                }

                optionValue.setActive(false);
                optionValueRepository.save(optionValue);

                return optionValue.getValueName();
        }


        /**
         * Restores a retired Option Value.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param optionValueId identifier of the Option Value
         * @param username username of the authenticated user
         * @return name of the restored Option Value
         */
        @Transactional
        public String restoreOptionValue(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                Long optionValueId,
                String username) {

                OptionGroup optionGroup =
                        optionGroupService.getOptionGroupById(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username);

                if (!optionGroup.isActive()) {
                        throw new IllegalArgumentException(
                                "This Option Value cannot be restored while its Option Group is retired.");
                }
                
                OptionValue optionValue = getOptionValueById(optionValueId, optionGroup);

                if (optionValue.isActive()) {
                        throw new IllegalArgumentException(
                                "This Option Value is already active.");
                }

                OptionValue parentOptionValue = optionValue.getParentOptionValue();

                if (parentOptionValue != null
                        && !parentOptionValue.isActive()) {

                        throw new IllegalArgumentException(
                                "This Option Value cannot be restored while its parent Option Value is retired.");
                }

                optionValue.setActive(true);
                optionValueRepository.save(optionValue);

                return optionValue.getValueName();
        }


        // -------------------- Input Normalization -------------------- //


        /**
         * Normalizes and validates an Option Value name.
         *
         * @param value value name to normalize
         * @return normalized value name
         */
        private String normalizeValueName(String value) {

                if (value == null || value.isBlank()) {
                        throw new IllegalArgumentException(
                                "Option value name is required.");
                }

                String normalizedValue = value.trim();

                if (normalizedValue.length() > 100) {
                        throw new IllegalArgumentException(
                                "Option value name cannot exceed 100 characters.");
                }

                return normalizedValue;
        }


        /**
         * Normalizes an optional Option Value description.
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


                /**
                 * Normalizes the Option Value display order.
                 *
                 * @param value display order to normalize
                 * @return normalized display order
                 */
                private Integer normalizeDisplayOrder(Integer value) {

                if (value == null) {
                        return 0;
                }

                if (value < 0) {
                        throw new IllegalArgumentException(
                                "Display order must be a non-negative integer.");
                }

                return value;
        }


        // -------------------- Parent Option Value Resolution -------------------- //

        /**
         * Resolves and validates the parent Option Value for an Option Group.
         *
         * <p>Values belonging to a root Option Group cannot have a parent value.
         * Values belonging to a child Option Group must reference a value from
         * that group's parent Option Group.</p>
         *
         * @param optionGroup Option Group containing the value
         * @param parentOptionValueId requested parent Option Value ID
         * @return resolved parent Option Value, or null for a root group
         */
        private OptionValue resolveParentOptionValue(OptionGroup optionGroup, Long parentOptionValueId) {

                OptionGroup parentOptionGroup = optionGroup.getParentOptionGroup();

                if (parentOptionGroup == null) {

                        if (parentOptionValueId != null) {
                        throw new IllegalArgumentException(
                                "Values in a root Option Group cannot have a parent value.");
                        }

                        return null;
                }

                if (parentOptionValueId == null) {
                        throw new IllegalArgumentException(
                                "A parent value is required for values in a child Option Group.");
                }

                OptionValue parentOptionValue = getOptionValueById(parentOptionValueId, parentOptionGroup);

                if (!parentOptionValue.isActive()) {
                throw new IllegalArgumentException(
                        "A retired Option Value cannot be used as a parent value.");
                }

                return parentOptionValue;
        }

        // -------------------- Active Option Values Retrieval -------------------- //

        /**
         * Returns all active option values belonging to the specified Option Group.
         *
         * @param optionGroup Option Group whose values are being retrieved
         * @return ordered list of active option values
         */
        @Transactional(readOnly = true)
        public List<OptionValue> getActiveOptionValuesForGroup(OptionGroup optionGroup) {

        return optionValueRepository
                .findByOptionGroupAndActiveTrueOrderByDisplayOrderAscValueNameAsc(
                        optionGroup);
        }


        // -------------------- Active Option Values Count -------------------- //


        /**
         * Returns the number of active Option Values belonging to an Option Group.
         *
         * @param optionGroup Option Group being evaluated
         * @return number of active Option Values
         */
        @Transactional(readOnly = true)
        public long countActiveOptionValues(OptionGroup optionGroup) {
        return optionValueRepository.countByOptionGroupAndActiveTrue(optionGroup);
        }

}