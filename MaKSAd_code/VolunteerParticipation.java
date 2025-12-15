package maksad1;

import java.time.*;

public class VolunteerParticipation {

    public enum AttendanceStatus { PRESENT, ABSENT, CANCELED, UNSET }

    private final Volunteer volunteer;  
    private final Event event;          
    private String role;                  

    private LocalDateTime checkInAt;      
    private LocalDateTime checkOutAt;     

    private AttendanceStatus status = AttendanceStatus.UNSET;

    public VolunteerParticipation(Volunteer volunteer, Event event, String role) {
        this.volunteer = volunteer;
        this.event = event;
        this.role = role;

        LocalDate d = event.getDate();
        if (d != null && event.getStartTime() != null && event.getEndTime() != null) {
            this.checkInAt = LocalDateTime.of(d, event.getStartTime());
            this.checkOutAt = LocalDateTime.of(d, event.getEndTime());
        }
    }

    public void setCheckIn(LocalDateTime time) {
        this.checkInAt = time;
        if (status != AttendanceStatus.CANCELED && status != AttendanceStatus.ABSENT) {
            status = AttendanceStatus.PRESENT;
        }
    }

    public void setCheckOut(LocalDateTime time) {
        this.checkOutAt = time;
        if (status != AttendanceStatus.CANCELED && status != AttendanceStatus.ABSENT) {
            status = AttendanceStatus.PRESENT;
        }
    }

    public void markAbsent() {
        status = AttendanceStatus.ABSENT;
        checkInAt = null;
        checkOutAt = null;
    }

    public void cancelParticipation() {
        status = AttendanceStatus.CANCELED;
        checkInAt = null;
        checkOutAt = null;
    }

    public boolean isEligibleForCertificate() {
        return status == AttendanceStatus.PRESENT && getParticipationHours() > 0.0;
    }

    public double getParticipationHours() {
        if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.CANCELED) return 0.0;
        if (checkInAt == null || checkOutAt == null || !checkOutAt.isAfter(checkInAt)) return 0.0;

        long minutes = Duration.between(checkInAt, checkOutAt).toMinutes();
        return minutes / 60.0;
    }

    public int getVolunteerId()          { return volunteer.getId(); }
    public String getVolunteerName()     { return volunteer.getName(); }
    public String getEventName()         { return event.getName(); }
    public LocalDate getEventDate()      { return event.getDate(); }
    public LocalTime getEventStartTime() { return event.getStartTime(); }
    public LocalTime getEventEndTime()   { return event.getEndTime(); }

    public String getRole()              { return role; }
    public void setRole(String role)     { this.role = role; }

    public AttendanceStatus getStatus()  { return status; }
    public void setStatus(AttendanceStatus status) { this.status = status; }

    public LocalDateTime getCheckInAt()  { return checkInAt; }
    public LocalDateTime getCheckOutAt() { return checkOutAt; }
}





