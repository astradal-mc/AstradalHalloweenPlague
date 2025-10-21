package net.astradal.astradalHalloweenPlague.plague;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.listeners.HospitalListener;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.concurrent.TimeUnit;

/**
 * Represents a single, active plague curing session for a player.
 * This is a BukkitRunnable that manages the BossBar and cure progression.
 */
public class CuringTask extends BukkitRunnable {

    private final AstradalHalloweenPlague plugin;
    private final HospitalListener listener; // Reference back to the listener to remove from cache
    private final Player player;

    private final int DURATION_SECONDS;
    private final int DURATION_TICKS;

    // BossBar definitions TODO: (can be moved to config later)
    private static final BossBar.Color BAR_COLOR = BossBar.Color.GREEN;
    private static final BossBar.Overlay BAR_STYLE = BossBar.Overlay.PROGRESS;
    private static final Component BAR_TITLE =
        Component.text("CURE IN PROGRESS", NamedTextColor.GREEN, net.kyori.adventure.text.format.TextDecoration.BOLD);

    private final BossBar bossBar;

    private int ticksElapsed = 0;

    public CuringTask(AstradalHalloweenPlague plugin, HospitalListener listener, Player player) {
        this.plugin = plugin;
        this.listener = listener;
        this.player = player;

        // --- READ DURATION FROM CONFIG ---
        this.DURATION_SECONDS = plugin.getPlagueConfig().getCureTimeSeconds();
        this.DURATION_TICKS = DURATION_SECONDS * 20;
        // ---------------------------------

        // Initialize BossBar
        this.bossBar = BossBar.bossBar(BAR_TITLE, 0f, BAR_COLOR, BAR_STYLE);
    }

    public void start() {
        // Run the task every 10 ticks (or every second, 20 ticks is fine)
        runTaskTimer(plugin, 0L, 10L); // Run every tick for smoother checking
        player.showBossBar(bossBar);
    }

    public void cancelSession() {
        // Stop the Bukkit task
        this.cancel();
        player.hideBossBar(bossBar);
    }

    @Override
    public void run() {
        // 1. Check for infection status and hospital zone (safety checks)
        // The check for leaving the region is crucial now that we allow movement.
        if (!plugin.getPlagueManager().isPlayerInfected(player.getUniqueId()) ||
            !plugin.getRegionUtil().isInHospitalRegion(player.getLocation())) { // MUST use player.getLocation() now!

            // If player left the hospital bounds (detected by this internal task or command cured), cancel.
            // We rely on the HospitalListener's onPlayerMove event to send the specific "You left" message.
            listener.cancelCuringSession(player, null);
            return;
        }

        // Increment ticks based on the run interval (5 ticks per interval)
        ticksElapsed += 5;

        // 2. Update BossBar Progress
        float progress = (float) ticksElapsed / DURATION_TICKS; // Uses dynamic DURATION_TICKS
        bossBar.progress(Math.min(progress, 1.0f));

        // Update title
        if (ticksElapsed % 20 == 0 || ticksElapsed == 5) {
            long remainingSeconds = (DURATION_TICKS - ticksElapsed) / 20; // Uses dynamic DURATION_TICKS
            bossBar.name(BAR_TITLE.append(Component.text(" (" + Math.max(0, remainingSeconds) + "s remaining)", NamedTextColor.YELLOW)));
        }

        // 3. Check for Completion
        if (ticksElapsed >= DURATION_TICKS) { // Uses dynamic DURATION_TICKS
            plugin.getPlagueManager().curePlayer(player.getUniqueId());
            listener.finishCuringSession(player);
        }
    }

    public Player getPlayer() { return player; }
}