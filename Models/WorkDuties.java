package Models;

import java.time.LocalDate;

/**
 * WorkDuties Model - Represents work duties assigned to bunker occupants
 */
public class WorkDuties {
    private int dutyId;
    private int occupantId;
    private String dutyName;
    private LocalDate assignedDate;
    private String shift;  // Morning, Afternoon, Night
    private String status; // Pending, Ongoing, Completed

    // Constructor with all parameters (for existing duties from database)
    public WorkDuties(int dutyId, int occupantId, String dutyName, LocalDate assignedDate, String shift, String status) {
        this.dutyId = dutyId;
        this.occupantId = occupantId;
        this.dutyName = dutyName;
        this.assignedDate = assignedDate;
        this.shift = shift;
        this.status = status;
    }

    // Constructor for creating new duties (dutyId auto-generated)
    public WorkDuties(int occupantId, String dutyName, LocalDate assignedDate, String shift, String status) {
        this.occupantId = occupantId;
        this.dutyName = dutyName;
        this.assignedDate = assignedDate;
        this.shift = shift;
        this.status = status;
    }

    // Getters
    public int getDutyId() {
        return dutyId;
    }

    public int getOccupantId() {
        return occupantId;
    }

    public String getDutyName() {
        return dutyName;
    }

    public LocalDate getAssignedDate() {
        return assignedDate;
    }

    public String getShift() {
        return shift;
    }

    public String getStatus() {
        return status;
    }

    // Setters
    public void setDutyId(int dutyId) {
        this.dutyId = dutyId;
    }

    public void setOccupantId(int occupantId) {
        this.occupantId = occupantId;
    }

    public void setDutyName(String dutyName) {
        this.dutyName = dutyName;
    }

    public void setAssignedDate(LocalDate assignedDate) {
        this.assignedDate = assignedDate;
    }

    public void setShift(String shift) {
        this.shift = shift;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // toString method for easy printing
    @Override
    public String toString() {
        return "WorkDuties{" +
                "dutyId=" + dutyId +
                ", occupantId=" + occupantId +
                ", dutyName='" + dutyName + '\'' +
                ", assignedDate=" + assignedDate +
                ", shift='" + shift + '\'' +
                ", status='" + status + '\'' +
                '}';
    }
}
