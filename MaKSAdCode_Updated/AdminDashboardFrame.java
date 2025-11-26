package maksadpro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.sql.*;
import java.net.URI;
import java.security.SecureRandom; // <--- للباسورد العشوائي
import java.awt.Desktop;          // لاستخدام Desktop.getDesktop()

// LGoodDatePicker
import com.github.lgooddatepicker.components.DatePicker;
import com.github.lgooddatepicker.components.TimePicker;

public class AdminDashboardFrame extends JFrame {

    // ================= THEME COLORS =================
    private static final Color COLOR_BACKGROUND = Color.decode("#FFFADD");
    private static final Color COLOR_DARK_GREEN = Color.decode("#263717");
    private static final Color COLOR_OLIVE      = Color.decode("#74835A");
    private static final Color COLOR_CARD_BG    = new Color(0xF6F1DD);

    private final Admin admin;

    private CardLayout cardLayout;
    private JPanel mainPanel;

    private JTable organizerTable;
    private JTable eventTable;
    private JTable adminTable;   // جدول الأدمنز

    // واجهة التقارير (نمسك نسخة واحدة فقط)
    private ReportFrame reportFrame;

    public AdminDashboardFrame(Admin admin) {
        this.admin = admin;

        setTitle("MaKSAd – Admin Dashboard");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 720);
        setLocationRelativeTo(null);

        getContentPane().setBackground(COLOR_BACKGROUND);

