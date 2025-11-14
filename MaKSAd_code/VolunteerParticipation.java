/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package maksad1;

import java.time.*;

public class VolunteerParticipation {

    public enum AttendanceStatus { PRESENT, ABSENT, CANCELED, UNSET }

    private final Volunteer volunteer;   // نجيب منه id + name
    private final Event event;           // نجيب منه name + date + start/end time
    private String role;                 // دور المتطوع بالفعالية

    private LocalDateTime checkInAt;     // وقت الحضور
    private LocalDateTime checkOutAt;    // وقت الانصراف

    private AttendanceStatus status = AttendanceStatus.UNSET;

    public VolunteerParticipation(Volunteer volunteer, Event event, String role) {
        this.volunteer = volunteer;
        this.event = event;
        this.role = role;

        // مبدئياً: نخلي check-in/out مساوية لوقت الفعالية
        LocalDate d = event.getDate();
        if (d != null && event.getStartTime() != null && event.getEndTime() != null) {
            this.checkInAt = LocalDateTime.of(d, event.getStartTime());
            this.checkOutAt = LocalDateTime.of(d, event.getEndTime());
        }
    }

    // لو حبيتي تعيدين ضبطهم من الواجهة:
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

    // أهلية الشهادة: حضر وله ساعات > 0 (إنشاء الشهادة يتم في Certificate فقط)
    public boolean isEligibleForCertificate() {
        return status == AttendanceStatus.PRESENT && getParticipationHours() > 0.0;
    }

    // حساب الساعات من check-in/check-out فقط
    public double getParticipationHours() {
        if (status == AttendanceStatus.ABSENT || status == AttendanceStatus.CANCELED) return 0.0;
        if (checkInAt == null || checkOutAt == null || !checkOutAt.isAfter(checkInAt)) return 0.0;

        long minutes = Duration.between(checkInAt, checkOutAt).toMinutes();
        return minutes / 60.0;
    }

    // للواجهة / الربط مع الداتابيس
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




