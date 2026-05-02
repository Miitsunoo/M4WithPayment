package Repositories;

import Models.WorkDuties;
import Database.DatabaseConnection;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * WorkDutiesRepo - Repository for work duty database operations.
 */
public class WorkDutiesRepo {

    public static boolean assignDuty(WorkDuties duty) {
        String sql = "INSERT INTO work_duties (occupant_id, duty_name, assigned_date, shift, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, duty.getOccupantId());
            stmt.setString(2, duty.getDutyName());
            stmt.setDate(3, Date.valueOf(duty.getAssignedDate()));
            stmt.setString(4, duty.getShift());
            stmt.setString(5, duty.getStatus());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        duty.setDutyId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error assigning work duty: " + e.getMessage());
        }
        return false;
    }

    public static WorkDuties getDutyById(int dutyId) {
        String sql = "SELECT duty_id, occupant_id, duty_name, assigned_date, shift, status FROM work_duties WHERE duty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dutyId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new WorkDuties(
                        rs.getInt("duty_id"),
                        rs.getInt("occupant_id"),
                        rs.getString("duty_name"),
                        rs.getDate("assigned_date").toLocalDate(),
                        rs.getString("shift"),
                        rs.getString("status")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting work duty by ID: " + e.getMessage());
        }
        return null;
    }

    public static List<WorkDuties> getDutiesByOccupantId(int occupantId) {
        List<WorkDuties> duties = new ArrayList<>();
        String sql = "SELECT duty_id, occupant_id, duty_name, assigned_date, shift, status FROM work_duties WHERE occupant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, occupantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    duties.add(new WorkDuties(
                        rs.getInt("duty_id"),
                        rs.getInt("occupant_id"),
                        rs.getString("duty_name"),
                        rs.getDate("assigned_date").toLocalDate(),
                        rs.getString("shift"),
                        rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting duties by occupant ID: " + e.getMessage());
        }
        return duties;
    }

    public static List<WorkDuties> getAllDuties() {
        List<WorkDuties> duties = new ArrayList<>();
        String sql = "SELECT duty_id, occupant_id, duty_name, assigned_date, shift, status FROM work_duties";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                duties.add(new WorkDuties(
                    rs.getInt("duty_id"),
                    rs.getInt("occupant_id"),
                    rs.getString("duty_name"),
                    rs.getDate("assigned_date").toLocalDate(),
                    rs.getString("shift"),
                    rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all duties: " + e.getMessage());
        }
        return duties;
    }

    public static boolean updateDuty(WorkDuties duty) {
        String sql = "UPDATE work_duties SET duty_name = ?, assigned_date = ?, shift = ?, status = ? WHERE duty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, duty.getDutyName());
            stmt.setDate(2, Date.valueOf(duty.getAssignedDate()));
            stmt.setString(3, duty.getShift());
            stmt.setString(4, duty.getStatus());
            stmt.setInt(5, duty.getDutyId());
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating work duty: " + e.getMessage());
        }
        return false;
    }

    public static boolean removeDuty(int dutyId) {
        String sql = "DELETE FROM work_duties WHERE duty_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, dutyId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error removing work duty: " + e.getMessage());
        }
        return false;
    }
}
