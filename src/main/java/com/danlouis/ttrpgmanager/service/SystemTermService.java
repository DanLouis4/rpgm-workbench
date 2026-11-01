package com.danlouis.ttrpgmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.repository.OptionGroupRepository;
import com.danlouis.ttrpgmanager.repository.StatDefinitionRepository;
import com.danlouis.ttrpgmanager.repository.SystemTermRepository;

/**
 * Provides business logic for Game System terminology management.
 *
 * <p>The service manages retrieval, creation, modification, retirement
 * and restoration of System Terms while enforcing Game System ownership, permission,
 * uniqueness, and input validation rules.</p>
 *
 * @author Daniel Louis
 */
@Service
public class SystemTermService {

        private final SystemTermRepository systemTermRepository;
        private final GameSystemService gameSystemService;
        private final OptionGroupRepository optionGroupRepository;
        private final StatDefinitionRepository statDefinitionRepository;


        /**
         * Constructs the SystemTermService with its required dependencies.
         *
         * @param systemTermRepository repository used to manage system terms
         * @param gameSystemService service used to retrieve and authorize
         *        Game Systems
         */
        public SystemTermService(
                SystemTermRepository systemTermRepository,
                GameSystemService gameSystemService,
                OptionGroupRepository optionGroupRepository,
                StatDefinitionRepository statDefinitionRepository) {

        this.systemTermRepository = systemTermRepository;
        this.gameSystemService = gameSystemService;
        this.optionGroupRepository = optionGroupRepository;
        this.statDefinitionRepository = statDefinitionRepository;
        }


        // -------------------- System Term Retrieval -------------------- //


        /**
         * Returns all system terms belonging to a manageable Game System.
         *
         * @param gameSystemId identifier of the game system
         * @param username username of the authenticated user
         * @return ordered list of system terms
         * @throws SecurityException if the user cannot manage the game system
         */
        @Transactional(readOnly = true)
        public List<SystemTerm> getSystemTerms(
                Long gameSystemId,
                String username) {

        GameSystem gameSystem =
                gameSystemService.getManageableGameSystemById(
                        gameSystemId,
                        username);

        return systemTermRepository
                .findByGameSystemOrderByDisplayOrderAscSingularLabelAsc(
                        gameSystem);
        }


        /**
         * Returns a system term belonging to a manageable Game System.
         *
         * @param gameSystemId identifier of the game system
         * @param systemTermId identifier of the system term
         * @param username username of the authenticated user
         * @return matching system term
         * @throws IllegalArgumentException if the system term does not exist
         *         within the specified Game System
         * @throws SecurityException if the user cannot manage the game system
         */
        @Transactional(readOnly = true)
        public SystemTerm getSystemTermById(
                Long gameSystemId,
                Long systemTermId,
                String username) {

        GameSystem gameSystem =
                gameSystemService.getManageableGameSystemById(
                        gameSystemId,
                        username);

        return systemTermRepository
                .findBySystemTermIdAndGameSystem(
                        systemTermId,
                        gameSystem)
                .orElseThrow(() -> new IllegalArgumentException(
                        "System term unavailable."));
        }


        // -------------------- Creation and Modification -------------------- //


        /**
         * Creates a new system term for a manageable Game System.
         *
         * <p>The internal term key is generated from the initial singular
         * label and remains stable after creation.</p>
         *
         * @param gameSystemId identifier of the game system
         * @param username username of the authenticated user
         * @param singularLabel singular user-facing label
         * @param pluralLabel plural user-facing label
         * @param description description of the term
         * @param hasCharacterOptions indicates whether the term has character options
         * @param hasStats indicates whether the term has stats
         * @param displayOrder display position of the term
         * @return newly created system term
         * @throws IllegalArgumentException if submitted data is invalid or
         *         produces a duplicate term key
         * @throws SecurityException if the user cannot manage the game system
         */
        @Transactional
        public SystemTerm createSystemTerm(
                Long gameSystemId,
                String username,
                String singularLabel,
                String pluralLabel,
                String description,
                boolean hasCharacterOptions,
                boolean hasStats,
                Integer displayOrder) {

        GameSystem gameSystem =
                gameSystemService.getManageableGameSystemById(
                        gameSystemId,
                        username);

        String normalizedSingularLabel =
                normalizeRequiredLabel(
                        singularLabel,
                        "Singular label");


        if (systemTermRepository
                .existsByGameSystemAndSingularLabelIgnoreCase(
                        gameSystem,
                        normalizedSingularLabel)) {

                throw new IllegalArgumentException(
                        "This Game System already contains that Terminology.");
        }

        SystemTerm systemTerm = new SystemTerm(
                gameSystem,
                normalizedSingularLabel,
                normalizeOptionalLabel(
                        pluralLabel,
                        "Plural label"),
                normalizeDescription(description),

                hasCharacterOptions,
                hasStats,
                normalizeDisplayOrder(displayOrder));

        return systemTermRepository.save(systemTerm);
        }


