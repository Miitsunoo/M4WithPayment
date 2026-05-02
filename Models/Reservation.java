package Models;

import java.time.LocalDateTime;
import java.util.Objects;

/**
 * Reservation Model - Represents a room reservation in the bunker system.
 */
public class Reservation {
    private int reservationId;
    private int occupantId;
    private int roomId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;
    private LocalDateTime createdAt;

    public Reservation(int reservationId, int occupantId, int roomId, LocalDateTime startTime, LocalDateTime endTime, String status, LocalDateTime createdAt) {
        this.reservationId = reservationId;
        this.occupantId = occupantId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
        this.createdAt = createdAt;
    }

    public Reservation(int occupantId, int roomId, LocalDateTime startTime, LocalDateTime endTime, String status) {
        this.occupantId = occupantId;
        this.roomId = roomId;
        this.startTime = startTime;
        this.endTime = endTime;
        this.status = status;
    }

    public int getReservationId() {
        return reservationId;
    }

    public void setReservationId(int reservationId) {
        this.reservationId = reservationId;
    }

    public int getOccupantId() {
        return occupantId;
    }

    public void setOccupantId(int occupantId) {
        this.occupantId = occupantId;
    }

    public int getRoomId() {
        return roomId;
    }

    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return String.format("Reservation{id=%d, occupantId=%d, roomId=%d, start=%s, end=%s, status=%s, createdAt=%s}",
                reservationId, occupantId, roomId, startTime, endTime, status, createdAt);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Reservation)) return false;
        Reservation that = (Reservation) o;
        return reservationId == that.reservationId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(reservationId);
    }
}
