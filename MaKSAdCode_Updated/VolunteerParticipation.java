package maksadpro;

import java.sql.*;
import java.time.*;

public class VolunteerParticipation {

    public enum AttendanceStatus { PRESENT, ABSENT, CANCELED, UNSET }

    private int volunteerId;
    private String volunteerName;
    private int eventId;
    private String eventName;

    private LocalDate eventDate;   
    private String role;

    private LocalDateTime checkInAt;
    private LocalDateTime checkOutAt;
    private AttendanceStatus status = AttendanceStatus.UNSET;

    // CONSTRUCTOR
    public VolunteerParticipation(int volunteerId,
                                  String volunteerName,
                                  int eventId,
                                  String eventName,
                                  LocalDate eventDate,
                                  String role,
                                  LocalDateTime checkInAt,
                                  LocalDateTime checkOutAt,
                                  String statusStr) {

        this.volunteerId = volunteerId;
        this.volunteerName = volunteerName;
        this.eventId = eventId;
        this.eventName = eventName;
        this.eventDate = eventDate;
        this.role = role;
        this.checkInAt = checkInAt;
        this.checkOutAt = checkOutAt;

        if (statusStr != null) {
            try { this.status = AttendanceStatus.valueOf(statusStr); }
            catch (Exception ignore) { this.status = AttendanceStatus.UNSET; }
        }
    }

    // GETTERS
    public int getVolunteerId() { return volunteerId; }
    public String getVolunteerName() { return volunteerName; }
    public int getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public LocalDate getEventDate() { return eventDate; }
    public LocalDateTime getCheckInAt() { return checkInAt; }
    public LocalDateTime getCheckOutAt() { return checkOutAt; }
    public String getRole() { return role; }
    public AttendanceStatus getStatus() { return status; }

    public double getHours() {
        if (checkInAt == null || checkOutAt == null) return 0.0;
        if (!checkOutAt.isAfter(checkInAt)) return 0.0;
        return Duration.between(checkInAt, checkOutAt).toMinutes() / 60.0;
    }

    // SETTERS + AUTO UPDATE TO DB
    public void setRole(String role) throws SQLException {
        this.role = role;
        updateField("role", role);
    }

    public void setStatus(AttendanceStatus status) throws SQLException {
        this.status = status;
        updateField("status", status.name());
    }

    public void setCheckInAt(LocalDateTime t) throws SQLException {
        this.checkInAt = t;
        updateField("check_in", (t == null ? null : Timestamp.valueOf(t)));
        updateHoursInDB();
    }

    public void setCheckOutAt(LocalDateTime t) throws SQLException {
        this.checkOutAt = t;
        updateField("check_out", (t == null ? null : Timestamp.valueOf(t)));
        updateHoursInDB();
    }

    // HELPERS: UPDATE DB
    private void updateField(String column, Object value) throws SQLException {

        String sql = "UPDATE volunteer_participations " +
                     "SET " + column + " = ? " +
                     "WHERE volunteer_id = ? AND event_name = ? AND event_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (value == null)
                ps.setNull(1, Types.NULL);
            else
                ps.setObject(1, value);

            ps.setInt(2, volunteerId);
            ps.setString(3, eventName);
            ps.setDate(4, java.sql.Date.valueOf(eventDate));

            ps.executeUpdate();
        }
    }

    private void updateHoursInDB() throws SQLException {

        Double hours = null;

        if (checkInAt != null && checkOutAt != null && checkOutAt.isAfter(checkInAt)) {
            hours = getHours();
        }

        String sql = """
            UPDATE volunteer_participations
            SET hours = ?
            WHERE volunteer_id = ? AND event_name = ? AND event_date = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            if (hours == null) ps.setNull(1, Types.DOUBLE);
            else ps.setDouble(1, hours);

            ps.setInt(2, volunteerId);
            ps.setString(3, eventName);
            ps.setDate(4, java.sql.Date.valueOf(eventDate));

            ps.executeUpdate();
        }

        updateVolunteerTotalHours();
    }

    private void updateVolunteerTotalHours() throws SQLException {

        String sql = """
            UPDATE volunteers
            SET total_hours = COALESCE(
                (SELECT SUM(hours)
                 FROM volunteer_participations
                 WHERE volunteer_id = ?),
                0
            )
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, volunteerId);
            ps.executeUpdate();
        }
    }

    // DELETE
    public void deleteFromDB() throws SQLException {
        String sql = """
            DELETE FROM volunteer_participations
            WHERE volunteer_id = ? AND event_name = ? AND event_date = ?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, volunteerId);
            ps.setString(2, eventName);
            ps.setDate(3, java.sql.Date.valueOf(eventDate));

            ps.executeUpdate();
        }

        updateVolunteerTotalHours();
    }

}
