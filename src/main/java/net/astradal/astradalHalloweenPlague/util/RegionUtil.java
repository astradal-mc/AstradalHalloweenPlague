package net.astradal.astradalHalloweenPlague.util;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import net.astradal.astradalHalloweenPlague.database.RegionRepository;
import net.astradal.astradalHalloweenPlague.plague.HospitalRegion;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * Utility for managing and checking hospital regions using the database.
 */
public class RegionUtil {

    private final AstradalHalloweenPlague plugin;
    private final RegionRepository repository;
    private final Map<String, HospitalRegion> cachedRegions = new HashMap<>();

    public RegionUtil(AstradalHalloweenPlague plugin, RegionRepository repository) {
        this.plugin = plugin;
        this.repository = repository;
        loadRegionsFromDatabase();
    }

    /**
     * Loads or reloads all hospital regions from the database into the cache.
     */
    public void loadRegionsFromDatabase() {
        cachedRegions.clear();
        repository.getAllRegions().forEach(region -> {
            // Check if world exists before caching
            if (plugin.getServer().getWorld(region.getWorldName()) == null) {
                plugin.getLogger().warning("Region '" + region.getName() + "' references unknown world: " + region.getWorldName() + ". Skipping.");
                return;
            }
            cachedRegions.put(region.getName().toLowerCase(), region);
        });
        plugin.getLogger().info("Loaded " + cachedRegions.size() + " hospital regions from database.");
    }

    /**
     * Checks if a player is currently inside any registered hospital region.
     * DEPRECATED: Use the Location overload instead.
     */
    public boolean isInHospitalRegion(Player player) {
        // Defer to the Location-based check
        return isInHospitalRegion(player.getLocation());
    }

    /**
     * Checks if a player is currently inside any registered hospital region.
     * @param player The player to check.
     * @return true if the player is inside a hospital region.
     */
    /**
     * Checks if a given Location is inside any registered hospital region.
     * @param loc The location to check.
     * @return true if the location is inside a hospital region.
     */
    public boolean isInHospitalRegion(Location loc) {
        World locWorld = loc.getWorld();

        for (HospitalRegion region : cachedRegions.values()) {

            // Check world match first (must use world name for robustness across server reloads)
            if (!region.getWorldName().equals(locWorld.getName())) {
                continue;
            }

            // Check bounding box containment
            int x = loc.getBlockX();
            int y = loc.getBlockY();
            int z = loc.getBlockZ();

            // Note: We're using the corrected Z getters from the HospitalRegion class
            if (x >= region.getMinX() && x <= region.getMaxX() &&
                y >= region.getMinY() && y <= region.getMaxY() &&
                z >= region.getCorrectedMinZ() && z <= region.getCorrectedMaxZ()) {
                return true;
            }
        }
        return false;
    }

    // --- Public methods for adding/removing regions (used by commands) ---

    public void addRegion(HospitalRegion region) {
        repository.saveRegion(region);
        cachedRegions.put(region.getName().toLowerCase(), region);
    }

    public void removeRegion(String name) {
        repository.deleteRegion(name);
        cachedRegions.remove(name.toLowerCase());
    }

    public Collection<HospitalRegion> getRegions() {
        return cachedRegions.values();
    }
}