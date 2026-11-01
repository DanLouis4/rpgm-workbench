package com.danlouis.ttrpgmanager.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.entity.StatDefinition;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.repository.StatDefinitionRepository;

/**
 * Provides business logic for Stat Definition management.
 *
 * <p>The service manages Stat Definition retrieval, creation,
 * modification, retirement, restoration, hierarchy validation,
 * duplicate prevention, and input normalization.</p>
 *
 * <p>RPGM Workbench stores Stat Definitions as descriptive labels
 * and relationships without enforcing system-specific calculations,
 * formulas, ranges, or value types.</p>
 *
 * @author Daniel Louis
 */
@Service
public class StatDefinitionService {

    private final StatDefinitionRepository statDefinitionRepository;
    private final SystemTermService systemTermService;


    /**
     * Constructs the StatDefinitionService with its required dependencies.
     *
     * @param statDefinitionRepository repository used to manage Stat Definitions
     * @param systemTermService service used to retrieve and validate System Terms
     */
    public StatDefinitionService(
            StatDefinitionRepository statDefinitionRepository,
            SystemTermService systemTermService) {

        this.statDefinitionRepository = statDefinitionRepository;
        this.systemTermService = systemTermService;
    }


    // -------------------- Stat Definition Retrieval -------------------- //


    /**
     * Returns all Stat Definitions belonging to the specified System Term.
     *
     * <p>Both active and retired definitions are returned so the Game
     * Designer can manage the complete configuration history.</p>
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param username username of the authenticated user
     * @return ordered list of Stat Definitions
     */
    @Transactional(readOnly = true)
    public List<StatDefinition> getStatDefinitions(
            Long gameSystemId,
            Long systemTermId,
            String username) {

        SystemTerm systemTerm = systemTermService.getSystemTermById(gameSystemId, systemTermId, username);

        return statDefinitionRepository
                .findBySystemTermOrderByDisplayOrderAscStatNameAsc(systemTerm);
    }


    /**
     * Returns all active Stat Definitions belonging to the specified
     * System Term.
     *
     * @param systemTerm System Term whose Stats are being retrieved
     * @return ordered list of active Stat Definitions
     */
    @Transactional(readOnly = true)
    public List<StatDefinition> getActiveStatDefinitions(
            SystemTerm systemTerm) {

        return statDefinitionRepository
                .findBySystemTermAndActiveTrueOrderByDisplayOrderAscStatNameAsc(systemTerm);
    }


    /**
     * Returns a Stat Definition belonging to the specified System Term.
     *
     * @param statDefinitionId identifier of the Stat Definition
     * @param systemTerm System Term that must own the Stat Definition
     * @return matching Stat Definition
     * @throws IllegalArgumentException if the Stat Definition does not exist
     */
    @Transactional(readOnly = true)
    public StatDefinition getStatDefinitionById(
            Long statDefinitionId,
            SystemTerm systemTerm) {

        return statDefinitionRepository
                .findByStatDefinitionIdAndSystemTerm(
                        statDefinitionId,
                        systemTerm)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Stat Definition unavailable."));
    }


    // -------------------- Creation and Modification -------------------- //


    /**
     * Creates a new Stat Definition within a manageable System Term.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param parentStatId identifier of the parent Stat Definition, if any
     * @param username username of the authenticated user
     * @param statName display name of the Stat
     * @param abbreviation optional abbreviated label
     * @param description optional description
     * @param displayOrder presentation order
     * @return newly created Stat Definition
     */
    @Transactional
    public StatDefinition createStatDefinition(
            Long gameSystemId,
            Long systemTermId,
            Long parentStatId,
            String username,
            String statName,
            String abbreviation,
            String description,
            Integer displayOrder) {

        SystemTerm systemTerm = systemTermService.getSystemTermById(gameSystemId, systemTermId, username);

        if (!systemTerm.isHasStats()) {
                throw new IllegalArgumentException(
                        "Stats are not enabled for this Terminology.");
        }

        if (!systemTerm.isActive()) {
                throw new IllegalArgumentException(
                        "Stat Definitions cannot be added to retired Terminology.");
        }

        String normalizedStatName = normalizeStatName(statName);

        if (statDefinitionRepository
                        .existsBySystemTermAndStatNameIgnoreCase(
                                systemTerm,
                                normalizedStatName)) {

                throw new IllegalArgumentException(
                        "This Terminology already contains a Stat "
                        + "with that name.");
        }

        StatDefinition parentStat =
                resolveParentStat(
                        systemTerm,
                        parentStatId,
                        null);

        StatDefinition statDefinition = new StatDefinition(
                systemTerm,
                parentStat,
                normalizedStatName,
                normalizeAbbreviation(abbreviation),
                normalizeDescription(description),
                normalizeDisplayOrder(displayOrder));

        return statDefinitionRepository.save(statDefinition);
    }


