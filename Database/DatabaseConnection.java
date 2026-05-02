package Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection {
    private static final String URL = "jdbc:mysql://localhost:3306/bunker_system";
    private static final String USER = "root";
    private static final String PASSWORD = "aldrei123!";

    public static Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(URL, USER, PASSWORD);
        } catch (SQLException e) {
            if (e.getMessage() != null && e.getMessage().contains("No suitable driver")) {
                throw new SQLException("MySQL JDBC Driver not found on classpath. Place the Connector/J jar in lib/ and run with it on the classpath.", e);
            }
            throw e;
        }
    }

    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
    }

    // ================================
    // SCHEMA TESTING METHODS
    // ================================

    /**
     * Test database schema and display table information
     * (From SchemaTest.class functionality)
     */
    public static void testDatabaseSchema() {
        System.out.println("=== DATABASE SCHEMA TEST ===");

        try (Connection conn = getConnection()) {
            java.sql.DatabaseMetaData metaData = conn.getMetaData();

            // Test keycards table schema
            System.out.println("\n--- KEYCARDS TABLE COLUMNS ---");
            try (java.sql.ResultSet columns = metaData.getColumns(null, null, "keycards", null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    System.out.println(columnName + " - " + columnType + "(" + columnSize + ")");
                }
            }

            // Test occupants table schema
            System.out.println("\n--- OCCUPANTS TABLE COLUMNS ---");
            try (java.sql.ResultSet columns = metaData.getColumns(null, null, "occupants", null)) {
                while (columns.next()) {
                    String columnName = columns.getString("COLUMN_NAME");
                    String columnType = columns.getString("TYPE_NAME");
                    int columnSize = columns.getInt("COLUMN_SIZE");
                    System.out.println(columnName + " - " + columnType + "(" + columnSize + ")");
                }
            }

            // Check for access_level column
            boolean hasAccessLevel = false;
            try (java.sql.ResultSet columns = metaData.getColumns(null, null, "keycards", "access_level")) {
                hasAccessLevel = columns.next();
            }

            System.out.println("\nSchema validation:");
            System.out.println("Access level column exists: " + hasAccessLevel);

            // Test table existence
            String[] tables = {"occupants", "keycards", "rooms", "reservations", "work_duties"};
            System.out.println("Tables existence check:");
            for (String table : tables) {
                try (java.sql.ResultSet rs = metaData.getTables(null, null, table, new String[]{"TABLE"})) {
                    boolean exists = rs.next();
                    System.out.println("   " + table + ": " + (exists ? "EXISTS" : "MISSING"));
                }
            }

        } catch (SQLException e) {
            System.err.println("Schema test failed: " + e.getMessage());
        }

        System.out.println("\n=== SCHEMA TEST COMPLETE ===\n");
    }

    /**
     * Verify database connectivity
     * @return true if connection successful
     */
    public static boolean testConnection() {
        System.out.println("Testing database connection...");

        try (Connection conn = getConnection()) {
            if (conn != null && !conn.isClosed()) {
                System.out.println("Database connection successful");
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Database connection failed: " + e.getMessage());
        }

        return false;
    }
}
