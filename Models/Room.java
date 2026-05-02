package Models;

import java.util.Objects;

/**
 * Room Model - Represents a bunker room or space.
 */
public class Room {
    private int roomId;
    private String roomName;
    private String roomType;
    private int capacity;
    private boolean isAvailable;

    public Room(int roomId, String roomName, String roomType, int capacity, boolean isAvailable) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.isAvailable = isAvailable;
    }

    public Room(String roomName, String roomType, int capacity, boolean isAvailable) {
        this.roomName = roomName;
        this.roomType = roomType;
        this.capacity = capacity;
        this.isAvailable = isAvailable;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public void setRoomName(String roomName) {
        this.roomName = roomName;
    }

    public String getRoomType() {
        return roomType;
    }

    public void setRoomType(String roomType) {
        this.roomType = roomType;
    }

    public int getCapacity() {
        return capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = capacity;
    }

    public boolean isAvailable() {
        return isAvailable;
    }

    public void setAvailable(boolean available) {
        isAvailable = available;
    }

    public int getId() {
        return roomId;
    }

    @Override
    public String toString() {
        return String.format("Room{id=%d, name='%s', type='%s', capacity=%d, available=%s}",
                roomId, roomName, roomType, capacity, isAvailable);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Room)) return false;
        Room room = (Room) o;
        return roomId == room.roomId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(roomId);
    }
}
