/**
 *
 * @author hayaa
 */

package maksadpro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class Volunteer extends JFrame { // main volunteer dashboard window

    private static final Color COLOR_BG_MAIN = Color.decode("#FFFADD"); // main background color
    private static final Color COLOR_DARK = Color.decode("#263717"); // dark green for header and panels
    private static final Color COLOR_CARD = Color.decode("#74835A"); // cards background color
    private static final Color COLOR_CARD_BORDER = Color.decode("#5A5A3A"); // border color for cards
    private static final Color COLOR_BTN = Color.decode("#263717"); // button background color
    private static final Color COLOR_LOG_BG = Color.decode("#F2F2EE"); // log text area background
    private static final Color COLOR_INTEREST_BG = Color.decode("#263717"); // interests panel outer background
    private static final Color COLOR_INTEREST_INNER = Color.decode("#F2F2EE"); // interests list inner background

    private static final double HOURS_GOAL = 200.0; // target volunteer hours for the progress bar

    private final MaKSAdUserSystem.AuthenticatedUser user; // logged-in volunteer user

    private JLabel lblTotalHours = new JLabel("0"); // shows total hours
    private JLabel lblUpcoming = new JLabel("0"); // shows count of upcoming events
    private JLabel lblJoined = new JLabel("0"); // shows count of joined events
    private JLabel lblCertificates = new JLabel("0"); // shows count of certificates

    private JProgressBar hoursProgress = new JProgressBar(0, 100); // progress bar for hours goal

    private JTextArea log = new JTextArea(7, 40); // log area for actions and errors

    private DefaultListModel<String> interestModel = new DefaultListModel<>(); // list model for interests
    private JTextField interestInput = new JTextField(); // input field to add new interest

    public Volunteer(MaKSAdUserSystem.AuthenticatedUser user) { // constructor receives authenticated user
        this.user = user;
        initFrame(); // build the UI
    }

    private void initFrame() { // build main frame and containers
        setTitle("MaKSAd Volunteer Dashboard - " + user.getName()); // window title with user name
        setDefaultCloseOperation(EXIT_ON_CLOSE); // close app when window closes
        setSize(1200, 900); // window size
        setLocationRelativeTo(null); // center on screen

        JPanel root = new JPanel(new BorderLayout()); // root layout
        root.setBackground(COLOR_BG_MAIN);
        setContentPane(root); // set root as content

        root.add(createHeader(), BorderLayout.NORTH); // top header
        root.add(createCenterContent(), BorderLayout.CENTER); // middle content
        root.add(createBottomBar(), BorderLayout.SOUTH); // bottom logout bar

        refreshSummary(); // load stats from DB
        loadInterestsFromDB(); // load saved interests from DB into the list
    }

    private JComponent createHeader() {
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(COLOR_DARK);
        header.setBorder(new EmptyBorder(10, 24, 10, 24));

        String logoPath = "C:\\Users\\hayaa\\OneDrive\\Desktop\\MaKSAdpro\\src\\main\\java\\maksadpro\\MaKSAdLogo.png"; // logo image path
        ImageIcon rawIcon = new ImageIcon(logoPath); // original logo image
        Image scaledImg = rawIcon.getImage().getScaledInstance(50, 50, Image.SCALE_SMOOTH); // resize to small icon
        ImageIcon logoIcon = new ImageIcon(scaledImg); // scaled logo icon

        JLabel logoLabel = new JLabel(logoIcon);
        logoLabel.setBorder(new EmptyBorder(0, 0, 0, 12)); // spacing from title
        logoLabel.setVerticalAlignment(SwingConstants.TOP); // align to top
        header.add(logoLabel, BorderLayout.WEST);

        logoLabel.setBorder(new EmptyBorder(0, 0, 0, 16)); // extra left spacing
        header.add(logoLabel, BorderLayout.WEST);

        JLabel title = new JLabel("Welcome, " + user.getName() + ". We are happy to have you on MaKSAd!"); // welcome message
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 20));
        title.setHorizontalAlignment(SwingConstants.CENTER);

        header.add(title, BorderLayout.CENTER);
        return header;
    }

    private JComponent createCenterContent() { // main middle area (stats + buttons + log)
        JPanel container = new JPanel(new BorderLayout());
        container.setOpaque(false);
        container.setBorder(new EmptyBorder(18, 24, 24, 24)); // outer margin

        JPanel middleRow = new JPanel(new BorderLayout());
        middleRow.setOpaque(false);

        middleRow.add(createLeftColumn(), BorderLayout.WEST); // stats + interests
        middleRow.add(createRightButtons(), BorderLayout.EAST); // action buttons

        container.add(middleRow, BorderLayout.CENTER);
        container.add(createActiveLogPanel(), BorderLayout.SOUTH); // activity log at bottom

        return container;
    }

    private JComponent createLeftColumn() { // left column with cards and interests
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS)); // vertical layout

        JPanel statsPanel = new JPanel();
        statsPanel.setOpaque(false);
        statsPanel.setLayout(new BoxLayout(statsPanel, BoxLayout.Y_AXIS));

        // order: Hours - Upcoming - Joined - Certificates
        statsPanel.add(createHoursCard());
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(createSimpleStatCard("Upcoming Events", lblUpcoming));
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(createSimpleStatCard("Joined Events", lblJoined));
        statsPanel.add(Box.createVerticalStrut(10));
        statsPanel.add(createSimpleStatCard("Certificates", lblCertificates));

        statsPanel.add(Box.createVerticalStrut(16));

        left.add(statsPanel);
        left.add(Box.createVerticalStrut(25));
        left.add(createInterestsPanel()); // interests section

        return left;
    }

    private JPanel createCardShell() { // base style for stat cards
        JPanel p = new JPanel();
        p.setBackground(COLOR_CARD);
        p.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(COLOR_CARD_BORDER, 1), // border line
                new EmptyBorder(8, 12, 8, 12) // inner padding
        ));
        p.setLayout(new BorderLayout());
        p.setPreferredSize(new Dimension(220, 55)); // card size
        return p;
    }

    private JComponent createHoursCard() { // card for total volunteer hours and progress bar
        JPanel card = createCardShell();

        JLabel title = new JLabel("Total Volunteer Hours");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));

        lblTotalHours.setForeground(Color.WHITE);
        lblTotalHours.setFont(new Font("SansSerif", Font.BOLD, 18));
        lblTotalHours.setHorizontalAlignment(SwingConstants.CENTER);

        hoursProgress.setStringPainted(true); // show percentage text
        hoursProgress.setForeground(Color.decode("#3C5C2A"));
        hoursProgress.setBackground(Color.WHITE);

        JPanel top = new JPanel(new BorderLayout());
        top.setOpaque(false);
        top.add(title, BorderLayout.WEST);
        top.add(lblTotalHours, BorderLayout.EAST);

        card.add(top, BorderLayout.NORTH);
        card.add(hoursProgress, BorderLayout.SOUTH);

        return card;
    }

    private JComponent createSimpleStatCard(String titleText, JLabel valueLabel) { // generic card for stats
        JPanel card = createCardShell();

        JLabel title = new JLabel(titleText);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 13));

        valueLabel.setForeground(Color.WHITE);
        valueLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        valueLabel.setHorizontalAlignment(SwingConstants.CENTER);

        card.add(title, BorderLayout.NORTH);
        card.add(valueLabel, BorderLayout.CENTER);

        return card;
    }

    private JComponent createInterestsPanel() { // interests list panel
        JPanel outer = new JPanel(new BorderLayout(6, 6));
        outer.setBackground(COLOR_INTEREST_BG);
        outer.setBorder(BorderFactory.createLineBorder(Color.BLACK, 1));
        outer.setPreferredSize(new Dimension(260, 150));

        JLabel title = new JLabel("Interests");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(4, 8, 0, 8));

        JList<String> list = new JList<>(interestModel); // list bound to interestModel
        list.setSelectionBackground(Color.decode("#B0BE97"));
        list.setSelectionForeground(Color.BLACK);
        JScrollPane scroll = new JScrollPane(list);
        scroll.getViewport().setBackground(COLOR_INTEREST_INNER);
        scroll.setBorder(BorderFactory.createEmptyBorder());

        JPanel inner = new JPanel(new BorderLayout());
        inner.setBackground(COLOR_INTEREST_INNER);
        inner.setBorder(new EmptyBorder(4, 8, 4, 8));
        inner.add(scroll, BorderLayout.CENTER);

        JPanel bottom = new JPanel(new BorderLayout(4, 4));
        bottom.setOpaque(false);

        JButton addBtn = createSmallButton("Add"); // button to add interest
        addBtn.addActionListener(e -> { // when user clicks Add
            String text = interestInput.getText().trim(); // get new interest text
            if (text.isEmpty()) return; // ignore empty input

            if (interestModel.contains(text)) { // avoid duplicate interests
                JOptionPane.showMessageDialog(Volunteer.this,
                        "This interest already exists.");
                return;
            }

            interestModel.addElement(text); // add to UI list

            if (updateInterestsInDB()) { // save all interests to maksad_users.interests
                interestInput.setText(""); // clear only if DB update success
                log.append("Added interest: " + text + "\n"); // log action
            } else {
                interestModel.removeElement(text); // rollback if DB failed
            }
        });

        bottom.add(interestInput, BorderLayout.CENTER);
        bottom.add(addBtn, BorderLayout.EAST);
        bottom.setBorder(new EmptyBorder(4, 8, 6, 8));

        outer.add(title, BorderLayout.NORTH);
        outer.add(inner, BorderLayout.CENTER);
        outer.add(bottom, BorderLayout.SOUTH);

        return outer;
    }

    private JComponent createRightButtons() { // main action buttons column
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(20, 40, 0, 0));

        JButton requestBtn = createMainButton("Request to Join"); // open events to join
        JButton cancelBtn = createMainButton("Cancel Join"); // cancel upcoming join
        JButton viewPartBtn = createMainButton("View Participation"); // show participation history
        JButton viewCertBtn = createMainButton("View Certificate"); // show certificates

        requestBtn.addActionListener(e -> new VolunteerDialogs.EventDialog(this)); // show event selection dialog
        cancelBtn.addActionListener(e -> new VolunteerDialogs.CancelJoinDialog(this)); // show cancel dialog
        viewPartBtn.addActionListener(e -> new VolunteerDialogs.ParticipationTable(this)); // open participation table
        viewCertBtn.addActionListener(e -> new Certificate(user.getId(), user.getName())); // open certificates table

        right.add(requestBtn);
        right.add(Box.createVerticalStrut(16));
        right.add(cancelBtn);
        right.add(Box.createVerticalStrut(16));
        right.add(viewPartBtn);
        right.add(Box.createVerticalStrut(16));
        right.add(viewCertBtn);

        return right;
    }

    private JComponent createActiveLogPanel() { // bottom log area
        JPanel panel = new JPanel(new BorderLayout());
        panel.setOpaque(false);
        panel.setBorder(new EmptyBorder(30, 60, 0, 60));

        JLabel title = new JLabel("Active Log");
        title.setForeground(COLOR_DARK);
        title.setFont(new Font("SansSerif", Font.BOLD, 14));
        title.setBorder(new EmptyBorder(0, 8, 4, 0));

        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_DARK);
        card.setBorder(new EmptyBorder(10, 10, 10, 10));

        log.setEditable(false); // user cannot type here
        log.setBackground(COLOR_LOG_BG);
        log.setFont(new Font("Consolas", Font.PLAIN, 13));

        card.add(new JScrollPane(log), BorderLayout.CENTER);

        panel.add(title, BorderLayout.NORTH);
        panel.add(card, BorderLayout.CENTER);

        return panel;
    }

    private JComponent createBottomBar() { // bottom bar with logout button
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(4, 24, 10, 24));

        JButton logoutBtn = createSmallButton("Logout"); // logout button
        logoutBtn.setPreferredSize(new Dimension(90, 32));

        // close this frame and go back to login
        logoutBtn.addActionListener(e -> {
            dispose(); // close Volunteer window
            new LoginFrame().setVisible(true); // open login screen
        });

        bottom.add(logoutBtn, BorderLayout.EAST);
        return bottom;
    }

    public JButton createMainButton(String text) { // big rounded main button style
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) { // custom painting for rounded button
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false); // we handle background ourselves
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));
        btn.setPreferredSize(new Dimension(210, 46));
        btn.setMaximumSize(new Dimension(210, 46));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setHorizontalAlignment(SwingConstants.CENTER);
        btn.setBorder(new EmptyBorder(6, 18, 6, 18));
        return btn;
    }

    public JButton createSmallButton(String text) { // smaller rounded button style
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) { // custom painting for small button
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_BTN);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                super.paintComponent(g);
            }
        };
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 13));
        return btn;
    }

    private void refreshSummary() { // load volunteer stats from database
        double hours = 0.0; // total hours
        int joined = 0; // total joined events
        int upcoming = 0; // upcoming UNSET events
        int certs = 0; // certificates count

        String sqlHours = "SELECT total_hours FROM volunteers WHERE volunteer_id = ?"; // read total hours
        String sqlJoined = "SELECT COUNT(*) FROM volunteer_participations WHERE volunteer_id = ?"; // all participations
        String sqlUpcoming = """
                SELECT COUNT(*)
                FROM volunteer_participations
                WHERE volunteer_id = ?
                  AND status = 'UNSET'
                  AND event_date >= CURDATE()
                """; // only future UNSET events
        String sqlCerts = "SELECT COUNT(*) FROM certificates WHERE volunteer_id = ?"; // certificates count

        try (Connection conn = DBConnection.getConnection()) { // open DB connection

            PreparedStatement ps1 = conn.prepareStatement(sqlHours); // query total hours
            ps1.setInt(1, user.getId());
            ResultSet rs1 = ps1.executeQuery();
            if (rs1.next()) hours = rs1.getDouble(1);

            PreparedStatement ps2 = conn.prepareStatement(sqlJoined); // query joined events count
            ps2.setInt(1, user.getId());
            ResultSet rs2 = ps2.executeQuery();
            if (rs2.next()) joined = rs2.getInt(1);

            PreparedStatement ps3 = conn.prepareStatement(sqlUpcoming); // query upcoming events
            ps3.setInt(1, user.getId());
            ResultSet rs3 = ps3.executeQuery();
            if (rs3.next()) upcoming = rs3.getInt(1);

            PreparedStatement ps4 = conn.prepareStatement(sqlCerts); // query certificates count
            ps4.setInt(1, user.getId());
            ResultSet rs4 = ps4.executeQuery();
            if (rs4.next()) certs = rs4.getInt(1);

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading summary:\n" + ex.getMessage());
            log.append("Error loading summary: " + ex.getMessage() + "\n");
        }

        lblTotalHours.setText(String.format("%.0f", hours)); // show rounded hours
        lblJoined.setText(String.valueOf(joined));
        lblUpcoming.setText(String.valueOf(upcoming));
        lblCertificates.setText(String.valueOf(certs));

        int percent = 0; // progress percentage
        if (HOURS_GOAL > 0) {
            percent = (int) Math.round((hours / HOURS_GOAL) * 100.0); // calculate percent
        }
        if (percent > 100) percent = 100;
        if (percent < 0) percent = 0;

        hoursProgress.setValue(percent);
        hoursProgress.setString(percent + "%");

        if (percent < 30) {
            hoursProgress.setToolTipText("Nice start! Keep going to build your volunteering journey."); // low progress
        } else if (percent < 70) {
            hoursProgress.setToolTipText("You’re making great progress - keep it up!"); // medium progress
        } else {
            hoursProgress.setToolTipText("Go ahead to reach your goal!!"); // high progress
        }
    }

    // DB 
    private void loadInterestsFromDB() { // load interests from maksad_users table
        interestModel.clear(); // clear UI list first

        String sql = "SELECT interests FROM maksad_users WHERE volunteer_id = ?";

        try (Connection conn = DBConnection.getConnection(); // open DB connection
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, user.getId()); // use current volunteer id
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                String interestsStr = rs.getString("interests"); // JSON-like string
                if (interestsStr != null && !interestsStr.trim().isEmpty()) { // not empty
                    String cleaned = interestsStr.replace("[", "").replace("]", ""); // remove []
                    String[] parts = cleaned.split(","); // split by comma

                    for (String raw : parts) {
                        String t = raw.trim(); // trim spaces
                        if (t.startsWith("\"") || t.startsWith("'")) t = t.substring(1); // remove first quote
                        if (t.endsWith("\"") || t.endsWith("'")) t = t.substring(0, t.length() - 1); // remove last quote
                        if (!t.isEmpty()) interestModel.addElement(t); // add each interest to UI list
                    }
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading interests:\n" + ex.getMessage());
            log.append("Error loading interests: " + ex.getMessage() + "\n");
        }
    }

    private boolean updateInterestsInDB() { // save current list of interests to DB
        StringBuilder sb = new StringBuilder("["); // build JSON-like array string

        for (int i = 0; i < interestModel.size(); i++) {
            if (i > 0) sb.append(", "); // add comma between items

            String v = interestModel.getElementAt(i); // get interest text
            v = v.replace("\"", "\\\""); // escape double quotes
            sb.append("\"").append(v).append("\""); // wrap with quotes
        }

        sb.append("]"); // close array

        String sql = "UPDATE maksad_users SET interests = ? WHERE volunteer_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, sb.toString()); // interests as string like ["A","B"]
            ps.setInt(2, user.getId()); // update this volunteer row
            ps.executeUpdate();

            return true; // DB update success

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error saving interests:\n" + ex.getMessage());
            log.append("Error saving interests: " + ex.getMessage() + "\n");
            return false; // DB update failed
        }
    }

    // dialog classes 
    public int getVolunteerId() {
        return user.getId();
    }

    public String getVolunteerName() {
        return user.getName();
    }

    public void appendLogLine(String msg) {
        log.append(msg + "\n");
    }

    public void refreshSummaryPublic() {
        refreshSummary();
    }

}
