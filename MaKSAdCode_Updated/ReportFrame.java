package maksadpro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class ReportFrame extends JFrame {

    // ================= THEME COLORS =================
    private static final Color COLOR_BACKGROUND = Color.decode("#4C5834"); // الأخضر الخارجي
    private static final Color COLOR_DARK_GREEN = Color.decode("#263717"); // عمود المنيو
    private static final Color COLOR_OLIVE      = Color.decode("#74835A"); // أزرار المنيو
    private static final Color COLOR_CARD_BG    = Color.decode("#EDE4C4"); // البيج
    private static final Color COLOR_WHITE      = Color.WHITE;
    private static final Color COLOR_BROWN      = new Color(0x5A3018);     // البني

    // Scaling factor لعناصر داخل البيج
    private static final double SCALE = 0.90;

    // Logo path
    private static final String LOGO_RESOURCE = "/maksadpro/MaKSAdPH/maKSAdLogo.png";

    private final Admin admin;

    private CardLayout cardLayout;
    private JPanel cardPanel;

    // ---------- General Report Labels ----------
    private JLabel lblGenReportId;
    private JLabel lblGenType;
    private JLabel lblGenDate;
    private JLabel lblGenBy;
    private JLabel lblGenTotalVolunteers;
    private JLabel lblGenTotalOrganizers;
    private JLabel lblGenTotalEvents;
    private JLabel lblGenTotalCertificates;

    // ---------- Timed Report Labels ----------
    private JLabel lblTimedReportId;
    private JLabel lblTimedType;
    private JLabel lblTimedDate;
    private JLabel lblTimedBy;
    private JLabel lblTimedRange;
    private JLabel lblTimedEvents;
    private JLabel lblTimedParticipants;

    // ---------- Category Report Labels ----------
    private JLabel lblCatReportId;
    private JLabel lblCatType;
    private JLabel lblCatDate;
    private JLabel lblCatBy;
    private JLabel lblCatCategory;
    private JLabel lblCatCount;

    public ReportFrame(Admin admin) {
        this.admin = admin;

        setTitle("MaKSAd – Reports");
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // حجم افتراضي + حد أدنى
        setSize(1200, 720);
        setMinimumSize(new Dimension(1100, 650));
        setLocationRelativeTo(null);
        getContentPane().setBackground(COLOR_BACKGROUND);

        initLayout();

        addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                cardPanel.revalidate();
                cardPanel.repaint();
            }
        });
    }

    // ======================= HELPERS: SCALE =======================

    private static int s(int v) {
        return (int) Math.round(v * SCALE);
    }

    private static int sf(int v) {
        return (int) Math.round(v * SCALE);
    }

    // ======================= LAYOUT =======================

    private void initLayout() {
        getContentPane().setLayout(new BorderLayout());

        // ---------- LEFT MENU ----------
        JPanel menu = new JPanel();
        menu.setBackground(COLOR_DARK_GREEN);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        menu.setPreferredSize(new Dimension(210, getHeight()));

        JButton btnGeneral  = createMenuButton("Generate General Report");
        JButton btnTimed    = createMenuButton("Generate Timed Report");
        JButton btnCategory = createMenuButton("Generate Category Report");
        JButton btnHistory  = createMenuButton("View Past Reports");
        JButton btnReturn   = createMenuButton("Return to Dashboard");

        menu.add(btnGeneral);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnTimed);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnCategory);
        menu.add(Box.createVerticalStrut(10));
        menu.add(btnHistory);
        menu.add(Box.createVerticalGlue());
        menu.add(btnReturn);

        // ---------- CENTER CARD PANEL ----------
        cardLayout = new CardLayout();
        cardPanel  = new JPanel(cardLayout);
        cardPanel.setBackground(COLOR_BACKGROUND);
        cardPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));

        cardPanel.add(buildGeneralPanel(),  "general");
        cardPanel.add(buildTimedPanel(),    "timed");
        cardPanel.add(buildCategoryPanel(), "category");

        getContentPane().add(menu, BorderLayout.WEST);
        getContentPane().add(cardPanel, BorderLayout.CENTER);

        // ---------- ACTIONS ----------
        btnGeneral.addActionListener(e -> {
            cardLayout.show(cardPanel, "general");
            generateGeneralReport();
        });

        btnTimed.addActionListener(e -> {
            cardLayout.show(cardPanel, "timed");
            generateTimedReport();
        });

        btnCategory.addActionListener(e -> {
            cardLayout.show(cardPanel, "category");
            generateCategoryReport();
        });

        btnHistory.addActionListener(e -> showPastReportsDialog());
        btnReturn.addActionListener(e -> dispose());
    }

    // ======================= PANELS =======================

    private JPanel buildGeneralPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COLOR_BACKGROUND);

        RoundedPanel left = new RoundedPanel(COLOR_CARD_BG, null, 50);
        left.setLayout(null);

        RoundedPanel main = new RoundedPanel(COLOR_CARD_BG, null, 50);
        main.setLayout(null);

        panel.add(left);
        panel.add(main);

        layoutGeneralPanels(panel, left, main);

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutGeneralPanels(panel, left, main);
            }
        });

        lblGenReportId = createBrownLabel("-", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report ID", 30, lblGenReportId);

        lblGenType = createBrownLabel("General Report", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report Type", 160, lblGenType);

        lblGenDate = createBrownLabel(LocalDate.now().toString(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated On", 290, lblGenDate);

        lblGenBy = createBrownLabel(admin.getName(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated By", 420, lblGenBy);

        lblGenTotalVolunteers   = createValueRow(main, "Total Volunteers",   90);
        lblGenTotalOrganizers   = createValueRow(main, "Total Organizers",   190);
        lblGenTotalEvents       = createValueRow(main, "Total Events",       290);
        lblGenTotalCertificates = createValueRow(main, "Total Certificates", 390);

        addLogoToMainPanel(main);

        return panel;
    }

    // يصغر / يحدد مكان البانلات البيج داخل الخلفية الخضراء
    private void layoutGeneralPanels(JPanel container, JPanel left, JPanel main) {
        int w = container.getWidth();
        int h = container.getHeight();
        if (w <= 0 || h <= 0) return;

        int sideMargin = (int) (0.10 * w);
        int topBottomMargin = (int) (0.12 * h);

        int leftWidth  = (int) (0.18 * w);
        int leftHeight = h - 2 * topBottomMargin;

        left.setBounds(sideMargin, topBottomMargin, leftWidth, leftHeight);

        int gap = (int) (0.04 * w);

        int mainX      = sideMargin + leftWidth + gap;
        int mainWidth  = w - mainX - sideMargin;
        int mainHeight = h - 2 * topBottomMargin;

        main.setBounds(mainX, topBottomMargin, mainWidth, mainHeight);
    }

    private JPanel buildTimedPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COLOR_BACKGROUND);

        RoundedPanel left = new RoundedPanel(COLOR_CARD_BG, null, 50);
        left.setLayout(null);

        RoundedPanel main = new RoundedPanel(COLOR_CARD_BG, null, 50);
        main.setLayout(null);

        panel.add(left);
        panel.add(main);

        layoutGeneralPanels(panel, left, main);

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutGeneralPanels(panel, left, main);
            }
        });

        lblTimedReportId = createBrownLabel("-", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report ID", 30, lblTimedReportId);

        lblTimedType = createBrownLabel("Timed Report", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report Type", 160, lblTimedType);

        lblTimedDate = createBrownLabel(LocalDate.now().toString(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated On", 290, lblTimedDate);

        lblTimedBy = createBrownLabel(admin.getName(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated By", 420, lblTimedBy);

        JLabel lblRangeTitle = createBrownLabel("Range:", sf(18), SwingConstants.LEFT);
        lblRangeTitle.setBounds(s(70), s(70), s(120), s(32));
        main.add(lblRangeTitle);

        RoundedPanel rangeBox = new RoundedPanel(COLOR_BROWN, null, 30);
        rangeBox.setLayout(new BorderLayout());
        rangeBox.setBounds(s(190), s(60), s(420), s(50));

        lblTimedRange = new JLabel("YYYY-MM-DD  to  YYYY-MM-DD", SwingConstants.CENTER);
        lblTimedRange.setForeground(COLOR_WHITE);
        lblTimedRange.setFont(new Font("SansSerif", Font.BOLD, sf(16)));
        rangeBox.add(lblTimedRange, BorderLayout.CENTER);
        main.add(rangeBox);

        lblTimedEvents        = createValueRow(main, "Events in period",       180);
        lblTimedParticipants  = createValueRow(main, "Participants in period", 280);

        addLogoToMainPanel(main);

        return panel;
    }

    private JPanel buildCategoryPanel() {
        JPanel panel = new JPanel(null);
        panel.setBackground(COLOR_BACKGROUND);

        RoundedPanel left = new RoundedPanel(COLOR_CARD_BG, null, 50);
        left.setLayout(null);

        RoundedPanel main = new RoundedPanel(COLOR_CARD_BG, null, 50);
        main.setLayout(null);

        panel.add(left);
        panel.add(main);

        layoutGeneralPanels(panel, left, main);

        panel.addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                layoutGeneralPanels(panel, left, main);
            }
        });

        lblCatReportId = createBrownLabel("-", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report ID", 30, lblCatReportId);

        lblCatType = createBrownLabel("Category Report", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report Type", 160, lblCatType);

        lblCatDate = createBrownLabel(LocalDate.now().toString(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated On", 290, lblCatDate);

        lblCatBy = createBrownLabel(admin.getName(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated By", 420, lblCatBy);

        RoundedPanel catField = new RoundedPanel(COLOR_WHITE, COLOR_BROWN, 30);
        catField.setLayout(new BorderLayout());
        catField.setBounds(s(70), s(130), s(590), s(80));

        lblCatCategory = new JLabel("Category", SwingConstants.LEFT);
        lblCatCategory.setBorder(BorderFactory.createEmptyBorder(0, s(25), 0, 0));
        lblCatCategory.setForeground(COLOR_BROWN);
        lblCatCategory.setFont(new Font("SansSerif", Font.BOLD, sf(20)));
        catField.add(lblCatCategory, BorderLayout.CENTER);
        main.add(catField);

        RoundedPanel countField = new RoundedPanel(COLOR_WHITE, COLOR_BROWN, 30);
        countField.setLayout(new BorderLayout());
        countField.setBounds(s(70), s(250), s(590), s(80));

        JLabel lblCountTitle = new JLabel("Count", SwingConstants.LEFT);
        lblCountTitle.setBorder(BorderFactory.createEmptyBorder(0, s(25), 0, 0));
        lblCountTitle.setForeground(COLOR_BROWN);
        lblCountTitle.setFont(new Font("SansSerif", Font.BOLD, sf(20)));
        countField.add(lblCountTitle, BorderLayout.CENTER);
        main.add(countField);

        RoundedPanel countSquare = new RoundedPanel(COLOR_BROWN, null, 28);
        countSquare.setLayout(new BorderLayout());
        countSquare.setBounds(s(680), s(260), s(80), s(60));

        lblCatCount = new JLabel("0", SwingConstants.CENTER);
        lblCatCount.setForeground(COLOR_WHITE);
        lblCatCount.setFont(new Font("SansSerif", Font.BOLD, sf(22)));
        countSquare.add(lblCatCount, BorderLayout.CENTER);
        main.add(countSquare);

        addLogoToMainPanel(main);

        return panel;
    }

    // ======================= REPORT GENERATION =======================

    private void generateGeneralReport() {
        try (Connection conn = DBConnection.getConnection()) {

            int totalVolunteers   = queryCount(conn, "SELECT COUNT(*) FROM volunteers");
            int totalOrganizers   = queryCount(conn, "SELECT COUNT(*) FROM organizers");
            int totalEvents       = queryCount(conn, "SELECT COUNT(*) FROM events");
            int totalCertificates = queryCount(conn, "SELECT COUNT(*) FROM certificates");

            lblGenTotalVolunteers.setText(String.valueOf(totalVolunteers));
            lblGenTotalOrganizers.setText(String.valueOf(totalOrganizers));
            lblGenTotalEvents.setText(String.valueOf(totalEvents));
            lblGenTotalCertificates.setText(String.valueOf(totalCertificates));

            int reportId = insertReportRow(conn, "GENERAL", "General System Summary");
            lblGenReportId.setText(String.format("%06d", reportId));
            lblGenType.setText("General Report");
            lblGenDate.setText(LocalDate.now().toString());
            lblGenBy.setText(admin.getName());

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void generateTimedReport() {
        com.github.lgooddatepicker.components.DatePicker fromPicker =
                new com.github.lgooddatepicker.components.DatePicker();
        com.github.lgooddatepicker.components.DatePicker toPicker =
                new com.github.lgooddatepicker.components.DatePicker();

        Object[] msg = { "From date:", fromPicker, "To date:", toPicker };

        int result = JOptionPane.showConfirmDialog(
                this, msg, "Select Period", JOptionPane.OK_CANCEL_OPTION
        );

        if (result != JOptionPane.OK_OPTION
                || fromPicker.getDate() == null
                || toPicker.getDate() == null) return;

        LocalDate from = fromPicker.getDate();
        LocalDate to   = toPicker.getDate();

        try (Connection conn = DBConnection.getConnection()) {

            int eventsInPeriod = queryCount(
                    conn,
                    "SELECT COUNT(*) FROM events WHERE event_date BETWEEN ? AND ?",
                    Date.valueOf(from),
                    Date.valueOf(to)
            );

            int participantsInPeriod = queryCount(
                    conn,
                    "SELECT COUNT(*) FROM volunteer_participations WHERE event_date BETWEEN ? AND ?",
                    Date.valueOf(from),
                    Date.valueOf(to)
            );

            lblTimedRange.setText(from + "  to  " + to);
            lblTimedEvents.setText(String.valueOf(eventsInPeriod));
            lblTimedParticipants.setText(String.valueOf(participantsInPeriod));

            int reportId = insertReportRow(conn, "TIMED",
                    "Events & Participants between " + from + " and " + to);
            lblTimedReportId.setText(String.format("%06d", reportId));
            lblTimedType.setText("Timed Report");
            lblTimedDate.setText(LocalDate.now().toString());
            lblTimedBy.setText(admin.getName());

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void generateCategoryReport() {
        String[] categories = {
                "Academic / Workshops", "Training Programs", "Competitions", "Community Events",
                "Technology & Innovation", "Health & Wellness",
                "Conferences & Talks", "Exhibitions & Booths",
                "Environmental Activities", "Social Responsibility"
        };

        String selectedCategory = (String) JOptionPane.showInputDialog(
                this, "Select category:", "Category Report",
                JOptionPane.QUESTION_MESSAGE, null, categories, categories[0]
        );

        if (selectedCategory == null) return;

        try (Connection conn = DBConnection.getConnection()) {

            int count = queryCount(
                    conn, "SELECT COUNT(*) FROM events WHERE category = ?", selectedCategory
            );

            lblCatCategory.setText("Category: " + selectedCategory);
            lblCatCount.setText(String.valueOf(count));

            int reportId = insertReportRow(conn, "CATEGORY",
                    "Events in category: " + selectedCategory);

            lblCatReportId.setText(String.format("%06d", reportId));
            lblCatType.setText("Category Report");
            lblCatDate.setText(LocalDate.now().toString());
            lblCatBy.setText(admin.getName());

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private void showPastReportsDialog() {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "SELECT report_id, report_type, report_title, date_generated " +
                             "FROM reports ORDER BY report_id DESC"
             );
             ResultSet rs = ps.executeQuery()) {

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"ID", "Type", "Title", "Generated On"}, 0
            );

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("report_id"),
                        rs.getString("report_type"),
                        rs.getString("report_title"),
                        rs.getTimestamp("date_generated")
                });
            }

            JTable table = new JTable(model);
            JScrollPane scroll = new JScrollPane(table);
            scroll.setPreferredSize(new Dimension(600, 300));

            JOptionPane.showMessageDialog(
                    this, scroll, "Past Reports", JOptionPane.PLAIN_MESSAGE
            );

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    // ======================= DB HELPERS =======================

    private int queryCount(Connection conn, String sql, Object... params) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setObject(i + 1, params[i]);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private int insertReportRow(Connection conn, String type, String title) throws SQLException {
        String sql = """
                INSERT INTO reports (report_type, report_title, date_generated, generated_by)
                VALUES (?, ?, ?, ?)
                """;

        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, type);
            ps.setString(2, title);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setInt(4, resolveCurrentAdminId(conn));
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) return rs.getInt(1);
            }
        }
        return 0;
    }

    private int resolveCurrentAdminId(Connection conn) throws SQLException {
        try {
            var m = admin.getClass().getMethod("getAdminId");
            Object val = m.invoke(admin);
            if (val instanceof Integer i) return i;
        } catch (Exception ignored) {}

        try {
            var mEmail = admin.getClass().getMethod("getEmail");
            String email = (String) mEmail.invoke(admin);
            try (PreparedStatement ps = conn.prepareStatement(
                    "SELECT admin_id FROM admins WHERE email=? LIMIT 1")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) return rs.getInt(1);
                }
            }
        } catch (Exception ignored) {}

        throw new SQLException("Could not resolve current admin_id.");
    }

    private void showDbError(Exception ex) {
        ex.printStackTrace();
        JOptionPane.showMessageDialog(
                this, "Database error:\n" + ex.getMessage(),
                "Database Error", JOptionPane.ERROR_MESSAGE
        );
    }

    // ======================= UI HELPERS =======================

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
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(190, 38));
        return btn;
    }

    private JLabel createBrownLabel(String text, int fontSize, int align) {
        JLabel lbl = new JLabel(text, align);
        lbl.setForeground(COLOR_BROWN);
        lbl.setFont(new Font("SansSerif", Font.BOLD, fontSize));
        return lbl;
    }

    // block في العمود اليسار (عنوان + قيمة تحت)
    private void createMetaBox(JPanel parent, String title, int y, JLabel valueLabel) {
        RoundedPanel box = new RoundedPanel(COLOR_CARD_BG, COLOR_BROWN, 35);
        box.setLayout(null);
        box.setBounds(s(20), s(y), s(220), s(110));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(COLOR_BROWN);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, sf(18)));
        titleLabel.setBounds(s(10), s(8), s(200), s(30));
        box.add(titleLabel);

        valueLabel.setBounds(s(10), s(55), s(200), s(28));
        box.add(valueLabel);

        parent.add(box);
    }

    // صف أبيض + مربع بني فيه الرقم
    private JLabel createValueRow(JPanel parent, String title, int y) {
        RoundedPanel field = new RoundedPanel(COLOR_WHITE, COLOR_BROWN, 30);
        field.setLayout(null);
        field.setBounds(s(70), s(y), s(600), s(80));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(COLOR_BROWN);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, sf(20)));
        lblTitle.setBounds(s(25), s(25), s(400), s(30));
        field.add(lblTitle);
        parent.add(field);

        RoundedPanel badge = new RoundedPanel(COLOR_BROWN, null, 28);
        badge.setLayout(new BorderLayout());
        badge.setBounds(s(690), s(y + 10), s(80), s(60));

        JLabel valueLabel = new JLabel("0", SwingConstants.CENTER);
        valueLabel.setForeground(COLOR_WHITE);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, sf(22)));
        badge.add(valueLabel, BorderLayout.CENTER);

        parent.add(badge);
        return valueLabel;
    }

    private void addLogoToMainPanel(JPanel main) {
        try {
            java.net.URL url = getClass().getResource(LOGO_RESOURCE);
            if (url == null) return;

            ImageIcon icon = new ImageIcon(url);
            int size = 130; // نخلي اللوقو واضح
            Image img = icon.getImage().getScaledInstance(size, size, Image.SCALE_SMOOTH);
            JLabel logo = new JLabel(new ImageIcon(img));
            logo.setSize(size, size);
            main.add(logo);

            positionLogo(main, logo);

            main.addComponentListener(new ComponentAdapter() {
                @Override
                public void componentResized(ComponentEvent e) {
                    positionLogo(main, logo);
                }
            });

        } catch (Exception ignored) {}
    }

    private void positionLogo(JPanel main, JLabel logoLabel) {
        int w = main.getWidth();
        int h = main.getHeight();
        if (w <= 0 || h <= 0) return;

        int margin = 25;
        logoLabel.setLocation(
                w - logoLabel.getWidth() - margin,
                h - logoLabel.getHeight() - margin
        );
    }

    // بانل بحواف دائرية
    private static class RoundedPanel extends JPanel {
        private final Color bg;
        private final Color border;
        private final int arc;

        public RoundedPanel(Color bg, Color border, int arc) {
            this.bg = bg;
            this.border = border;
            this.arc = arc;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                    RenderingHints.VALUE_ANTIALIAS_ON);

            if (bg != null) {
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth() - 1, getHeight() - 1, arc, arc);
            }
            if (border != null) {
                g2.setColor(border);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth() - 3, getHeight() - 3, arc, arc);
            }
            g2.dispose();
            super.paintComponent(g);
        }
    }
}