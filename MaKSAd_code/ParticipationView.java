package maksad1;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.sql.*;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.regex.Pattern;

public class ParticipationView extends JFrame {

    private JTextField searchField;
    private JTable table;
    private DefaultTableModel model;

    private static final String[] STATUS_OPTIONS = {
            "PRESENT", "ABSENT", "CANCELED", "UNSET"
    };

    public ParticipationView(List<VolunteerParticipation> data) {
        setTitle("Volunteer Participation Records");
        setSize(1000, 550);
        setLayout(new BorderLayout());

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(18);
        
        JButton saveBtn = new JButton("Save Changes");
        // saveBtn.addActionListener(e -> saveChangesToDatabase());
        saveBtn.setPreferredSize(new Dimension(120, 27));

        top.add(new JLabel("Search Volunteer:"));
        top.add(searchField);
        top.add(saveBtn);
        add(top, BorderLayout.NORTH);

        String[] cols = {
                "Volunteer ID",
                "Volunteer Name",
                "Event",
                "Event Date",
                "Check-in",
                "Check-out",
                "Hours",
                "Role",
                "Status"
        };

        Object[][] rows = new Object[data.size()][cols.length];
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

        for (int i = 0; i < data.size(); i++) {
            VolunteerParticipation vp = data.get(i);
            rows[i][0] = vp.getVolunteerId();
            rows[i][1] = vp.getVolunteerName();
            rows[i][2] = vp.getEventName();
            rows[i][3] = vp.getEventDate();   // LocalDate
            rows[i][4] = vp.getCheckInAt()  == null ? null : vp.getCheckInAt().format(dtf);
            rows[i][5] = vp.getCheckOutAt() == null ? null : vp.getCheckOutAt().format(dtf);
            rows[i][6] = vp.getParticipationHours();
            rows[i][7] = vp.getRole();
            rows[i][8] = vp.getStatus().name();
        }

        model = new DefaultTableModel(rows, cols) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 4 || column == 5 || column == 7 || column == 8;
            }
        };

        table = new JTable(model);

        // ComboBox  Status
        JComboBox<String> statusCombo = new JComboBox<>(STATUS_OPTIONS);
        TableColumn statusColumn = table.getColumnModel().getColumn(8);
        statusColumn.setCellEditor(new DefaultCellEditor(statusCombo));

        add(new JScrollPane(table), BorderLayout.CENTER);

        TableRowSorter<TableModel> sorter = new TableRowSorter<>(table.getModel());
        table.setRowSorter(sorter);
        searchField.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            void filter() {
                String q = searchField.getText();
                sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(q), 1));  
            }
            public void insertUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void removeUpdate(javax.swing.event.DocumentEvent e) { filter(); }
            public void changedUpdate(javax.swing.event.DocumentEvent e) { filter(); }
        });
        

        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }


    //  MySQL
//    private void saveChangesToDatabase() {
//        String url = "jdbc:mysql://localhost:3306/maksad";
//        String user = "root";
//        String pass = "your_password";  
//
//        String updateSql =
//                "UPDATE volunteer_participations " +
//                "SET role = ?, status = ?, check_in = ?, check_out = ?, hours = ? " +
//                "WHERE volunteer_id = ? AND event_name = ? AND event_date = ?";
//
//        try (Connection conn = DriverManager.getConnection(url, user, pass);
//             PreparedStatement ps = conn.prepareStatement(updateSql)) {
//
//            DateTimeFormatter dtf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
//
//            for (int i = 0; i < model.getRowCount(); i++) {
//                int volunteerId   = (int)    model.getValueAt(i, 0);
//                String volunteerName = (String) model.getValueAt(i, 1);
//                String eventName  = (String) model.getValueAt(i, 2);
//                java.sql.Date eventDate = java.sql.Date.valueOf(model.getValueAt(i, 3).toString());
//
//                String checkInStr  = (String) model.getValueAt(i, 4);
//                String checkOutStr = (String) model.getValueAt(i, 5);
//
//                Timestamp checkIn  = (checkInStr  == null || checkInStr.isEmpty())
//                        ? null : Timestamp.valueOf(checkInStr.replace('T', ' ').substring(0, 16) + ":00");
//                Timestamp checkOut = (checkOutStr == null || checkOutStr.isEmpty())
//                        ? null : Timestamp.valueOf(checkOutStr.replace('T', ' ').substring(0, 16) + ":00");
//
//                Double hours = null;
//                if (checkIn != null && checkOut != null && checkOut.after(checkIn)) {
//                    long minutes = (checkOut.getTime() - checkIn.getTime()) / (1000 * 60);
//                    hours = minutes / 60.0;
//                }
//
//                String role   = (String) model.getValueAt(i, 7);
//                String status = (String) model.getValueAt(i, 8);
//
//                ps.setString(1, role);
//                ps.setString(2, status);
//                ps.setTimestamp(3, checkIn);
//                ps.setTimestamp(4, checkOut);
//                if (hours == null) ps.setNull(5, java.sql.Types.DOUBLE);
//                else ps.setDouble(5, hours);
//                ps.setInt(6, volunteerId);
//                ps.setString(7, eventName);
//                ps.setDate(8, eventDate);
//
//                ps.addBatch();
//            }
//
//            ps.executeBatch();
//            JOptionPane.showMessageDialog(this, "Changes saved to database.");
//        } catch (Exception ex) {
//            ex.printStackTrace();
//            JOptionPane.showMessageDialog(this, "Error saving changes: " + ex.getMessage());
//        }
//    }
//
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new ParticipationView(new java.util.ArrayList<>());
        });
    }
}




