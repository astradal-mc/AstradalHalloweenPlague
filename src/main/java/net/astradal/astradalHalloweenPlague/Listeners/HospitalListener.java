package net.astradal.astradalHalloweenPlague.Listeners;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.Plague.PlagueManager;
import net.astradal.astradalHalloweenPlague.Util.MessageUtil;
import net.astradal.astradalHalloweenPlague.Util.RegionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * Handles healing logic when players enter or remain in a hospital zone.
 */
public class HospitalListener implements Listener {

    private final AstradalHalloweenPlague plugin;
    private final PlagueManager plagueManager;
    private final RegionUtil regionUtil;

    // Tracks players currently healing and the time they started (milliseconds)
    private final Map<UUID, Long> healingPlayers = new HashMap<>();

    // Healing time constant (30 seconds to 1 minute)
    private static final long REQUIRED_HEAL_TIME_MILLIS = TimeUnit.SECONDS.toMillis(45); // Set to 45 seconds

    public HospitalListener(AstradalHalloweenPlague plugin, PlagueManager plagueManager, RegionUtil regionUtil) {
        this.plugin = plugin;
        this.plagueManager = plagueManager;
        this.regionUtil = regionUtil;
        startHealingTask();
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        boolean isInHospital = regionUtil.isInHospitalRegion(player);
        boolean isHealing = healingPlayers.containsKey(playerId);
        boolean isInfected = plagueManager.isPlayerInfected(playerId);

        if (!isInfected) return; // Only track infected players

        // Transition: Entered Hospital
        if (isInHospital && !isHealing) {
            healingPlayers.put(playerId, System.currentTimeMillis());
            MessageUtil.sendMessage(player, Component.text("You found a treatment center! Hold still for " + (REQUIRED_HEAL_TIME_MILLIS / 1000) + " seconds to be cured.", NamedTextColor.AQUA));

            // Transition: Left Hospital
        } else if (!isInHospital && isHealing) {
            healingPlayers.remove(playerId);
            MessageUtil.sendMessage(player, Component.text("You left the treatment area. Healing stopped.", NamedTextColor.YELLOW));
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Stop tracking if they leave
        healingPlayers.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Starts the repeating task that checks and applies the cure.
     */
    private void startHealingTask() {
        new BukkitRunnable() {
            @Override
            public void run() {
                // Iterate over a copy to safely remove/modify entries
                for (Map.Entry<UUID, Long> entry : new HashMap<>(healingPlayers).entrySet()) {
                    UUID playerId = entry.getKey();
                    long startTime = entry.getValue();
                    Player player = Bukkit.getPlayer(playerId);

                    // Check if player is still online, infected, and in the zone
                    if (player == null || !plagueManager.isPlayerInfected(playerId) || !regionUtil.isInHospitalRegion(player)) {
                        healingPlayers.remove(playerId); // If any check fails, stop healing
                        continue;
                    }

                    long elapsed = System.currentTimeMillis() - startTime;

                    if (elapsed >= REQUIRED_HEAL_TIME_MILLIS) {
                        // CURE THE PLAYER!
                        plagueManager.curePlayer(playerId);
                        healingPlayers.remove(playerId);
                    } else {
                        // Give progress feedback every 5 seconds (100 ticks)
                        if (elapsed % 5000 < 100) {
                            long remainingSeconds = (REQUIRED_HEAL_TIME_MILLIS - elapsed) / 1000;
                            MessageUtil.sendMessage(player, Component.text("Healing in progress... " + remainingSeconds + " seconds remaining.", NamedTextColor.AQUA));
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L); // Run every 1 second (20 ticks)
    }
}