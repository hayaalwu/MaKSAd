package maksadpro;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.Map;

public class CertificatesView extends JFrame {

    public CertificatesView(List<Certificate> certificates,
                            Map<Integer, String> volunteerNames) {

        setTitle("Volunteer Certificates");
        setSize(700, 420);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // ===================== TABLE SETUP =====================
        String[] columns = {"Volunteer Name", "Event", "Hours Earned", "Issue Date"};
        Object[][] rows = new Object[certificates.size()][columns.length];

        for (int i = 0; i < certificates.size(); i++) {
            Certificate c = certificates.get(i);

            String volunteerName = volunteerNames.getOrDefault(
                    c.getVolunteerId(),
                    "Unknown"
            );

            rows[i][0] = volunteerName;
            rows[i][1] = c.getEventName();
            rows[i][2] = c.getHoursEarned();
            rows[i][3] = c.getIssueDate().toString();
        }

        JTable table = new JTable(new DefaultTableModel(rows, columns));
        table.setRowHeight(24);
        table.setFillsViewportHeight(true);
        table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 14));

        JScrollPane scrollPane = new JScrollPane(table);
        add(scrollPane, BorderLayout.CENTER);

        setVisible(true);
    }

    // ===================== MAIN (بدون أمثلة) =====================
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new CertificatesView(
                    java.util.Collections.emptyList(),
                    java.util.Collections.emptyMap()
            );
        });
    }
}