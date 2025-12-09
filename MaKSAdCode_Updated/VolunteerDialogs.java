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

public class VolunteerDialogs {

    // shared colors
    private static final Color COLOR_BG   = Color.decode("#FFFADD");
    private static final Color COLOR_DARK = Color.decode("#263717");
    private static final Color COLOR_CARD = Color.decode("#74835A");

    // event row from the events table
    static class DBEvent {
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
            this.eventId = eventId;
            this.name = name;
            this.date = date;
            this.startTime = startTime;
            this.endTime = endTime;
            this.location = location;
            this.description = description;
            this.status = status;
            this.volunteers = volunteers;
        }

        @Override
        public String toString() {
            return name + " (" + date + ")";
        }
    }

    // upcoming participation that is still UNSET (used for cancel)
    static class PendingParticipation {
        String eventName;
        LocalDate eventDate;

        PendingParticipation(String eventName, LocalDate eventDate) {
            this.eventName = eventName;
            this.eventDate = eventDate;
        }

        @Override
        public String toString() {
            return eventName + " (" + eventDate + ")";
        }
    }

    // participation row used for the history table
    static class ParticipationRow {
        String eventName;
        LocalDate eventDate;
        LocalTime startTime;
        LocalTime endTime;

        ParticipationRow(String n, LocalDate d, LocalTime s, LocalTime e) {
            eventName = n;
            eventDate = d;
            startTime = s;
            endTime = e;
        }
    }

    // EventDialog (Request to Join)
    static class EventDialog extends JDialog {
        private final Volunteer parent;
        private DefaultListModel<DBEvent> model = new DefaultListModel<>();
        private JList<DBEvent> list;

        // detail labels for the selected event
        private JLabel lblName = new JLabel("-");
        private JLabel lblDate = new JLabel("-");
        private JLabel lblTime = new JLabel("-");
        private JLabel lblLocation = new JLabel("-");
        private JLabel lblStatus = new JLabel("-");
        private JLabel lblVolunteers = new JLabel("-");
        private JTextArea txtDescription = new JTextArea(5, 20);

        EventDialog(Volunteer parent) {
            super(parent, "Request to Join", true);
            this.parent = parent;

            setSize(720, 460);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(COLOR_BG);
            setLayout(new BorderLayout(10, 10));

            // top header bar
            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(COLOR_DARK);
            top.setBorder(new EmptyBorder(8, 18, 8, 18));
            JLabel title = new JLabel("Choose an event to join");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setHorizontalAlignment(SwingConstants.CENTER);
            top.add(title, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);

            // load list of events from DB
            loadEventsFromDB();
            list = new JList<>(model);
            list.setSelectionBackground(Color.decode("#B0BE97"));
            list.setSelectionForeground(Color.BLACK);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            // when the user selects an event, update the right details panel
            list.addListSelectionListener(e -> {
                if (!e.getValueIsAdjusting()) {
                    DBEvent ev = list.getSelectedValue();
                    if (ev != null) showEventDetails(ev);
                }
            });

            // left side: list of events
            JPanel leftPanel = new JPanel(new BorderLayout());
            leftPanel.setOpaque(false);
            leftPanel.setBorder(new EmptyBorder(10, 12, 10, 6));

            JLabel listLabel = new JLabel("Available Events");
            listLabel.setFont(new Font("SansSerif", Font.BOLD, 13));
            leftPanel.add(listLabel, BorderLayout.NORTH);

            JScrollPane listScroll = new JScrollPane(list);
            listScroll.setBorder(BorderFactory.createLineBorder(COLOR_DARK, 1));
            leftPanel.add(listScroll, BorderLayout.CENTER);
            leftPanel.setPreferredSize(new Dimension(230, 0));

            // right side: card with full event details
            JPanel detailsCard = new JPanel();
            detailsCard.setBackground(COLOR_CARD);
            detailsCard.setBorder(new EmptyBorder(14, 18, 14, 18));
            detailsCard.setLayout(new BoxLayout(detailsCard, BoxLayout.Y_AXIS));

            txtDescription.setLineWrap(true);
            txtDescription.setWrapStyleWord(true);
            txtDescription.setEditable(false);
            txtDescription.setFont(new Font("SansSerif", Font.PLAIN, 12));

            detailsCard.add(makeDetailRow("Name:", lblName));
            detailsCard.add(makeDetailRow("Date:", lblDate));
            detailsCard.add(makeDetailRow("Time:", lblTime));
            detailsCard.add(makeDetailRow("Location:", lblLocation));
            detailsCard.add(makeDetailRow("Status:", lblStatus));
            detailsCard.add(makeDetailRow("Volunteers:", lblVolunteers));
            detailsCard.add(Box.createVerticalStrut(8));

            JLabel descLabel = new JLabel("Description:");
            descLabel.setForeground(Color.WHITE);
            descLabel.setFont(new Font("SansSerif", Font.BOLD, 12));
            detailsCard.add(descLabel);
            detailsCard.add(Box.createVerticalStrut(4));

            JScrollPane descScroll = new JScrollPane(txtDescription);
            descScroll.setPreferredSize(new Dimension(320, 120));
            detailsCard.add(descScroll);

            JPanel center = new JPanel(new BorderLayout());
            center.setOpaque(false);
            center.add(leftPanel, BorderLayout.WEST);
            center.add(detailsCard, BorderLayout.CENTER);
            add(center, BorderLayout.CENTER);

            // bottom buttons (Close / Join)
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.setBorder(new EmptyBorder(4, 18, 10, 18));

            JButton closeBtn = parent.createSmallButton("Close");
            JButton joinBtn = parent.createMainButton("Join Event");

            closeBtn.addActionListener(e -> dispose());
            joinBtn.addActionListener(e -> joinSelectedEvent());

            JPanel btnRow = new JPanel();
            btnRow.setOpaque(false);
            btnRow.add(closeBtn);
            btnRow.add(Box.createHorizontalStrut(8));
            btnRow.add(joinBtn);

            bottom.add(btnRow, BorderLayout.EAST);
            add(bottom, BorderLayout.SOUTH);

            // auto-select first event if list is not empty
            if (!model.isEmpty()) list.setSelectedIndex(0);

            setVisible(true);
        }

        // one line of label + value inside the card
        private JPanel makeDetailRow(String labelText, JLabel valueLabel) {
            JPanel row = new JPanel(new BorderLayout(4, 4));
            row.setOpaque(false);
            JLabel lbl = new JLabel(labelText);
            lbl.setFont(new Font("SansSerif", Font.BOLD, 12));
            lbl.setForeground(Color.WHITE);
            valueLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            valueLabel.setForeground(Color.WHITE);
            row.add(lbl, BorderLayout.WEST);
            row.add(valueLabel, BorderLayout.CENTER);
            return row;
        }

        // update the right panel with the selected event information
        private void showEventDetails(DBEvent ev) {
            lblName.setText(ev.name);
            lblDate.setText(ev.date.toString());
            lblTime.setText(ev.startTime + " - " + ev.endTime);
            lblLocation.setText(ev.location);
            lblStatus.setText(ev.status);
            lblVolunteers.setText(String.valueOf(ev.volunteers));
            txtDescription.setText(ev.description != null ? ev.description : "-");
            txtDescription.setCaretPosition(0);
        }

        // load all eligible events that the volunteer can join
        private void loadEventsFromDB() {
            model.clear();
            String sql = """
                    SELECT event_id, name, event_date, start_time, end_time,
                           location, description, status, volunteers
                    FROM events
                    WHERE status = 'APPROVED'
                      AND event_date >= CURDATE()
                      AND event_date NOT IN (
                          SELECT event_date
                          FROM volunteer_participations
                          WHERE volunteer_id = ?
                            AND status <> 'CANCELED'
                      )
                    ORDER BY event_date
                    """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, parent.getVolunteerId());
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
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Error loading events:\n" + ex.getMessage());
                parent.appendLogLine("Error loading events: " + ex.getMessage());
            }
        }

        // insert the selected event into volunteer_participations and update events.volunteers
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
                ps1.setInt(1, parent.getVolunteerId());
                ps1.setString(2, parent.getVolunteerName());
                ps1.setString(3, ev.name);
                ps1.setDate(4, java.sql.Date.valueOf(ev.date));
                ps1.setString(5, "Volunteer");
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(updateEventSql);
                ps2.setInt(1, ev.eventId);
                ps2.executeUpdate();

                conn.commit();

                parent.appendLogLine("Joined event: " + ev.name);
                parent.refreshSummaryPublic();
                dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Error joining event:\n" + ex.getMessage());
                parent.appendLogLine("Error joining event: " + ex.getMessage());
            }
        }
    }

    // CancelJoinDialog 
    static class CancelJoinDialog extends JDialog {
        private final Volunteer parent;
        private DefaultListModel<PendingParticipation> model = new DefaultListModel<>();
        private JList<PendingParticipation> list;

        CancelJoinDialog(Volunteer parent) {
            super(parent, "Cancel Upcoming Join", true);
            this.parent = parent;

            setSize(520, 360);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(COLOR_BG);
            setLayout(new BorderLayout(8, 8));

            // header bar
            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(COLOR_DARK);
            top.setBorder(new EmptyBorder(8, 18, 8, 18));
            JLabel title = new JLabel("Select a future event to cancel your join");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 15));
            title.setHorizontalAlignment(SwingConstants.CENTER);
            top.add(title, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);

            // load list of upcoming UNSET participations
            loadPending();

            list = new JList<>(model);
            list.setSelectionBackground(Color.decode("#B0BE97"));
            list.setSelectionForeground(Color.BLACK);
            list.setBorder(new EmptyBorder(6, 10, 6, 10));

            JScrollPane scroll = new JScrollPane(list);
            scroll.setBorder(BorderFactory.createEmptyBorder(10, 18, 10, 18));
            add(scroll, BorderLayout.CENTER);

            // bottom buttons: (Close \ Cancel Selected)
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.setBorder(new EmptyBorder(4, 18, 10, 18));

            JButton closeBtn = parent.createSmallButton("Close");
            JButton cancelBtn = parent.createMainButton("Cancel Selected");

            closeBtn.addActionListener(e -> dispose());
            cancelBtn.addActionListener(e -> cancelSelected());

            JPanel btnRow = new JPanel();
            btnRow.setOpaque(false);
            btnRow.add(closeBtn);
            btnRow.add(Box.createHorizontalStrut(8));
            btnRow.add(cancelBtn);

            bottom.add(btnRow, BorderLayout.EAST);
            add(bottom, BorderLayout.SOUTH);

            setVisible(true);
        }

        // load all upcoming UNSET participations for this volunteer
        private void loadPending() {
            model.clear();
            String sql = """
                    SELECT event_name, event_date
                    FROM volunteer_participations
                    WHERE volunteer_id = ?
                      AND status = 'UNSET'
                      AND event_date >= CURDATE()
                    ORDER BY event_date
                    """;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setInt(1, parent.getVolunteerId());
                ResultSet rs = ps.executeQuery();

                while (rs.next()) {
                    model.addElement(new PendingParticipation(
                            rs.getString("event_name"),
                            rs.getDate("event_date").toLocalDate()
                    ));
                }
            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Error loading pending joins:\n" + ex.getMessage());
                parent.appendLogLine("Error loading pending joins: " + ex.getMessage());
            }
        }

        // remove the selected pending participation and decrement volunteers counter
        private void cancelSelected() {
            PendingParticipation p = list.getSelectedValue();
            if (p == null) return;

            String deleteSql = """
                    DELETE FROM volunteer_participations
                    WHERE volunteer_id = ?
                      AND event_name = ?
                      AND event_date = ?
                      AND status = 'UNSET'
                    LIMIT 1
                    """;

            String decSql = """
                    UPDATE events
                    SET volunteers = GREATEST(volunteers - 1, 0)
                    WHERE name = ?
                    """;

            try (Connection conn = DBConnection.getConnection()) {
                conn.setAutoCommit(false);

                PreparedStatement ps1 = conn.prepareStatement(deleteSql);
                ps1.setInt(1, parent.getVolunteerId());
                ps1.setString(2, p.eventName);
                ps1.setDate(3, java.sql.Date.valueOf(p.eventDate));
                ps1.executeUpdate();

                PreparedStatement ps2 = conn.prepareStatement(decSql);
                ps2.setString(1, p.eventName);
                ps2.executeUpdate();

                conn.commit();

                parent.appendLogLine("Cancelled join: " + p.eventName);
                parent.refreshSummaryPublic();
                dispose();

            } catch (Exception ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Error cancelling join:\n" + ex.getMessage());
                parent.appendLogLine("Error cancelling join: " + ex.getMessage());
            }
        }
    }

    // ParticipationTable (history)
    static class ParticipationTable extends JFrame {
        private final Volunteer parent;

        ParticipationTable(Volunteer parent) {
            this.parent = parent;

            setTitle("Participation Records");
            setSize(720, 440);
            setLocationRelativeTo(parent);
            getContentPane().setBackground(COLOR_BG);
            setLayout(new BorderLayout());

            // header bar
            JPanel top = new JPanel(new BorderLayout());
            top.setBackground(COLOR_DARK);
            top.setBorder(new EmptyBorder(8, 18, 8, 18));
            JLabel title = new JLabel("Your Participation History");
            title.setForeground(Color.WHITE);
            title.setFont(new Font("SansSerif", Font.BOLD, 16));
            title.setHorizontalAlignment(SwingConstants.CENTER);
            top.add(title, BorderLayout.CENTER);
            add(top, BorderLayout.NORTH);

            String[] cols = {"Event", "Date", "Start", "End"};
            List<ParticipationRow> rowsData = loadParticipationRows(parent.getVolunteerId());

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
            table.setSelectionBackground(Color.decode("#B0BE97"));
            table.setSelectionForeground(Color.BLACK);
            table.setRowHeight(22);
            table.setFont(new Font("SansSerif", Font.PLAIN, 12));
            table.getTableHeader().setFont(new Font("SansSerif", Font.BOLD, 12));

            JPanel card = new JPanel(new BorderLayout());
            card.setBackground(COLOR_CARD);
            card.setBorder(new EmptyBorder(14, 26, 14, 26));
            card.add(new JScrollPane(table), BorderLayout.CENTER);

            add(card, BorderLayout.CENTER);

            // Close button
            JPanel bottom = new JPanel(new BorderLayout());
            bottom.setOpaque(false);
            bottom.setBorder(new EmptyBorder(6, 18, 10, 18));

            JButton closeBtn = createMainButton("Close");
            closeBtn.addActionListener(e -> dispose());
            bottom.add(closeBtn, BorderLayout.EAST);

            add(bottom, BorderLayout.SOUTH);

            setVisible(true);
        }

        // load all participations (joined events) for this volunteer
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
                ex.printStackTrace();
                JOptionPane.showMessageDialog(parent,
                        "Error loading participation:\n" + ex.getMessage());
                parent.appendLogLine("Error loading participation: " + ex.getMessage());
            }

            return list;
        }

        // local button
        private JButton createMainButton(String text) {
            JButton btn = new JButton(text) {
                @Override
                protected void paintComponent(Graphics g) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING,
                                        RenderingHints.VALUE_ANTIALIAS_ON);
                    g2.setColor(COLOR_DARK);
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 26, 26);
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
}
