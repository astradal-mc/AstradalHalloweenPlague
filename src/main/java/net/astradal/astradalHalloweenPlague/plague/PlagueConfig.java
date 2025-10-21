package net.astradal.astradalHalloweenPlague.plague;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

public class PlagueConfig {

    private final AstradalHalloweenPlague plugin;
    private final Map<String, StageSettings> stageSettingsMap = new HashMap<>();
    private double infectionRadius;
    private int cureTimeSeconds;
    private double zombieInfectionChance;
    private int immunityDurationSeconds;

    public PlagueConfig(AstradalHalloweenPlague plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        plugin.saveDefaultConfig(); // Ensure default configuration is present
        plugin.reloadConfig(); // Always reload to get fresh values

        ConfigurationSection settings = plugin.getConfig().getConfigurationSection("progression_settings");
        if (settings == null) {
            plugin.getLogger().severe("Missing 'progression_settings' section in config.yml. Using defaults.");
            return;
        }

        this.infectionRadius = settings.getDouble("infection_radius", 5.0);

        this.cureTimeSeconds = settings.getInt("cure_time_seconds", 45);

        this.zombieInfectionChance = settings.getDouble("zombie_infection_chance_percent", 10.0) / 100.0; // Store as 0.10 for calculation

        this.immunityDurationSeconds = settings.getInt("immunity_duration_seconds", 300);

        ConfigurationSection stagesSection = settings.getConfigurationSection("stages");
        if (stagesSection == null) {
            plugin.getLogger().severe("Missing 'stages' section in config.yml. Plague effects will not be applied.");
            return;
        }

        for (String key : stagesSection.getKeys(false)) {
            ConfigurationSection stageData = stagesSection.getConfigurationSection(key);
            if (stageData == null) continue;

            int timeToNext = stageData.getInt("time_to_next_stage_seconds", 0);
            List<String> effectStrings = stageData.getStringList("effects");
            List<PotionEffect> effects = parseEffects(key, effectStrings);

            stageSettingsMap.put(key, new StageSettings(timeToNext, effects));
        }
    }

    /**
     * Parses a list of effect strings into PotionEffect objects, using the modern Registry API.
     * Format: "EFFECT_TYPE, AMPLIFIER, DURATION_SECONDS"
     */
    private List<PotionEffect> parseEffects(String stageName, List<String> effectStrings) {
        List<PotionEffect> effects = new ArrayList<>();
        for (String effectString : effectStrings) {
            try {
                String[] parts = effectString.split(",");
                if (parts.length != 3) {
                    throw new IllegalArgumentException("Incorrect format: expected TYPE,AMPLIFIER,DURATION");
                }

                String typeName = parts[0].trim().toLowerCase(); // Potion keys are typically lowercase (e.g., 'slowness')
                int amplifier = Integer.parseInt(parts[1].trim());
                int durationSeconds = Integer.parseInt(parts[2].trim());

                // --- Use Registry and NamespacedKey ---
                NamespacedKey key = NamespacedKey.minecraft(typeName);
                PotionEffectType type = Registry.POTION_EFFECT_TYPE.get(key);

                if (type == null) {
                    plugin.getLogger().log(Level.WARNING, "Unknown PotionEffectType '" + typeName + "' in " + stageName + ". Check spelling against vanilla keys.");
                    continue;
                }

                // Duration is in ticks (20 ticks per second)
                effects.add(new PotionEffect(type, durationSeconds * 20, amplifier, true, false));

            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "Failed to parse effect '" + effectString + "' for stage " + stageName + ": " + e.getMessage());
            }
        }
        return effects;
    }

    // --- Getters ---

    public StageSettings getStageSettings(String stageKey) {
        return stageSettingsMap.getOrDefault(stageKey, new StageSettings(0, List.of()));
    }

    public double getInfectionRadius() {
        return infectionRadius;
    }

    public int getCureTimeSeconds() {
        return cureTimeSeconds;
    }

    public double getZombieInfectionChance() {
        return zombieInfectionChance;
    }

    public int getImmunityDurationSeconds() {
        return immunityDurationSeconds;
    }

    // --- Inner Class to hold settings for one stage ---
    public record StageSettings(int timeToNextStageSeconds, List<PotionEffect> effects) {}
}