    /**
     * Updates an existing Stat Definition.
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionId identifier of the Stat Definition
     * @param parentStatId identifier of the parent Stat Definition, if any
     * @param username username of the authenticated user
     * @param statName updated Stat name
     * @param abbreviation updated abbreviation
     * @param description updated description
     * @param displayOrder updated presentation order
     * @return updated Stat Definition
     */
    @Transactional
    public StatDefinition updateStatDefinition(
            Long gameSystemId,
            Long systemTermId,
            Long statDefinitionId,
            Long parentStatId,
            String username,
            String statName,
            String abbreviation,
            String description,
            Integer displayOrder) {

        SystemTerm systemTerm = systemTermService.getSystemTermById(gameSystemId, systemTermId, username);


        if (!systemTerm.isActive()) {
                throw new IllegalArgumentException(
                        "Stat Definitions cannot be updated for retired Terminology.");
        }

        StatDefinition statDefinition = getStatDefinitionById(statDefinitionId, systemTerm);

        String normalizedStatName = normalizeStatName(statName);

        if (statDefinitionRepository
                .existsBySystemTermAndStatNameIgnoreCaseAndStatDefinitionIdNot(
                        systemTerm,
                        normalizedStatName,
                        statDefinitionId)) {

            throw new IllegalArgumentException(
                    "This Terminology already contains a Stat with that name.");
        }

        StatDefinition parentStat = resolveParentStat(systemTerm, parentStatId, statDefinition);

        statDefinition.setParentStat(parentStat);
        
        statDefinition.setStatName(normalizedStatName);

        statDefinition.setAbbreviation(normalizeAbbreviation(abbreviation));

        statDefinition.setDescription(normalizeDescription(description));

        statDefinition.setDisplayOrder(normalizeDisplayOrder(displayOrder));

        return statDefinitionRepository.save(statDefinition);
    }


    // -------------------- Retirement and Restoration -------------------- //


    /**
     * Retires an active Stat Definition.
     *
     * <p>Retired Stat Definitions remain available to existing Character
     * records but should not be offered for new Character data.</p>
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionId identifier of the Stat Definition
     * @param username username of the authenticated user
     * @return name of the retired Stat Definition
     */
    @Transactional
    public String retireStatDefinition(
            Long gameSystemId,
            Long systemTermId,
            Long statDefinitionId,
            String username) {

        SystemTerm systemTerm = systemTermService.getSystemTermById(gameSystemId, systemTermId, username);

        StatDefinition statDefinition = getStatDefinitionById(statDefinitionId, systemTerm);

        if (!statDefinition.isActive()) {
            throw new IllegalArgumentException(
                    "This Stat Definition is already retired.");
        }

        if (statDefinitionRepository
                .existsByParentStatAndActiveTrue(
                        statDefinition)) {

            throw new IllegalArgumentException(
                    "This Stat Definition cannot be retired while it contains active child Stats.");
        }

        statDefinition.setActive(false);
        statDefinitionRepository.save(statDefinition);

        return statDefinition.getStatName();
    }


