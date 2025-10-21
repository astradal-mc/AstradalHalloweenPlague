package net.astradal.astradalHalloweenPlague.Database;

import net.astradal.astradalHalloweenPlague.AstradalHalloweenPlague;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

public class DatabaseManager {

    private final Logger logger;
    private final String dbUrl;

    /**
     * Holds the active database connection.
     */
    private final AtomicReference<Connection> connection = new AtomicReference<>();

    /**
     * Construct a DatabaseManager.
     *
     * @param plugin The main plugin instance to get data folder and logger.
     */
    public DatabaseManager(AstradalHalloweenPlague plugin) {
        this.logger = plugin.getLogger();

        // Ensure the plugin data folder exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        // JDBC URL to the SQLite database file in the plugin's data folder
        String dbPath = plugin.getDataFolder().getAbsolutePath() + "/plague.db";
        this.dbUrl = "jdbc:sqlite:" + dbPath;
    }

    /**
     * Establish a connection if none exists.
     */
    public void connect() {
        try {
            // Check if connection is null OR if the connection is closed
            if (connection.get() == null || connection.get().isClosed()) {
                Class.forName("org.sqlite.JDBC"); // Load the SQLite driver
                connection.set(DriverManager.getConnection(dbUrl));
                logger.info("Successfully connected to the SQLite database.");
            }
        } catch (SQLException | ClassNotFoundException e) {
            logger.severe("Database connection failed: " + e.getMessage());
        }
    }

    /**
     * Get the current connection, auto-connect if needed.
     * @return The active database connection.
     */
    public Connection getConnection() {
        try {
            // Attempt to re-establish if closed/null
            if (connection.get() == null || connection.get().isClosed()) {
                connect();
            }
        } catch (SQLException e) {
            logger.severe("Failed to verify or re-establish database connection: " + e.getMessage());
            // Re-throw as unchecked exception to be handled by repository methods
            throw new RuntimeException("Failed to get database connection.", e);
        }
        return connection.get();
    }

    /**
     * Close the connection if open. Should be called on plugin disable.
     */
    public void disconnect() {
        try {
            Connection conn = connection.get();
            if (conn != null && !conn.isClosed()) {
                conn.close();
                logger.info("Database connection closed successfully.");
            }
        } catch (SQLException e) {
            logger.severe("Failed to close database connection: " + e.getMessage());
        } finally {
            connection.set(null); // Clear the reference
        }
    }

    /**
     * Reads a SQL file from the plugin's resources and executes the statements
     * to set up the database schema.
     *
     * @param path The absolute path to the schema file within the JAR's resources (e.g., "/schema.sql").
     */
    public void runSchemaFromResource(String path) {
        try (InputStream stream = getClass().getResourceAsStream(path)) {
            if (stream == null) {
                logger.severe("Missing schema resource: " + path);
                return;
            }
            String sql = new String(stream.readAllBytes(), StandardCharsets.UTF_8);

            // Split by semicolon and execute each statement
            for (String stmt : sql.split(";")) {
                stmt = stmt.trim();
                if (!stmt.isEmpty()) {
                    try (var s = getConnection().createStatement()) {
                        s.execute(stmt);
                    }
                }
            }
            logger.info("Database schema initialized successfully from " + path);
        } catch (IOException | SQLException e) {
            logger.severe("Failed to load and execute database schema: " + e.getMessage());
            // It's generally better to let the server crash/stop if the database cannot initialize
            throw new RuntimeException("Database initialization failed.", e);
        }
    }
}