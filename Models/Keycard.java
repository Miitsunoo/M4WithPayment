package Models;

/**
 * Keycard Model - Represents a security keycard for bunker access
 */
public class Keycard {
    private int keycardId;
    private int occupantId;
    private String keycardCode;
    private boolean isActive;
    private String issuedAt;
    private int accessLevel;

    // Constructor
    public Keycard(int keycardId, int occupantId, String keycardCode, boolean isActive, String issuedAt, int accessLevel) {
        this.keycardId = keycardId;
        this.occupantId = occupantId;
        this.keycardCode = keycardCode;
        this.isActive = isActive;
        this.issuedAt = issuedAt;
        this.accessLevel = accessLevel;
    }

    // Getters
    public int getKeycardId() {
        return keycardId;
    }

    public int getOccupantId() {
        return occupantId;
    }

    public String getKeycardCode() {
        return keycardCode;
    }

    public boolean isActive() {
        return isActive;
    }

    public String getIssuedAt() {
        return issuedAt;
    }

    public int getAccessLevel() {
        return accessLevel;
    }

    public String getAccessLevelName() {
        switch (accessLevel) {
            case 1:
                return "Resident";
            case 2:
                return "Employee";
            case 3:
                return "Admin";
            default:
                return "Unknown";
        }
    }

    // Setters
    public void setActive(boolean active) {
        isActive = active;
    }

    public void setAccessLevel(int accessLevel) {
        this.accessLevel = accessLevel;
    }

    @Override
    public String toString() {
        return String.format("ID: %d | Code: %s | Occupant ID: %d | Active: %s | Issued: %s | Level: %d",
                keycardId, keycardCode, occupantId, isActive, issuedAt, accessLevel);
    }

    // ================================
    // UTILITY METHODS
    // ================================

    /**
     * Validate keycard code format
     * @param code Keycard code to validate
     * @return true if format is valid
     */
    public static boolean isValidKeycardCode(String code) {
        if (code == null || code.length() != 9) {
            return false;
        }
        return code.startsWith("KC-") && code.substring(3).matches("\\d{6}");
    }

    /**
     * Generate a formatted keycard display string
     * @return Formatted display string
     */
    public String getFormattedDisplay() {
        return String.format("%s | %s | Level %d (%s)",
                keycardCode,
                isActive ? "ACTIVE" : "INACTIVE",
                accessLevel,
                getAccessLevelName());
    }

    /**
     * Check if keycard has admin access
     * @return true if admin level (3)
     */
    public boolean isAdmin() {
        return accessLevel == 3;
    }

    /**
     * Check if keycard has employee access
     * @return true if employee level (2) or higher
     */
    public boolean isEmployee() {
        return accessLevel >= 2;
    }

    /**
     * Check if keycard has resident access
     * @return true if resident level (1) or higher
     */
    public boolean isResident() {
        return accessLevel >= 1;
    }
}
