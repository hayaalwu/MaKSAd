package maksadpro;

import java.sql.*;
import java.time.LocalDateTime;

public class Report {

    private int reportID;
    private String reportType;
    private String reportTitle;
    private LocalDateTime dateGenerated;
    private int generatedBy;     // admin_id (FK)

    // ================= CONSTRUCTOR =================
    public Report(String reportType, int adminId) {
        this.reportType = reportType;
        this.generatedBy = adminId;
        this.dateGenerated = LocalDateTime.now();
    }

    // ================= GETTERS / SETTERS =================
    public int getReportID() {
        return reportID;
    }

    public String getReportType() {
        return reportType;
    }

    public String getReportTitle() {
        return reportTitle;
    }

    public void setReportTitle(String title) {
        this.reportTitle = title;
    }

    public LocalDateTime getDateGenerated() {
        return dateGenerated;
    }

    public int getGeneratedBy() {
        return generatedBy;
    }

    // ============================================================
    // INSERT INTO DATABASE (مشترك لكل الأنواع)
    // ============================================================
    private void insertBaseReportRecord() throws SQLException {

        String sql = """
            INSERT INTO reports (report_type, report_title, date_generated, generated_by)
            VALUES (?, ?, ?, ?)
        """;

        Connection conn = DBConnection.getConnection();
        if (conn == null)
            throw new SQLException("Database connection failed.");

        try (conn;
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, reportType);
            ps.setString(2, reportTitle);
            ps.setTimestamp(3, Timestamp.valueOf(dateGenerated));
            ps.setInt(4, generatedBy);

            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    this.reportID = rs.getInt(1);
                }
            }
        }
    }

    // ============================================================
    // GENERAL REPORT
    // ============================================================
    public void generateGeneralReport() {
        try {
            insertBaseReportRecord();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    // ============================================================
    // TIMED REPORT
    // ============================================================
    public void generateTimedReport(String rangeStart, String rangeEnd) {
        try {
            insertBaseReportRecord();
            insertTimedDetails(rangeStart, rangeEnd);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void insertTimedDetails(String start, String end) throws SQLException {

        String sql = """
            INSERT INTO report_timed_details (report_id, range_start, range_end)
            VALUES (?, ?, ?)
        """;

        Connection conn = DBConnection.getConnection();
        if (conn == null)
            throw new SQLException("DB connection failed.");

        try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportID);
            ps.setString(2, start);
            ps.setString(3, end);
            ps.executeUpdate();
        }
    }

    // ============================================================
    // CATEGORY REPORT
    // ============================================================
    public void generateCategoryReport(String categoryName) {
        try {
            insertBaseReportRecord();
            insertCategoryDetails(categoryName);
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void insertCategoryDetails(String category) throws SQLException {

        String sql = """
            INSERT INTO report_category_details (report_id, category_name)
            VALUES (?, ?)
        """;

        Connection conn = DBConnection.getConnection();
        if (conn == null)
            throw new SQLException("DB connection failed.");

        try (conn; PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, reportID);
            ps.setString(2, category);
            ps.executeUpdate();
        }
    }
}