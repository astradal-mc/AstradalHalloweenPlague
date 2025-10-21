package net.astradal.astradalHalloweenPlague.plague;

import java.util.UUID;

/**
 * Data model representing a player's current plague infection status.
 */
public class InfectionData {

    private final UUID playerId;
    private int stage;
    private final long infectedTime; // Milliseconds since epoch

    public InfectionData(UUID playerId, int stage, long infectedTime) {
        this.playerId = playerId;
        this.stage = stage;
        this.infectedTime = infectedTime;
    }

    // --- Getters ---

    public UUID getPlayerId() {
        return playerId;
    }

    public int getStage() {
        return stage;
    }

    public long getInfectedTime() {
        return infectedTime;
    }

    // --- Setter ---
    // Only stage is mutable after creation, as it changes with progression.

    public void setStage(int stage) {
        this.stage = stage;
    }

    @Override
    public String toString() {
        return "InfectionData{" +
            "playerId=" + playerId +
            ", stage=" + stage +
            ", infectedTime=" + infectedTime +
            '}';
    }
}