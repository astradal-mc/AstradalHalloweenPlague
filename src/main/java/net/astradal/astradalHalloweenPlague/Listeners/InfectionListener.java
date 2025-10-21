package net.astradal.astradalHalloweenPlague.Listeners;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Plague.PlagueManager;
import net.astradal.astradalHalloweenPlague.Plague.PlagueStage;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * Handles events related to the initial infection vectors and player lifecycle.
 */
public class InfectionListener implements Listener {

    private final AstradalHalloweenPlague plugin;
    private final PlagueManager plagueManager;

    public InfectionListener(AstradalHalloweenPlague plugin, PlagueManager plagueManager) {
        this.plugin = plugin;
        this.plagueManager = plagueManager;
    }

    /**
     * Handles infection via contact (Player A hits Player B).
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        // Ensure the event involves a player attacking another player
        if (!(event.getDamager() instanceof Player) || !(event.getEntity() instanceof Player)) {
            return;
        }

        Player damager = (Player) event.getDamager();
        Player target = (Player) event.getEntity();

        // 1. Check if the damager (attacker) is infected
        if (!plagueManager.isPlayerInfected(damager.getUniqueId())) {
            return;
        }

        // 2. Check the attacker's plague stage (only contagious at Stage 2+)
        int damagerStage = plagueManager.getActiveInfections()
            .get(damager.getUniqueId())
            .getStage();

        PlagueStage contagiousStage = PlagueStage.getByLevel(damagerStage);

        if (contagiousStage != PlagueStage.STAGE_TWO && contagiousStage != PlagueStage.STAGE_FINAL) {
            return;
        }

        // 3. Check if the target is already infected
        if (plagueManager.isPlayerInfected(target.getUniqueId())) {
            return;
        }

        // 4. Infect the target
        plagueManager.infectPlayer(target);
        plugin.getLogger().info(target.getName() + " was infected by contact from " + damager.getName());
    }

    /**
     * Loads infection data from the database into memory upon login.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
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
        // Remove from cache to save memory, but keep in DB
        plagueManager.getActiveInfections().remove(event.getPlayer().getUniqueId());
    }
}