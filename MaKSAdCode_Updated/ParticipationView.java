package maksadpro;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.regex.Pattern;

public class ParticipationView extends JFrame {

    private final int organizerId;

    private JTextField searchField;
    private JTable table;
    private DefaultTableModel model;

    // ====== THEME COLORS ======
    private static final Color BG_MAIN   = Color.decode("#263717");
    private static final Color BG_CARD   = Color.decode("#FFFADD");
    private static final Color BTN_COLOR = Color.decode("#74835A");
    private static final Color TEXT_DARK = Color.decode("#1E1E1E");

    private static final String[] STATUS_OPTIONS = {
            "PRESENT", "ABSENT", "CANCELED", "UNSET"
    };

    private final DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    public ParticipationView(int organizerId) {
        this.organizerId = organizerId;

        setTitle("Volunteer Participation Records");
        setSize(1100, 600);
        setLayout(new BorderLayout());

        getContentPane().setBackground(BG_MAIN);

        buildTopBar();
        buildTable();
        loadParticipationFromDB();

        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    // -----------------------------------------------------------
    // 1) Top Bar
    // -----------------------------------------------------------
    private void buildTopBar() {

        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(BG_MAIN);
        top.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel left = new JPanel(new FlowLayout(FlowLayout.LEFT));
        left.setOpaque(false);

        JLabel sLbl = new JLabel("Search:");
        sLbl.setForeground(Color.WHITE);

        searchField = new JTextField(20);

        left.add(sLbl);
        left.add(searchField);

        JPanel right = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        right.setOpaque(false);

        JButton saveBtn = makeRoundButton("Save Changes");
        saveBtn.setPreferredSize(new Dimension(150, 32));
        saveBtn.addActionListener(e -> saveChangesToDatabase());

        JButton backBtn = makeRoundButton("Back");
        backBtn.addActionListener(e -> dispose());

        right.add(saveBtn);
        right.add(backBtn);

        top.add(left, BorderLayout.WEST);
        top.add(right, BorderLayout.EAST);

        add(top, BorderLayout.NORTH);
    }

    // -----------------------------------------------------------
    // 2) Table
    // -----------------------------------------------------------
    private void buildTable() {

        String[] cols = {
                "Volunteer ID",
                "Volunteer Name",
                "Event Name",
                "Event Date",
                "Check-in",
                "Check-out",
                "Hours",
                "Role",
                "Status"
        };

        model = new DefaultTableModel(cols, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return !(col == 0 || col == 1 || col == 2 || col == 3);
            }
        };

        table = new JTable(model);
        table.setRowHeight(24);
        table.setGridColor(Color.GRAY);

        table.getTableHeader().setBackground(BG_CARD);
        table.getTableHeader().setForeground(TEXT_DARK);
        table.getTableHeader().setFont(new Font("Serif", Font.BOLD, 13));

        table.setBackground(Color.WHITE);

        JComboBox<String> statusCombo = new JComboBox<>(STATUS_OPTIONS);
        table.getColumnModel().getColumn(8).setCellEditor(new DefaultCellEditor(statusCombo));

        // SEARCH
        TableRowSorter<TableModel> sorter = new TableRowSorter<>(model);
        table.setRowSorter(sorter);

        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {

            void filter() {
                sorter.setRowFilter(RowFilter.regexFilter(
                        "(?i)" + Pattern.quote(searchField.getText()),
                        1, 2
                ));
            }

            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });

