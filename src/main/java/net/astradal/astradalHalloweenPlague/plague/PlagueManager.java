package net.astradal.astradalHalloweenPlague.plague;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.database.ImmunityRepository;
import net.astradal.astradalHalloweenPlague.database.InfectionRepository;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;

public class PlagueManager {

    private final AstradalHalloweenPlague plugin;
    private final InfectionRepository infectionRepository;
    private final ImmunityRepository immunityRepository;

    // Cache to hold currently infected players in memory for quick access
    // Key: Player UUID, Value: InfectionData
    private final Map<UUID, InfectionData> activeInfections = new HashMap<>();

    // Cache for active immunity (UUID -> Expiration Time (ms))
    private final Map<UUID, Long> activeImmunity = new HashMap<>();

    public PlagueManager(AstradalHalloweenPlague plugin, InfectionRepository infectionRepository, ImmunityRepository immunityRepository) {
        this.plugin = plugin;
        this.infectionRepository = infectionRepository;
        this.immunityRepository = immunityRepository;

        // TODO: Add logic in onEnable/onPluginStartup to load existing immunity data from DB into cache.
    }

    /**
     * Checks if a player is currently immune to infection.
     */
    public boolean isPlayerImmune(UUID playerId) {
        // 1. Check in-memory cache
        Long expires = activeImmunity.get(playerId);

        if (expires != null) {
            if (System.currentTimeMillis() < expires) {
                return true; // Still immune
            } else {
                // Immunity expired, remove from cache and DB
                removeImmunity(playerId);
                return false;
            }
        }

        // 2. Check DB (if not in cache, though ideally it should be loaded on login)
        // For simplicity, we rely on the cache being updated or loaded on login/cure.
        return false;
    }

