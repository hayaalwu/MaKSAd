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
    // ---------- Timed Entries State ----------
    private LocalDate lastTimedFrom = null;
    private LocalDate lastTimedTo   = null;

    // ---------- Category Entries State ----------
    private String lastCategory = null;

    // ---------- New Category Label ----------
    private JLabel lblCatParticipants;

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
    private JPanel catCountField;
    private JPanel catCountSquare;

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

    private static int s(int v) {
        return (int) Math.round(v * SCALE);
    }

    private static int sf(int v) {
        return (int) Math.round(v * SCALE);
    }
    
    private JLabel createMenuTitle(String text) {
        JLabel lbl = new JLabel(text, SwingConstants.CENTER);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
        lbl.setBorder(BorderFactory.createEmptyBorder(8, 0, 18, 0));
        return lbl;
    }

    // ======================= LAYOUT =======================

    private void initLayout() {
        getContentPane().setLayout(new BorderLayout());

        // ---------- LEFT MENU ----------
        JPanel menu = new JPanel();
        menu.setBackground(COLOR_DARK_GREEN);
        menu.setLayout(new BoxLayout(menu, BoxLayout.Y_AXIS));
        menu.setBorder(BorderFactory.createEmptyBorder(20, 15, 20, 15));
        menu.setPreferredSize(new Dimension(240, getHeight())); 
        
        JLabel menuTitle = createMenuTitle("Report Dashboard");
        menu.add(menuTitle);

        JButton btnGeneral  = createMenuButton("Generate General Report");
        JButton btnTimed    = createMenuButton("Generate Timed Report");
        JButton btnCategory = createMenuButton("Generate Category Report");
        JButton btnHistory  = createMenuButton("View Past Reports");
        JButton btnReturn   = createMenuButton("Return to Dashboard");

        menu.add(Box.createVerticalStrut(12));
        menu.add(Box.createVerticalGlue());

        menu.add(btnGeneral);
        menu.add(Box.createVerticalStrut(16));

        menu.add(btnTimed);
        menu.add(Box.createVerticalStrut(16));

        menu.add(btnCategory);
        menu.add(Box.createVerticalStrut(16));

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
        
        JButton btnExportGen = createMenuButton("Export to Excel (CSV)");
        btnExportGen.setMaximumSize(new Dimension(220, 52));

        btnExportGen.addActionListener(e -> exportGeneralReportToCSV());

        btnExportGen.setBounds(s(70), s(500), s(260), s(52)); 

        main.add(btnExportGen);

        return panel;
    }

    // يصغر / يحدد مكان البانلات البيج داخل الخلفية الخضراء
    private void layoutGeneralPanels(JPanel container, JPanel left, JPanel main) {
        int w = container.getWidth();
        int h = container.getHeight();
        if (w <= 0 || h <= 0) return;

        int sideMargin = (int) (0.08 * w);
        int topBottomMargin = (int) (0.12 * h);

        int leftWidth  = (int) (0.22 * w);
        int leftHeight = h - 2 * topBottomMargin;

        left.setBounds(sideMargin, topBottomMargin, leftWidth, leftHeight);

        int gap = (int) (0.03 * w);

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
        
        JButton btnTimedEntries = createMenuButton("Show Entries");
        btnTimedEntries.setBounds(s(70), s(370), s(260), s(52));
        btnTimedEntries.addActionListener(e -> showTimedEntriesPopup());
        main.add(btnTimedEntries);

        addLogoToMainPanel(main);
        
        JButton btnExportTimed = createMenuButton("Export to Excel (CSV)");
        btnExportTimed.setBounds(s(70), s(440), s(260), s(52));
        btnExportTimed.addActionListener(e -> exportTimedReportToCSV());
        main.add(btnExportTimed);

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
                positionCategoryCountBadge(catCountField, catCountSquare);
            }
        });

        // ===== Left meta boxes =====
        lblCatReportId = createBrownLabel("-", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report ID", 30, lblCatReportId);

        lblCatType = createBrownLabel("Category Report", sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Report Type", 160, lblCatType);

        lblCatDate = createBrownLabel(LocalDate.now().toString(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated On", 290, lblCatDate);

        lblCatBy = createBrownLabel(admin.getName(), sf(16), SwingConstants.CENTER);
        createMetaBox(left, "Generated By", 420, lblCatBy);

        // ===== Category name field =====
        RoundedPanel catField = new RoundedPanel(COLOR_WHITE, COLOR_BROWN, 30);
        catField.setLayout(new BorderLayout());
        catField.setBounds(s(70), s(110), s(590), s(80));

        lblCatCategory = new JLabel("Category", SwingConstants.LEFT);
        lblCatCategory.setBorder(BorderFactory.createEmptyBorder(0, s(25), 0, 0));
        lblCatCategory.setForeground(COLOR_BROWN);
        lblCatCategory.setFont(new Font("SansSerif", Font.BOLD, sf(20)));
        catField.add(lblCatCategory, BorderLayout.CENTER);
        main.add(catField);

        // ===== Total Events row =====
        RoundedPanel countField = new RoundedPanel(COLOR_WHITE, COLOR_BROWN, 30);
        catCountField = countField;
        countField.setLayout(new BorderLayout());
        countField.setBounds(s(70), s(230), s(590), s(80));

        JLabel lblCountTitle = new JLabel("Total Events", SwingConstants.LEFT);
        lblCountTitle.setBorder(BorderFactory.createEmptyBorder(0, s(25), 0, 0));
        lblCountTitle.setForeground(COLOR_BROWN);
        lblCountTitle.setFont(new Font("SansSerif", Font.BOLD, sf(20)));
        countField.add(lblCountTitle, BorderLayout.CENTER);
        main.add(countField);

        RoundedPanel countSquare = new RoundedPanel(COLOR_BROWN, null, 28);
        catCountSquare = countSquare;
        countSquare.setLayout(new BorderLayout());
        positionCategoryCountBadge(countField, countSquare);

        lblCatCount = new JLabel("0", SwingConstants.CENTER);
        lblCatCount.setForeground(COLOR_WHITE);
        lblCatCount.setFont(new Font("SansSerif", Font.BOLD, sf(22)));
        countSquare.add(lblCatCount, BorderLayout.CENTER);
        main.add(countSquare);

        // ===== Total Participants row =====
        lblCatParticipants = createValueRow(main, "Total Participants", 350);

        // ===== Show Entries button =====
        JButton btnCatEntries = createMenuButton("Show Entries");
        btnCatEntries.setBounds(s(70), s(440), s(260), s(52));
        btnCatEntries.addActionListener(e -> showCategoryEntriesPopup());
        main.add(btnCatEntries);

        // Logo
        addLogoToMainPanel(main);
        
        JButton btnExportCat = createMenuButton("Export to Excel (CSV)");
        btnExportCat.setBounds(s(70), s(510), s(260), s(52));
        btnExportCat.addActionListener(e -> exportCategoryReportToCSV());
        main.add(btnExportCat);

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
        LocalDate[] dates = showTimedPopupFigma();
        if (dates == null) return;

        LocalDate from = dates[0];
        LocalDate to   = dates[1];
        
        lastTimedFrom = from;
        lastTimedTo = to;


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

        String selectedCategory = showCategoryPopupFigma(categories);
        if (selectedCategory == null) return;
        
        lastCategory = selectedCategory;
        try (Connection conn = DBConnection.getConnection()) {

            // Total events in this category
            int totalEvents = queryCount(
                    conn,
                    "SELECT COUNT(*) FROM events WHERE category = ?",
                    selectedCategory
            );

            // Total participants in this category
            int totalParticipants = queryCount(
                    conn,
                    """
                    SELECT COUNT(*)
                    FROM volunteer_participations vp
                    JOIN events e
                      ON vp.event_name = e.name
                     AND vp.event_date = e.event_date
                    WHERE e.category = ?
                    """,
                    selectedCategory
            );

            // Update UI labels
            lblCatCategory.setText("Category: " + selectedCategory);
            lblCatCount.setText(String.valueOf(totalEvents));
            lblCatParticipants.setText(String.valueOf(totalParticipants));

            // Insert report row
            int reportId = insertReportRow(
                    conn,
                    "CATEGORY",
                    "Category Report - " + selectedCategory +
                            " (Events: " + totalEvents + ", Participants: " + totalParticipants + ")"
            );

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
                    new Object[]{"ID", "Type", "Title", "Generated on"}, 0
            ) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            while (rs.next()) {
                model.addRow(new Object[]{
                        rs.getInt("report_id"),
                        rs.getString("report_type"),
                        rs.getString("report_title"),
                        rs.getTimestamp("date_generated")
                });
            }

            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF); 

            // Column widths 
            table.getColumnModel().getColumn(0).setPreferredWidth(90); 
            table.getColumnModel().getColumn(1).setPreferredWidth(140);
            table.getColumnModel().getColumn(2).setPreferredWidth(420);
            table.getColumnModel().getColumn(3).setPreferredWidth(220);

            table.setRowHeight(38);
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.setGridColor(new Color(0xB9B2A7));
            table.setShowGrid(true);
            table.setFillsViewportHeight(true);
            table.getTableHeader().setUI(null);
            table.setSelectionBackground(new Color(0xDDD6C7));

            JScrollPane scroll = new JScrollPane(table);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(new Color(0xEFECE6));

            JDialog d = createFigmaDialog(980, 540);
            d.setBackground(new Color(0, 0, 0, 0)); 

            RoundedPanel card = (RoundedPanel) ((JPanel)d.getContentPane()).getComponent(0);
            card.setLayout(new BorderLayout());
            card.setBackground(new Color(0,0,0,0));

            RoundedPanel green = new RoundedPanel(new Color(0x3E4F2A), null, 28);
            green.setLayout(new BorderLayout());
            green.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

            JPanel pills = new JPanel(new GridLayout(1, 4, 18, 0));
            pills.setOpaque(false);
            pills.add(makeHeaderPill("ID"));
            pills.add(makeHeaderPill("Type"));
            pills.add(makeHeaderPill("Title"));
            pills.add(makeHeaderPill("Generated on"));

            green.add(pills, BorderLayout.NORTH);

            RoundedPanel tableBg = new RoundedPanel(new Color(0xEFECE6), null, 18);
            tableBg.setPreferredSize(new Dimension(900, 360));
            tableBg.setLayout(new BorderLayout());
            tableBg.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            tableBg.add(scroll, BorderLayout.CENTER);

            green.add(tableBg, BorderLayout.CENTER);
            
            // ===== Close button row =====
            JButton btnClose = createMenuButton("Close");
            btnClose.addActionListener(e -> d.dispose());

            JPanel closeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            JButton btnExport = createMenuButton("Export CSV");
            btnExport.addActionListener(e -> exportReportsHistoryToCSV());

            closeRow.add(btnExport);
            closeRow.add(Box.createHorizontalStrut(12));
            closeRow.add(btnClose);

            closeRow.setOpaque(false);
            closeRow.add(btnClose);
            
            JPanel south = new JPanel();
            south.setOpaque(false);
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.add(Box.createVerticalStrut(12));
            south.add(closeRow);

            green.add(south, BorderLayout.SOUTH);

            card.add(green, BorderLayout.CENTER);
            
            d.setLocationRelativeTo(this);
            d.pack();
            d.setVisible(true);

        } catch (SQLException ex) {
            showDbError(ex);
        }
    }

    private JComponent makeHeaderPill(String text) {
        RoundedPanel pill = new RoundedPanel(new Color(0x5A3018), null, 20);
        pill.setLayout(new GridBagLayout());
        pill.setPreferredSize(new Dimension(120, 40));

        JLabel l = new JLabel(text);
        l.setForeground(Color.WHITE);
        l.setFont(new Font("SansSerif", Font.BOLD, 14));
        pill.add(l);

        return pill;
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
        btn.setMaximumSize(new Dimension(220, 52));
        btn.setMinimumSize(new Dimension(220, 52));
        btn.setPreferredSize(new Dimension(220, 52));
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
        box.setBounds(s(20), s(y), s(260), s(125));

        JLabel titleLabel = new JLabel(title, SwingConstants.CENTER);
        titleLabel.setForeground(COLOR_BROWN);
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, sf(18)));
        titleLabel.setBounds(s(10), s(10), s(240), s(32));
        box.add(titleLabel);

        valueLabel.setBounds(s(10), s(62), s(240), s(30));
        box.add(valueLabel);

        parent.add(box);
    }

    // صف أبيض + مربع بني فيه الرقم
    private JLabel createValueRow(JPanel parent, String title, int y) {
        RoundedPanel field = new RoundedPanel(COLOR_WHITE, COLOR_BROWN, 30);
        field.setLayout(null);
        field.setBounds(s(70), s(y), s(520), s(80));

        JLabel lblTitle = new JLabel(title);
        lblTitle.setForeground(COLOR_BROWN);
        lblTitle.setFont(new Font("SansSerif", Font.BOLD, sf(20)));
        lblTitle.setBounds(s(25), s(25), s(340), s(30));
        field.add(lblTitle);
        parent.add(field);

        RoundedPanel badge = new RoundedPanel(COLOR_BROWN, null, 28);
        badge.setLayout(new BorderLayout());
        badge.setBounds(s(610), s(y + 10), s(80), s(60));

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
    
    // ========== FIGMA REPLICATION INTERFACE HELPERS ==========
    private static final Color POPUP_BROWN = new Color(0x6B3B1F);
    private static final Color POPUP_HEADER = new Color(0x8B5A3C);
    private static final Color POPUP_BTN = new Color(0xA8745A);
    private static final Color POPUP_FIELD_BG = new Color(0xF4F2EF);
    private static final Color POPUP_FIELD_BORDER = new Color(0xE3DDD4);

    private JDialog createFigmaDialog(int w, int h) {
        JDialog d = new JDialog(this, true);
        d.setUndecorated(true);
        d.setSize(w, h);
        d.setLocationRelativeTo(this);

        d.getRootPane().registerKeyboardAction(
                e -> d.dispose(),
                KeyStroke.getKeyStroke("ESCAPE"),
                JComponent.WHEN_IN_FOCUSED_WINDOW
        );

        JPanel wrapper = new JPanel(new GridBagLayout());
        wrapper.setOpaque(false);
        wrapper.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));


        RoundedPanel card = new RoundedPanel(POPUP_BROWN, null, 28);
        card.setLayout(new BorderLayout());
        card.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));

        wrapper.add(card);
        d.setContentPane(wrapper);

        return d;
    }

    private JPanel figmaHeader(String title) {
        JPanel header = new JPanel(new BorderLayout());
        header.setOpaque(false);

        RoundedPanel strip = new RoundedPanel(POPUP_HEADER, null, 28) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                int w = getWidth(), h = getHeight();
                g2.setColor(POPUP_HEADER);
                g2.fillRoundRect(0, 0, w - 1, h + 30, 28, 28); 
                g2.dispose();
                super.paintComponent(g);
            }
        };
        strip.setLayout(new BorderLayout());
        strip.setOpaque(false);
        strip.setPreferredSize(new Dimension(10, 52));
        strip.setBorder(BorderFactory.createEmptyBorder(10, 16, 10, 16));

        JLabel t = new JLabel(title);
        t.setForeground(Color.WHITE);
        t.setFont(new Font("SansSerif", Font.BOLD, 22));
        strip.add(t, BorderLayout.WEST);

        header.add(strip, BorderLayout.CENTER);
        return header;
    }

    private JLabel figmaLabel(String text) {
        JLabel lbl = new JLabel(text);
        lbl.setForeground(Color.WHITE);
        lbl.setFont(new Font("SansSerif", Font.BOLD, 22));
        return lbl;
    }

    private void styleField(JComponent c) {
        c.setFont(new Font("SansSerif", Font.PLAIN, 14));
        c.setBackground(POPUP_FIELD_BG);
        c.setForeground(Color.DARK_GRAY);
        c.setOpaque(true);

        if (c instanceof JTextField tf) {
            tf.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(POPUP_FIELD_BORDER, 2, true),
                    BorderFactory.createEmptyBorder(6, 12, 6, 12)
            ));
        } else if (c instanceof JComboBox<?> cb) {
            cb.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(POPUP_FIELD_BORDER, 2, true),
                    BorderFactory.createEmptyBorder(4, 10, 4, 10)
            ));
        }
    }

    private JButton figmaButton(String text) {
        JButton b = new JButton(text) {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(POPUP_BTN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.setColor(new Color(0x3A1E10));
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(1, 1, getWidth()-3, getHeight()-3, 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }
        };
        b.setOpaque(false);
        b.setContentAreaFilled(false);
        b.setBorderPainted(false);
        b.setFocusPainted(false);
        b.setForeground(Color.WHITE);
        b.setFont(new Font("SansSerif", Font.BOLD, 18));
        b.setPreferredSize(new Dimension(120, 52));
        b.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        return b;
    }

    private LocalDate[] showTimedPopupFigma() {
        var fromPicker = new com.github.lgooddatepicker.components.DatePicker();
        var toPicker   = new com.github.lgooddatepicker.components.DatePicker();

        var fromTf = fromPicker.getComponentDateTextField();
        var toTf   = toPicker.getComponentDateTextField();
        fromTf.setPreferredSize(new Dimension(260, 32));
        toTf.setPreferredSize(new Dimension(260, 32));
        styleField(fromTf);
        styleField(toTf);

        JDialog d = createFigmaDialog(720, 470);
        RoundedPanel card = (RoundedPanel) ((JPanel)d.getContentPane()).getComponent(0);

        card.add(figmaHeader("Select Period"), BorderLayout.NORTH);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(40, 40, 20, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(18, 18, 18, 18);
        gc.anchor = GridBagConstraints.WEST;

        gc.gridx=0; gc.gridy=0;
        body.add(figmaLabel("From Date :"), gc);

        gc.gridx=1; gc.gridy=0;
        body.add(fromPicker, gc);

        gc.gridx=0; gc.gridy=1;
        body.add(figmaLabel("To Date :"), gc);

        gc.gridx=1; gc.gridy=1;
        body.add(toPicker, gc);

        card.add(body, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 26, 20));
        buttons.setOpaque(false);

        JButton ok = figmaButton("OK");
        JButton cancel = figmaButton("Cancel");

        final LocalDate[] result = new LocalDate[2];

        ok.addActionListener(e -> {
            if (fromPicker.getDate() == null || toPicker.getDate() == null) return;
            result[0] = fromPicker.getDate();
            result[1] = toPicker.getDate();
            d.dispose();
        });
        cancel.addActionListener(e -> {
            result[0] = null; result[1] = null;
            d.dispose();
        });

        buttons.add(ok);
        buttons.add(cancel);

        card.add(buttons, BorderLayout.SOUTH);

        d.setVisible(true);

        if (result[0] == null || result[1] == null) return null;
        return result;
    }
    
    private String showCategoryPopupFigma(String[] categories) {
        JDialog d = createFigmaDialog(720, 430);
        RoundedPanel card = (RoundedPanel) ((JPanel)d.getContentPane()).getComponent(0);

        card.add(figmaHeader("Select Period"), BorderLayout.NORTH);

        JComboBox<String> combo = new JComboBox<>(categories);
        combo.setPreferredSize(new Dimension(420, 32));
        styleField(combo);

        JPanel body = new JPanel(new GridBagLayout());
        body.setOpaque(false);
        body.setBorder(BorderFactory.createEmptyBorder(60, 40, 20, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(18, 18, 18, 18);
        gc.anchor = GridBagConstraints.CENTER;

        JLabel lbl = figmaLabel("Select Category");
        gc.gridx=0; gc.gridy=0;
        body.add(lbl, gc);

        gc.gridx=0; gc.gridy=1;
        body.add(combo, gc);

        card.add(body, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new FlowLayout(FlowLayout.CENTER, 26, 20));
        buttons.setOpaque(false);

        JButton ok = figmaButton("OK");
        JButton cancel = figmaButton("Cancel");

        final String[] result = new String[1];

        ok.addActionListener(e -> { result[0] = (String) combo.getSelectedItem(); d.dispose(); });
        cancel.addActionListener(e -> { result[0] = null; d.dispose(); });

        buttons.add(ok);
        buttons.add(cancel);

        card.add(buttons, BorderLayout.SOUTH);

        d.setVisible(true);
        return result[0];
    }
    
    private void positionCategoryCountBadge(JPanel countField, JPanel countSquare) {
        int badgeW = s(80), badgeH = s(60);
        int gap = s(20);

        int badgeX = countField.getX() + countField.getWidth() + gap;
        int badgeY = countField.getY() + (countField.getHeight() - badgeH) / 2;

        countSquare.setBounds(badgeX, badgeY, badgeW, badgeH);
    }
    
    private void exportGeneralReportToCSV() {

        if (lblGenReportId == null || "-".equals(lblGenReportId.getText())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please generate the General Report before exporting.",
                    "Export Not Allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save General Report as CSV");
        chooser.setSelectedFile(
                new java.io.File("GeneralReport_" + lblGenReportId.getText() + ".csv")
        );

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            file = new java.io.File(path + ".csv");
        }

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                file, java.nio.charset.StandardCharsets.UTF_8)) {

            pw.println("MaKSAd General Report");
            pw.println("Report ID," + lblGenReportId.getText());
            pw.println("Generated On," + lblGenDate.getText());
            pw.println("Generated By," + lblGenBy.getText());
            pw.println();

            pw.println("Metric,Value");
            pw.println("Total Volunteers," + lblGenTotalVolunteers.getText());
            pw.println("Total Organizers," + lblGenTotalOrganizers.getText());
            pw.println("Total Events," + lblGenTotalEvents.getText());
            pw.println("Total Certificates," + lblGenTotalCertificates.getText());

            JOptionPane.showMessageDialog(
                    this,
                    "Export successful.\nFile saved as:\n" + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export CSV:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }
    
    private void exportTimedReportToCSV() {

        if (lblTimedReportId == null || "-".equals(lblTimedReportId.getText())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please generate the Timed Report before exporting.",
                    "Export Not Allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Timed Report as CSV");
        chooser.setSelectedFile(
                new java.io.File("TimedReport_" + lblTimedReportId.getText() + ".csv")
        );

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            file = new java.io.File(path + ".csv");
        }

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                file, java.nio.charset.StandardCharsets.UTF_8)) {

            pw.println("MaKSAd Timed Report");
            pw.println("Report ID," + lblTimedReportId.getText());
            pw.println("Generated On," + lblTimedDate.getText());
            pw.println("Generated By," + lblTimedBy.getText());
            pw.println("Range," + lblTimedRange.getText());
            pw.println();

            pw.println("Metric,Value");
            pw.println("Total Events," + lblTimedEvents.getText());
            pw.println("Total Participants," + lblTimedParticipants.getText());

            JOptionPane.showMessageDialog(
                    this,
                    "Export successful.\nFile saved as:\n" + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export CSV:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void exportCategoryReportToCSV() {

        if (lblCatReportId == null || "-".equals(lblCatReportId.getText())) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please generate the Category Report before exporting.",
                    "Export Not Allowed",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Category Report as CSV");
        chooser.setSelectedFile(
                new java.io.File("CategoryReport_" + lblCatReportId.getText() + ".csv")
        );

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            file = new java.io.File(path + ".csv");
        }

        try (java.io.PrintWriter pw = new java.io.PrintWriter(
                file, java.nio.charset.StandardCharsets.UTF_8)) {

            pw.println("MaKSAd Category Report");
            pw.println("Report ID," + lblCatReportId.getText());
            pw.println("Generated On," + lblCatDate.getText());
            pw.println("Generated By," + lblCatBy.getText());
            pw.println("Category," + lblCatCategory.getText());
            pw.println();

            pw.println("Metric,Value");
            pw.println("Total Events," + lblCatCount.getText());

            JOptionPane.showMessageDialog(
                    this,
                    "Export successful.\nFile saved as:\n" + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export CSV:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }

    private void exportReportsHistoryToCSV() {

        JFileChooser chooser = new JFileChooser();
        chooser.setDialogTitle("Save Reports History as CSV");
        chooser.setSelectedFile(new java.io.File("ReportsHistory.csv"));

        int result = chooser.showSaveDialog(this);
        if (result != JFileChooser.APPROVE_OPTION) return;

        java.io.File file = chooser.getSelectedFile();
        String path = file.getAbsolutePath();
        if (!path.toLowerCase().endsWith(".csv")) {
            file = new java.io.File(path + ".csv");
        }

        String sql = "SELECT report_id, report_type, report_title, date_generated FROM reports ORDER BY report_id DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery();
             java.io.PrintWriter pw = new java.io.PrintWriter(file, java.nio.charset.StandardCharsets.UTF_8)) {

            pw.println("Report ID,Type,Title,Generated On");

            while (rs.next()) {
                int id = rs.getInt("report_id");
                String type = rs.getString("report_type");
                String title = rs.getString("report_title");
                Timestamp ts = rs.getTimestamp("date_generated");

                pw.printf("\"%d\",\"%s\",\"%s\",\"%s\"%n",
                        id,
                        type == null ? "" : type.replace("\"", "\"\""),
                        title == null ? "" : title.replace("\"", "\"\""),
                        ts == null ? "" : ts.toString()
                );
            }

            JOptionPane.showMessageDialog(
                    this,
                    "Export successful.\nFile saved as:\n" + file.getAbsolutePath(),
                    "Export Complete",
                    JOptionPane.INFORMATION_MESSAGE
            );

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(
                    this,
                    "Failed to export CSV:\n" + ex.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    private void showTimedEntriesPopup() {

        String sql =
                "SELECT event_id, name, event_date, volunteers, status " +
                "FROM events " +
                "WHERE event_date BETWEEN ? AND ? " +
                "ORDER BY event_date ASC, event_id ASC";

        showEntriesDialog(
                "Timed Report Entries (" + lastTimedFrom + " to " + lastTimedTo + ")",
                sql,
                Date.valueOf(lastTimedFrom),
                Date.valueOf(lastTimedTo)
        );
    }

    private void showCategoryEntriesPopup() {

        String sql =
                "SELECT event_id, name, event_date, volunteers, status " +
                "FROM events " +
                "WHERE category = ? " +
                "ORDER BY event_date ASC, event_id ASC";

        showEntriesDialog(
                "Category Report Entries (" + lastCategory + ")",
                sql,
                lastCategory
        );
    }

    private void showEntriesDialog(String title, String sql, Object... params) {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.length; i++) {
                ps.setObject(i + 1, params[i]);
            }

            DefaultTableModel model = new DefaultTableModel(
                    new Object[]{"Event ID", "Name", "Date", "Volunteers", "Status"}, 0
            ) {
                @Override public boolean isCellEditable(int r, int c) { return false; }
            };

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    model.addRow(new Object[]{
                            rs.getInt("event_id"),
                            rs.getString("name"),
                            rs.getDate("event_date"),
                            rs.getInt("volunteers"),
                            rs.getString("status")
                    });
                }
            }

            JTable table = new JTable(model);
            table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
            table.setRowHeight(34);
            table.setFont(new Font("SansSerif", Font.PLAIN, 14));
            table.setGridColor(new Color(0xB9B2A7));
            table.setShowGrid(true);
            table.setFillsViewportHeight(true);
            table.getTableHeader().setUI(null);
            table.setSelectionBackground(new Color(0xDDD6C7));

            table.getColumnModel().getColumn(0).setPreferredWidth(90);
            table.getColumnModel().getColumn(1).setPreferredWidth(260);
            table.getColumnModel().getColumn(2).setPreferredWidth(140);
            table.getColumnModel().getColumn(3).setPreferredWidth(120);
            table.getColumnModel().getColumn(4).setPreferredWidth(140);

            JScrollPane scroll = new JScrollPane(table);
            scroll.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            scroll.setBorder(BorderFactory.createEmptyBorder());
            scroll.getViewport().setBackground(new Color(0xEFECE6));

            JDialog d = createFigmaDialog(980, 560);
            d.setBackground(new Color(0, 0, 0, 0));

            RoundedPanel card = (RoundedPanel) ((JPanel) d.getContentPane()).getComponent(0);
            card.setLayout(new BorderLayout());
            card.setBackground(new Color(0, 0, 0, 0));

            RoundedPanel green = new RoundedPanel(new Color(0x3E4F2A), null, 28);
            green.setLayout(new BorderLayout());
            green.setBorder(BorderFactory.createEmptyBorder(18, 18, 18, 18));

            green.add(figmaHeader(title), BorderLayout.NORTH);

            RoundedPanel tableBg = new RoundedPanel(new Color(0xEFECE6), null, 18);
            tableBg.setLayout(new BorderLayout());
            tableBg.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            tableBg.add(scroll, BorderLayout.CENTER);

            green.add(tableBg, BorderLayout.CENTER);

            JButton btnClose = createMenuButton("Close");
            btnClose.addActionListener(e -> d.dispose());

            JPanel closeRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
            closeRow.setOpaque(false);
            closeRow.add(btnClose);

            JPanel south = new JPanel();
            south.setOpaque(false);
            south.setLayout(new BoxLayout(south, BoxLayout.Y_AXIS));
            south.add(Box.createVerticalStrut(12));
            south.add(closeRow);

            green.add(south, BorderLayout.SOUTH);

            card.add(green, BorderLayout.CENTER);

            d.setLocationRelativeTo(this);
            d.pack();
            d.setVisible(true);

        } catch (SQLException ex) {
            showDbError(ex);
        }
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