        initLayout();
        setVisible(true);
    }

    // ======================= RANDOM PASSWORD =======================

    private String generateRandomPassword() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(10);
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(random.nextInt(chars.length())));
        }
        return sb.toString();
    }

    // ======================= LAYOUT =======================

    private void initLayout() {

        getContentPane().setLayout(new BorderLayout());

        // ---------- TOP BAR ----------
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(COLOR_DARK_GREEN);
        topBar.setBorder(BorderFactory.createEmptyBorder(10, 25, 10, 25));

        JLabel titleLabel = new JLabel(
                "We’re glad to have you as part of MaKSAd  –  Admin Dashboard",
                SwingConstants.LEFT
        );
        titleLabel.setForeground(Color.WHITE);
        titleLabel.setFont(new Font("Serif", Font.BOLD, 20));

        topBar.add(titleLabel, BorderLayout.WEST);
        getContentPane().add(topBar, BorderLayout.NORTH);

        // ---------- LEFT MENU ----------
        JPanel menu = new JPanel();
        menu.setBackground(COLOR_DARK_GREEN);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        menu.setPreferredSize(new Dimension(210, getHeight()));

        JLabel logo = new JLabel();
        loadImage(logo, "/maksadpro/MaKSAdPH/MaKSAdLogo.png", 110, 110);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton btnOverview   = createMenuButton("Overview");
        JButton btnOrganizers = createMenuButton("Organizers");
        JButton btnEvents     = createMenuButton("Events");
        JButton btnReports    = createMenuButton("Reports");
        JButton btnAdmins     = createMenuButton("Admins");
        JButton btnLogout     = createMenuButton("Logout");

        menu.add(btnOverview);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnOrganizers);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnEvents);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnReports);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnAdmins);
        menu.add(Box.createVerticalGlue());
        menu.add(logo);
        menu.add(Box.createVerticalStrut(15));
        menu.add(btnLogout);

        // ---------- MAIN PANEL ----------
        cardLayout = new CardLayout();
        mainPanel  = new JPanel(cardLayout);
        mainPanel.setBackground(COLOR_BACKGROUND);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        mainPanel.add(buildOverviewPanel(),   "overview");
        mainPanel.add(buildOrganizersPanel(), "organizers");
        mainPanel.add(buildEventsPanel(),     "events");
        mainPanel.add(buildAdminsPanel(),     "admins");

        getContentPane().add(menu, BorderLayout.WEST);
        getContentPane().add(mainPanel, BorderLayout.CENTER);

        // ---------- ACTIONS ----------
        btnOverview.addActionListener(e -> cardLayout.show(mainPanel, "overview"));

        btnOrganizers.addActionListener(e -> {
            reloadOrganizersTable();
            cardLayout.show(mainPanel, "organizers");
        });

        btnEvents.addActionListener(e -> {
            reloadEventsTable();
            cardLayout.show(mainPanel, "events");
        });

        // ======= جزء الـ Reports (نسخة واحدة فقط من ReportFrame) =======
        btnReports.addActionListener(e -> {
            try {
                // لو ما في واجهة تقارير أو مسكّرة -> نفتح جديدة
                if (reportFrame == null || !reportFrame.isDisplayable()) {
                    reportFrame = new ReportFrame(admin);
                    reportFrame.setLocationRelativeTo(this);
                }

                // نضمن إنها ظاهرة
                reportFrame.setVisible(true);
                reportFrame.toFront();
                reportFrame.requestFocus();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        this,
                        "Error opening Reports screen:\n" + ex.getMessage(),
                        "Reports Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });

        btnAdmins.addActionListener(e -> {
            if (checkSuperAdminAccess()) {
                reloadAdminsTable();
                cardLayout.show(mainPanel, "admins");
            }
        });

        btnLogout.addActionListener(e -> {
            dispose();
            new MainWelcomeFrame().setVisible(true);
        });
    }

    // ======================= PANELS =======================

    private JPanel buildOverviewPanel() {

        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(COLOR_BACKGROUND);

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(25, 30, 25, 30));

        JLabel lbl = new JLabel(
                "<html><h2>Welcome, " + admin.getName() +
                        "</h2><p>Role: " + admin.getRoleTitle() +
                        "<br/>Use the menu on the left to manage organizers and events.</p></html>"
        );
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 15));

        card.add(lbl, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildOrganizersPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BACKGROUND);

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Organizers Management");
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(COLOR_DARK_GREEN);

        card.add(header, BorderLayout.NORTH);

        organizerTable = new JTable();
        organizerTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{"ID", "Name", "Email", "Phone", "Status", "Role"}
        ));
        styleTable(organizerTable);

        card.add(new JScrollPane(organizerTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttons.setOpaque(false);

        JButton btnAdd      = createPrimaryButton("Add Organizer");
        JButton btnEdit     = createPrimaryButton("Edit");
        JButton btnActivate = createPrimaryButton("Activate");
        JButton btnSuspend  = createPrimaryButton("Suspend");

        buttons.add(btnAdd);
        buttons.add(btnEdit);
        buttons.add(btnActivate);
        buttons.add(btnSuspend);

        card.add(buttons, BorderLayout.SOUTH);
        panel.add(card, BorderLayout.CENTER);

        btnAdd.addActionListener(e -> onAddOrganizer());
        btnEdit.addActionListener(e -> onEditOrganizer());
        btnActivate.addActionListener(e -> onChangeOrganizerStatus(true));
        btnSuspend.addActionListener(e -> onChangeOrganizerStatus(false));

        return panel;
    }

    private JPanel buildEventsPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BACKGROUND);

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Events Management");
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(COLOR_DARK_GREEN);

        card.add(header, BorderLayout.NORTH);

        eventTable = new JTable();
        eventTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "ID",
                        "Event Name",
                        "Organizer ID",
                        "Category",
                        "Location",
                        "Volunteers",
                        "Event Date",
                        "Start Time",
                        "End Time",
                        "Description",
                        "Status"
                }
        ));
        styleTable(eventTable);

        card.add(new JScrollPane(eventTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttons.setOpaque(false);

        JButton btnCreate  = createPrimaryButton("Create Event");
        JButton btnApprove = createPrimaryButton("Approve");
        JButton btnReject  = createPrimaryButton("Reject");

        buttons.add(btnCreate);
        buttons.add(btnApprove);
        buttons.add(btnReject);

        card.add(buttons, BorderLayout.SOUTH);
        panel.add(card, BorderLayout.CENTER);

        btnCreate.addActionListener(e -> onCreateEvent());
        btnApprove.addActionListener(e -> onApproveEvent());
        btnReject.addActionListener(e -> onRejectEvent());

        return panel;
    }

    // ======== Admins Panel =========
    private JPanel buildAdminsPanel() {

        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(COLOR_BACKGROUND);

        JPanel card = new JPanel(new BorderLayout(10, 10));
        card.setBackground(COLOR_CARD_BG);
        card.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel("Admins Management");
        header.setFont(new Font("SansSerif", Font.BOLD, 18));
        header.setForeground(COLOR_DARK_GREEN);

        card.add(header, BorderLayout.NORTH);

        adminTable = new JTable();
        adminTable.setModel(new DefaultTableModel(
                new Object[][]{},
                new String[]{
                        "Admin ID",
                        "Name",
                        "Email",
                        "Role Title",
                        "Super Admin",
                        "Last Login"
                }
        ));
        styleTable(adminTable);

        card.add(new JScrollPane(adminTable), BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 5));
        buttons.setOpaque(false);

        JButton btnAddAdmin = createPrimaryButton("Add Admin");
        buttons.add(btnAddAdmin);

        card.add(buttons, BorderLayout.SOUTH);
        panel.add(card, BorderLayout.CENTER);

        btnAddAdmin.addActionListener(e -> onAddAdmin());

        return panel;
    }

    // ======================= STYLE HELPERS =======================

    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(COLOR_OLIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 40));
        return btn;
    }

    private JButton createPrimaryButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                        RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(COLOR_OLIVE);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 20, 20);

                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setPreferredSize(new Dimension(130, 36));
        return btn;
    }

    private void styleTable(JTable table) {
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.setBackground(Color.WHITE);
        table.setGridColor(new Color(230, 230, 230));
        table.setSelectionBackground(new Color(0xD2DEC1));
        table.setSelectionForeground(Color.BLACK);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));

        JTableHeader header = table.getTableHeader();
        header.setBackground(COLOR_DARK_GREEN);
        header.setForeground(Color.WHITE);
        header.setFont(new Font("SansSerif", Font.BOLD, 12));
    }

    private void loadImage(JLabel label, String path, int w, int h) {
        try {
            java.net.URL url = getClass().getResource(path);
            if (url != null) {
                ImageIcon icon = new ImageIcon(url);
                Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
                label.setIcon(new ImageIcon(scaled));
            } else {
                label.setText("Missing: " + path);
            }
        } catch (Exception e) {
            label.setText("Error");
        }
    }

    // ======================= VALIDATION HELPERS =======================

    private void showValidationError(String message) {
        JOptionPane.showMessageDialog(
                this,
                message,
                "Validation Error",
                JOptionPane.ERROR_MESSAGE
        );
    }

    private void validateOrganizerName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name is required.");
        }
    }

    private void validateEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email is required.");
        }
        if (!email.contains("@")) {
            throw new IllegalArgumentException("Email must contain '@'.");
        }
    }

    private void validatePhone(String phone) {
        if (phone == null || phone.trim().isEmpty()) {
            throw new IllegalArgumentException("Phone is required.");
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

    private void validateEventBasic(String eventName, String location, int volunteers) {
        if (eventName == null || eventName.trim().isEmpty()) {
            throw new IllegalArgumentException("Event name is required.");
        }
        if (location == null || location.trim().isEmpty()) {
            throw new IllegalArgumentException("Location is required.");
        }
        if (volunteers <= 0) {
            throw new IllegalArgumentException("Volunteers must be a positive integer.");
        }
    }

    // ======================= LOADERS =======================

    private void reloadOrganizersTable() {
        DefaultTableModel model = (DefaultTableModel) organizerTable.getModel();
        model.setRowCount(0);

        String sql = """
                SELECT organizer_id, name, email, phone, active, role
                FROM organizers
                ORDER BY organizer_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                model.addRow(new Object[]{
                        rs.getInt("organizer_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("phone"),
                        rs.getBoolean("active") ? "ACTIVE" : "SUSPENDED",
                        rs.getString("role")
                });
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void reloadEventsTable() {
        DefaultTableModel model = (DefaultTableModel) eventTable.getModel();
        model.setRowCount(0);

        String sql = """
                SELECT event_id, name, organizer_id, category, location, volunteers,
                       event_date, start_time, end_time, description, status
                FROM events
                ORDER BY event_id DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {

                model.addRow(new Object[]{
                        rs.getInt("event_id"),
                        rs.getString("name"),
                        rs.getInt("organizer_id"),
                        rs.getString("category"),
                        rs.getString("location"),
                        rs.getInt("volunteers"),
                        rs.getDate("event_date"),
                        rs.getTime("start_time"),
                        rs.getTime("end_time"),
                        rs.getString("description"),
                        rs.getString("status")
                });
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void reloadAdminsTable() {
        DefaultTableModel model = (DefaultTableModel) adminTable.getModel();
        model.setRowCount(0);

        String sql = """
                SELECT admin_id, name, email, role_title, is_super_admin, last_login
                FROM admins
                ORDER BY admin_id
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("admin_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("role_title"),
                        rs.getBoolean("is_super_admin") ? "Yes" : "No",
                        rs.getTimestamp("last_login")
                });
            }

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ======================= ACTIONS =======================

    private void onAddOrganizer() {
        JTextField tfName  = new JTextField();
        JTextField tfPhone = new JTextField();

        Object[] inputs = {
                "Name:",  tfName,
                "Phone:", tfPhone
        };

        int result = JOptionPane.showConfirmDialog(this, inputs,
                "Add Organizer", JOptionPane.OK_CANCEL_OPTION);

        if (result != JOptionPane.OK_OPTION) return;

        String name  = tfName.getText().trim();
        String phone = tfPhone.getText().trim();

        try {
            validateOrganizerName(name);
            validatePhone(phone);
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
            return;
        }

        String generatedPassword = generateRandomPassword();   // باسورد عشوائي

        String insertOrgSql = """
                INSERT INTO organizers (name, email, phone, active, role, password)
                VALUES (?,?,?,?,?,?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertOrgSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, "temp"); // مؤقت
            ps.setString(3, phone);
            ps.setBoolean(4, true);
            ps.setString(5, "Organizer");
            ps.setString(6, generatedPassword);

            ps.executeUpdate();

            int newId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) newId = rs.getInt(1);
            }

            if (newId <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to get new organizer ID.");
                return;
            }

            String finalEmail = newId + "@maksad.org.sa";

            String updateOrgSql = "UPDATE organizers SET email=? WHERE organizer_id=?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateOrgSql)) {
                psUpdate.setString(1, finalEmail);
                psUpdate.setInt(2, newId);
                psUpdate.executeUpdate();
            }

            String insertUserSql = """
                    INSERT INTO maksad_users (
                        admin_id, organizer_id, volunteer_id,
                        full_name, email, phone, password, gender,
                        date_of_birth, preferred_type, interests, skills, role
                    ) VALUES (NULL, ?, NULL, ?, ?, ?, ?, NULL, NULL, NULL, NULL, NULL, 'Organizer')
                    """;

            try (PreparedStatement psUser = conn.prepareStatement(insertUserSql)) {
                psUser.setInt(1, newId);
                psUser.setString(2, name);
                psUser.setString(3, finalEmail);
                psUser.setString(4, phone);
                psUser.setString(5, generatedPassword);
                psUser.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "New Organizer Created:\n" +
                            "ID: " + newId + "\n" +
                            "Email: " + finalEmail + "\n" +
                            "Temporary Password: " + generatedPassword,
                    "Organizer Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            reloadOrganizersTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void onCreateEvent() {
        int selectedOrg = chooseOrganizer();
        if (selectedOrg == -1) return;

        JTextField tfName     = new JTextField();

        JComboBox<String> cbCategory = new JComboBox<>(new String[]{
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
        });

        JTextField tfLocation   = new JTextField();
        JTextField tfVolunteers = new JTextField();
        JTextArea  tfDesc       = new JTextArea(3, 20);

        JPanel locationPanel = new JPanel(new BorderLayout(5, 0));
        locationPanel.add(tfLocation, BorderLayout.CENTER);
        JButton btnMap = createPrimaryButton("Open Map");
        locationPanel.add(btnMap, BorderLayout.EAST);

        DatePicker datePicker   = new DatePicker();
        TimePicker startPicker  = new TimePicker();
        TimePicker endPicker    = new TimePicker();

        Object[] inputs = {
                "Event Name:", tfName,
                "Category:", cbCategory,
                "Location:", locationPanel,
                "Volunteers (INT):", tfVolunteers,
                "Event Date:", datePicker,
                "Start Time:", startPicker,
                "End Time:", endPicker,
                "Description:", new JScrollPane(tfDesc)
        };

        btnMap.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI("https://www.google.com/maps"));
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this,
                        "Could not open map:\n" + ex.getMessage(),
                        "Map Error",
                        JOptionPane.ERROR_MESSAGE);
            }
        });

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Add New Event", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String eventName = tfName.getText().trim();
        String location  = tfLocation.getText().trim();
        String desc      = tfDesc.getText().trim();
        String volText   = tfVolunteers.getText().trim();

        int volunteersInt;
        try {
            volunteersInt = Integer.parseInt(volText);
        } catch (Exception e) {
            showValidationError("Volunteers must be an integer.");
            return;
        }

        try {
            validateEventBasic(eventName, location, volunteersInt);
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
            return;
        }

        if (datePicker.getDate() == null ||
                startPicker.getTime() == null ||
                endPicker.getTime() == null) {

            showValidationError("Please select valid date and time.");
            return;
        }

        String dateStr  = datePicker.getDate().toString();
        String startStr = startPicker.getTime().toString() + ":00";
        String endStr   = endPicker.getTime().toString() + ":00";

        String sql = """
        INSERT INTO events
        (name, organizer_id, category, location, volunteers,
         event_date, start_time, end_time, description, status)
        VALUES (?,?,?,?,?,?,?,?,?, 'PENDING')
        """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, eventName);
            ps.setInt(2, selectedOrg);
            ps.setString(3, cbCategory.getSelectedItem().toString());
            ps.setString(4, location);
            ps.setInt(5, volunteersInt);

            ps.setDate(6, java.sql.Date.valueOf(dateStr));
            ps.setTime(7, java.sql.Time.valueOf(startStr));
            ps.setTime(8, java.sql.Time.valueOf(endStr));
            ps.setString(9, desc);

            ps.executeUpdate();
            reloadEventsTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void onEditOrganizer() {
        int row = organizerTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an organizer first.");
            return;
        }

        DefaultTableModel model = (DefaultTableModel) organizerTable.getModel();

        int id       = (int) model.getValueAt(row, 0);
        String name  = (String) model.getValueAt(row, 1);
        String email = (String) model.getValueAt(row, 2);
        String phone = (String) model.getValueAt(row, 3);
        String role  = (String) model.getValueAt(row, 5);

        JTextField tfName  = new JTextField(name);
        JTextField tfEmail = new JTextField(email);
        JTextField tfPhone = new JTextField(phone);
        JTextField tfRole  = new JTextField(role);

        Object[] inputs = {
                "Name:",  tfName,
                "Email:", tfEmail,
                "Phone:", tfPhone,
                "Role:",  tfRole
        };

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Edit Organizer", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String newName  = tfName.getText().trim();
        String newEmail = tfEmail.getText().trim();
        String newPhone = tfPhone.getText().trim();
        String newRole  = tfRole.getText().trim();

        try {
            validateOrganizerName(newName);
            validateEmail(newEmail);
            validatePhone(newPhone);
        } catch (IllegalArgumentException ex) {
            showValidationError(ex.getMessage());
            return;
        }

        String sql = """
                UPDATE organizers
                SET name=?, email=?, phone=?, role=?
                WHERE organizer_id=?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, newName);
            ps.setString(2, newEmail);
            ps.setString(3, newPhone);
            ps.setString(4, newRole);
            ps.setInt(5, id);

            ps.executeUpdate();
            reloadOrganizersTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void onChangeOrganizerStatus(boolean activate) {
        int row = organizerTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an organizer first.");
            return;
        }

        int id = (int) organizerTable.getValueAt(row, 0);

        String sql = "UPDATE organizers SET active=? WHERE organizer_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setBoolean(1, activate);
            ps.setInt(2, id);
            ps.executeUpdate();

            reloadOrganizersTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private int chooseOrganizer() {

        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>();

        String sql = "SELECT organizer_id, name FROM organizers ORDER BY organizer_id";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                model.addElement(rs.getInt("organizer_id") + " - " + rs.getString("name"));
            }

        } catch (SQLException ex) {
            showDbError(ex);
            return -1;
        }

        JComboBox<String> cb = new JComboBox<>(model);

        int result = JOptionPane.showConfirmDialog(
                this, cb, "Select Organizer", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return -1;

        String selected = (String) cb.getSelectedItem();
        return Integer.parseInt(selected.split(" - ")[0]);
    }

    private void onApproveEvent() {
        int row = eventTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }

        int eventId = (int) eventTable.getValueAt(row, 0);
        updateEventStatus(eventId, "APPROVED");
    }

    private void onRejectEvent() {
        int row = eventTable.getSelectedRow();

        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }

        int eventId = (int) eventTable.getValueAt(row, 0);
        updateEventStatus(eventId, "REJECTED");
    }

    private void updateEventStatus(int eventId, String status) {
        String sql = "UPDATE events SET status=? WHERE event_id=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setInt(2, eventId);
            ps.executeUpdate();

            reloadEventsTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ======== Add Admin (بدون إدخال باسورد) ========
    private void onAddAdmin() {

        JTextField tfName = new JTextField();
        JComboBox<String> cbRole = new JComboBox<>(new String[]{
                "Operations Admin",
                "Content Supervisor",
                "Finance Admin",
                "Audit Admin",
                "Other"
        });

        Object[] inputs = {
                "Admin Name:", tfName,
                "Role Title:", cbRole
        };

        int result = JOptionPane.showConfirmDialog(
                this, inputs, "Add Admin", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) return;

        String name = tfName.getText().trim();
        String roleTitle = cbRole.getSelectedItem().toString();

        if (name.isEmpty()) {
            showValidationError("Admin name is required.");
            return;
        }

        String generatedPassword = generateRandomPassword();

        String insertAdminSql = """
                INSERT INTO admins (name, email, password, is_super_admin, role_title)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(insertAdminSql, Statement.RETURN_GENERATED_KEYS)) {

            ps.setString(1, name);
            ps.setString(2, "temp");          // مؤقت
            ps.setString(3, generatedPassword);
            ps.setBoolean(4, false);          // الإدمن الجديد ليس سوبر أدمن
            ps.setString(5, roleTitle);

            ps.executeUpdate();

            int newId = -1;
            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) newId = rs.getInt(1);
            }

            if (newId <= 0) {
                JOptionPane.showMessageDialog(this, "Failed to get new admin ID.");
                return;
            }

            String finalEmail = newId + "@maksad.org.sa";

            String updateAdminSql = "UPDATE admins SET email=? WHERE admin_id=?";
            try (PreparedStatement psUpdate = conn.prepareStatement(updateAdminSql)) {
                psUpdate.setString(1, finalEmail);
                psUpdate.setInt(2, newId);
                psUpdate.executeUpdate();
            }

            String insertUserSql = """
                    INSERT INTO maksad_users (
                        admin_id, organizer_id, volunteer_id,
                        full_name, email, phone, password, gender,
                        date_of_birth, preferred_type, interests, skills, role
                    ) VALUES (?, NULL, NULL, ?, ?, NULL, ?, NULL, NULL, NULL, NULL, NULL, 'Admin')
                    """;

            try (PreparedStatement psUser = conn.prepareStatement(insertUserSql)) {
                psUser.setInt(1, newId);
                psUser.setString(2, name);
                psUser.setString(3, finalEmail);
                psUser.setString(4, generatedPassword);
                psUser.executeUpdate();
            }

            JOptionPane.showMessageDialog(
                    this,
                    "New Admin Created:\n" +
                            "ID: " + newId + "\n" +
                            "Email: " + finalEmail + "\n" +
                            "Temporary Password: " + generatedPassword,
                    "Admin Created",
                    JOptionPane.INFORMATION_MESSAGE
            );

            reloadAdminsTable();

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ======== Super Admin Check ========
    private boolean checkSuperAdminAccess() {

        JTextField tfId = new JTextField();
        JPasswordField pfPassword = new JPasswordField();

        Object[] msg = {
                "Super Admin ID:", tfId,
                "Password:", pfPassword
        };

        int result = JOptionPane.showConfirmDialog(
                this, msg, "Super Admin Verification", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION) {
            return false;
        }

        int adminId;
        try {
            adminId = Integer.parseInt(tfId.getText().trim());
        } catch (NumberFormatException ex) {
            showValidationError("Admin ID must be a number.");
            return false;
        }

        String password = new String(pfPassword.getPassword());

        String sql = "SELECT is_super_admin FROM admins WHERE admin_id=? AND password=?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ps.setString(2, password);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next() && rs.getBoolean("is_super_admin")) {
                    return true;
                } else {
                    showValidationError("Access denied. Only Super Admin can open this screen.");
                    return false;
                }
            }

        } catch (SQLException ex) {
            showDbError(ex);
            return false;
        }
    }

    private void showDbError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(this,
                "Database error:\n" + ex.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
    }

    // ======================= MAIN =======================

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         """
                         SELECT admin_id, name, email, password, is_super_admin, role_title
                         FROM admins
                         ORDER BY admin_id
                         LIMIT 1
                         """
                 );
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    Admin admin = new Admin(
                            rs.getInt("admin_id"),
                            rs.getString("name"),
                            rs.getString("email"),
                            rs.getString("password"),
                            rs.getBoolean("is_super_admin"),
                            rs.getString("role_title")
                    );

                    new AdminDashboardFrame(admin);

                } else {
                    JOptionPane.showMessageDialog(
                            null,
                            "No admin found in database.\nPlease insert one.",
                            "No Admin",
                            JOptionPane.WARNING_MESSAGE
                    );
                }

            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(
                        null,
                        "Error loading admin:\n" + ex.getMessage(),
                        "DB Error",
                        JOptionPane.ERROR_MESSAGE
                );
            }
        });
    }
}