package net.astradal.astradalHalloweenPlague.listeners;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.plague.PlagueManager;
import net.astradal.astradalHalloweenPlague.plague.PlagueStage;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.Random;

/**
 * Handles events related to the initial infection vectors and player lifecycle.
 */
public class InfectionListener implements Listener {

    private final AstradalHalloweenPlague plugin;
    private final PlagueManager plagueManager;
    private final Random random = new Random();

    public InfectionListener(AstradalHalloweenPlague plugin, PlagueManager plagueManager) {
        this.plugin = plugin;
        this.plagueManager = plagueManager;
    }

    /**
     * Handles both Player-to-Player Contact infection and Zombie-to-Player infection chance.
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!plugin.isPluginEnabled()) return;
        // --- 1. INITIAL CHECKS ---

        // Ensure the receiver is a player
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }

        // If the player is already infected, no further infection checks are needed
        if (plagueManager.isPlayerInfected(target.getUniqueId())) {
            return;
        }

        // --- 2. PLAYER-TO-PLAYER CONTACT INFECTION ---

        if (event.getDamager() instanceof Player damager) {
            // Check if the damager (attacker) is infected
            if (plagueManager.isPlayerInfected(damager.getUniqueId())) {

                // Check the attacker's plague stage (only contagious at Stage 2+)
                int damagerStage = plugin.getPlagueManager().getActiveInfections()
                    .get(damager.getUniqueId())
                    .getStage();

                PlagueStage contagiousStage = PlagueStage.getByLevel(damagerStage);

                if (contagiousStage == PlagueStage.STAGE_TWO || contagiousStage == PlagueStage.STAGE_FINAL) {
                    // Infect the target
                    plagueManager.infectPlayer(target);
                    plugin.getLogger().info(target.getName() + " was infected by contact from " + damager.getName());
                    return; // EXIT: Infection successful via contact
                }
            }
            // If the damager is a player but is not contagious, we're done with player-to-player
            return;
        }

        // --- 3. ZOMBIE-TO-PLAYER INFECTION CHANCE ---

        // Check if the damager is a Zombie
        if (event.getDamager() instanceof LivingEntity damagerEntity) {
            if (damagerEntity.getType() == EntityType.ZOMBIE) {

                // Calculate chance to infect
                double chance = plugin.getPlagueConfig().getZombieInfectionChance();

                if (random.nextDouble() < chance) {
                    // Infect the target
                    plagueManager.infectPlayer(target);
                    plugin.getLogger().info(target.getName() + " was infected by a Zombie attack (" + (chance * 100) + "% chance).");
                }
            }
        }
    }

    /**
     * Loads infection data from the database into memory upon login.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.isPluginEnabled()) return;
        // Load data on the main thread for simplicity, but consider async if data is large.
        // For a single SQLite entry, sync is usually fine on join.
        plagueManager.loadInfection(event.getPlayer().getUniqueId());

        // Note: The progression task will immediately apply effects if needed.
    }

    /**
     * Removes the player from the in-memory cache upon logout.
     * The data remains in the database until cure/delete.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isPluginEnabled()) return;
        // Remove from cache to save memory, but keep in DB
        plagueManager.getActiveInfections().remove(event.getPlayer().getUniqueId());
    }
}