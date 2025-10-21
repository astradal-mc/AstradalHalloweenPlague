package net.astradal.astradalHalloweenPlague.database;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;
import java.util.UUID;

public class ImmunityRepository {

    private final DatabaseManager dbManager;

    private static final String SELECT_EXPIRES_TIME = "SELECT expires_time FROM immunity WHERE player_uuid = ?;";
    private static final String INSERT_OR_REPLACE_IMMUNITY = "INSERT OR REPLACE INTO immunity (player_uuid, expires_time) VALUES (?, ?);";
    private static final String DELETE_IMMUNITY = "DELETE FROM immunity WHERE player_uuid = ?;";

    public ImmunityRepository(DatabaseManager dbManager) {
        this.dbManager = dbManager;
    }

    /**
     * Gets the expiration time for a player's immunity.
     * @param uuid The player's UUID.
     * @return Optional containing the expiration timestamp (ms), or empty.
     */
    public Optional<Long> getImmunityExpiration(UUID uuid) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT_EXPIRES_TIME)) {

            ps.setString(1, uuid.toString());

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return Optional.of(rs.getLong("expires_time"));
                }
            }
        } catch (SQLException e) {
            System.err.println("Database error fetching immunity for " + uuid + ": " + e.getMessage());
        }
        return Optional.empty();
    }

    /**
     * Grants immunity to a player until a specific time.
     */
    public void grantImmunity(UUID uuid, long expiresTimeMillis) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(INSERT_OR_REPLACE_IMMUNITY)) {

            ps.setString(1, uuid.toString());
            ps.setLong(2, expiresTimeMillis);

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error granting immunity for " + uuid + ": " + e.getMessage());
        }
    }

    /**
     * Removes the immunity record (e.g., when it expires).
     */
    public void removeImmunity(UUID uuid) {
        try (Connection conn = dbManager.getConnection();
             PreparedStatement ps = conn.prepareStatement(DELETE_IMMUNITY)) {

            ps.setString(1, uuid.toString());

            ps.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Database error removing immunity for " + uuid + ": " + e.getMessage());
        }
    }
}