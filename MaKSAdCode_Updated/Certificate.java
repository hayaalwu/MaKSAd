package maksadpro;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/* Represents a certificate awarded to a volunteer after completing an event. */
public class Certificate {

    // Primary key from database
    private final int certificateId;

    // ID of the volunteer who earned the certificate
    private final int volunteerId;

    // Event name (references events.name)
    private final String eventName;

    // Hours earned (matches hours_earned in DB)
    private final double hoursEarned;

    // Issue date of the certificate
    private final LocalDate issueDate;

    // ===================== VALIDATION HELPERS =====================

    private static void validateVolunteerId(int volunteerId) {
        if (volunteerId <= 0) {
            throw new IllegalArgumentException("Volunteer ID must be a positive integer.");
        }
    }

    private static void validateEventName(String eventName) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name cannot be empty.");
        }
    }

    private static void validateHoursEarned(double hoursEarned) {
        if (hoursEarned <= 0) {
            throw new IllegalArgumentException("Hours earned must be greater than 0.");
        }
    }

    // ===================== CONSTRUCTORS =====================

    public Certificate(int certificateId, int volunteerId,
                       String eventName, double hoursEarned,
                       LocalDate issueDate) {

        // هذا الكونستركتر يُستخدم عند تحميل البيانات من الداتابيس
        // نفترض أن الداتا اللي في الداتابيس أصلاً صحيحة، لذلك ما حطيت validation هنا
        this.certificateId = certificateId;
        this.volunteerId = volunteerId;
        this.eventName = eventName;
        this.hoursEarned = hoursEarned;
        this.issueDate = issueDate;
    }

    // Used before inserting into DB
    public Certificate(int volunteerId, String eventName, double hoursEarned) {
        // هنا نتحقق من البيانات لأنها جاية من التطبيق قبل ما تدخل الداتابيس
        validateVolunteerId(volunteerId);
        validateEventName(eventName);
        validateHoursEarned(hoursEarned);

        this.certificateId = -1;
        this.volunteerId = volunteerId;
        this.eventName = eventName;
        this.hoursEarned = hoursEarned;
        this.issueDate = LocalDate.now();
    }

    // ===================== GETTERS =====================
    public int getCertificateId() { return certificateId; }
    public int getVolunteerId() { return volunteerId; }
    public String getEventName() { return eventName; }
    public double getHoursEarned() { return hoursEarned; }
    public LocalDate getIssueDate() { return issueDate; }

    // ===================== INSERT INTO DATABASE =====================

    public int saveToDatabase() {

        // تأكيد إضافي قبل الإدخال (لو أحد مستقبلاً عدّل القيم قبل النداء لهذه الدالة)
        validateVolunteerId(volunteerId);
        validateEventName(eventName);
        validateHoursEarned(hoursEarned);

        String sql = """
            INSERT INTO certificates (volunteer_id, event_name, issue_date, hours_earned)
            VALUES (?, ?, ?, ?)
            """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setInt(1, volunteerId);
            ps.setString(2, eventName);
            ps.setDate(3, Date.valueOf(issueDate));
            ps.setDouble(4, hoursEarned);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    return rs.getInt(1); // new certificate_id
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return -1;
    }

    // ===================== LOAD ALL =====================

    public static List<Certificate> loadAllCertificates() {

        List<Certificate> list = new ArrayList<>();

        String sql = """
            SELECT certificate_id, volunteer_id, event_name, hours_earned, issue_date
            FROM certificates
            """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Certificate cert = new Certificate(
                        rs.getInt("certificate_id"),
                        rs.getInt("volunteer_id"),
                        rs.getString("eventName"), // if your column is event_name, keep "event_name"
                        rs.getDouble("hours_earned"),
                        rs.getDate("issue_date").toLocalDate()
                );

                list.add(cert);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    // ===================== LOAD BY VOLUNTEER =====================

    public static List<Certificate> loadByVolunteer(int volunteerId) {

        List<Certificate> list = new ArrayList<>();

        String sql = """
            SELECT certificate_id, volunteer_id, event_name, hours_earned, issue_date
            FROM certificates
            WHERE volunteer_id = ?
            """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, volunteerId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    Certificate cert = new Certificate(
                            rs.getInt("certificate_id"),
                            rs.getInt("volunteer_id"),
                            rs.getString("event_name"),
                            rs.getDouble("hours_earned"),
                            rs.getDate("issue_date").toLocalDate()
                    );
                    list.add(cert);
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return list;
    }

    // ===================== SUPPORT METHODS =====================

    public Object[] toTableRow(String volunteerName) {
        return new Object[]{
                volunteerName,
                eventName,
                hoursEarned,
                issueDate.toString()
        };
    }

    @Override
    public String toString() {
        return "Certificate #" + certificateId +
                " | Volunteer ID: " + volunteerId +
                " | Event: " + eventName +
                " | Hours: " + hoursEarned +
                " | Issued: " + issueDate;
    }
}