package Repositories;

import Models.Keycard;
import Database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * KeycardRepo - Repository for Keycard database operations
 */
public class KeycardRepo {

    /**
     * Get all keycards for a specific occupant
     * @param occupantId ID of the occupant
     * @return List of Keycard objects for the occupant
     */
    public static List<Keycard> getKeycardsByOccupantId(int occupantId) {
        List<Keycard> keycards = new ArrayList<>();
        String sql = "SELECT keycard_id, occupant_id, keycard_code, is_active, issued_at, access_level FROM keycards WHERE occupant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, occupantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keycards.add(mapKeycard(rs, 1));
                }
            }
        } catch (SQLException e) {
            if (isMissingAccessLevelColumn(e)) {
                keycards = getKeycardsByOccupantIdWithoutAccessLevel(occupantId);
            } else {
                System.err.println("Error getting keycards by occupant ID: " + e.getMessage());
            }
        }

        return keycards;
    }

    private static List<Keycard> getKeycardsByOccupantIdWithoutAccessLevel(int occupantId) {
        List<Keycard> keycards = new ArrayList<>();
        String sql = "SELECT keycard_id, occupant_id, keycard_code, is_active, issued_at FROM keycards WHERE occupant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, occupantId);

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    keycards.add(mapKeycard(rs, 0));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting keycards by occupant ID without access_level: " + e.getMessage());
        }

        return keycards;
    }

    /**
     * Get keycard by ID
     * @param keycardId ID of the keycard
     * @return Keycard object if found, null otherwise
     */
    public static Keycard getKeycardById(int keycardId) {
        String sql = "SELECT keycard_id, occupant_id, keycard_code, is_active, issued_at, access_level FROM keycards WHERE keycard_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, keycardId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKeycard(rs, 1);
                }
            }
        } catch (SQLException e) {
            if (isMissingAccessLevelColumn(e)) {
                return getKeycardByIdWithoutAccessLevel(keycardId);
            }
            System.err.println("Error getting keycard by ID: " + e.getMessage());
        }

        return null;
    }

    private static Keycard getKeycardByIdWithoutAccessLevel(int keycardId) {
        String sql = "SELECT keycard_id, occupant_id, keycard_code, is_active, issued_at FROM keycards WHERE keycard_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, keycardId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapKeycard(rs, 0);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting keycard by ID without access_level: " + e.getMessage());
        }

        return null;
    }

    /**
     * Create a new keycard with default Resident access level
     * @param occupantId ID of the occupant
     * @param keycardCode Code for the keycard
     * @return true if created successfully, false otherwise
     */
    public static boolean createKeycard(int occupantId, String keycardCode) {
        return createKeycard(occupantId, keycardCode, 1);
    }

    /**
     * Create a new keycard with a specific access level
     * @param occupantId ID of the occupant
     * @param keycardCode Code for the keycard
     * @param accessLevel Access level (1=Resident, 2=Employee, 3=Admin)
     * @return true if created successfully, false otherwise
     */
    public static boolean createKeycard(int occupantId, String keycardCode, int accessLevel) {
        String sql = "INSERT INTO keycards (occupant_id, keycard_code, access_level) VALUES (?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, occupantId);
            stmt.setString(2, keycardCode);
            stmt.setInt(3, accessLevel);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (isMissingAccessLevelColumn(e)) {
                return createKeycardWithoutAccessLevel(occupantId, keycardCode);
            }
            System.err.println("Error creating keycard: " + e.getMessage());
        }

        return false;
    }

    private static boolean createKeycardWithoutAccessLevel(int occupantId, String keycardCode) {
        String sql = "INSERT INTO keycards (occupant_id, keycard_code) VALUES (?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, occupantId);
            stmt.setString(2, keycardCode);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error creating keycard without access_level: " + e.getMessage());
        }

        return false;
    }

    /**
     * Update keycard active status
     * @param keycardId ID of the keycard
     * @param isActive New active status
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateKeycardStatus(int keycardId, boolean isActive) {
        String sql = "UPDATE keycards SET is_active = ? WHERE keycard_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setBoolean(1, isActive);
            stmt.setInt(2, keycardId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating keycard status: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get all keycards
     * @return List of all keycards
     */
    public static List<Keycard> getAllKeycards() {
        List<Keycard> keycards = new ArrayList<>();
        String sql = "SELECT keycard_id, occupant_id, keycard_code, is_active, issued_at, access_level FROM keycards";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                keycards.add(mapKeycard(rs, 1));
            }
        } catch (SQLException e) {
            if (isMissingAccessLevelColumn(e)) {
                keycards = getAllKeycardsWithoutAccessLevel();
            } else {
                System.err.println("Error getting all keycards: " + e.getMessage());
            }
        }

        return keycards;
    }

    private static List<Keycard> getAllKeycardsWithoutAccessLevel() {
        List<Keycard> keycards = new ArrayList<>();
        String sql = "SELECT keycard_id, occupant_id, keycard_code, is_active, issued_at FROM keycards";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                keycards.add(mapKeycard(rs, 0));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all keycards without access_level: " + e.getMessage());
        }

        return keycards;
    }

    /**
     * Delete a keycard by ID
     * @param keycardId ID of the keycard to delete
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteKeycard(int keycardId) {
        String sql = "DELETE FROM keycards WHERE keycard_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, keycardId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting keycard: " + e.getMessage());
        }

        return false;
    }

    /**
     * Delete all keycards assigned to an occupant
     * @param occupantId occupant ID
     * @return number of keycards deleted
     */
    public static int deleteKeycardsByOccupantId(int occupantId) {
        String sql = "DELETE FROM keycards WHERE occupant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, occupantId);
            return stmt.executeUpdate();
        } catch (SQLException e) {
            System.err.println("Error deleting keycards for occupant: " + e.getMessage());
        }

        return 0;
    }

    private static boolean isMissingAccessLevelColumn(SQLException e) {
        String message = e.getMessage();
        return message != null && message.contains("access_level") && message.contains("Unknown column");
    }

    private static Keycard mapKeycard(ResultSet rs, int accessLevelColumnIndex) throws SQLException {
        int accessLevel = 1;
        if (accessLevelColumnIndex == 1) {
            try {
                accessLevel = rs.getInt("access_level");
            } catch (SQLException ignore) {
                accessLevel = 1;
            }
        }
        return new Keycard(
            rs.getInt("keycard_id"),
            rs.getInt("occupant_id"),
            rs.getString("keycard_code"),
            rs.getBoolean("is_active"),
            rs.getString("issued_at"),
            accessLevel
        );
    }

    // ================================
    // TESTING AND UTILITY METHODS
    // ================================

    /**
     * Test database connectivity and display all keycards
     * (From DatabaseTest.class functionality)
     */
    public static void testDatabaseConnection() {
        System.out.println("=== DATABASE TEST - KEYCARDS ===");

        try {
            List<Keycard> keycards = getAllKeycards();
            System.out.println("Found " + keycards.size() + " keycards in database:");

            for (Keycard keycard : keycards) {
                System.out.println("ID: " + keycard.getKeycardId() +
                                 ", Occupant: " + keycard.getOccupantId() +
                                 ", Code: " + keycard.getKeycardCode() +
                                 ", Level: " + keycard.getAccessLevelName() +
                                 ", Active: " + keycard.isActive());
            }
        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
        }

        System.out.println("=== TEST COMPLETE ===\n");
    }

    /**
     * Assign keycards to occupants who don't have them
     * (From AssignKeycards.class functionality)
     * @return number of keycards assigned
     */
    public static int assignKeycardsToOccupantsWithoutCards() {
        System.out.println("=== ASSIGNING KEYCARDS TO OCCUPANTS WITHOUT CARDS ===");

        List<Models.Occupant> occupants = Repositories.OccupantRepo.getAllOccupants();
        System.out.println("Found " + occupants.size() + " total occupants");

        int assignedCount = 0;

        for (Models.Occupant occupant : occupants) {
            List<Keycard> existingKeycards = getKeycardsByOccupantId(occupant.getId());

            if (existingKeycards.isEmpty()) {
                String keycardCode = "KC-" + String.format("%06d", occupant.getId());
                boolean success = createKeycard(occupant.getId(), keycardCode, 1); // Default to Resident level

                if (success) {
                    System.out.println("Assigned keycard to " + occupant.getFullName() +
                                     " (ID: " + occupant.getId() + "): " + keycardCode);
                    assignedCount++;
                } else {
                    System.out.println("Failed to assign keycard to " + occupant.getFullName());
                }
            } else {
                System.out.println("Already has " + existingKeycards.size() + " keycard(s): " + occupant.getFullName());
            }
        }

        System.out.println("\n=== ASSIGNMENT COMPLETE ===");
        System.out.println("Assigned keycards to " + assignedCount + " occupants");
        return assignedCount;
    }

    /**
     * Assign random keycard levels to occupants
     * (From AssignRandomKeycards.class functionality)
     * @return number of keycards updated
     */
    public static int assignRandomKeycardLevels() {
        System.out.println("=== ASSIGNING RANDOM KEYCARD LEVELS ===");

        List<Keycard> keycards = getAllKeycards();
        java.util.Random random = new java.util.Random();
        int updatedCount = 0;

        for (Keycard keycard : keycards) {
            int newLevel = random.nextInt(3) + 1; // Random level 1-3
            boolean success = updateKeycardAccessLevel(keycard.getKeycardId(), newLevel);

            if (success) {
                String oldLevel = keycard.getAccessLevelName();
                String newLevelName = getAccessLevelName(newLevel);
                System.out.println("Updated " + keycard.getKeycardCode() +
                                 " (Occupant " + keycard.getOccupantId() +
                                 "): " + oldLevel + " -> " + newLevelName);
                updatedCount++;
            } else {
                System.out.println("Failed to update " + keycard.getKeycardCode());
            }
        }

        System.out.println("\n=== RANDOM ASSIGNMENT COMPLETE ===");
        System.out.println("Updated " + updatedCount + " keycards with random levels");
        return updatedCount;
    }

    /**
     * Update keycard access level
     * @param keycardId ID of the keycard
     * @param newAccessLevel New access level (1-3)
     * @return true if updated successfully
     */
    public static boolean updateKeycardAccessLevel(int keycardId, int newAccessLevel) {
        String sql = "UPDATE keycards SET access_level = ? WHERE keycard_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, newAccessLevel);
            stmt.setInt(2, keycardId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            if (isMissingAccessLevelColumn(e)) {
                System.err.println("Access level column not available in database");
                return false;
            }
            System.err.println("Error updating keycard access level: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get access level name from level number
     * @param level Access level number
     * @return Access level name
     */
    public static String getAccessLevelName(int level) {
        switch (level) {
            case 1: return "Resident";
            case 2: return "Employee";
            case 3: return "Admin";
            default: return "Unknown";
        }
    }
}
