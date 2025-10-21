package net.astradal.astradalHalloweenPlague.Commands;

import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Utility class to track temporary staff region selections (2 corners).
 * This simulates a simple selection tool for defining a region.
 */
public class StaffSelection {

    // Key: Player UUID, Value: Map of corner name ("pos1", "pos2") to Location
    private final Map<UUID, Map<String, Location>> selections = new HashMap<>();

    /**
     * Sets a selection point (pos1 or pos2) for a player.
     */
    public void setSelection(Player player, String posName, Location location) {
        selections.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>()).put(posName, location);
    }

    /**
     * Gets a selection point (pos1 or pos2) for a player.
     */
    public Location getSelection(Player player, String posName) {
        return selections.getOrDefault(player.getUniqueId(), new HashMap<>()).get(posName);
    }

    /**
     * Checks if a player has both selection points (pos1 and pos2) set.
     */
    public boolean hasCompleteSelection(Player player) {
        Map<String, Location> playerSelection = selections.get(player.getUniqueId());
        return playerSelection != null && playerSelection.containsKey("pos1") && playerSelection.containsKey("pos2");
    }

    /**
     * Retrieves the complete selection (pos1 and pos2) if available.
     * The array contains [pos1, pos2]. Returns null if incomplete.
     */
    public Location[] getCompleteSelection(Player player) {
        Map<String, Location> playerSelection = selections.get(player.getUniqueId());
        if (playerSelection == null || !playerSelection.containsKey("pos1") || !playerSelection.containsKey("pos2")) {
            return null;
        }
        return new Location[]{playerSelection.get("pos1"), playerSelection.get("pos2")};
    }
}