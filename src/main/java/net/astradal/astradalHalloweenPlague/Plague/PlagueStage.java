package net.astradal.astradalHalloweenPlague.Plague;

import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;

/**
 * Defines the stages of the plague, their progression time, and associated debuffs.
 */
public enum PlagueStage {

    // Stage 0: Healthy (Used for reference, not stored in DB)
    HEALTHY(0, 0, List.of()),

    // Stage 1: Incubation / Early Symptoms
    STAGE_ONE(
        1,
        3 * 60, // 3 minutes to progress to Stage 2
        List.of(
            // Minor, annoying debuffs
            new PotionEffect(PotionEffectType.HUNGER, 20 * 6, 0, true, false), // Hunger I, applied for 6 seconds
            new PotionEffect(PotionEffectType.NAUSEA, 20 * 3, 0, true, false) // Nausea I, applied for 3 seconds
        )
    ),

    // Stage 2: Visible Symptoms / Contagious
    STAGE_TWO(
        2,
        5 * 60, // 5 minutes to progress to Final Stage
        List.of(
            // Increased debuffs
            new PotionEffect(PotionEffectType.WEAKNESS, 20 * 6, 0, true, false), // Weakness I
            new PotionEffect(PotionEffectType.SLOWNESS, 20 * 6, 0, true, false),     // Slowness I
            new PotionEffect(PotionEffectType.NAUSEA, 20 * 5, 1, true, false) // Nausea II
        )
    ),

    // Stage 3: Final Stage / Critical
    STAGE_FINAL(
        3,
        0, // No further progression
        List.of(
            // Severe debuffs
            new PotionEffect(PotionEffectType.WEAKNESS, 20 * 6, 1, true, false),  // Weakness II
            new PotionEffect(PotionEffectType.SLOWNESS, 20 * 6, 1, true, false),      // Slowness II
            new PotionEffect(PotionEffectType.BLINDNESS, 20 * 3, 0, true, false) // Blindness I
            // You might add occasional damage or more severe effects here
        )
    );

    private final int stageLevel;
    private final int requiredTimeSeconds; // Time required in this stage before progressing
    private final List<PotionEffect> effects;

    PlagueStage(int stageLevel, int requiredTimeSeconds, List<PotionEffect> effects) {
        this.stageLevel = stageLevel;
        this.requiredTimeSeconds = requiredTimeSeconds;
        this.effects = effects;
    }

    /**
     * Finds the PlagueStage enum by its integer level.
     * @param level The integer stage (1, 2, 3).
     * @return The corresponding PlagueStage, or STAGE_FINAL if level is too high.
     */
    public static PlagueStage getByLevel(int level) {
        for (PlagueStage stage : values()) {
            if (stage.getStageLevel() == level) {
                return stage;
            }
        }
        // If a level outside the defined range is somehow passed, default to final/healthy
        return level > STAGE_FINAL.getStageLevel() ? STAGE_FINAL : HEALTHY;
    }

    /**
     * Gets the next stage in the progression.
     * @return The next PlagueStage, or the current stage if it's the final one.
     */
    public PlagueStage getNextStage() {
        if (this == STAGE_FINAL || this == HEALTHY) {
            return this;
        }
        return getByLevel(this.stageLevel + 1);
    }

    /**
     * Gets the list of PotionEffects to apply for this stage.
     * Note: The duration here is how long the effect *lasts* when applied by the task.
     */
    public List<PotionEffect> getEffects() {
        return effects;
    }

    // --- Getters ---

    public int getStageLevel() {
        return stageLevel;
    }

    public int getRequiredTimeSeconds() {
        return requiredTimeSeconds;
    }
}