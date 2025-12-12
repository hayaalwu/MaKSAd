package maksadpro;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.HashMap;
import java.util.ArrayList;
import java.io.File;
import javax.swing.filechooser.FileNameExtensionFilter;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import com.toedter.calendar.JDateChooser;
import com.github.lgooddatepicker.components.TimePicker;
import com.github.lgooddatepicker.components.TimePickerSettings;

public class OrgClass extends JFrame {

    private final int organizerId;

    JComboBox<String> eventComboBox;
    JLabel eventCountLabel;

    private HashMap<String, String[]> eventDetails = new HashMap<>();

    private static final Color SIDEBAR_COLOR = Color.decode("#263717");
    private static final Color BUTTON_COLOR  = Color.decode("#74835A");
    private static final Color MAIN_BG       = Color.decode("#FFFADD");

    private static final Color COLOR_BG      = Color.decode("#FFFADD");
    private static final Color COLOR_DARK    = Color.decode("#263717");
    private static final Color COLOR_OLIVE   = Color.decode("#74835A");
    private static final Color COLOR_DELETE  = Color.decode("#5C3A23");

    private static final String[] EVENT_CATEGORIES = {
            "Academic / Workshops",
            "Training Programs",
            "Competitions",
            "Community Events",
            "Technology & Innovation",
            "Health & Wellness",
            "Conferences & Talks",
            "Exhibitions & Booths",
            "Environmental Activities",
            "Social Responsibility"
    };

    private static final String[] EVENT_STATUS = {
            "PENDING",
            "APPROVED",
            "CANCELED"
    };