    /**
     * Restores a retired Stat Definition.
     *
     * <p>A child Stat cannot be restored while its parent Stat remains
     * retired.</p>
     *
     * @param gameSystemId identifier of the Game System
     * @param systemTermId identifier of the System Term
     * @param statDefinitionId identifier of the Stat Definition
     * @param username username of the authenticated user
     * @return name of the restored Stat Definition
     */
    @Transactional
    public String restoreStatDefinition(
            Long gameSystemId,
            Long systemTermId,
            Long statDefinitionId,
            String username) {

        SystemTerm systemTerm = systemTermService.getSystemTermById(gameSystemId, systemTermId, username);

        if (!systemTerm.isActive()) {
                throw new IllegalArgumentException(
                        "This Stat Definition cannot be restored while its Terminology is retired.");
        }

        if (!systemTerm.isHasStats()) {
                throw new IllegalArgumentException(
                        "Stats must be enabled for this Terminology before this Stat Definition can be restored.");
        }

        StatDefinition statDefinition = getStatDefinitionById(statDefinitionId, systemTerm);

        if (statDefinition.isActive()) {
            throw new IllegalArgumentException(
                    "This Stat Definition is already active.");
        }

        StatDefinition parentStat =
                statDefinition.getParentStat();

        if (parentStat != null
                && !parentStat.isActive()) {

            throw new IllegalArgumentException(
                    "This Stat Definition cannot be restored while "
                    + "its parent Stat is retired.");
        }

        statDefinition.setActive(true);
        statDefinitionRepository.save(statDefinition);

        return statDefinition.getStatName();
    }


    // -------------------- Hierarchy Handling -------------------- //


    /**
     * Resolves and validates an optional parent Stat Definition.
     *
     * <p>The parent Stat must belong to the same System Term and must
     * be active. During editing, a Stat Definition cannot be its own
     * parent or be placed beneath one of its descendants.</p>
     *
     * @param systemTerm owning System Term
     * @param parentStatId requested parent Stat Definition ID
     * @param currentStat Stat Definition being edited, or null during creation
     * @return resolved parent Stat Definition, or null
     */
    private StatDefinition resolveParentStat(
            SystemTerm systemTerm,
            Long parentStatId,
            StatDefinition currentStat) {

        if (parentStatId == null) {
            return null;
        }

        StatDefinition parentStat = getStatDefinitionById(parentStatId, systemTerm);

        if (!parentStat.isActive()) {
            throw new IllegalArgumentException(
                    "A retired Stat Definition cannot be used as a parent Stat.");
        }

        if (currentStat == null) {
            return parentStat;
        }

        if (parentStat.getStatDefinitionId()
                .equals(currentStat.getStatDefinitionId())) {

            throw new IllegalArgumentException(
                    "A Stat Definition cannot be its own parent.");
        }

        StatDefinition ancestor = parentStat;

        while (ancestor != null) {

            if (ancestor.getStatDefinitionId()
                    .equals(currentStat.getStatDefinitionId())) {

                throw new IllegalArgumentException(
                        "A Stat Definition cannot be moved beneath one of its descendants.");
            }

            ancestor = ancestor.getParentStat();
        }

        return parentStat;
    }


    // -------------------- Input Normalization -------------------- //


    /**
     * Normalizes and validates a Stat name.
     *
     * @param value Stat name to normalize
     * @return normalized Stat name
     */
    private String normalizeStatName(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Stat name is required.");
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > 100) {
            throw new IllegalArgumentException(
                    "Stat name cannot exceed 100 characters.");
        }

        return normalizedValue;
    }


    /**
     * Normalizes and validates an optional Stat abbreviation.
     *
     * @param value abbreviation to normalize
     * @return normalized abbreviation or null
     */
    private String normalizeAbbreviation(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > 20) {
            throw new IllegalArgumentException(
                    "Abbreviation cannot exceed 20 characters.");
        }

        return normalizedValue;
    }


    /**
     * Normalizes an optional Stat description.
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
     * Normalizes and validates a Stat display order.
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
                    "Display order cannot be negative.");
        }

        return value;
    }
}