    /**
     * Removes immunity from a player (internal cleanup).
     */
    private void removeImmunity(UUID playerId) {
        activeImmunity.remove(playerId);
        immunityRepository.removeImmunity(playerId);

        // Inform player if they are online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            MessageUtil.sendMessage(player, Component.text("Your plague immunity has worn off.", NamedTextColor.YELLOW));
        }
    }

    /**
     * Attempts to load infection data OR immunity data for a player on login.
     */
    public void loadInfection(UUID playerId) {
        // Load Immunity Data
        immunityRepository.getImmunityExpiration(playerId).ifPresent(expiration -> {
            if (System.currentTimeMillis() < expiration) {
                activeImmunity.put(playerId, expiration);
            } else {
                // Remove expired immunity from DB
                immunityRepository.removeImmunity(playerId);
            }
        });

        // Load Infection Data (rest remains the same)
        Optional<InfectionData> data = infectionRepository.getInfectionData(playerId);
        data.ifPresent(infectionData -> activeInfections.put(playerId, infectionData));
    }


    /**
     * Marks a player as infected, starting at STAGE_ONE.
     * @return true if the player was newly infected, false if already infected or immune.
     */
    public boolean infectPlayer(Player player) {
        UUID playerId = player.getUniqueId();

        if (activeInfections.containsKey(playerId)) {
            return false; // Already infected
        }

        // --- IMMUNITY CHECK ---
        if (isPlayerImmune(playerId)) {
            // Optional: Send feedback to the attacker/source if this was a contagion event
            // MessageUtil.sendMessage(player, Component.text("You are currently immune to the plague.", NamedTextColor.LIGHT_PURPLE));
            return false;
        }
        // ----------------------

        // ... (rest of infection logic remains the same) ...
        long now = System.currentTimeMillis();
        PlagueStage initialStage = PlagueStage.STAGE_ONE;
        InfectionData data = new InfectionData(playerId, initialStage.getStageLevel(), now);

        activeInfections.put(playerId, data);
        infectionRepository.insertInfection(data);
        MessageUtil.sendInfectionMessage(player);

        plugin.getLogger().info(player.getName() + " has been infected with the plague.");
        return true;
    }

    /**
     * Cures a player and grants them temporary immunity.
     */
    public void curePlayer(UUID playerId) {
        // 1. Grant Immunity
        int durationSeconds = plugin.getPlagueConfig().getImmunityDurationSeconds();
        long expiresTime = System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(durationSeconds);

        activeImmunity.put(playerId, expiresTime);
        immunityRepository.grantImmunity(playerId, expiresTime);

        // Inform player of immunity
        Player player = Bukkit.getPlayer(playerId);
        if (player != null) {
            MessageUtil.sendMessage(player, Component.text("You are now immune for " + durationSeconds + " seconds!", NamedTextColor.LIGHT_PURPLE));
        }

        // 2. Remove Infection (rest remains the same)
        activeInfections.remove(playerId);
        infectionRepository.deleteInfection(playerId);

        if (player != null && player.isOnline()) {
            MessageUtil.sendCureMessage(player);
            // Also remove any active potion effects applied by the plague
            removePlagueEffects(player);
        }

        plugin.getLogger().info("Player " + playerId + " has been cured and granted immunity.");
    }

    /**
     * The core progression and debuff application method, called by the repeating task.
     *
     * @param player The online, infected player.
     */
    public void handleProgressionAndEffects(Player player) {
        UUID playerId = player.getUniqueId();
        InfectionData data = activeInfections.get(playerId);
        if (data == null) {
            // Should not happen if called from the task which iterates over activeInfections keys
            return;
        }

        PlagueStage currentStage = PlagueStage.getByLevel(data.getStage());
        long elapsedMillis = System.currentTimeMillis() - data.getInfectedTime();
        long requiredMillis = TimeUnit.SECONDS.toMillis(currentStage.getRequiredTimeSeconds());

        // 1. Check for Stage Progression
        if (currentStage != PlagueStage.STAGE_FINAL && elapsedMillis >= requiredMillis) {
            PlagueStage nextStage = currentStage.getNextStage();

            // Update model
            data.setStage(nextStage.getStageLevel());

            // Update database (asynchronously is better)
            infectionRepository.updateInfectionStage(playerId, nextStage.getStageLevel());

            // Inform player if it's the final stage notification
            if (nextStage == PlagueStage.STAGE_FINAL) {
                MessageUtil.sendFinalStageMessage(player);
            }

            plugin.getLogger().log(Level.INFO, player.getName() + " advanced to Plague Stage " + nextStage.getStageLevel());
            currentStage = nextStage; // Use the new stage for applying effects
        }

        // 2. Apply Potion Effects/Debuffs
        applyPlagueEffects(player, currentStage);
    }

    /**
     * Applies the potion effects associated with the current plague stage.
     */
    private void applyPlagueEffects(Player player, PlagueStage stage) {
        for (PotionEffect effect : stage.getEffects()) {
            // Check if player already has a stronger or equal effect (to prevent conflict)
            // For this project, we simply apply the effect, letting the Paper API handle conflicts

            // The duration in the enum is set to be short (e.g., 6 seconds) so the repeating task
            // keeps it active, and if the task stops, the debuffs naturally wear off.
            player.addPotionEffect(effect);
        }
    }

    /**
     * Removes all plague-related potion effects from a player.
     */
    private void removePlagueEffects(Player player) {
        // Iterate through the effects defined in all stages
        for (PlagueStage stage : PlagueStage.values()) {
            for (PotionEffect effect : stage.getEffects()) {
                player.removePotionEffect(effect.getType());
            }
        }
    }

    /**
     * Checks if a player is currently infected.
     *
     * @param playerId The UUID of the player.
     * @return true if infected, false otherwise.
     */
    public boolean isPlayerInfected(UUID playerId) {
        return activeInfections.containsKey(playerId);
    }

    /**
     * Gets the current map of actively infected players. Used by the main task runner.
     */
    public Map<UUID, InfectionData> getActiveInfections() {
        return activeInfections;
    }

    /**
     * Retrieves the infection data for a player from the cache.
     * @param playerId The UUID of the player.
     * @return An Optional containing InfectionData if the player is infected.
     */
    public Optional<InfectionData> getInfectionData(UUID playerId) {
        // Note: This relies on the data being loaded into the cache on join.
        InfectionData data = activeInfections.get(playerId);
        return Optional.ofNullable(data);
    }

    // ... (getActiveImmunity method added for access by CheckCommand) ...
    public Map<UUID, Long> getActiveImmunity() {
        return activeImmunity;
    }
}