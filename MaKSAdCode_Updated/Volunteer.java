package maksadpro;

import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.sql.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Volunteer extends JFrame {

    private final MaKSAdUserSystem.AuthenticatedUser user;

    private JLabel hoursLbl  = new JLabel("0.0");
    private JLabel partLbl   = new JLabel("0");
    private JTextArea log    = new JTextArea(7, 30);

    private DefaultListModel<String> interestModel = new DefaultListModel<>();
    private JTextField interestInput = new JTextField();

    private static final Color BG_MAIN     = Color.decode("#263717");
    private static final Color BG_CARD     = Color.decode("#FFFADD");
    private static final Color BTN_COLOR   = Color.decode("#74835A");
    private static final Color TEXT_DARK   = Color.decode("#1E1E1E");

    private static class ParticipationRow {
        String eventName;
        LocalDate eventDate;
        LocalTime startTime;
        LocalTime endTime;

        ParticipationRow(String n, LocalDate d, LocalTime s, LocalTime e) {
            eventName = n;
            eventDate = d;
            startTime = s;
            endTime   = e;
        }
    }

    private static class DBEvent {
        int eventId;
        String name;
        LocalDate date;
        LocalTime startTime;
        LocalTime endTime;
        String location;
        String description;
        String status;
        int volunteers;

        DBEvent(int eventId, String name, LocalDate date,
                LocalTime startTime, LocalTime endTime,
                String location, String description,
                String status, int volunteers) {

            this.eventId    = eventId;
            this.name       = name;
            this.date       = date;
            this.startTime  = startTime;
            this.endTime    = endTime;
            this.location   = location;
            this.description= description;
            this.status     = status;
            this.volunteers = volunteers;
        }

        @Override
        public String toString() {
            return name + " (" + date + ")";
        }
    }

    public Volunteer(MaKSAdUserSystem.AuthenticatedUser user) {
        this.user = user;
        initFrame();
    }

    private void initFrame() {

        setTitle("Volunteer Panel - " + user.getName());
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(940, 570);
        setLocationRelativeTo(null);

        JPanel root = new JPanel(new BorderLayout(16, 16));
        root.setBackground(BG_MAIN);
        root.setBorder(new EmptyBorder(18, 18, 18, 18));
        setContentPane(root);

        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setOpaque(false);

        JLabel welcomeLbl = new JLabel("Volunteer Dashboard — " + user.getName());
        welcomeLbl.setFont(new Font("Serif", Font.BOLD, 20));
        welcomeLbl.setForeground(Color.WHITE);

        JButton logoutBtn = makeButton("Logout");
        logoutBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        topBar.add(welcomeLbl, BorderLayout.WEST);
        topBar.add(logoutBtn, BorderLayout.EAST);

        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));

        left.add(makeStatCard("Total Hours", hoursLbl));
        left.add(Box.createVerticalStrut(16));
        left.add(makeStatCard("Joined Events", partLbl));
        left.add(Box.createVerticalStrut(16));
        left.add(makeInterestCard());

        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));

        JButton joinBtn = makeButton("Join Event");
        joinBtn.addActionListener(e -> new EventDialog());

        JButton viewBtn = makeButton("View Participation");
        viewBtn.addActionListener(e -> new ParticipationTable(user.getId()));

        right.add(joinBtn);
        right.add(Box.createVerticalStrut(16));
        right.add(viewBtn);
        right.add(Box.createVerticalStrut(16));
        right.add(makeLogCard());

        root.add(topBar, BorderLayout.NORTH);
        root.add(left, BorderLayout.WEST);
        root.add(right, BorderLayout.CENTER);

        refreshSummary();
    }

    // =================================================================
    // Event Dialog
    // =================================================================
    class EventDialog extends JDialog {

        private DefaultListModel<DBEvent> model = new DefaultListModel<>();
        private JList<DBEvent> list;

        EventDialog() {
            super(Volunteer.this, "Select Event", true);
            setSize(420, 360);
            setLocationRelativeTo(Volunteer.this);
            setLayout(new BorderLayout());

            loadEventsFromDB();
            list = new JList<>(model);

            JButton joinBtn = makeButton("Join Event");
            joinBtn.addActionListener(e -> joinSelectedEvent());

            add(new JScrollPane(list), BorderLayout.CENTER);
            add(joinBtn, BorderLayout.SOUTH);

            setVisible(true);
        }

        private void loadEventsFromDB() {
            model.clear();

            String sql = """
                SELECT event_id, name, category, location, volunteers,
                       event_date, start_time, end_time, description, status
                FROM events
                WHERE status = 'APPROVED'
                  AND name NOT IN (
                      SELECT event_name
                      FROM volunteer_participations
                      WHERE volunteer_id = ?
                  )
                ORDER BY event_date
            """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, user.getId());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    model.addElement(new DBEvent(
                            rs.getInt("event_id"),
                            rs.getString("name"),
                            rs.getDate("event_date").toLocalDate(),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime(),
                            rs.getString("location"),
                            rs.getString("description"),
                            rs.getString("status"),
                            rs.getInt("volunteers")
                    ));
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Volunteer.this,
                        "Error loading events:\n" + ex.getMessage());
            }
        }

        private void joinSelectedEvent() {
            DBEvent ev = list.getSelectedValue();
            if (ev == null) return;

            String insertSql = """
                INSERT INTO volunteer_participations
                (volunteer_id, volunteer_name, event_name, event_date,
                 role, check_in, check_out, hours, status)
                VALUES (?, ?, ?, ?, ?, NULL, NULL, NULL, 'UNSET')
            """;

            String updateEventSql = """
                UPDATE events
                SET volunteers = LEAST(volunteers + 1, 100)
                WHERE event_id = ?
            """;

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement ps1 = conn.prepareStatement(insertSql);
                ps1.setInt(1, user.getId());
                ps1.setString(2, user.getName());
                ps1.setString(3, ev.name);
                ps1.setDate(4, java.sql.Date.valueOf(ev.date));
                ps1.setString(5, "Volunteer");
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(updateEventSql);
                ps2.setInt(1, ev.eventId);
                ps2.executeUpdate();

                conn.commit();

                log.append("Joined event: " + ev.name + "\n");
                refreshSummary();
                dispose();

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Volunteer.this,
                        "Error joining:\n" + ex.getMessage());
            }
        }
    }

    // =================================================================
    // Participation Table
    // =================================================================
    class ParticipationTable extends JFrame {

        ParticipationTable(int volunteerId) {
            setTitle("Participation Records");
            setSize(650, 420);
            setLocationRelativeTo(Volunteer.this);

            String[] cols = {"Event", "Date", "Start", "End"};
            List<ParticipationRow> rowsData = loadParticipationRows(volunteerId);

            Object[][] rows = new Object[rowsData.size()][cols.length];
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("HH:mm");

            for (int i = 0; i < rowsData.size(); i++) {
                ParticipationRow p = rowsData.get(i);
                rows[i][0] = p.eventName;
                rows[i][1] = p.eventDate.toString();
                rows[i][2] = p.startTime.format(fmt);
                rows[i][3] = p.endTime.format(fmt);
            }

            JTable table = new JTable(rows, cols);
            add(new JScrollPane(table));
            setVisible(true);
        }

        private List<ParticipationRow> loadParticipationRows(int volunteerId) {
            List<ParticipationRow> list = new ArrayList<>();

            String sql = """
                SELECT p.event_name,
                       p.event_date,
                       e.start_time,
                       e.end_time
                FROM volunteer_participations p
                JOIN events e ON p.event_name = e.name
                WHERE p.volunteer_id = ?
                ORDER BY p.event_date DESC
            """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, volunteerId);
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    list.add(new ParticipationRow(
                            rs.getString("event_name"),
                            rs.getDate("event_date").toLocalDate(),
                            rs.getTime("start_time").toLocalTime(),
                            rs.getTime("end_time").toLocalTime()
                    ));
                }

            } catch (Exception ex) {
                JOptionPane.showMessageDialog(Volunteer.this,
                        "Error loading participation:\n" + ex.getMessage());
            }

            return list;
        }
    }

    // =================================================================
    // UI HELPERS (THEMED)
    // =================================================================

    private JPanel makeStatCard(String title, JLabel value) {
        JPanel p = new RoundedPanel(25);
        p.setBackground(BG_CARD);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel t = new JLabel(title);
        t.setFont(new Font("Serif", Font.BOLD, 16));
        t.setForeground(TEXT_DARK);

        value.setFont(new Font("Serif", Font.BOLD, 20));
        value.setForeground(TEXT_DARK);

        p.add(t, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);

        return p;
    }

    private JPanel makeInterestCard() {
        JPanel p = new RoundedPanel(25);
        p.setBackground(BG_CARD);
        p.setLayout(new BorderLayout(10, 10));
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel title = new JLabel("Interests");
        title.setFont(new Font("Serif", Font.BOLD, 16));

        JList<String> list = new JList<>(interestModel);

        JButton addBtn = makeButtonSmall("Add");
        addBtn.addActionListener(e -> {
            String s = interestInput.getText().trim();
            if (!s.isEmpty()) {
                interestModel.addElement(s);
                interestInput.setText("");
            }
        });

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.add(interestInput, BorderLayout.CENTER);
        bottom.add(addBtn, BorderLayout.EAST);

        p.add(title, BorderLayout.NORTH);
        p.add(new JScrollPane(list), BorderLayout.CENTER);
        p.add(bottom, BorderLayout.SOUTH);

        return p;
    }

    private JPanel makeLogCard() {
        JPanel p = new RoundedPanel(25);
        p.setBackground(BG_CARD);
        p.setLayout(new BorderLayout());
        p.setBorder(new EmptyBorder(12, 14, 12, 14));

        JLabel title = new JLabel("Activity Log");
        title.setFont(new Font("Serif", Font.BOLD, 16));

        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 13));

        p.add(title, BorderLayout.NORTH);
        p.add(new JScrollPane(log), BorderLayout.CENTER);

        return p;
    }

    // =================================================================
    // ROUNDED BUTTONS (NEW)
    // =================================================================
    private JButton makeButton(String text) {

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BTN_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Serif", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 16, 8, 16));

        return btn;
    }

    private JButton makeButtonSmall(String text) {

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BTN_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                super.paintComponent(g);
            }
        };

        btn.setFont(new Font("Serif", Font.BOLD, 13));
        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(6, 12, 6, 12));

        return btn;
    }

    // =================================================================
    // SUMMARY REFRESH
    // =================================================================
    private void refreshSummary() {
        double hours = 0.0;
        int count = 0;

        String sqlHours = "SELECT total_hours FROM volunteers WHERE volunteer_id = ?";
        String sqlCount = "SELECT COUNT(*) FROM volunteer_participations WHERE volunteer_id = ?";

        try (Connection conn = DBConnection.getConnection()) {

            PreparedStatement ps1 = conn.prepareStatement(sqlHours);
            ps1.setInt(1, user.getId());
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) hours = rs1.getDouble("total_hours");

            PreparedStatement ps2 = conn.prepareStatement(sqlCount);
            ps2.setInt(1, user.getId());
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) count = rs2.getInt(1);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading summary:\n" + ex.getMessage());
        }

        hoursLbl.setText(String.format("%.1f", hours));
        partLbl.setText(String.valueOf(count));
    }

    // =================================================================
    // ROUNDED PANEL
    // =================================================================
    class RoundedPanel extends JPanel {
        private int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            g2.dispose();
            super.paintComponent(g);
        }
    }
}