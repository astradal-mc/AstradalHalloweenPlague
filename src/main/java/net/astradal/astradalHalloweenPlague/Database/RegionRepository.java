package net.astradal.astradalHalloweenPlague.Database;

import net.astradal.astradalHalloweenPlague.Plague.HospitalRegion;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class RegionRepository {

    private final DatabaseManager dbManager;

    private static final String SELECT_ALL_REGIONS = "SELECT * FROM regions;";
    private static final String SELECT_REGION_BY_NAME = "SELECT * FROM regions WHERE name = ?;";
    private static final String INSERT_REGION =
        "INSERT OR REPLACE INTO regions (name, world, x1, y1, z1, x2, y2, z2) VALUES (?, ?, ?, ?, ?, ?, ?, ?);";
    private static final String DELETE_REGION = "DELETE FROM regions WHERE name = ?;";

    public RegionRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    private HospitalRegion mapResultSetToRegion(ResultSet rs) throws SQLException {
        return new HospitalRegion(
            rs.getString("name"),
            rs.getString("world"),
            rs.getInt("x1"),
            rs.getInt("y1"),
            rs.getInt("z1"),
            rs.getInt("x2"),
            rs.getInt("y2"),
            rs.getInt("z2")
        );
    }

    public List<HospitalRegion> getAllRegions() {
        List<HospitalRegion> regions = new ArrayList<>();
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_ALL_REGIONS);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                regions.add(mapResultSetToRegion(rs));
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching all regions: " + e.getMessage());
        }
        return regions;
    }

    public Optional<HospitalRegion> getRegion(String name) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_REGION_BY_NAME)) {

            ps.setString(1, name);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(mapResultSetToRegion(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching region " + name + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    public void saveRegion(HospitalRegion region) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_REGION)) {

            ps.setString(1, region.getName().toLowerCase()); // Store name lowercase for consistency
            ps.setString(2, region.getWorldName());
            ps.setInt(3, region.getX1());
            ps.setInt(4, region.getY1());
            ps.setInt(5, region.getZ1());
            ps.setInt(6, region.getX2());
            ps.setInt(7, region.getY2());
            ps.setInt(8, region.getZ2());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error saving region " + region.getName() + ": " + e.getMessage());
        }
    }

    public void deleteRegion(String name) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_REGION)) {

            ps.setString(1, name.toLowerCase());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error deleting region " + name + ": " + e.getMessage());
        }
    }
}