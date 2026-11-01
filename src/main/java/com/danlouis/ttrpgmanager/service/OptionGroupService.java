package com.danlouis.ttrpgmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.repository.OptionGroupRepository;
import com.danlouis.ttrpgmanager.repository.OptionValueRepository;

/**
 * Provides business logic for Game System Option Group management.
 *
 * <p>The service manages retrieval, creation, modification, and deletion
 * of Option Groups while enforcing System Term association, Game System
 * ownership, uniqueness, and input validation rules.</p>
 *
 * @author Daniel Louis
 */
@Service
public class OptionGroupService {

        private final OptionGroupRepository optionGroupRepository;
        private final SystemTermService systemTermService;
        private final OptionValueRepository optionValueRepository;


        /**
         * Constructs the OptionGroupService with its required dependencies.
         *
         * @param optionGroupRepository repository used to manage option groups
         * @param systemTermService service used to retrieve and authorize
         *        System Terms
         */
        public OptionGroupService(
                OptionGroupRepository optionGroupRepository,
                SystemTermService systemTermService,
                OptionValueRepository optionValueRepository) {

                this.optionGroupRepository = optionGroupRepository;
                this.systemTermService = systemTermService;
                this.optionValueRepository = optionValueRepository;
        }


        // -------------------- Option Group Retrieval -------------------- //


        /**
         * Returns all Option Groups belonging to a manageable System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param username username of the authenticated user
         * @return ordered list of Option Groups
         * @throws SecurityException if the user cannot manage the Game System
         */
        @Transactional(readOnly = true)
        public List<OptionGroup> getOptionGroups(
                Long gameSystemId,
                Long systemTermId,
                String username) {

                SystemTerm systemTerm =
                        systemTermService.getSystemTermById(
                                gameSystemId,
                                systemTermId,
                                username);

                return optionGroupRepository
                        .findBySystemTermOrderByDisplayOrderAscGroupNameAsc(
                                systemTerm);
        }

        /**
         * Returns an Option Group belonging to a manageable System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param username username of the authenticated user
         * @return matching Option Group
         * @throws IllegalArgumentException if the Option Group does not exist
         *         within the specified System Term
         * @throws SecurityException if the user cannot manage the Game System
         */
        @Transactional(readOnly = true)
        public OptionGroup getOptionGroupById(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                String username) {

                SystemTerm systemTerm =
                        systemTermService.getSystemTermById(
                                gameSystemId,
                                systemTermId,
                                username);

                return optionGroupRepository
                        .findByOptionGroupIdAndSystemTerm(
                                optionGroupId,
                                systemTerm)
                        .orElseThrow(() -> new IllegalArgumentException(
                                "Option group unavailable."));
        }

        /**
         * Returns all active Option Groups belonging to the specified System Term
         * for Game System profile presentation.
         *
         * @param systemTerm System Term whose Option Groups are being retrieved
         * @return ordered list of active Option Groups
         */
        @Transactional(readOnly = true)
        public List<OptionGroup> getOptionGroupsForProfile(SystemTerm systemTerm) {

        return optionGroupRepository
                .findBySystemTermAndActiveTrueOrderByDisplayOrderAscGroupNameAsc(
                        systemTerm);
        }

        /**
         * Returns all active Option Groups belonging to the specified System Term.
         *
         * @param systemTerm System Term whose Option Groups are being retrieved
         * @return ordered list of active Option Groups
         */
        @Transactional(readOnly = true)
        public List<OptionGroup> getActiveOptionGroupsForTerm(SystemTerm systemTerm) {

        return optionGroupRepository
                .findBySystemTermAndActiveTrueOrderByDisplayOrderAscGroupNameAsc(
                        systemTerm);
        }


        // -------------------- Creation, Update Retire and Restore Services -------------------- //


