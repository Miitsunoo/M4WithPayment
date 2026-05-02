package Repositories;

import Models.Reservation;
import Database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * ReservationRepo - Repository for reservation database operations.
 */
public class ReservationRepo {

    public static boolean createReservation(Reservation reservation) {
        String sql = "INSERT INTO reservations (occupant_id, room_id, start_time, end_time, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql, PreparedStatement.RETURN_GENERATED_KEYS)) {
            stmt.setInt(1, reservation.getOccupantId());
            stmt.setInt(2, reservation.getRoomId());
            stmt.setTimestamp(3, Timestamp.valueOf(reservation.getStartTime()));
            stmt.setTimestamp(4, Timestamp.valueOf(reservation.getEndTime()));
            stmt.setString(5, reservation.getStatus());

            int rowsAffected = stmt.executeUpdate();
            if (rowsAffected > 0) {
                try (ResultSet keys = stmt.getGeneratedKeys()) {
                    if (keys.next()) {
                        reservation.setReservationId(keys.getInt(1));
                    }
                }
                return true;
            }
        } catch (SQLException e) {
            System.err.println("Error creating reservation: " + e.getMessage());
        }
        return false;
    }

    public static Reservation getReservationById(int reservationId) {
        String sql = "SELECT reservation_id, occupant_id, room_id, start_time, end_time, status, created_at FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReservation(rs);
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reservation by ID: " + e.getMessage());
        }
        return null;
    }

    public static List<Reservation> getReservationsByRoomId(int roomId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, occupant_id, room_id, start_time, end_time, status, created_at FROM reservations WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reservations by room ID: " + e.getMessage());
        }
        return reservations;
    }

    public static List<Reservation> getReservationsByOccupantId(int occupantId) {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, occupant_id, room_id, start_time, end_time, status, created_at FROM reservations WHERE occupant_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, occupantId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    reservations.add(mapResultSetToReservation(rs));
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting reservations by occupant ID: " + e.getMessage());
        }
        return reservations;
    }

    public static List<Reservation> getAllReservations() {
        List<Reservation> reservations = new ArrayList<>();
        String sql = "SELECT reservation_id, occupant_id, room_id, start_time, end_time, status, created_at FROM reservations";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                reservations.add(mapResultSetToReservation(rs));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all reservations: " + e.getMessage());
        }
        return reservations;
    }

    public static boolean updateReservationStatus(int reservationId, String status) {
        String sql = "UPDATE reservations SET status = ? WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, status);
            stmt.setInt(2, reservationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating reservation status: " + e.getMessage());
        }
        return false;
    }

    public static boolean deleteReservation(int reservationId) {
        String sql = "DELETE FROM reservations WHERE reservation_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, reservationId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting reservation: " + e.getMessage());
        }
        return false;
    }

    private static Reservation mapResultSetToReservation(ResultSet rs) throws SQLException {
        return new Reservation(
            rs.getInt("reservation_id"),
            rs.getInt("occupant_id"),
            rs.getInt("room_id"),
            rs.getTimestamp("start_time").toLocalDateTime(),
            rs.getTimestamp("end_time").toLocalDateTime(),
            rs.getString("status"),
            rs.getTimestamp("created_at").toLocalDateTime()
        );
    }
}
