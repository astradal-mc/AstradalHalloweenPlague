package net.astradal.astradalHalloweenPlague.Plague;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Database.InfectionRepository;
import net.astradal.astradalHalloweenPlague.Util.MessageUtil;
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
    private final InfectionRepository repository;

    // Cache to hold currently infected players in memory for quick access
    // Key: Player UUID, Value: InfectionData
    private final Map<UUID, InfectionData> activeInfections = new HashMap<>();

    public PlagueManager(AstradalHalloweenPlague plugin, InfectionRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
    }

    /**
     * Attempts to load infection data for a player on login or returns empty if not infected.
     *
     * @param playerId The UUID of the player.
     * @return The InfectionData if the player is infected.
     */
    public Optional<InfectionData> loadInfection(UUID playerId) {
        Optional<InfectionData> data = repository.getInfectionData(playerId);
        data.ifPresent(infectionData -> activeInfections.put(playerId, infectionData));
        return data;
    }

    /**
     * Marks a player as infected, starting at STAGE_ONE.
     * Informs the player and saves the data to the repository.
     *
     * @param player The player to infect.
     * @return true if the player was newly infected, false if already infected.
     */
    public boolean infectPlayer(Player player) {
        if (activeInfections.containsKey(player.getUniqueId())) {
            return false; // Already infected
        }

        long now = System.currentTimeMillis();
        PlagueStage initialStage = PlagueStage.STAGE_ONE;

        // Create the data object
        InfectionData data = new InfectionData(player.getUniqueId(), initialStage.getStageLevel(), now);

        // Add to cache
        activeInfections.put(player.getUniqueId(), data);

        // Save to database (asynchronously is better, but synchronously for simplicity here)
        repository.insertInfection(data);

        // Inform the player (Need to implement MessageUtil later)
        MessageUtil.sendInfectionMessage(player);

        plugin.getLogger().info(player.getName() + " has been infected with the plague.");
        return true;
    }

    /**
     * Cures a player, removing their infection data.
     *
     * @param playerId The UUID of the player to cure.
     */
    public void curePlayer(UUID playerId) {
        // Remove from cache
        activeInfections.remove(playerId);

        // Remove from database
        repository.deleteInfection(playerId);

        // Inform player if they are online
        Player player = Bukkit.getPlayer(playerId);
        if (player != null && player.isOnline()) {
            MessageUtil.sendCureMessage(player);
            // Also remove any active potion effects applied by the plague
            removePlagueEffects(player);
        }

        plugin.getLogger().info("Player " + playerId + " has been cured of the plague.");
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
            repository.updateInfectionStage(playerId, nextStage.getStageLevel());

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
}