        JScrollPane scroll = new JScrollPane(table);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        add(scroll, BorderLayout.CENTER);
    }

    // -----------------------------------------------------------
    // 3) Load Participation Data
    // -----------------------------------------------------------
    private void loadParticipationFromDB() {

        model.setRowCount(0);

        String sql = """
            SELECT 
                vp.volunteer_id,
                vp.volunteer_name,
                vp.event_name,
                vp.event_date,

                COALESCE(vp.check_in,  e.start_time) AS resolved_check_in,
                COALESCE(vp.check_out, e.end_time)   AS resolved_check_out,

                vp.hours,
                vp.role,
                vp.status
            FROM volunteer_participations vp
            JOIN events e 
                ON  vp.event_name = e.name
                AND vp.event_date = e.event_date
            ORDER BY vp.event_date DESC
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                Timestamp ci = rs.getTimestamp("resolved_check_in");
                Timestamp co = rs.getTimestamp("resolved_check_out");

                Double hoursVal = null;

                Object hoursObj = rs.getObject("hours");
                if (hoursObj != null) {
                    hoursVal = rs.getDouble("hours");
                } else if (ci != null && co != null && co.after(ci)) {
                    long minutes = (co.getTime() - ci.getTime()) / 60000;
                    hoursVal = minutes / 60.0;
                }

                model.addRow(new Object[]{
                        rs.getInt("volunteer_id"),
                        rs.getString("volunteer_name"),
                        rs.getString("event_name"),
                        rs.getDate("event_date").toString(),
                        (ci == null ? "" : dtf.format(ci.toLocalDateTime())),
                        (co == null ? "" : dtf.format(co.toLocalDateTime())),
                        (hoursVal == null ? "" : hoursVal),
                        rs.getString("role"),
                        rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading participation data:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    // -----------------------------------------------------------
    // 4) Save Changes
    // -----------------------------------------------------------
    private void saveChangesToDatabase() {

        String sql = """
            UPDATE volunteer_participations
            SET role=?, status=?, check_in=?, check_out=?, hours=?
            WHERE volunteer_id=? AND event_name=? AND event_date=?
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < model.getRowCount(); i++) {

                Object volIdObj    = model.getValueAt(i, 0);
                Object eventNameObj= model.getValueAt(i, 2);
                Object eventDateObj= model.getValueAt(i, 3);
                Object checkInObj  = model.getValueAt(i, 4);
                Object checkOutObj = model.getValueAt(i, 5);
                Object roleObj     = model.getValueAt(i, 7);
                Object statusObj   = model.getValueAt(i, 8);

                if (volIdObj == null || eventNameObj == null || eventDateObj == null) {
                    showValidationError("Missing key data in row " + (i + 1) + ".");
                    return;
                }

                int volunteerId;
                try {
                    volunteerId = Integer.parseInt(volIdObj.toString());
                } catch (NumberFormatException ex) {
                    showValidationError("Invalid volunteer ID in row " + (i + 1) + ".");
                    return;
                }

                String eventName = eventNameObj.toString();
                String eventDate = eventDateObj.toString();

                String checkInStr  = checkInObj  == null ? "" : checkInObj.toString().trim();
                String checkOutStr = checkOutObj == null ? "" : checkOutObj.toString().trim();
                String role
                        = roleObj == null ? "" : roleObj.toString().trim();
                String status
                        = statusObj == null ? "" : statusObj.toString().trim();

                Timestamp ci = null;
                if (!checkInStr.isEmpty()) {
                    ci = parseTimestampStrict(checkInStr, "Check-in", i);
                    if (ci == null) return;
                }

                Timestamp co = null;
                if (!checkOutStr.isEmpty()) {
                    co = parseTimestampStrict(checkOutStr, "Check-out", i);
                    if (co == null) return;
                }

                Double hours = null;
                if (ci != null && co != null) {
                    if (!co.after(ci)) {
                        showValidationError("Check-out must be after Check-in (row " + (i + 1) + ").");
                        return;
                    }
                    long minutes = (co.getTime() - ci.getTime()) / 60000;
                    hours = minutes / 60.0;
                }

                ps.setString(1, role);
                ps.setString(2, status);
                ps.setTimestamp(3, ci);
                ps.setTimestamp(4, co);

                if (hours != null) ps.setDouble(5, hours);
                else ps.setNull(5, Types.DOUBLE);

                ps.setInt(6, volunteerId);
                ps.setString(7, eventName);
                ps.setString(8, eventDate);

                ps.addBatch();
            }

            ps.executeBatch();

            updateVolunteerHours(conn);

            JOptionPane.showMessageDialog(this,
                    "Changes saved successfully!",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);

            loadParticipationFromDB();

        } catch (SQLException ex) {
            JOptionPane.showMessageDialog(this,
                    "Saving error:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Timestamp parseTimestampStrict(String s, String fieldName, int row) {
        try {
            LocalDateTime dt = LocalDateTime.parse(s, dtf);
            return Timestamp.valueOf(dt);
        } catch (Exception ex) {
            showValidationError(fieldName + " must be yyyy-MM-dd HH:mm (row " + (row + 1) + ")");
            return null;
        }
    }

    private void updateVolunteerHours(Connection conn) throws SQLException {

        String sql = """
            UPDATE volunteers
            SET total_hours = COALESCE((
                SELECT SUM(hours)
                FROM volunteer_participations
                WHERE volunteer_id = volunteers.volunteer_id
            ), 0)
        """;

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.executeUpdate();
        }
    }

    private JButton makeRoundButton(String text) {

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BTN_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
            }
        };

        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("Serif", Font.BOLD, 14));

        return btn;
    }

    public static void main(String[] args) {

        SwingUtilities.invokeLater(() -> {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT organizer_id FROM organizers ORDER BY organizer_id LIMIT 1"
                 );
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    int organizerId = rs.getInt("organizer_id");
                    new ParticipationView(organizerId).setVisible(true);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "No organizers found in DB",
                            "Info",
                            JOptionPane.WARNING_MESSAGE);
                }

            } catch (SQLException ex) {
                JOptionPane.showMessageDialog(null,
                        "Error:\n" + ex.getMessage(),
                        "Database Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });
    }
}