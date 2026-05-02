package Repositories;

import Models.Room;
import Database.DatabaseConnection;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * RoomRepo - Repository for room database operations.
 */
public class RoomRepo {

    public static Room getRoomById(int roomId) {
        String sql = "SELECT room_id, room_name, room_type, capacity, is_available FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_name"),
                        rs.getString("room_type"),
                        rs.getInt("capacity"),
                        rs.getBoolean("is_available")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting room by ID: " + e.getMessage());
        }
        return null;
    }

    public static Room getRoomByName(String roomName) {
        String sql = "SELECT room_id, room_name, room_type, capacity, is_available FROM rooms WHERE room_name = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return new Room(
                        rs.getInt("room_id"),
                        rs.getString("room_name"),
                        rs.getString("room_type"),
                        rs.getInt("capacity"),
                        rs.getBoolean("is_available")
                    );
                }
            }
        } catch (SQLException e) {
            System.err.println("Error getting room by name: " + e.getMessage());
        }
        return null;
    }

    public static List<Room> getAllRooms() {
        List<Room> rooms = new ArrayList<>();
        String sql = "SELECT room_id, room_name, room_type, capacity, is_available FROM rooms";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                rooms.add(new Room(
                    rs.getInt("room_id"),
                    rs.getString("room_name"),
                    rs.getString("room_type"),
                    rs.getInt("capacity"),
                    rs.getBoolean("is_available")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error getting all rooms: " + e.getMessage());
        }
        return rooms;
    }

    public static boolean createRoom(String roomName, String roomType, int capacity) {
        String sql = "INSERT INTO rooms (room_name, room_type, capacity) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, roomName);
            stmt.setString(2, roomType);
            stmt.setInt(3, capacity);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error creating room: " + e.getMessage());
        }
        return false;
    }

    public static boolean updateRoomAvailability(int roomId, boolean isAvailable) {
        String sql = "UPDATE rooms SET is_available = ? WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setBoolean(1, isAvailable);
            stmt.setInt(2, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error updating room availability: " + e.getMessage());
        }
        return false;
    }

    public static boolean deleteRoom(int roomId) {
        String sql = "DELETE FROM rooms WHERE room_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, roomId);
            return stmt.executeUpdate() > 0;
        } catch (SQLException e) {
            System.err.println("Error deleting room: " + e.getMessage());
        }
        return false;
    }
}
