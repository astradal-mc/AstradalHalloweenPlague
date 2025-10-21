package net.astradal.astradalHalloweenPlague.Database;

import net.astradal.astradalHalloweenPlague.Plague.InfectionData;

import java.sql.*;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;

public class InfectionRepository {

    private final DatabaseManager dbManager;

    // Prepared statement strings
    private static final String SELECT_BY_UUID = "SELECT stage, infected_time FROM infections WHERE player_uuid = ?;";
    private static final String INSERT_INFECTION = "INSERT INTO infections (player_uuid, stage, infected_time) VALUES (?, ?, ?);";
    private static final String UPDATE_STAGE = "UPDATE infections SET stage = ? WHERE player_uuid = ?;";
    private static final String DELETE_INFECTION = "DELETE FROM infections WHERE player_uuid = ?;";

    public InfectionRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Retrieves the infection data for a player.
     * @param uuid The UUID of the player.
     * @return An Optional containing InfectionData if the player is infected, otherwise empty.
     */
    public Optional<InfectionData> getInfectionData(UUID uuid) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_BY_UUID)) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int stage = rs.getInt("stage");
                    long infectedTime = rs.getLong("infected_time");
                    // Assuming InfectionData has a constructor:
                    return Optional.of(new InfectionData(uuid, stage, infectedTime));
                }
            }
        } catch (SQLException e) {
            // Log the error using the plugin's logger (assuming access or passing it)
            // For now, simple print/log:
            System.err.println("Database error fetching infection data for " + uuid + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Inserts a new infection record.
     * @param data The InfectionData object to save.
     */
    public void insertInfection(InfectionData data) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_INFECTION)) {

            ps.setString(1, data.getPlayerId().toString());
            ps.setInt(2, data.getStage());
            ps.setLong(3, data.getInfectedTime());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error inserting infection data for " + data.getPlayerId() + ": " + e.getMessage());
        }
    }

    /**
     * Updates the infection stage of an existing record.
     * @param uuid The UUID of the player.
     * @param newStage The new stage level.
     */
    public void updateInfectionStage(UUID uuid, int newStage) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(UPDATE_STAGE)) {

            ps.setInt(1, newStage);
            ps.setString(2, uuid.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error updating infection stage for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Removes the infection record for a player (curing them).
     * @param uuid The UUID of the player to cure.
     */
    public void deleteInfection(UUID uuid) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_INFECTION)) {

            ps.setString(1, uuid.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error deleting infection data for " + uuid + ": " + e.getMessage());
        }
    }
}