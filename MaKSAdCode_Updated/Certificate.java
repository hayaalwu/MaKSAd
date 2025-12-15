/**
 *
 * @author hayaa
 */

package maksadpro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class Certificate extends JFrame {

    private static final Color COLOR_BG_MAIN = Color.decode("#FFFADD");
    private static final Color COLOR_DARK    = Color.decode("#263717");
    private static final Color COLOR_CARD    = Color.decode("#74835A");

    private JTable table;
    private final String volunteerName;

    public Certificate(int volunteerId, String volunteerName) {
        this.volunteerName = volunteerName;

        setTitle("Certificates");
        setSize(900, 520);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        getContentPane().setBackground(COLOR_BG_MAIN);
        setLayout(new BorderLayout());

        // top dark header
        JPanel top = new JPanel(new BorderLayout());
        top.setBackground(COLOR_DARK);
        top.setBorder(new EmptyBorder(10, 24, 10, 24));

        JLabel title = new JLabel("Explore Your Certificates, " + volunteerName);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 18));
        title.setHorizontalAlignment(SwingConstants.CENTER);
        top.add(title, BorderLayout.CENTER);

        add(top, BorderLayout.NORTH);

        // center card with table
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(COLOR_CARD);
        card.setBorder(new EmptyBorder(20, 40, 20, 40));

        String[] cols = {"certificate_id", "volunteer_id", "event_name", "issue_date", "hours_earned"};
        Object[][] rows = loadCertificates(volunteerId);

        table = new JTable(rows, cols);
        table.setSelectionBackground(Color.decode("#b0be97")); // selection highlight color
        table.setSelectionForeground(Color.BLACK);

        table.setFillsViewportHeight(true);
        table.setRowHeight(24);
        table.setFont(new Font("SansSerif", Font.PLAIN, 12));
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

        JScrollPane scroll = new JScrollPane(table);
        card.add(scroll, BorderLayout.CENTER);

        add(card, BorderLayout.CENTER);

        // bottom area: logo + buttons
        JPanel bottom = new JPanel(new BorderLayout());
        bottom.setOpaque(false);
        bottom.setBorder(new EmptyBorder(10, 40, 10, 40));

        // left logo
        JLabel logoLabel = new JLabel();
        try {
            ImageIcon logoIcon = new ImageIcon(
                    "C:\\Users\\hayaa\\OneDrive\\Desktop\\MaKSAdpro\\src\\main\\java\\maksadpro\\MaKSAdLogo.png"
            );
            Image scaledLogo = logoIcon.getImage().getScaledInstance(80, 80, Image.SCALE_SMOOTH);
            logoLabel.setIcon(new ImageIcon(scaledLogo));
        } catch (Exception ignored) { }

        bottom.add(logoLabel, BorderLayout.WEST);

        // right buttons (View + Back)
        JButton backBtn = createMainButton("Back");
        backBtn.setPreferredSize(new Dimension(120, 40));
        backBtn.addActionListener(e -> dispose());

        JButton viewBtn = createMainButton("View");
        viewBtn.setPreferredSize(new Dimension(120, 40));
        viewBtn.addActionListener(e -> openSelectedCertificate());

        JPanel btnRow = new JPanel();
        btnRow.setOpaque(false);
        btnRow.add(viewBtn);
        btnRow.add(Box.createHorizontalStrut(16));
        btnRow.add(backBtn);

        bottom.add(btnRow, BorderLayout.EAST);
        add(bottom, BorderLayout.SOUTH);

        setVisible(true);
    }

    // open the detailed view for the selected certificate
    private void openSelectedCertificate() {
        int row = table.getSelectedRow();
        if (row == -1) {
            JOptionPane.showMessageDialog(this, "Please select a certificate first.");
            return;
        }

        String certId   = table.getValueAt(row, 0).toString();
        String volId    = table.getValueAt(row, 1).toString();
        String event    = table.getValueAt(row, 2).toString();
        String issue    = table.getValueAt(row, 3).toString();
        String hoursStr = table.getValueAt(row, 4).toString();

        double hours = 0;
        try {
            hours = Double.parseDouble(hoursStr);
        } catch (NumberFormatException ignored) { }

        new CertificatesView(
                certId, volId, volunteerName, event, issue, hours
        ).setVisible(true);
    }

    // load certificates from the DB for this volunteer
    private Object[][] loadCertificates(int volunteerId) {
        java.util.List<Object[]> list = new ArrayList<>();

        String sql = """
                SELECT certificate_id, volunteer_id, event_name, issue_date, hours_earned
                FROM certificates
                WHERE volunteer_id = ?
                ORDER BY issue_date DESC
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, volunteerId);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                list.add(new Object[]{
                        rs.getInt("certificate_id"),
                        rs.getInt("volunteer_id"),
                        rs.getString("event_name"),
                        rs.getDate("issue_date"),
                        rs.getDouble("hours_earned")
                });
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error loading certificates:\n" + ex.getMessage());
        }

        Object[][] arr = new Object[list.size()][5];
        for (int i = 0; i < list.size(); i++) arr[i] = list.get(i);
        return arr;
    }

    // rounded main button style (consistent with Volunteer UI)
    private JButton createMainButton(String text) {
        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(COLOR_DARK);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 28, 28);
                super.paintComponent(g);
            }
        };
        
        btn.setForeground(Color.WHITE);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setOpaque(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 14));
        btn.setBorder(new EmptyBorder(6, 18, 6, 18));
        return btn;
    }
}