    public OrgClass(int organizerId) {
        this.organizerId = organizerId;

        setTitle("Organizer Dashboard – MaKSAd");
        setSize(1000, 600);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        getContentPane().setBackground(MAIN_BG);
        getContentPane().setLayout(new BorderLayout());

        loadEventsFromDB();
        buildUI();

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void buildUI() {

        getContentPane().removeAll();

        JPanel sidebar = new JPanel();
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(220, getHeight()));
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 20));

        sidebar.add(makeSidebarButton("My Events", e -> {}));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(makeSidebarButton("Add Event", e -> addEvent()));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(makeSidebarButton("Manage Volunteers", e -> new ParticipationApprovalView(organizerId)));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(makeSidebarButton("Show All Events", e -> showMyEventsTable()));
        sidebar.add(Box.createVerticalGlue());

        sidebar.add(makeSidebarButton("Archive", e -> new ArchiveClass(organizerId)));
        sidebar.add(Box.createVerticalStrut(20));

        sidebar.add(makeSidebarButton("Logout", e -> {
            dispose();
            new MainWelcomeFrame().setVisible(true);
        }));

        getContentPane().add(sidebar, BorderLayout.WEST);

        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(MAIN_BG);
        content.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));

        eventCountLabel = new JLabel("Events: " + eventDetails.size());
        eventCountLabel.setFont(new Font("SansSerif", Font.BOLD, 15));
        eventCountLabel.setForeground(COLOR_DARK);

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setOpaque(false);
        headerPanel.add(eventCountLabel, BorderLayout.WEST);

        content.add(headerPanel, BorderLayout.NORTH);
        JPanel cardsPanel = new JPanel();
        cardsPanel.setLayout(new FlowLayout(FlowLayout.LEFT, 18, 18));
        cardsPanel.setBackground(MAIN_BG);

        for (String eventName : eventDetails.keySet()) {

            String[] data = eventDetails.get(eventName);

            String date = data[0];
            String location = data[1];
            String start = data[2];
            String end = data[3];
            String description = data[4];

            RoundedPanel card = new RoundedPanel(28);
            card.setPreferredSize(new Dimension(260, 210));
            card.setBackground(COLOR_OLIVE);
            card.setLayout(null);
            card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel lblTitle = new JLabel("Event: " + eventName);
            lblTitle.setFont(new Font("SansSerif", Font.BOLD, 15));
            lblTitle.setForeground(COLOR_BG);
            lblTitle.setBounds(20, 12, 230, 22);
            card.add(lblTitle);

            JLabel lblDate = new JLabel("Date: " + date);
            lblDate.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblDate.setForeground(COLOR_BG);
            lblDate.setBounds(20, 40, 230, 18);
            card.add(lblDate);

            JLabel lblLocation = new JLabel("Location: " + location);
            lblLocation.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblLocation.setForeground(COLOR_BG);
            lblLocation.setBounds(20, 60, 230, 18);
            card.add(lblLocation);

            JLabel lblTime = new JLabel("Time: " + start + " - " + end);
            lblTime.setFont(new Font("SansSerif", Font.PLAIN, 13));
            lblTime.setForeground(COLOR_BG);
            lblTime.setBounds(20, 80, 230, 18);
            card.add(lblTime);

            JLabel lblDesc = new JLabel("<html>Description: " + description + "</html>");
            lblDesc.setFont(new Font("SansSerif", Font.PLAIN, 12));
            lblDesc.setForeground(COLOR_BG);
            lblDesc.setBounds(20, 100, 230, 40);
            card.add(lblDesc);

            JPanel line = new JPanel();
            line.setBackground(COLOR_BG);
            line.setBounds(10, 145, 240, 2);
            card.add(line);

            JButton btnEdit = new JButton("Edit") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_DARK);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    super.paintComponent(g);
                }
            };
            btnEdit.setBounds(25, 155, 90, 32);
            btnEdit.setForeground(Color.WHITE);
            btnEdit.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnEdit.setContentAreaFilled(false);
            btnEdit.setBorderPainted(false);
            btnEdit.setFocusPainted(false);
            btnEdit.addActionListener(e -> new OrgClassWithEventEditor(organizerId));
            card.add(btnEdit);

            JButton btnDelete = new JButton("Delete") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                            RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_DELETE);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                    super.paintComponent(g);
                }
            };
            btnDelete.setBounds(140, 155, 90, 32);
            btnDelete.setForeground(Color.WHITE);
            btnDelete.setFont(new Font("SansSerif", Font.BOLD, 12));
            btnDelete.setContentAreaFilled(false);
            btnDelete.setBorderPainted(false);
            btnDelete.setFocusPainted(false);
            btnDelete.addActionListener(e -> {
                deleteEventFromName(eventName);
                loadEventsFromDB();
                rebuildCards();
            });
            card.add(btnDelete);

            cardsPanel.add(card);
        }

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        content.add(scroll, BorderLayout.CENTER);

        getContentPane().add(content, BorderLayout.CENTER);

        revalidate();
        repaint();
    }

    private void deleteEventFromName(String name) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM events WHERE organizer_id = ? AND name = ?"
             )) {

            ps.setInt(1, organizerId);
            ps.setString(2, name);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event deleted.");

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error deleting:\n" + e.getMessage());
        }
    }

    private void rebuildCards() {
        buildUI();
    }

    class RoundedPanel extends JPanel {
        int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }

    private JButton makeSidebarButton(String text, java.awt.event.ActionListener action) {

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(BUTTON_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                super.paintComponent(g);
            }
        };

        btn.setMaximumSize(new Dimension(200, 45));
        btn.setForeground(Color.WHITE);

        // ⬇️ هنا صغّرنا الخط بشكل منطقي (Manage Volunteers يطلع أرتب)
        btn.setFont(new Font("SansSerif", Font.BOLD, 12));

        btn.setContentAreaFilled(false);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.addActionListener(action);

        return btn;
    }

    private void loadEventsFromDB() {
        eventDetails.clear();

        String sql = """
                SELECT name, event_date, location, start_time, end_time, description
                FROM events
                WHERE organizer_id = ?
                ORDER BY event_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                eventDetails.put(
                        rs.getString("name"),
                        new String[]{
                                rs.getString("event_date"),
                                rs.getString("location"),
                                rs.getString("start_time"),
                                rs.getString("end_time"),
                                rs.getString("description")
                        }
                );
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading events:\n" + e.getMessage());
        }
    }

    // ============================================================
    //               ⭐️⭐️   ADD EVENT (UPDATED)   ⭐️⭐️
    // ============================================================

    private void addEvent() {

        JTextField nameField = new JTextField(15);
        JComboBox<String> categoryBox = new JComboBox<>(EVENT_CATEGORIES);
        JTextField locationField = new JTextField(15);
        JTextField volunteersField = new JTextField(15);
        JTextArea descField = new JTextArea(3, 15);

        JDateChooser dateChooser = new JDateChooser();
        dateChooser.setDateFormatString("yyyy-MM-dd");
        dateChooser.setPreferredSize(new Dimension(150, 25));

        TimePickerSettings tps = new TimePickerSettings();
        TimePicker startPicker = new TimePicker(tps);
        TimePicker endPicker   = new TimePicker(tps);

        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(MAIN_BG);
        panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(6, 6, 6, 6);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Event Name:"), c);
        c.gridx = 1; panel.add(nameField, c);

        y++; c.gridx = 0; c.gridy = y; panel.add(new JLabel("Category:"), c);
        c.gridx = 1; panel.add(categoryBox, c);

        y++; c.gridx = 0; c.gridy = y; panel.add(new JLabel("Location:"), c);
        c.gridx = 1; panel.add(locationField, c);

        y++; c.gridx = 0; c.gridy = y; panel.add(new JLabel("Volunteers:"), c);
        c.gridx = 1; panel.add(volunteersField, c);

        y++; c.gridx = 0; c.gridy = y; panel.add(new JLabel("Date:"), c);
        c.gridx = 1; panel.add(dateChooser, c);

        y++; c.gridx = 0; c.gridy = y; panel.add(new JLabel("Start Time:"), c);
        c.gridx = 1; panel.add(startPicker, c);

        y++; c.gridx = 0; c.gridy = y; panel.add(new JLabel("End Time:"), c);
        c.gridx = 1; panel.add(endPicker, c);

        y++; c.gridx = 0; c.gridy = y; panel.add(new JLabel("Description:"), c);
        c.gridx = 1; panel.add(new JScrollPane(descField), c);

        int result = JOptionPane.showConfirmDialog(
                this, panel, "Add Event", JOptionPane.OK_CANCEL_OPTION
        );
        if (result != JOptionPane.OK_OPTION) return;

        try {
            String name        = nameField.getText().trim();
            String category    = categoryBox.getSelectedItem().toString();
            String location    = locationField.getText().trim();
            String description = descField.getText().trim();
            int volunteers     = Integer.parseInt(volunteersField.getText().trim());

            if (dateChooser.getDate() == null) {
                JOptionPane.showMessageDialog(this, "Please select a date.");
                return;
            }
            if (startPicker.getTime() == null || endPicker.getTime() == null) {
                JOptionPane.showMessageDialog(this, "Please select time.");
                return;
            }

            java.sql.Date sqlDate = new java.sql.Date(dateChooser.getDate().getTime());

            String startStr = startPicker.getTime() + ":00";
            String endStr   = endPicker.getTime() + ":00";

            try (Connection conn = DBConnection.getConnection()) {

                String checkSql =
                        "SELECT COUNT(*) FROM events WHERE organizer_id = ? AND name = ?";

                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setInt(1, organizerId);
                    psCheck.setString(2, name);

                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next() && rsCheck.getInt(1) > 0) {
                        JOptionPane.showMessageDialog(
                                this,
                                "There is already an event with this name.",
                                "Duplicate Event",
                                JOptionPane.WARNING_MESSAGE
                        );
                        return;
                    }
                }

                String insert = """
                        INSERT INTO events
                        (organizer_id, name, category, location,
                         volunteers, event_date, start_time, end_time, description)
                        VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)
                        """;

                try (PreparedStatement ps = conn.prepareStatement(insert)) {

                    ps.setInt(1, organizerId);
                    ps.setString(2, name);
                    ps.setString(3, category);
                    ps.setString(4, location);
                    ps.setInt(5, volunteers);
                    ps.setDate(6, sqlDate);
                    ps.setString(7, startStr);
                    ps.setString(8, endStr);
                    ps.setString(9, description);

                    ps.executeUpdate();
                }
            }

            JOptionPane.showMessageDialog(this, "Event added successfully!");

            loadEventsFromDB();
            rebuildCards();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error adding event:\n" + ex.getMessage());
        }
    }

    // =================== NEW STYLE FOR "MY EVENTS" WINDOW ===================
    private void showMyEventsTable() {

        String sql = """
                SELECT name, category, location, volunteers,
                       event_date, start_time, end_time, status
                FROM events
                WHERE organizer_id = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            ResultSet rs = ps.executeQuery();

            String[] cols = {
                    "Name", "Category", "Location", "Volunteers",
                    "Date", "Start", "End", "Status"
            };

            javax.swing.table.DefaultTableModel model =
                    new javax.swing.table.DefaultTableModel(cols, 0) {
                        @Override
                        public boolean isCellEditable(int row, int col) {
                            return col == 7;
                        }
                    };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getString("name"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getInt("volunteers"),
                        rs.getDate("event_date"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time"),
                        rs.getString("status")
                });
            }

            JTable table = new JTable(model);
            table.setRowHeight(24);
            table.setGridColor(Color.LIGHT_GRAY);
            table.setBackground(Color.WHITE);
            javax.swing.table.JTableHeader header = table.getTableHeader();
            header.setBackground(COLOR_BG);
            header.setForeground(Color.decode("#1E1E1E"));
            header.setFont(new Font("Serif", Font.BOLD, 13));

            JComboBox<String> statusCombo = new JComboBox<>(EVENT_STATUS);
            table.getColumnModel()
                    .getColumn(7)
                    .setCellEditor(new DefaultCellEditor(statusCombo));

            JScrollPane scroll = new JScrollPane(table);
            scroll.setBorder(BorderFactory.createEmptyBorder());

            JPanel topBar = new JPanel(new BorderLayout());
            topBar.setBackground(SIDEBAR_COLOR);
            topBar.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel title = new JLabel("My Events");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 16));

            JPanel rightButtons = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            rightButtons.setOpaque(false);

            JButton saveBtn  = makeTopButton("Save Changes");
            JButton closeBtn = makeTopButton("Close");

            rightButtons.add(saveBtn);
            rightButtons.add(closeBtn);

            topBar.add(title, BorderLayout.WEST);
            topBar.add(rightButtons, BorderLayout.EAST);

            JFrame f = new JFrame("My Events");
            f.setSize(900, 450);
            f.setLayout(new BorderLayout());
            f.getContentPane().setBackground(MAIN_BG);
            f.add(topBar, BorderLayout.NORTH);
            f.add(scroll, BorderLayout.CENTER);
            f.setLocationRelativeTo(this);

            saveBtn.addActionListener(e -> {
                String updateSql = """
                        UPDATE events
                        SET status = ?
                        WHERE organizer_id = ? AND name = ?
                        """;

                try (Connection c2 = DBConnection.getConnection();
                     PreparedStatement ups = c2.prepareStatement(updateSql)) {

                    for (int i = 0; i < model.getRowCount(); i++) {
                        String eventName = model.getValueAt(i, 0).toString();
                        String status    = model.getValueAt(i, 7).toString();

                        ups.setString(1, status);
                        ups.setInt(2, organizerId);
                        ups.setString(3, eventName);
                        ups.addBatch();
                    }

                    ups.executeBatch();

                    JOptionPane.showMessageDialog(this,
                            "Event statuses updated successfully!",
                            "Success",
                            JOptionPane.INFORMATION_MESSAGE);

                    loadEventsFromDB();
                    rebuildCards();

                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error saving statuses:\n" + ex.getMessage(),
                            "Database Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            });

            closeBtn.addActionListener(e -> f.dispose());

            f.setVisible(true);

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error loading events:\n" + e.getMessage());
        }
    }

    private JButton makeTopButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BUTTON_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setPreferredSize(new Dimension(140, 32));
        return btn;
    }

}

