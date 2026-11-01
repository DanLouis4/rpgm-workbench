package com.danlouis.ttrpgmanager.service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.danlouis.ttrpgmanager.entity.GameSystem;
import com.danlouis.ttrpgmanager.entity.OptionGroup;
import com.danlouis.ttrpgmanager.entity.OptionValue;
import com.danlouis.ttrpgmanager.entity.StatDefinition;
import com.danlouis.ttrpgmanager.entity.SystemTerm;
import com.danlouis.ttrpgmanager.entity.User;
import com.danlouis.ttrpgmanager.repository.GameSystemRepository;
import com.danlouis.ttrpgmanager.repository.OptionGroupRepository;
import com.danlouis.ttrpgmanager.repository.OptionValueRepository;
import com.danlouis.ttrpgmanager.repository.StatDefinitionRepository;
import com.danlouis.ttrpgmanager.repository.SystemTermRepository;
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
    private static final String GAME_ADMIN_ROLE = "ADMINISTRATOR";

    private final UserRepository userRepository;
    private final GameSystemRepository gameSystemRepository;
    private final SystemTermRepository systemTermRepository;
    private final OptionGroupRepository optionGroupRepository;
    private final OptionValueRepository optionValueRepository;
    private final StatDefinitionRepository statDefinitionRepository;

    /**
     * Constructs the GameSystemService with its required repositories.
     *
     * @param gameSystemRepository repository used to manage game systems
     * @param userRepository repository used to retrieve application users
     */
    public GameSystemService(
            UserRepository userRepository,
            GameSystemRepository gameSystemRepository,
            SystemTermRepository systemTermRepository,
            OptionGroupRepository optionGroupRepository,
            OptionValueRepository optionValueRepository,
            StatDefinitionRepository statDefinitionRepository) {

        this.userRepository = userRepository;
        this.gameSystemRepository = gameSystemRepository;
        this.systemTermRepository = systemTermRepository;
        this.optionGroupRepository = optionGroupRepository;
        this.optionValueRepository = optionValueRepository;
        this.statDefinitionRepository = statDefinitionRepository;
    }


    // -------------------- Game System Retrieval -------------------- //


    /**
     * Returns all publicly viewable game systems.
     *
     * @return public game systems ordered by name
     */
    @Transactional(readOnly = true)
    public List<GameSystem> getPublicGameSystems() {

        return gameSystemRepository.findByIsPublicTrueOrderByNameAsc();
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

        return gameSystemRepository.findByOwnerOrderByUpdatedAtDesc(owner);
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


    /**
     * Returns a game system when the specified user has permission to manage it.
     *
     * <p>The user must have Game System management permissions and must own
     * the requested Game System.</p>
     *
     * @param gameSystemId identifier of the game system
     * @param username username of the authenticated user
     * @return the manageable game system
     * @throws IllegalArgumentException if the user or game system does not exist
     * @throws SecurityException if the user does not have permission to manage
     *         the game system
     */
    @Transactional(readOnly = true)
    public GameSystem getManageableGameSystemById(Long gameSystemId, String username) {

        User user = getUserByUsername(username);

        requireGameSystemManagementRole(user);

        GameSystem gameSystem =
                getGameSystemById(gameSystemId);

        requireOwner(gameSystem, user);

        return gameSystem;
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
    public boolean isOwner(GameSystem gameSystem, String username) {

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
     * Creates a new game system owned by the specified authorized user.
     *
     * @param username username of the authenticated user
     * @param name display name of the game system
     * @param genre primary genre of the game system
     * @param rulesetFamily ruleset family used by the game system
     * @param rulesetEdition ruleset edition used by the game system
     * @param description descriptive information for the game system
     * @param systemOverview detailed overview of the game system
     * @param isPublic whether the game system is publicly viewable
     * @param cloneable whether the game system may be cloned by other users
     * @return the newly created game system
     * @throws SecurityException if the user does not have permission to manage game systems
     */
    @Transactional
    public GameSystem createGameSystem(
            String username,
            String name,
            String genre,
            String rulesetFamily,
            String rulesetEdition,
            String description,
            String systemOverview,
            boolean isPublic,
            boolean cloneable) {

        User owner = getUserByUsername(username);

        requireGameSystemManagementRole(owner);

        GameSystem gameSystem = new GameSystem(
                owner,
                normalizeName(name),
                normalizeOptionalText(genre, 50),
                normalizeOptionalText(rulesetFamily, 100),
                normalizeOptionalText(rulesetEdition, 50),
                normalizeDescription(description),
                normalizeSystemOverview(systemOverview),
                isPublic,
                false);

        gameSystem.setCloneable(isPublic && cloneable);

        return gameSystemRepository.save(gameSystem);
    }
   
    /**
     * Updates a game system owned by the specified authorized user.
     *
     * @param gameSystemId identifier of the game system
     * @param username username of the authenticated user
     * @param name updated display name
     * @param description updated description
     * @param isPublic updated visibility setting
     * @param cloneable updated cloneable setting
     * @return the updated game system
     * @throws SecurityException if the user does not have permission to manage the game system
     */
    @Transactional
    public GameSystem updateGameSystem(
            Long gameSystemId,
            String username,
            String name,
            String genre,
            String rulesetFamily,
            String rulesetEdition,
            String description,
            String systemOverview,
            boolean isPublic,
            boolean cloneable) {

        User user = getUserByUsername(username);

        requireGameSystemManagementRole(user);

        GameSystem gameSystem = getGameSystemById(gameSystemId);

        requireOwner(gameSystem, user);

        gameSystem.setName(normalizeName(name));
        gameSystem.setGenre(normalizeOptionalText(genre, 50));
        gameSystem.setRulesetFamily(normalizeOptionalText(rulesetFamily, 100));
        gameSystem.setRulesetEdition(normalizeOptionalText(rulesetEdition, 50));
        gameSystem.setDescription(normalizeDescription(description));
        gameSystem.setSystemOverview(normalizeSystemOverview(systemOverview));
        gameSystem.setPublic(isPublic);
        gameSystem.setCloneable(isPublic && cloneable);

        return gameSystemRepository.save(gameSystem);
    }


    // -------------------- Game System Cloning -------------------- //


    /**
     * Determines whether the specified user may clone the supplied Game System.
     *
     * <p>A Game System must be public and explicitly marked as cloneable.
     * The user must also have Game System management permissions.</p>
     *
     * @param gameSystem Game System being evaluated
     * @param username username of the authenticated user
     * @return true when the user may clone the Game System
     */
    @Transactional(readOnly = true)
    public boolean canClone(GameSystem gameSystem, String username) {

        if (gameSystem == null
                || !gameSystem.isPublic()
                || !gameSystem.isCloneable()
                || username == null
                || username.isBlank()) {

            return false;
        }

        User user = userRepository
                .findByUsername(username)
                .orElse(null);

        return user != null && hasGameSystemManagementRole(user);
    }


    /**
     * Creates an independent copy of a public cloneable Game System.
     *
     * <p>The cloned Game System receives a new owner and fresh database
     * identifiers. Only active System Terms, Option Groups, Option Values,
     * and Stat Definitions are copied. Parent-child relationships are
     * reconstructed using the identifiers of the newly created records.</p>
     *
     * <p>The new Game System is private and non-cloneable by default.
     * The immediate source Game System and the user who originally added
     * the source lineage are retained as provenance.</p>
     *
     * @param gameSystemId identifier of the Game System being cloned
     * @param username username of the authenticated cloning user
     * @param cloneName name assigned to the cloned Game System
     * @return newly created independent Game System
     * @throws SecurityException if cloning is not permitted
     * @throws IllegalArgumentException if the clone name is invalid
     * @throws IllegalStateException if an active hierarchy is inconsistent
     */
    @Transactional
    public GameSystem cloneGameSystem(Long gameSystemId, String username, String cloneName) {

        User newOwner = getUserByUsername(username);

        requireGameSystemManagementRole(newOwner);

        GameSystem sourceSystem = getGameSystemById(gameSystemId);

        if (!sourceSystem.isPublic()
                || !sourceSystem.isCloneable()) {

            throw new SecurityException(
                    "This Game System is not available for cloning.");
        }


        // Create the independent Game System record.

        GameSystem clonedSystem = new GameSystem(
                newOwner,
                normalizeName(cloneName),
                sourceSystem.getGenre(),
                sourceSystem.getRulesetFamily(),
                sourceSystem.getRulesetEdition(),
                sourceSystem.getDescription(),
                sourceSystem.getSystemOverview(),
                false,
                false);

        clonedSystem.setCloneable(false);

        clonedSystem.setClonedFromGameSystem(sourceSystem);


        // Preserve the first user in the clone lineage.

        User originalAddedBy =
                sourceSystem.getOriginalAddedBy() != null
                        ? sourceSystem.getOriginalAddedBy()
                        : sourceSystem.getOwner();

        clonedSystem.setOriginalAddedBy(originalAddedBy);

        clonedSystem = gameSystemRepository.save(clonedSystem);


        // Retrieve only active configuration from the source.

        List<SystemTerm> sourceTerms =
                systemTermRepository
                        .findByGameSystemAndActiveTrueOrderByDisplayOrderAscSingularLabelAsc(
                                sourceSystem);


        // Clone each configuration level and rebuild its hierarchy.

        Map<Long, SystemTerm> clonedTerms = cloneSystemTerms(sourceTerms, clonedSystem);

        Map<Long, OptionGroup> clonedGroups = cloneOptionGroups(sourceTerms, clonedTerms);

        cloneOptionValues(sourceTerms, clonedGroups);

        cloneStatDefinitions(sourceTerms, clonedTerms);

        return clonedSystem;
    }

    /**
     * Copies active System Terms into the cloned Game System.
     *
     * @param sourceTerms active System Terms belonging to the source system
     * @param clonedSystem newly created Game System
     * @return mapping of original System Term IDs to cloned System Terms
     */
    private Map<Long, SystemTerm> cloneSystemTerms(List<SystemTerm> sourceTerms, GameSystem clonedSystem) {

        Map<Long, SystemTerm> clonedTerms = new LinkedHashMap<>();

        for (SystemTerm sourceTerm : sourceTerms) {

            SystemTerm clonedTerm = new SystemTerm(
                    clonedSystem,
                    sourceTerm.getSingularLabel(),
                    sourceTerm.getPluralLabel(),
                    sourceTerm.getDescription(),
                    sourceTerm.isHasCharacterOptions(),
                    sourceTerm.isHasStats(),
                    sourceTerm.getDisplayOrder());

            clonedTerm = systemTermRepository.save(clonedTerm);

            clonedTerms.put(
                    sourceTerm.getSystemTermId(),
                    clonedTerm);
        }

        return clonedTerms;
    }

    /**
     * Copies active Option Groups and rebuilds their parent Group
     * relationships.
     *
     * @param sourceTerms active source System Terms
     * @param clonedTerms cloned System Terms mapped by original ID
     * @return mapping of original Option Group IDs to cloned Option Groups
     * @throws IllegalStateException if an active parent Group was not cloned
     */
    private Map<Long, OptionGroup> cloneOptionGroups(List<SystemTerm> sourceTerms, Map<Long, SystemTerm> clonedTerms) {

        Map<Long, OptionGroup> clonedGroups = new LinkedHashMap<>();

        List<OptionGroup> sourceGroups = new ArrayList<>();


        // First pass:
        // Create all active Groups without parent relationships.

        for (SystemTerm sourceTerm : sourceTerms) {

            SystemTerm clonedTerm =
                    clonedTerms.get(
                            sourceTerm.getSystemTermId());

            List<OptionGroup> termGroups =
                    optionGroupRepository
                            .findBySystemTermAndActiveTrueOrderByDisplayOrderAscGroupNameAsc(
                                    sourceTerm);

            for (OptionGroup sourceGroup : termGroups) {

                OptionGroup clonedGroup = new OptionGroup(
                        clonedTerm,
                        sourceGroup.getGroupName(),
                        sourceGroup.getDescription(),
                        sourceGroup.isAllowMultiple(),
                        sourceGroup.getDisplayOrder(),
                        null);

                clonedGroup = optionGroupRepository.save(clonedGroup);

                clonedGroups.put(sourceGroup.getOptionGroupId(), clonedGroup);

                sourceGroups.add(sourceGroup);
            }
        }


        // Second pass:
        // Rebuild parent Group relationships using cloned IDs.

        for (OptionGroup sourceGroup : sourceGroups) {

            if (sourceGroup.getParentOptionGroup() == null) {
                continue;
            }

            OptionGroup clonedGroup =
                    clonedGroups.get(
                            sourceGroup.getOptionGroupId());

            OptionGroup clonedParent =
                    clonedGroups.get(
                            sourceGroup
                                    .getParentOptionGroup()
                                    .getOptionGroupId());

            if (clonedParent == null) {

                throw new IllegalStateException(
                        "Active Option Group hierarchy is invalid.");
            }

            clonedGroup.setParentOptionGroup(clonedParent);
        }

        optionGroupRepository.saveAll(clonedGroups.values());

        return clonedGroups;
    }


    /**
     * Copies active Option Values and rebuilds their parent Value
     * relationships.
     *
     * @param sourceTerms active source System Terms
     * @param clonedGroups cloned Option Groups mapped by original ID
     * @throws IllegalStateException if an active parent Value was not cloned
     */
    private void cloneOptionValues(List<SystemTerm> sourceTerms, Map<Long, OptionGroup> clonedGroups) {

        Map<Long, OptionValue> clonedValues = new LinkedHashMap<>();

        List<OptionValue> sourceValues = new ArrayList<>();


        // First pass:
        // Create all active Values without parent relationships.

        for (SystemTerm sourceTerm : sourceTerms) {

            List<OptionGroup> sourceGroups =
                    optionGroupRepository
                            .findBySystemTermAndActiveTrueOrderByDisplayOrderAscGroupNameAsc(
                                    sourceTerm);

            for (OptionGroup sourceGroup : sourceGroups) {

                OptionGroup clonedGroup =
                        clonedGroups.get(
                                sourceGroup.getOptionGroupId());

                if (clonedGroup == null) {

                    throw new IllegalStateException(
                            "Active Option Group clone is unavailable.");
                }

                List<OptionValue> groupValues =
                        optionValueRepository
                                .findByOptionGroupAndActiveTrueOrderByDisplayOrderAscValueNameAsc(
                                        sourceGroup);

                for (OptionValue sourceValue : groupValues) {

                    OptionValue clonedValue = new OptionValue(
                            clonedGroup,
                            null,
                            sourceValue.getValueName(),
                            sourceValue.getDescription(),
                            sourceValue.getDisplayOrder());

                    clonedValue = optionValueRepository.save(clonedValue);

                    clonedValues.put(sourceValue.getOptionValueId(), clonedValue);

                    sourceValues.add(sourceValue);
                }
            }
        }


        // Second pass:
        // Rebuild parent Value relationships using cloned IDs.

        for (OptionValue sourceValue : sourceValues) {

            if (sourceValue.getParentOptionValue() == null) {
                continue;
            }

            OptionValue clonedValue =
                    clonedValues.get(
                            sourceValue.getOptionValueId());

            OptionValue clonedParent =
                    clonedValues.get(
                            sourceValue
                                    .getParentOptionValue()
                                    .getOptionValueId());

            if (clonedParent == null) {

                throw new IllegalStateException(
                        "Active Option Value hierarchy is invalid.");
            }

            clonedValue.setParentOptionValue(clonedParent);
        }

        optionValueRepository.saveAll(clonedValues.values());
    }


    /**
     * Copies active Stat Definitions and rebuilds their parent Stat
     * relationships.
     *
     * @param sourceTerms active source System Terms
     * @param clonedTerms cloned System Terms mapped by original ID
     * @throws IllegalStateException if an active parent Stat was not cloned
     */
    private void cloneStatDefinitions(List<SystemTerm> sourceTerms, Map<Long, SystemTerm> clonedTerms) {

        Map<Long, StatDefinition> clonedStats = new LinkedHashMap<>();

        List<StatDefinition> sourceStats = new ArrayList<>();


        // First pass:
        // Create all active Stats without parent relationships.

        for (SystemTerm sourceTerm : sourceTerms) {

            SystemTerm clonedTerm =
                    clonedTerms.get(
                            sourceTerm.getSystemTermId());

            List<StatDefinition> termStats =
                    statDefinitionRepository
                            .findBySystemTermAndActiveTrueOrderByDisplayOrderAscStatNameAsc(
                                    sourceTerm);

            for (StatDefinition sourceStat : termStats) {

                StatDefinition clonedStat =
                        new StatDefinition(
                                clonedTerm,
                                null,
                                sourceStat.getStatName(),
                                sourceStat.getAbbreviation(),
                                sourceStat.getDescription(),
                                sourceStat.getDisplayOrder());

                clonedStat = statDefinitionRepository.save(clonedStat);

                clonedStats.put(sourceStat.getStatDefinitionId(), clonedStat);

                sourceStats.add(sourceStat);
            }
        }


        // Second pass:
        // Rebuild parent Stat relationships using cloned IDs.

        for (StatDefinition sourceStat : sourceStats) {

            if (sourceStat.getParentStat() == null) {
                continue;
            }

            StatDefinition clonedStat =
                    clonedStats.get(
                            sourceStat.getStatDefinitionId());

            StatDefinition clonedParent =
                    clonedStats.get(
                            sourceStat
                                    .getParentStat()
                                    .getStatDefinitionId());

            if (clonedParent == null) {

                throw new IllegalStateException(
                        "Active Stat Definition hierarchy is invalid.");
            }

            clonedStat.setParentStat(clonedParent);
        }

        statDefinitionRepository.saveAll(clonedStats.values());
    }



     // -------------------- Game System Deletion -------------------- //

    /**
     * Deletes a game system owned by the specified authorized user.
     *
     * @param gameSystemId identifier of the game system
     * @param username username of the authenticated user
     * @return name of the deleted game system
     * @throws SecurityException if the user does not have permission to manage the game system
     */
    @Transactional
    public String deleteGameSystem(
            Long gameSystemId,
            String username) {

        User user = getUserByUsername(username);

        requireGameSystemManagementRole(user);

        GameSystem gameSystem =
                getGameSystemById(gameSystemId);

        requireOwner(gameSystem, user);

        String deletedSystemName = gameSystem.getName();

        gameSystemRepository.delete(gameSystem);

        return deletedSystemName;
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
     * Verifies that the specified user has permission to manage
     * Game System records.
     *
     * @param user user whose role is evaluated
     * @throws SecurityException if the user does not have sufficient permissions
     */
    private void requireGameSystemManagementRole(User user) {

        if (!hasGameSystemManagementRole(user)) {

            throw new SecurityException(
                    "Game System management permissions are required.");
        }
    }

    /**
     * Verifies that the specified user has permission to manage the game system.
     *clear
     * @param gameSystem game system being modified
     * @param user authenticated user
     * @throws SecurityException if the user does not have permission to manage the game system
     */
    private void requireOwner(GameSystem gameSystem, User user) {

        if (!gameSystem.getOwner()
                .getUserId()
                .equals(user.getUserId())) {

            throw new SecurityException(
                    "You do not have permission to manage this Game System.");
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
     * Normalizes an optional text field with a maximum length.
     *
     * @param value text to normalize
     * @param maxLength maximum allowed length
     * @return normalized text or null
     * @throws IllegalArgumentException if the text exceeds the maximum length
     */
    private String normalizeOptionalText(String value, int maxLength) {

        if (value == null || value.isBlank()) {
            return null;
        }

        String normalizedValue = value.trim();

        if (normalizedValue.length() > maxLength) {
            throw new IllegalArgumentException(
                    "Text cannot exceed " + maxLength + " characters.");
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

    /**
     * Normalizes an optional game system system overview.
     *
     * @param value system overview to normalize
     * @return normalized system overview or null
     */
    private String normalizeSystemOverview(String value) {

        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }


    // -------------------- Game System Management Role -------------------- //

    /**
     * Determines whether the user has Game System management permissions.
     *
     * @param user user being evaluated
     * @return true when the user may manage Game Systems
     */
    private boolean hasGameSystemManagementRole(User user) {

        String roleName = user.getRole().getRoleName();

        return GAME_DESIGNER_ROLE.equals(roleName)
                || GAME_ADMIN_ROLE.equals(roleName);
    }
    }