package net.astradal.astradalHalloweenPlague.Plague;

/**
 * Data model representing a staff-designated hospital region.
 */
public class HospitalRegion {

    private final String name;
    private final String worldName;
    private final int x1, y1, z1;
    private final int x2, y2, z2;

    public HospitalRegion(String name, String worldName, int x1, int y1, int z1, int x2, int y2, int z2) {
        this.name = name;
        this.worldName = worldName;
        this.x1 = x1;
        this.y1 = y1;
        this.z1 = z1;
        this.x2 = x2;
        this.y2 = y2;
        this.z2 = z2;
    }

    // --- Getters ---

    public String getName() {
        return name;
    }

    public String getWorldName() {
        return worldName;
    }

    // To simplify bounding box checks, we return the normalized min/max values
    public int getMinX() { return Math.min(x1, x2); }
    public int getMaxX() { return Math.max(x1, x2); }

    public int getMinY() { return Math.min(y1, y2); }
    public int getMaxY() { return Math.max(y1, y2); }

    public int getMinZ() { return Math.min(z1, z2); }
    public int getMaxZ() { return Math.max(z1, z2); }

    // Corrected Z getters
    public int getCorrectedMinZ() { return Math.min(z1, z2); }
    public int getCorrectedMaxZ() { return Math.max(z1, z2); }

    // Returning raw corners for command/storage purposes if needed
    public int getX1() { return x1; }
    public int getY1() { return y1; }
    public int getZ1() { return z1; }
    public int getX2() { return x2; }
    public int getY2() { return y2; }
    public int getZ2() { return z2; }
}