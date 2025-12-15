package maksadpro;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

//  NEW IMPORTS FOR DATABASE 
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

//  IMPORT FOR ERROR DIALOGS 
import javax.swing.JOptionPane;

//  IMPORT FOR RANDOM PASSWORD 
import java.security.SecureRandom;


 //Admin superclass.
 
public class Admin {

    //  ATTRIBUTES 
    private int adminID;
    private String name;
    private String email;
    private String password;
    private boolean isSuperAdmin;
    private String roleTitle;
    private Date lastLoginDate;

    private final List<OrganizerRecord> organizers = new ArrayList<>();
    private final List<EventRecord> events = new ArrayList<>();
    private final List<ReportSummary> reports = new ArrayList<>();

    //  DB CONNECTION HELPER 
    private static Connection getConnection() throws SQLException {
        String url = "jdbc:mysql://localhost:3306/maksad?useSSL=false&serverTimezone=UTC";
        String user = "root";
        String pass = "Ka24682468$";
        return DriverManager.getConnection(url, user, pass);
    }

    //  VALIDATION HELPERS 
    private static void showErrorDialog(String message, String title) {
        JOptionPane.showMessageDialog(
                null,
                message,
                title,
                JOptionPane.ERROR_MESSAGE
        );
    }

    private static void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'.");
        }
    }

    private static void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone number cannot be empty.");
        }
        String trimmed = phone.trim();
        if (!trimmed.startsWith("05")) {
            throw new IllegalArgumentException("Phone number must start with 05.");
        }
        if (trimmed.length() != 10) {
            throw new IllegalArgumentException("Phone number must be 10 digits.");
        }
        for (char ch : trimmed.toCharArray()) {
            if (!Character.isDigit(ch)) {
                throw new IllegalArgumentException("Phone number must contain digits only.");
            }
        }
    }

    private static void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters.");
        }
    }

    //  RANDOM PASSWORD GENERATOR 
    private static String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);   
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    //  CONSTRUCTORS 
    public Admin(int adminID, String name, String email, String password,
                 boolean isSuperAdmin, String roleTitle) {

        this.adminID = adminID;
        this.name = name;
        this.email = email;
        this.password = password;
        this.isSuperAdmin = isSuperAdmin;
        this.roleTitle = roleTitle;
        this.lastLoginDate = new Date();
    }

   

    //  MAIN BEHAVIOR 
    public void manageSystem() {
        System.out.println("Managing system configuration...");
    }

    public void viewSystemOverview() {
        System.out.println("System overview:");
        System.out.println("- Organizers: " + organizers.size());
        System.out.println("- Events: " + events.size());
    }

    //  ORGANIZERS 
    public void createOrganizer(String name, String email, String phone) {

        //  VALIDATION 
        try {
            validateEmail(email);
            validatePhone(phone);
        } catch (IllegalArgumentException ex) {
            showErrorDialog(ex.getMessage(), "Invalid Organizer Data");
            return; 
        }

        String sql = "INSERT INTO organizers (name, email, phone, active, role, password) VALUES (?, ?, ?, ?, ?, ?)";

        int newId = 0;
        String randomPassword = generateRandomPassword();

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setBoolean(4, true);
            ps.setString(5, "Organizer");
            ps.setString(6, randomPassword);   

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) newId = rs.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while creating organizer.\n" + ex.getMessage(),
                    "Database Error");
        }

        organizers.add(new OrganizerRecord(newId, name, email, phone, true, ""));
        System.out.println("Organizer created: " + name + " (ID=" + newId + ")");
        System.out.println("Generated password for organizer " + name + ": " + randomPassword);
    }

    public void updateOrganizer(int orgID, String name, String email, String phone) {

        //  VALIDATION 
        try {
            validateEmail(email);
            validatePhone(phone);
        } catch (IllegalArgumentException ex) {
            showErrorDialog(ex.getMessage(), "Invalid Organizer Data");
            return;
        }

        String sql = "UPDATE organizers SET name = ?, email = ?, phone = ? WHERE organizer_id = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name);
            ps.setString(2, email);
            ps.setString(3, phone);
            ps.setInt(4, orgID);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while updating organizer.\n" + ex.getMessage(),
                    "Database Error");
        }

        OrganizerRecord org = findOrganizer(orgID);
        if (org != null) {
            org.name = name;
            org.email = email;
            org.phone = phone;
        }
    }

    public void activateOrganizer(int orgID) {

        String sql = "UPDATE organizers SET active = 1 WHERE organizer_id = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, orgID);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while activating organizer.\n" + ex.getMessage(),
                    "Database Error");
        }

        OrganizerRecord org = findOrganizer(orgID);
        if (org != null) org.active = true;
    }

    public void suspendOrganizer(int orgID) {

        String sql = "UPDATE organizers SET active = 0 WHERE organizer_id = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, orgID);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while suspending organizer.\n" + ex.getMessage(),
                    "Database Error");
        }

        OrganizerRecord org = findOrganizer(orgID);
        if (org != null) org.active = false;
    }

    //  EVENTS 
    public void createEventForOrganizer(int organizerId, EventData eventData) {

        int newEventId = 0;

        String sql = """
                INSERT INTO events
                (name, organizer_id, category, location, volunteers, event_date, start_time, end_time, description, status)
                VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)
                """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, eventData.name);
            ps.setInt(2, organizerId);
            ps.setString(3, eventData.category);
            ps.setString(4, eventData.location);
            ps.setInt(5, eventData.volunteers);
            ps.setString(6, eventData.eventDate);
            ps.setString(7, eventData.startTime);
            ps.setString(8, eventData.endTime);
            ps.setString(9, eventData.description);
            ps.setString(10, "PENDING");

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) newEventId = rs.getInt(1);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while creating event.\n" + ex.getMessage(),
                    "Database Error");
        }

        events.add(new EventRecord(
                newEventId,
                eventData.name,
                eventData.category,
                eventData.location,
                eventData.volunteers,
                eventData.eventDate,
                eventData.startTime,
                eventData.endTime,
                eventData.description,
                "PENDING"
        ));

        System.out.println("Event created: " + eventData.name);
    }

    public void approveEvent(int eventID) {

        String sql = "UPDATE events SET status = 'APPROVED' WHERE event_id = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, eventID);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while approving event.\n" + ex.getMessage(),
                    "Database Error");
        }

        EventRecord ev = findEvent(eventID);
        if (ev != null) ev.status = "APPROVED";
    }

    public void rejectEvent(int eventID, String reason) {

        String sql = "UPDATE events SET status = 'REJECTED' WHERE event_id = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, eventID);
            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while rejecting event.\n" + ex.getMessage(),
                    "Database Error");
        }

        EventRecord ev = findEvent(eventID);
        if (ev != null) ev.status = "REJECTED";
    }

    //  REPORTS 
    public void addReportSummary(int reportID,
                                 String reportType,
                                 String reportTitle,
                                 Date dateGenerated,
                                 String generatedBy) {

        String sql = "INSERT INTO reports (report_type, report_title, date_generated, generated_by) " +
                     "VALUES (?, ?, ?, ?)";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, reportType);
            ps.setString(2, reportTitle);
            ps.setTimestamp(3, new Timestamp(dateGenerated.getTime()));
            ps.setInt(4, this.adminID);

            ps.executeUpdate();

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while adding report.\n" + ex.getMessage(),
                    "Database Error");
        }

        reports.add(new ReportSummary(reportID, reportType, reportTitle, dateGenerated, generatedBy));
    }

    public Object[][] getReportsTableData() {

        List<ReportSummary> list = new ArrayList<>();

        String sql =
                """
                SELECT r.report_id, r.report_type, r.report_title, 
                       r.date_generated, a.name AS admin_name
                FROM reports r
                JOIN admins a ON r.generated_by = a.admin_id
                """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                list.add(new ReportSummary(
                        rs.getInt("report_id"),
                        rs.getString("report_type"),
                        rs.getString("report_title"),
                        new Date(rs.getTimestamp("date_generated").getTime()),
                        rs.getString("admin_name")
                ));
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while loading reports.\n" + ex.getMessage(),
                    "Database Error");
        }

        Object[][] data = new Object[list.size()][5];

        for (int i = 0; i < list.size(); i++) {
            ReportSummary r = list.get(i);
            data[i][0] = r.reportID;
            data[i][1] = r.reportTitle;
            data[i][2] = r.reportType;
            data[i][3] = r.dateGenerated;
            data[i][4] = r.generatedBy;
        }

        return data;
    }

    //  ACCOUNT 
    public boolean login(String email, String password) {

        //  VALIDATION 
        try {
            validateEmail(email);
            validatePassword(password);
        } catch (IllegalArgumentException ex) {
            showErrorDialog(ex.getMessage(), "Invalid Login Data");
            return false;
        }

        String sql = "SELECT * FROM admins WHERE email = ? AND password = ?";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, email);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {

                    this.adminID = rs.getInt("admin_id");
                    this.name = rs.getString("name");
                    this.email = rs.getString("email");
                    this.password = rs.getString("password");
                    this.isSuperAdmin = rs.getBoolean("is_super_admin");
                    this.roleTitle = rs.getString("role_title");
                    this.lastLoginDate = new Date();

                    String update = "UPDATE admins SET last_login = ? WHERE admin_id = ?";

                    try (PreparedStatement ps2 = c.prepareStatement(update)) {
                        ps2.setTimestamp(1, new Timestamp(lastLoginDate.getTime()));
                        ps2.setInt(2, this.adminID);
                        ps2.executeUpdate();
                    }

                    return true;
                }
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while logging in.\n" + ex.getMessage(),
                    "Database Error");
        }

        showErrorDialog("Invalid email or password.", "Login Failed");
        return false;
    }

    //  TABLE HELPERS 
    public Object[][] getOrganizersTableData() {

        List<Object[]> rows = new ArrayList<>();
        organizers.clear();

        String sql = "SELECT organizer_id, name, email, phone, active, role FROM organizers";

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                OrganizerRecord o = new OrganizerRecord(
                        rs.getInt("organizer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getBoolean("active"),
                        rs.getString("role")
                );

                organizers.add(o);

                rows.add(new Object[]{
                        o.id, o.name, o.email, o.phone,
                        o.active ? "ACTIVE" : "SUSPENDED",
                        o.description
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while loading organizers.\n" + ex.getMessage(),
                    "Database Error");
        }

        Object[][] data = new Object[rows.size()][6];

        for (int i = 0; i < rows.size(); i++) data[i] = rows.get(i);

        return data;
    }

    public Object[][] getEventsTableData() {

        List<Object[]> rows = new ArrayList<>();
        events.clear();

        String sql = """
                SELECT event_id, name, category, location, volunteers,
                       event_date, start_time, end_time, description, status
                FROM events
                """;

        try (Connection c = getConnection();
             PreparedStatement ps = c.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                EventRecord e = new EventRecord(
                        rs.getInt("event_id"),
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getInt("volunteers"),
                        rs.getString("event_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("description"),
                        rs.getString("status")
                );

                events.add(e);

                rows.add(new Object[]{
                        e.id, e.name, e.category, e.location,
                        e.volunteers, e.eventDate, e.startTime,
                        e.endTime, e.description, e.status
                });
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            showErrorDialog("Database error while loading events.\n" + ex.getMessage(),
                    "Database Error");
        }

        Object[][] data = new Object[rows.size()][10];
        for (int i = 0; i < rows.size(); i++) data[i] = rows.get(i);

        return data;
    }

    //  FINDERS 
    private OrganizerRecord findOrganizer(int id) {
        for (OrganizerRecord o : organizers)
            if (o.id == id) return o;
        return null;
    }

    private EventRecord findEvent(int id) {
        for (EventRecord e : events)
            if (e.id == id) return e;
        return null;
    }

    //  INNER CLASSES 

    private static class OrganizerRecord {
        int id;
        String name;
        String email;
        String phone;
        boolean active;
        String description;

        OrganizerRecord(int id, String name, String email, String phone, boolean active, String description) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.phone = phone;
            this.active = active;
            this.description = description;
        }
    }

    private static class EventRecord {
        int id;
        String name;
        String category;
        String location;
        int volunteers;
        String eventDate;
        String startTime;
        String endTime;
        String description;
        String status;

        EventRecord(int id,
                    String name,
                    String category,
                    String location,
                    int volunteers,
                    String eventDate,
                    String startTime,
                    String endTime,
                    String description,
                    String status) {

            this.id = id;
            this.name = name;
            this.category = category;
            this.location = location;
            this.volunteers = volunteers;
            this.eventDate = eventDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.description = description;
            this.status = status;
        }
    }

    private static class ReportSummary {
        int reportID;
        String reportType;
        String reportTitle;
        Date dateGenerated;
        String generatedBy;

        ReportSummary(int reportID, String reportType, String reportTitle,
                      Date dateGenerated, String generatedBy) {

            this.reportID = reportID;
            this.reportType = reportType;
            this.reportTitle = reportTitle;
            this.dateGenerated = dateGenerated;
            this.generatedBy = generatedBy;
        }
    }

    public static class EventData {
        public String name;
        public String category;
        public String location;
        public int volunteers;
        public String eventDate;
        public String startTime;
        public String endTime;
        public String description;

        public EventData(String name,
                         String category,
                         String location,
                         int volunteers,
                         String eventDate,
                         String startTime,
                         String endTime,
                         String description) {

            this.name = name;
            this.category = category;
            this.location = location;
            this.volunteers = volunteers;
            this.eventDate = eventDate;
            this.startTime = startTime;
            this.endTime = endTime;
            this.description = description;
        }
    }

    //  GETTERS 
    public String getName() { return name; }
    public String getRoleTitle() { return roleTitle; }
    public int getAdminId() { return adminID; }

}
