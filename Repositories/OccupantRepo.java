package Repositories;

import Models.Occupant;
import Database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * OccupantRepo - Repository for Occupant database operations
 */
public class OccupantRepo {

    /**
     * Verify if an occupant exists with the given credentials
     * @param firstName First name of the occupant
     * @param lastName Last name of the occupant
     * @param occupantId ID of the occupant
     * @return true if occupant exists and credentials match, false otherwise
     */
    public static boolean verifyOccupant(String firstName, String lastName, int occupantId) {
        String sql = "SELECT COUNT(*) FROM occupants WHERE first_name = ? AND last_name = ? AND occupant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setInt(3, occupantId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1) > 0;
                }
            }
        } catch (SQLException e) {
            System.err.println("Error verifying occupant: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get occupant by ID
     * @param occupantId ID of the occupant to retrieve
     * @return Occupant object if found, null otherwise
     */
    public static Occupant getOccupantById(int occupantId) {
        String sql = "SELECT occupant_id, first_name, last_name, email, phone, registered_at FROM occupants WHERE occupant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, occupantId);

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Occupant(
                        rs.getInt("occupant_id"),
                        rs.getString("first_name"),
                        rs.getString("last_name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getString("registered_at")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting occupant by ID: " + e.getMessage());
        }

        return null;
    }

    /**
     * Create a new occupant
     * @param firstName First name
     * @param lastName Last name
     * @param email Email address
     * @param phone Phone number
     * @return the occupant ID if created successfully, -1 otherwise
     */
    public static int createOccupant(String firstName, String lastName, String email, String phone) {
        String sql = "INSERT INTO occupants (first_name, last_name, email, phone) VALUES (?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet generatedKeys = stmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        // Ensure IDs are sequential after creation
                        reassignOccupantIds();
                        
                        // Return the count of total occupants as the final ID
                        String countSql = "SELECT COUNT(*) FROM occupants";
                        PreparedStatement countStmt = conn.prepareStatement(countSql);
                        ResultSet countRs = countStmt.executeQuery();
                        if (countRs.next()) {
                            int totalOccupants = countRs.getInt(1);
                            countStmt.close();
                            countRs.close();
                            return totalOccupants;
                        }
                        countStmt.close();
                        countRs.close();
                    }
                }
            }
            return -1;
        } catch (SQLException e) {
            System.err.println("Error creating occupant: " + e.getMessage());
            return -1;
        }
    }

    /**
     * Update occupant information
     * @param occupantId ID of the occupant to update
     * @param firstName New first name
     * @param lastName New last name
     * @param email New email
     * @param phone New phone
     * @return true if updated successfully, false otherwise
     */
    public static boolean updateOccupant(int occupantId, String firstName, String lastName, String email, String phone) {
        String sql = "UPDATE occupants SET first_name = ?, last_name = ?, email = ?, phone = ? WHERE occupant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setString(1, firstName);
            stmt.setString(2, lastName);
            stmt.setString(3, email);
            stmt.setString(4, phone);
            stmt.setInt(5, occupantId);

            int rowsAffected = stmt.executeUpdate();
            return rowsAffected > 0;
        } catch (SQLException e) {
            System.err.println("Error updating occupant: " + e.getMessage());
        }

        return false;
    }

    /**
     * Get all occupants
     * @return List of all occupants
     */
    public static List<Occupant> getAllOccupants() {
        List<Occupant> occupants = new ArrayList<>();
        String sql = "SELECT occupant_id, first_name, last_name, email, phone, registered_at FROM occupants";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                occupants.add(new Occupant(
                    rs.getInt("occupant_id"),
                    rs.getString("first_name"),
                    rs.getString("last_name"),
                    rs.getString("email"),
                    rs.getString("phone"),
                    rs.getString("registered_at")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all occupants: " + e.getMessage());
        }

        return occupants;
    }

    /**
     * Delete an occupant by ID and reassign occupant IDs sequentially
     * @param occupantId ID of the occupant to delete
     * @return true if deleted successfully, false otherwise
     */
    public static boolean deleteOccupant(int occupantId) {
        String sql = "DELETE FROM occupants WHERE occupant_id = ?";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, occupantId);

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                // Reassign IDs after deletion to keep them sequential
                reassignOccupantIds();
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error deleting occupant: " + e.getMessage());
        }

        return false;
    }

    /**
     * Reassign occupant IDs sequentially (1, 2, 3, ...) based on registration date
     * Updates all foreign key references in related tables
     */
    public static void reassignOccupantIds() {
        try (Connection conn = DatabaseConnection.getConnection()) {
            // Disable foreign key checks temporarily
            PreparedStatement disableFK = conn.prepareStatement("SET FOREIGN_KEY_CHECKS=0");
            disableFK.executeUpdate();
            disableFK.close();

            // Get all occupants ordered by registration date
            String getSql = "SELECT occupant_id FROM occupants ORDER BY registered_at ASC";
            PreparedStatement getStmt = conn.prepareStatement(getSql);
            ResultSet rs = getStmt.executeQuery();

            List<Integer> oldIds = new ArrayList<>();
            while (rs.next()) {
                oldIds.add(rs.getInt("occupant_id"));
            }
            rs.close();
            getStmt.close();

            // Create a mapping of old IDs to new IDs
            Map<Integer, Integer> idMapping = new HashMap<>();
            for (int i = 0; i < oldIds.size(); i++) {
                idMapping.put(oldIds.get(i), i + 1);
            }

            // Step 1: Update occupants table using temporary negative IDs to avoid conflicts
            String updateOccupantTemp = "UPDATE occupants SET occupant_id = ? WHERE occupant_id = ?";
            for (Map.Entry<Integer, Integer> entry : idMapping.entrySet()) {
                int oldId = entry.getKey();
                int newId = entry.getValue();
                int tempId = -(newId);  // Use negative temporary ID

                if (oldId != newId) {
                    PreparedStatement stmt = conn.prepareStatement(updateOccupantTemp);
                    stmt.setInt(1, tempId);
                    stmt.setInt(2, oldId);
                    stmt.executeUpdate();
                    stmt.close();
                }
            }

            // Step 2: Update related tables with temporary negative IDs
            String updateKeycardTemp = "UPDATE keycards SET occupant_id = ? WHERE occupant_id = ?";
            String updateReservationTemp = "UPDATE reservations SET occupant_id = ? WHERE occupant_id = ?";
            String updateDutiesTemp = "UPDATE work_duties SET occupant_id = ? WHERE occupant_id = ?";

            for (Map.Entry<Integer, Integer> entry : idMapping.entrySet()) {
                int oldId = entry.getKey();
                int newId = entry.getValue();
                int tempId = -(newId);

                if (oldId != newId) {
                    // Update keycards
                    PreparedStatement keycardStmt = conn.prepareStatement(updateKeycardTemp);
                    keycardStmt.setInt(1, tempId);
                    keycardStmt.setInt(2, oldId);
                    keycardStmt.executeUpdate();
                    keycardStmt.close();

                    // Update reservations
                    PreparedStatement reservationStmt = conn.prepareStatement(updateReservationTemp);
                    reservationStmt.setInt(1, tempId);
                    reservationStmt.setInt(2, oldId);
                    reservationStmt.executeUpdate();
                    reservationStmt.close();

                    // Update work_duties
                    PreparedStatement dutiesStmt = conn.prepareStatement(updateDutiesTemp);
                    dutiesStmt.setInt(1, tempId);
                    dutiesStmt.setInt(2, oldId);
                    dutiesStmt.executeUpdate();
                    dutiesStmt.close();
                }
            }

            // Step 3: Update from temporary IDs to final sequential IDs
            String updateOccupantFinal = "UPDATE occupants SET occupant_id = ? WHERE occupant_id = ?";
            String updateKeycardFinal = "UPDATE keycards SET occupant_id = ? WHERE occupant_id = ?";
            String updateReservationFinal = "UPDATE reservations SET occupant_id = ? WHERE occupant_id = ?";
            String updateDutiesFinal = "UPDATE work_duties SET occupant_id = ? WHERE occupant_id = ?";

            for (Map.Entry<Integer, Integer> entry : idMapping.entrySet()) {
                int newId = entry.getValue();
                int tempId = -(newId);

                // Update occupants table
                PreparedStatement occupantStmt = conn.prepareStatement(updateOccupantFinal);
                occupantStmt.setInt(1, newId);
                occupantStmt.setInt(2, tempId);
                occupantStmt.executeUpdate();
                occupantStmt.close();

                // Update keycards
                PreparedStatement keycardStmt = conn.prepareStatement(updateKeycardFinal);
                keycardStmt.setInt(1, newId);
                keycardStmt.setInt(2, tempId);
                keycardStmt.executeUpdate();
                keycardStmt.close();

                // Update reservations
                PreparedStatement reservationStmt = conn.prepareStatement(updateReservationFinal);
                reservationStmt.setInt(1, newId);
                reservationStmt.setInt(2, tempId);
                reservationStmt.executeUpdate();
                reservationStmt.close();

                // Update work_duties
                PreparedStatement dutiesStmt = conn.prepareStatement(updateDutiesFinal);
                dutiesStmt.setInt(1, newId);
                dutiesStmt.setInt(2, tempId);
                dutiesStmt.executeUpdate();
                dutiesStmt.close();
            }

            // Reset AUTO_INCREMENT to max occupant_id + 1
            String resetAutoInc = "ALTER TABLE occupants AUTO_INCREMENT = (SELECT MAX(occupant_id) + 1 FROM (SELECT * FROM occupants) AS temp)";
            try {
                PreparedStatement resetStmt = conn.prepareStatement(resetAutoInc);
                resetStmt.executeUpdate();
                resetStmt.close();
            } catch (SQLException e) {
                // Fallback: manually set auto increment
                String getMaxId = "SELECT MAX(occupant_id) FROM occupants";
                PreparedStatement maxStmt = conn.prepareStatement(getMaxId);
                ResultSet maxRs = maxStmt.executeQuery();
                if (maxRs.next()) {
                    int maxId = maxRs.getInt(1);
                    String setAutoInc = "ALTER TABLE occupants AUTO_INCREMENT = " + (maxId + 1);
                    PreparedStatement setStmt = conn.prepareStatement(setAutoInc);
                    setStmt.executeUpdate();
                    setStmt.close();
                }
                maxRs.close();
                maxStmt.close();
            }

            // Re-enable foreign key checks
            PreparedStatement enableFK = conn.prepareStatement("SET FOREIGN_KEY_CHECKS=1");
            enableFK.executeUpdate();
            enableFK.close();

            System.out.println("Occupant IDs have been reassigned sequentially.");

        } catch (SQLException e) {
            System.err.println("Error reassigning occupant IDs: " + e.getMessage());
        }
    }

    // ================================
    // TESTING AND UTILITY METHODS
    // ================================

    /**
     * Test database connectivity and display all occupants
     * (From DatabaseTest.class and TestOccupants.class functionality)
     */
    public static void testDatabaseConnection() {
        System.out.println("=== DATABASE TEST - OCCUPANTS ===");

        try {
            List<Occupant> occupants = getAllOccupants();
            System.out.println("Found " + occupants.size() + " occupants in database:");

            for (Occupant occupant : occupants) {
                System.out.println("ID: " + occupant.getId() + ", Name: " + occupant.getFirstName() + " " + occupant.getLastName());
            }
        } catch (Exception e) {
            System.err.println("Database test failed: " + e.getMessage());
        }

        System.out.println("=== TEST COMPLETE ===\n");
    }

    /**
     * Test occupant registration process
     * (From RegistrationTest.class functionality)
     * @return true if test passed
     */
    public static boolean testRegistrationProcess() {
        System.out.println("=== REGISTRATION TEST ===");

        // Test data
        String firstName = "Test";
        String lastName = "User";
        String email = "test.user" + System.currentTimeMillis() + "@example.com";
        String phone = "555-TEST";

        System.out.println("Testing registration for: " + firstName + " " + lastName);

        // Attempt registration
        int occupantId = createOccupant(firstName, lastName, email, phone);

        if (occupantId != -1) {
            System.out.println("Occupant created with ID: " + occupantId);

            // Check if keycard was automatically assigned
            List<Models.Keycard> keycards = Repositories.KeycardRepo.getKeycardsByOccupantId(occupantId);
            if (!keycards.isEmpty()) {
                Models.Keycard keycard = keycards.get(0);
                System.out.println("Keycard automatically assigned:");
                System.out.println("   Code: " + keycard.getKeycardCode());
                System.out.println("   Access Level: " + keycard.getAccessLevelName());
                System.out.println("=== REGISTRATION TEST PASSED ===");
                return true;
            } else {
                System.out.println("No keycard was assigned to the test occupant");
            }
        } else {
            System.out.println("Failed to create test occupant");
        }

        System.out.println("=== REGISTRATION TEST FAILED ===");
        return false;
    }

    /**
     * Display comprehensive occupant and keycard summary
     * (From TestOccupants.class functionality)
     */
    public static void displayOccupantSummary() {
        System.out.println("=== OCCUPANT AND KEYCARD SUMMARY ===");

        List<Occupant> occupants = getAllOccupants();

        for (Occupant occupant : occupants) {
            System.out.println("\n--- " + occupant.getFullName() + " (ID: " + occupant.getId() + ") ---");
            System.out.println("Email: " + occupant.getEmail());
            System.out.println("Phone: " + occupant.getPhone());

            // Get associated keycards
            List<Models.Keycard> keycards = Repositories.KeycardRepo.getKeycardsByOccupantId(occupant.getId());
            if (!keycards.isEmpty()) {
                System.out.println("Keycards:");
                for (Models.Keycard keycard : keycards) {
                    System.out.println("  • " + keycard.getKeycardCode() +
                                     " (Level: " + keycard.getAccessLevelName() +
                                     ", Active: " + keycard.isActive() + ")");
                }
            } else {
                System.out.println("No keycards assigned");
            }
        }

        System.out.println("\n=== SUMMARY COMPLETE ===\n");
    }
}
