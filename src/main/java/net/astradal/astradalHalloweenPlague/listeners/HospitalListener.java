package net.astradal.astradalHalloweenPlague.listeners;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.plague.CuringTask; // <-- NEW IMPORT
import net.astradal.astradalHalloweenPlague.plague.PlagueManager;
import net.astradal.astradalHalloweenPlague.util.MessageUtil;
import net.astradal.astradalHalloweenPlague.util.RegionUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class HospitalListener implements Listener {

    private final AstradalHalloweenPlague plugin;
    private final PlagueManager plagueManager;
    private final RegionUtil regionUtil;

    // Tracks players actively running a CuringTask
    private final Map<UUID, CuringTask> activeCuringSessions = new HashMap<>();

    public HospitalListener(AstradalHalloweenPlague plugin, PlagueManager plagueManager, RegionUtil regionUtil) {
        this.plugin = plugin;
        this.plagueManager = plagueManager;
        this.regionUtil = regionUtil;
        // NOTE: No repeating task needed here anymore!
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!plugin.isPluginEnabled()) return;
        // Only check if player changes the block they are standing on OR leaves the region
        if (event.getFrom().getBlock().equals(event.getTo().getBlock())) return;

        Player player = event.getPlayer();
        UUID playerId = player.getUniqueId();
        boolean isInHospitalFrom = regionUtil.isInHospitalRegion(event.getFrom()); // Check start location
        boolean isInHospitalTo = regionUtil.isInHospitalRegion(event.getTo());     // Check end location
        boolean isCuring = activeCuringSessions.containsKey(playerId);
        boolean isInfected = plagueManager.isPlayerInfected(playerId);

        if (!isInfected) {
            if (isCuring) finishCuringSession(player);
            return;
        }

        // Case 1: Player enters the Hospital Zone (and is not already curing)
        // Player moves from outside OR not in region bounds to inside the bounds.
        if (isInHospitalTo && !isCuring) {
            CuringTask task = new CuringTask(plugin, this, player);
            activeCuringSessions.put(playerId, task);
            task.start(); // Start the task and show BossBar

            // Send the message only ONCE when the session starts
            MessageUtil.sendMessage(player, Component.text("You found a treatment center! You may move freely within the bounds, but do not leave! " +
                "(" + plugin.getPlagueConfig().getCureTimeSeconds() + ")",
                NamedTextColor.AQUA));

            // Case 2: Player leaves the Hospital Zone while curing
            // Player moves from inside the bounds to outside the bounds.
        } else if (!isInHospitalTo && isCuring) {
            // Note: We use !isInHospitalTo because if they move within the hospital, the task continues.
            // Only leaving cancels the task.
            cancelCuringSession(player, "You left the treatment area. Healing stopped.");
        }

        // Case 3: Player moves within the Hospital (isInHospitalTo && isCuring)
        // In this case, we do nothing and let the CuringTask continue running.
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.isPluginEnabled()) return;
        // Cancel and clean up the session if they log out
        CuringTask task = activeCuringSessions.get(event.getPlayer().getUniqueId());
        if (task != null) {
            task.cancelSession();
            activeCuringSessions.remove(event.getPlayer().getUniqueId());
        }
    }

    /**
     * Called by the CuringTask when the player moves or leaves the region unexpectedly.
     */
    public void cancelCuringSession(Player player, String message) {
        CuringTask task = activeCuringSessions.remove(player.getUniqueId());
        if (task != null) {
            task.cancelSession(); // Hides BossBar and stops scheduler
            if (message != null) {
                MessageUtil.sendMessage(player, Component.text(message, NamedTextColor.YELLOW));
            }
        }
    }

    /**
     * Called by the CuringTask upon successful completion (player is cured).
     */
    public void finishCuringSession(Player player) {
        CuringTask task = activeCuringSessions.remove(player.getUniqueId());
        if (task != null) {
            // Note: PlagueManager.curePlayer() already sent the cure message.
            task.cancelSession(); // Hides BossBar and stops scheduler
        }
    }
}