        /**
         * Updates an existing system term belonging to a manageable Game System.
         *
         * <p>The original internal term key is preserved so user-facing
         * terminology may change without changing the term's internal identity.</p>
         *
         * @param gameSystemId identifier of the game system
         * @param systemTermId identifier of the system term
         * @param username username of the authenticated user
         * @param singularLabel updated singular label
         * @param pluralLabel updated plural label
         * @param description updated description
         * @param hasCharacterOptions updated character options flag
         * @param hasStats updated stats flag
         * @param displayOrder updated display position
         * @return updated system term
         * @throws IllegalArgumentException if submitted data is invalid
         * @throws SecurityException if the user cannot manage the game system
         */
        @Transactional
        public SystemTerm updateSystemTerm(
                Long gameSystemId,
                Long systemTermId,
                String username,
                String singularLabel,
                String pluralLabel,
                String description,
                boolean hasCharacterOptions,
                boolean hasStats,
                Integer displayOrder) {

        SystemTerm systemTerm =
                getSystemTermById(
                        gameSystemId,
                        systemTermId,
                        username);

        String normalizedSingularLabel =
                normalizeRequiredLabel(
                        singularLabel,
                        "Singular label");

        if (systemTermRepository
                .existsByGameSystemAndSingularLabelIgnoreCaseAndSystemTermIdNot(
                        systemTerm.getGameSystem(),
                        normalizedSingularLabel,
                        systemTermId)) {

                throw new IllegalArgumentException(
                        "This Game System already contains that Terminology.");
        }

        if (!hasCharacterOptions
                && optionGroupRepository.existsBySystemTermAndActiveTrue(systemTerm)) {

        throw new IllegalArgumentException(
                "Character Options cannot be disabled while this Terminology contains active Option Groups.");
        }

        if (!hasStats
                && statDefinitionRepository.existsBySystemTermAndActiveTrue(systemTerm)) {

                throw new IllegalArgumentException(
                        "Stats cannot be disabled while this Terminology contains active Stat Definitions.");
        }

        systemTerm.setSingularLabel(normalizedSingularLabel);

        systemTerm.setPluralLabel(
                normalizeOptionalLabel(
                        pluralLabel,
                        "Plural label"));

        systemTerm.setDescription(
                normalizeDescription(description));

        systemTerm.setHasCharacterOptions(hasCharacterOptions);
        systemTerm.setHasStats(hasStats);

        systemTerm.setDisplayOrder(
                normalizeDisplayOrder(displayOrder));

        return systemTermRepository.save(systemTerm);
        }

        // -------------------- Retirement and Restoration -------------------- //


        /**
         * Retires an active System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param username username of the authenticated user
         * @return singular label of the retired System Term
         */
        @Transactional
        public String retireSystemTerm(
                Long gameSystemId,
                Long systemTermId,
                String username) {

        SystemTerm systemTerm =
                getSystemTermById(
                        gameSystemId,
                        systemTermId,
                        username);

        if (!systemTerm.isActive()) {
                throw new IllegalArgumentException(
                        "This Terminology is already retired.");
        }

        if (optionGroupRepository.existsBySystemTermAndActiveTrue(systemTerm)) {
                throw new IllegalArgumentException(
                        "This Terminology cannot be retired while it contains active Option Groups.");
        }

        if (statDefinitionRepository.existsBySystemTermAndActiveTrue(systemTerm)) {
                throw new IllegalArgumentException(
                        "This Terminology cannot be retired while it contains active Stat Definitions.");
        }

        systemTerm.setActive(false);
        systemTermRepository.save(systemTerm);

        return systemTerm.getSingularLabel();
        }


        /**
         * Restores a retired System Term.
         *
         * @param gameSystemId identifier of the Game System
         * @param systemTermId identifier of the System Term
         * @param username username of the authenticated user
         * @return singular label of the restored System Term
         */
        @Transactional
        public String restoreSystemTerm(
                Long gameSystemId,
                Long systemTermId,
                String username) {

        SystemTerm systemTerm =
                getSystemTermById(
                        gameSystemId,
                        systemTermId,
                        username);

        if (systemTerm.isActive()) {
                throw new IllegalArgumentException(
                        "This Terminology is already active.");
        }

        systemTerm.setActive(true);
        systemTermRepository.save(systemTerm);

        return systemTerm.getSingularLabel();
        }


        // -------------------- Input Normalization -------------------- //


        /**
         * Normalizes and validates a required system term label.
         *
         * @param value label to normalize
         * @param fieldName field name used in validation messages
         * @return normalized label
         * @throws IllegalArgumentException if the label is invalid
         */
        private String normalizeRequiredLabel(
                String value,
                String fieldName) {

        if (value == null || value.isBlank()) {
                throw new IllegalArgumentException(
                        fieldName + " is required.");
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > 50) {
                throw new IllegalArgumentException(
                        fieldName
                        + " cannot exceed 50 characters.");
        }

        return normalizedValue;
        }


        /**
         * Normalizes and validates an optional system term label.
         *
         * @param value label to normalize
         * @param fieldName field name used in validation messages
         * @return normalized label or null
         * @throws IllegalArgumentException if the label is too long
         */
        private String normalizeOptionalLabel(
                String value,
                String fieldName) {

        if (value == null || value.isBlank()) {
                return null;
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > 50) {
                throw new IllegalArgumentException(
                        fieldName
                        + " cannot exceed 50 characters.");
        }

        return normalizedValue;
        }


        /**
         * Normalizes an optional system term description.
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
         * Normalizes and validates a system term display order.
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

        
        /**
         * Returns all System Terms belonging to the specified Game System
         * for profile display.
         *
         * <p>Record visibility must be established by the calling workflow
         * before this method is used.</p>
         *
         * @param gameSystem Game System whose terms are being retrieved
         * @return ordered list of active System Terms
         */
        @Transactional(readOnly = true)
        public List<SystemTerm> getSystemTermsForProfile(
                GameSystem gameSystem) {

        return systemTermRepository.findByGameSystemAndActiveTrueOrderByDisplayOrderAscSingularLabelAsc(gameSystem);
        }
        
}