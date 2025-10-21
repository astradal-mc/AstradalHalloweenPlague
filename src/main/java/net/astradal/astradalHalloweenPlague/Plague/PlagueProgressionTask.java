package net.astradal.astradalHalloweenPlague.Plague;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.UUID;

/**
 * A repeating task that handles all core plague mechanics for online infected players:
 * 1. Stage Progression Check
 * 2. Debuff Application
 * 3. Proximity Infection Check
 */
public class PlagueProgressionTask extends BukkitRunnable { // <-- CHANGE HERE: extends BukkitRunnable

    private final AstradalHalloweenPlague plugin;
    private final PlagueManager plagueManager;

    // Configurable Infection Radius (for future use with PlagueConfig)
    private static final double PROXIMITY_RADIUS = 5.0;

    public PlagueProgressionTask(AstradalHalloweenPlague plugin, PlagueManager plagueManager) {
        this.plugin = plugin;
        this.plagueManager = plagueManager;
    }

    @Override
    public void run() {
        // Iterate over a copy of the key set to avoid ConcurrentModificationException
        // if an infection is removed/added during the loop.
        for (UUID infectedId : plagueManager.getActiveInfections().keySet()) {
            Player infectedPlayer = Bukkit.getPlayer(infectedId);

            // Ensure the infected player is online and valid
            if (infectedPlayer == null || !infectedPlayer.isOnline()) {
                continue;
            }

            // --- 1. Stage Progression and Debuff Application ---
            plagueManager.handleProgressionAndEffects(infectedPlayer);

            // --- 2. Proximity Infection Check ---
            checkProximityInfection(infectedPlayer);
        }
    }

    /**
     * Checks nearby healthy players for infection via proximity.
     * @param infectedPlayer The source of the infection.
     */
    private void checkProximityInfection(Player infectedPlayer) {
        // Only Stage 2 and Final Stage players are contagious via proximity
        InfectionData data = plagueManager.getActiveInfections().get(infectedPlayer.getUniqueId());
        if (data == null) return; // Should not happen

        PlagueStage currentStage = PlagueStage.getByLevel(data.getStage());

        if (currentStage != PlagueStage.STAGE_TWO && currentStage != PlagueStage.STAGE_FINAL) {
            return;
        }

        // Iterate over all online players
        for (Player target : infectedPlayer.getWorld().getPlayers()) {

            // Skip the source player, and only check players near the source
            if (target.equals(infectedPlayer) || target.getLocation().distance(infectedPlayer.getLocation()) > PROXIMITY_RADIUS) {
                continue;
            }

            // Check if the target is healthy
            if (!plagueManager.isPlayerInfected(target.getUniqueId())) {
                // Infect the target!
                plagueManager.infectPlayer(target);
                plugin.getLogger().info(target.getName() + " was infected by proximity to " + infectedPlayer.getName());
                // No need to check other players nearby for this source player once one is infected
                // continue is commented out here to allow one infected player to potentially infect multiple targets in one run
            }
        }
    }
}