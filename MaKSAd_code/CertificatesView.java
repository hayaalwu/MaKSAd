import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.BorderLayout;
import java.util.List;

public class CertificatesView extends JFrame {

    public CertificatesView(List<Certificate> certificates) { // [**Reemas**]
        setTitle("Volunteer Certificates");
        setSize(600, 350);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        String[] cols = {"Volunteer", "Event", "Hours", "Issue Date"};
        Object[][] rows = new Object[certificates.size()][cols.length];

        for (int i = 0; i < certificates.size(); i++) {
            Certificate c = certificates.get(i);
            rows[i][0] = c.getVolunteerName();
            rows[i][1] = c.getEventName();
            rows[i][2] = c.getHours();
            rows[i][3] = c.getIssueDate();
        }

        JTable table = new JTable(new DefaultTableModel(rows, cols));
        add(new JScrollPane(table), BorderLayout.CENTER);

        setVisible(true);
    }
}