        /**
         * Creates a new Option Group for a manageable System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param username username of the authenticated user
         * @param groupName display name of the Option Group
         * @param description description of the Option Group
         * @param allowMultiple whether multiple values may be selected
         * @param displayOrder display position of the Option Group
         * @param parentOptionGroupId identifier of the parent Option Group, if any
         * @return newly created Option Group
         * @throws IllegalArgumentException if submitted data is invalid
         *         or produces a duplicate group name
         * @throws SecurityException if the user cannot manage the Game System
         */
        @Transactional
        public OptionGroup createOptionGroup(
                Long gameSystemId,
                Long systemTermId,
                String username,
                String groupName,
                String description,
                boolean allowMultiple,
                Integer displayOrder,
                Long parentOptionGroupId) {

                SystemTerm systemTerm =
                        systemTermService.getSystemTermById(
                                gameSystemId,
                                systemTermId,
                                username);

                if (!systemTerm.isActive()) {
                throw new IllegalArgumentException(
                        "Option Groups cannot be added to retired Terminology.");
                }

                String normalizedGroupName =
                        normalizeGroupName(groupName);

                if (optionGroupRepository
                        .existsBySystemTermAndGroupNameIgnoreCase(
                                systemTerm,
                                normalizedGroupName)) {

                throw new IllegalArgumentException(
                        "This System Term already contains an Option Group "
                        + "with that name.");
                }

                OptionGroup parentOptionGroup = resolveParentOptionGroup(systemTerm, parentOptionGroupId, null);
                
                OptionGroup optionGroup = new OptionGroup(
                        systemTerm,
                        normalizedGroupName,
                        normalizeDescription(description),
                        allowMultiple,
                        normalizeDisplayOrder(displayOrder),
                        parentOptionGroup);

                return optionGroupRepository.save(optionGroup);
        }


        /**
         * Updates an existing Option Group belonging to a manageable System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param username username of the authenticated user
         * @param groupName updated group name
         * @param description updated description
         * @param allowMultiple updated multiple-selection setting
         * @param displayOrder updated display position
         * @param parentOptionGroupId updated parent Option Group ID
         * @return updated Option Group
         * @throws IllegalArgumentException if submitted data is invalid
         *         or produces a duplicate group name
         * @throws SecurityException if the user cannot manage the Game System
         */
        @Transactional
        public OptionGroup updateOptionGroup(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                String username,
                String groupName,
                String description,
                boolean allowMultiple,
                Integer displayOrder,
                Long parentOptionGroupId) {

                OptionGroup optionGroup =
                        getOptionGroupById(
                                gameSystemId,
                                systemTermId,
                                optionGroupId,
                                username);

                if (!optionGroup.getSystemTerm().isActive()) {
                        throw new IllegalArgumentException(
                                "Option Groups cannot be updated for retired Terminology.");
                }

                String normalizedGroupName =
                        normalizeGroupName(groupName);

                if (optionGroupRepository
                        .existsBySystemTermAndGroupNameIgnoreCaseAndOptionGroupIdNot(
                                optionGroup.getSystemTerm(),
                                normalizedGroupName,
                                optionGroupId)) {

                throw new IllegalArgumentException(
                        "This System Term already contains an Option Group "
                        + "with that name.");
                }

                OptionGroup parentOptionGroup = resolveParentOptionGroup(optionGroup.getSystemTerm(), parentOptionGroupId, optionGroup);

                optionGroup.setGroupName(normalizedGroupName);
                optionGroup.setDescription(normalizeDescription(description));

                optionGroup.setAllowMultiple(allowMultiple);

                optionGroup.setDisplayOrder(
                        normalizeDisplayOrder(displayOrder));

                optionGroup.setParentOptionGroup(parentOptionGroup);

                return optionGroupRepository.save(optionGroup);
        }


        // -------------------- Retirement and Restoration -------------------- //


        /**
         * Retires an active Option Group.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param username username of the authenticated user
         * @return name of the retired Option Group
         */
        @Transactional
        public String retireOptionGroup(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                String username) {

        OptionGroup optionGroup =
                getOptionGroupById(
                        gameSystemId,
                        systemTermId,
                        optionGroupId,
                        username);

        if (!optionGroup.isActive()) {
                throw new IllegalArgumentException(
                        "This Option Group is already retired.");
        }

        if (optionGroupRepository
                .existsByParentOptionGroupAndActiveTrue(optionGroup)) {

                throw new IllegalArgumentException(
                        "This Option Group cannot be retired while it contains active child Option Groups.");
        }

        if (optionValueRepository
                .existsByOptionGroupAndActiveTrue(optionGroup)) {

                throw new IllegalArgumentException(
                        "This Option Group cannot be retired while it contains active Option Values.");
        }

        optionGroup.setActive(false);
        optionGroupRepository.save(optionGroup);

        return optionGroup.getGroupName();
        }