// ======================================================================
//                         ArchiveClass (نفس الملف)
// ======================================================================

class ArchiveClass extends JFrame {

    private final int organizerId;

    private static final Color SIDEBAR_COLOR = Color.decode("#263717");
    private static final Color CARD_COLOR    = Color.decode("#74835A");
    private static final Color BG_COLOR      = Color.decode("#FFFADD");
    private static final Color BTN_COLOR     = Color.decode("#74835A");

    public ArchiveClass(int organizerId) {
        this.organizerId = organizerId;

        setTitle("Archive");
        setSize(1000, 650);
        setLayout(new BorderLayout());
        getContentPane().setBackground(BG_COLOR);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        add(buildSidebar(), BorderLayout.WEST);
        add(buildContent(), BorderLayout.CENTER);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildSidebar() {
        JPanel side = new JPanel();
        side.setBackground(SIDEBAR_COLOR);
        side.setPreferredSize(new Dimension(220, getHeight()));
        side.setLayout(new BoxLayout(side, BoxLayout.Y_AXIS));
        side.setBorder(BorderFactory.createEmptyBorder(25, 20, 25, 25));

        JLabel title = new JLabel("<html><span style='color:#FFFADD;font-size:12px;'> Here are the events<br>you completed</span></html>");
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        side.add(title);

        side.add(Box.createVerticalGlue());

        side.add(makeSidebarButton("Attendance of Volunteers", e -> new ParticipationView(organizerId)));
        side.add(Box.createVerticalStrut(15));

        side.add(makeSidebarButton("Back", e -> dispose()));

        return side;
    }

    private JButton makeSidebarButton(String text, ActionListener action) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(BTN_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);
                super.paintComponent(g);
            }
        };
        btn.setAlignmentX(Component.LEFT_ALIGNMENT);
        btn.setMaximumSize(new Dimension(180, 40));
        btn.setForeground(Color.WHITE);

        // ⬇️ صغّرنا الخط شوي عشان النص ياخذ مساحة أقل وما يطلع ... بسهولة
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));

        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.addActionListener(action);
        return btn;
    }

    private JPanel buildContent() {
        JPanel content = new JPanel(new BorderLayout());
        content.setBackground(BG_COLOR);
        content.setBorder(BorderFactory.createEmptyBorder(25, 25, 25, 25));
        JLabel header = new JLabel("Events:");
        header.setFont(new Font("SansSerif", Font.BOLD, 20));
        header.setForeground(Color.decode("#263717"));
        content.add(header, BorderLayout.NORTH);

        JPanel cardsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 20, 20));
        cardsPanel.setBackground(BG_COLOR);

        ArrayList<EventData> doneEvents = loadCompletedEvents();
        header.setText("Events : " + doneEvents.size());

        for (EventData ev : doneEvents) {

            RoundedPanel card = new RoundedPanel(26);
            card.setPreferredSize(new Dimension(260, 200));
            card.setBackground(CARD_COLOR);
            card.setLayout(null);
            card.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JLabel t = new JLabel("Event Title: " + ev.name);
            t.setForeground(new Color(0xFFFADD));
            t.setFont(new Font("SansSerif", Font.BOLD, 15));
            t.setBounds(20, 10, 220, 18);
            card.add(t);

            JLabel d = new JLabel("Date : " + ev.date);
            d.setForeground(new Color(0xFFFADD));
            d.setFont(new Font("SansSerif", Font.PLAIN, 13));
            d.setBounds(20, 35, 220, 18);
            card.add(d);

            JLabel loc = new JLabel("Location: " + ev.location);
            loc.setForeground(new Color(0xFFFADD));
            loc.setFont(new Font("SansSerif", Font.PLAIN, 13));
            loc.setBounds(20, 55, 220, 18);
            card.add(loc);

            JLabel time = new JLabel("Time : " + ev.start + " - " + ev.end);
            time.setForeground(new Color(0xFFFADD));
            time.setFont(new Font("SansSerif", Font.PLAIN, 13));
            time.setBounds(20, 75, 220, 18);
            card.add(time);

            JLabel desc = new JLabel("<html>Description: " + ev.desc + "</html>");
            desc.setForeground(new Color(0xFFFADD));
            desc.setFont(new Font("SansSerif", Font.PLAIN, 12));
            desc.setBounds(20, 95, 220, 40);
            card.add(desc);

            JButton uploadBtn = new JButton("Upload your documentation") {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(SIDEBAR_COLOR);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                    super.paintComponent(g);
                }
            };
            uploadBtn.setBounds(25, 145, 200, 32);
            uploadBtn.setForeground(Color.WHITE);
            uploadBtn.setFont(new Font("SansSerif", Font.BOLD, 12));
            uploadBtn.setContentAreaFilled(false);
            uploadBtn.setBorderPainted(false);
            uploadBtn.setFocusPainted(false);
            uploadBtn.addActionListener(e -> showUploadDialog(ev));

            card.add(uploadBtn);
            cardsPanel.add(card);
        }

        JScrollPane scroll = new JScrollPane(cardsPanel);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(14);

        content.add(scroll, BorderLayout.CENTER);
        return content;
    }

    private void showUploadDialog(EventData event) {

        final File[] selectedFile = new File[1];

        JDialog dialog = new JDialog(this, "Upload your documentation", true);
        dialog.setLayout(new BorderLayout());
        dialog.getContentPane().setBackground(SIDEBAR_COLOR);
        dialog.setSize(420, 320);
        dialog.setLocationRelativeTo(this);

        JLabel title = new JLabel("Upload your documentation", SwingConstants.CENTER);
        title.setForeground(new Color(0xFFFADD));
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(BorderFactory.createEmptyBorder(10, 10, 5, 10));
        dialog.add(title, BorderLayout.NORTH);

        JPanel center = new JPanel(new GridLayout(3, 1, 10, 12));
        center.setBackground(SIDEBAR_COLOR);
        center.setBorder(BorderFactory.createEmptyBorder(20, 80, 20, 80));

        JButton chooseFile  = makeDialogButton("Choose from files");
        JButton choosePhoto = makeDialogButton("Choose photo or video");
        JButton scanDoc     = makeDialogButton("Scan document");

        center.add(chooseFile);
        center.add(choosePhoto);
        center.add(scanDoc);

        dialog.add(center, BorderLayout.CENTER);

        chooseFile.addActionListener(e -> {
            File f = openFileChooser();
            if (f != null) {
                selectedFile[0] = f;
                JOptionPane.showMessageDialog(this, "Selected file: " + f.getName());
            }
        });

        choosePhoto.addActionListener(e -> {
            File f = openPhotoChooser();
            if (f != null) {
                selectedFile[0] = f;
                JOptionPane.showMessageDialog(this, "Selected photo/video: " + f.getName());
            }
        });

        scanDoc.addActionListener(e ->
                JOptionPane.showMessageDialog(this, "This feature isn’t supported on computers")
        );

        JPanel bottom = new JPanel(new FlowLayout(FlowLayout.CENTER, 20, 10));
        bottom.setOpaque(false);

        JButton okBtn     = makeBottomButton("OK");
        JButton cancelBtn = makeBottomButton("Cancel");

        bottom.add(okBtn);
        bottom.add(cancelBtn);
        dialog.add(bottom, BorderLayout.SOUTH);

        okBtn.addActionListener(e -> {
            if (selectedFile[0] != null) {
                try {
                    saveFileForEvent(event, selectedFile[0]);
                    JOptionPane.showMessageDialog(this,
                            "File uploaded and saved for event: " + event.name);
                    dialog.dispose();
                } catch (Exception ex) {
                    JOptionPane.showMessageDialog(this,
                            "Error while saving file:\n" + ex.getMessage(),
                            "Upload Error",
                            JOptionPane.ERROR_MESSAGE);
                }
            } else {
                JOptionPane.showMessageDialog(this, "No file selected.");
            }
        });

        cancelBtn.addActionListener(e -> dialog.dispose());

        dialog.setVisible(true);
    }

    private JButton makeDialogButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 22, 22);
                super.paintComponent(g);
            }
        };
        b.setForeground(Color.WHITE);

        // ⬇️ صغّرنا الخط لكل أزرار الاختيار بالدialog (Choose / photo / scan)
        b.setFont(new Font("SansSerif", Font.BOLD, 11));

        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        return b;
    }

    private JButton makeBottomButton(String text) {
        JButton b = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(CARD_COLOR);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);
                super.paintComponent(g);
            }
        };
        b.setForeground(Color.WHITE);

        // ⬇️ صغّرنا الخط شوية لزر OK و Cancel
        b.setFont(new Font("SansSerif", Font.BOLD, 12));

        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setPreferredSize(new Dimension(80, 34));
        return b;
    }

    private File openFileChooser() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        return (returnValue == JFileChooser.APPROVE_OPTION)
                ? fileChooser.getSelectedFile()
                : null;
    }

    private File openPhotoChooser() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileFilter(new FileNameExtensionFilter(
                "Image and Video Files", "jpg", "jpeg", "png", "gif", "mp4", "avi"));
        int returnValue = fileChooser.showOpenDialog(this);
        return (returnValue == JFileChooser.APPROVE_OPTION)
                ? fileChooser.getSelectedFile()
                : null;
    }

    private void saveFileForEvent(EventData event, File sourceFile) throws Exception {
        File uploadDir = new File("uploads");
        if (!uploadDir.exists()) uploadDir.mkdirs();

        String safeEventName = event.name.replaceAll("[^a-zA-Z0-9-_\\.]", "_");
        String targetFileName = safeEventName + "_" + sourceFile.getName();

        File targetFile = new File(uploadDir, targetFileName);
        Files.copy(sourceFile.toPath(), targetFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

        System.out.println("Saved file to: " + targetFile.getAbsolutePath());
    }

    private ArrayList<EventData> loadCompletedEvents() {
        ArrayList<EventData> list = new ArrayList<>();

        String sql = """
                SELECT name, location, event_date, start_time, end_time, description
                FROM events
                WHERE organizer_id = ?
                  AND status = 'APPROVED'
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, organizerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new EventData(
                        rs.getString("name"),
                        rs.getString("location"),
                        rs.getString("event_date"),
                        rs.getString("start_time"),
                        rs.getString("end_time"),
                        rs.getString("description")
                ));
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, e.getMessage());
        }

        return list;
    }

    static class EventData {
        String name, location, date, start, end, desc;

        EventData(String n, String loc, String d, String s, String e, String de) {
            name = n;
            location = loc;
            date = d;
            start = s;
            end = e;
            desc = de;
        }
    }

    static class RoundedPanel extends JPanel {
        private int radius;

        RoundedPanel(int radius) {
            this.radius = radius;
            setOpaque(false);
        }
        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);
            super.paintComponent(g);
        }
    }
}