        /**
         * Restores a retired Option Group.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param optionGroupId identifier of the Option Group
         * @param username username of the authenticated user
         * @return name of the restored Option Group
         */
        @Transactional
        public String restoreOptionGroup(
                Long gameSystemId,
                Long systemTermId,
                Long optionGroupId,
                String username) {

        OptionGroup optionGroup =
                getOptionGroupById(
                        gameSystemId,
                        systemTermId,
                        optionGroupId,
                        username);

        if (optionGroup.isActive()) {
                throw new IllegalArgumentException(
                        "This Option Group is already active.");
        }

        SystemTerm systemTerm = optionGroup.getSystemTerm();

        if (!systemTerm.isActive()) {
        throw new IllegalArgumentException(
                "This Option Group cannot be restored while its Terminology is retired.");
        }

        OptionGroup parentOptionGroup = optionGroup.getParentOptionGroup();

        if (parentOptionGroup != null
                && !parentOptionGroup.isActive()) {

                throw new IllegalArgumentException(
                        "This Option Group cannot be restored while its parent Option Group is retired.");
        }

        optionGroup.setActive(true);
        optionGroupRepository.save(optionGroup);

        return optionGroup.getGroupName();
        }


        // -------------------- Input Normalization -------------------- //


        /**
         * Normalizes and validates an Option Group name.
         *
         * @param value group name to normalize
         * @return normalized group name
         * @throws IllegalArgumentException if the group name is invalid
         */
        private String normalizeGroupName(String value) {

                if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                        "Option group name is required.");
                }

                String normalizedValue = value.trim();

                if (normalizedValue.length() > 100) {
                throw new IllegalArgumentException(
                        "Option group name cannot exceed 100 characters.");
                }

                return normalizedValue;
        }


        /**
         * Normalizes an optional Option Group description.
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
         * Normalizes and validates an Option Group display order.
         *
         * @param value display order to normalize
         * @return normalized display order
         * @throws IllegalArgumentException if the display order is negative
         */
        private Integer normalizeDisplayOrder(Integer value) {

                if (value == null) {
                return 0;
                }

                if (value < 0) {
                throw new IllegalArgumentException(
                        "Display order cannot be negative.");
                }

                return value;
        }

        // -------------------- Hierarchy Handling -------------------- //

        /**
         * Resolves and validates an optional parent Option Group.
         *
         * <p>The parent must belong to the same System Term. During editing,
         * an Option Group cannot be its own parent or be placed beneath one
         * of its descendants.</p>
         *
         * @param systemTerm System Term containing the Option Groups
         * @param parentOptionGroupId requested parent Option Group ID
         * @param currentOptionGroup Option Group being edited, or null during creation
         * @return resolved parent Option Group, or null for a root group
         */
        private OptionGroup resolveParentOptionGroup(
                SystemTerm systemTerm,
                Long parentOptionGroupId,
                OptionGroup currentOptionGroup) {

        if (parentOptionGroupId == null) {
                return null;
        }

        OptionGroup parentOptionGroup = optionGroupRepository
                .findByOptionGroupIdAndSystemTerm(
                        parentOptionGroupId,
                        systemTerm)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Parent Option Group unavailable."));

        if (currentOptionGroup == null) {
                return parentOptionGroup;
        }

        if (parentOptionGroup.getOptionGroupId()
                .equals(currentOptionGroup.getOptionGroupId())) {

                throw new IllegalArgumentException(
                        "An Option Group cannot be its own parent.");
        }

        OptionGroup ancestor = parentOptionGroup;

        while (ancestor != null) {

                if (ancestor.getOptionGroupId()
                        .equals(currentOptionGroup.getOptionGroupId())) {

                throw new IllegalArgumentException(
                        "An Option Group cannot be moved beneath one of its descendants.");
                }

                ancestor = ancestor.getParentOptionGroup();
        }

        return parentOptionGroup;
        }        
}