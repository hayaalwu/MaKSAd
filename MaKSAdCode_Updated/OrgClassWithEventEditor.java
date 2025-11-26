package maksadpro;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.sql.*;
import java.util.ArrayList;

public class OrgClassWithEventEditor extends JFrame {

    private final int organizerId;

    private JList<String> eventList;
    private ArrayList<EventData> currentEvents = new ArrayList<>();

    private DefaultListModel<String> volunteersListModel;
    private JTextArea detailsArea;

    private JButton deleteEventBtn;
    private JButton editEventBtn;

    private EventData selectedEvent = null;

    private static final Color BG_DARK   = Color.decode("#263717");
    private static final Color BG_LIGHT  = Color.decode("#FFFADD");
    private static final Color BTN_COLOR = Color.decode("#74835A");

    public OrgClassWithEventEditor(int organizerId) {
        this.organizerId = organizerId;

        setTitle("Events Viewer");
        setSize(900, 560);
        setLayout(new BorderLayout());
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        getContentPane().setBackground(BG_DARK);

        loadEventsFromDB();

        add(buildLeftPanel(), BorderLayout.WEST);
        add(buildDetailsPanel(), BorderLayout.CENTER);
        add(buildControlsPanel(), BorderLayout.SOUTH);

        deleteEventBtn.setEnabled(false);
        editEventBtn.setEnabled(false);

        setLocationRelativeTo(null);
        setVisible(true);
    }

    private JPanel buildLeftPanel() {
        JPanel left = new JPanel(new BorderLayout());
        left.setPreferredSize(new Dimension(240, 0));
        left.setBackground(BG_DARK);
        left.setBorder(new EmptyBorder(15, 15, 15, 15));

        left.add(buildEventListPanel(), BorderLayout.CENTER);
        return left;
    }

    private void loadEventsFromDB() {
        currentEvents.clear();

        String sql = """
                SELECT event_id, name, category, location, volunteers,
                       event_date, start_time, end_time, description
                FROM events
                WHERE organizer_id = ?
                ORDER BY event_date DESC, event_id DESC
                """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, organizerId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    currentEvents.add(new EventData(
                            rs.getInt("event_id"),
                            rs.getString("name"),
                            rs.getString("category"),
                            rs.getString("location"),
                            rs.getInt("volunteers"),
                            rs.getString("event_date"),
                            rs.getString("start_time"),
                            rs.getString("end_time"),
                            rs.getString("description")
                    ));
                }
            }

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Error loading events:\n" + ex.getMessage(),
                    "Database Error", JOptionPane.ERROR_MESSAGE);
        }
    }

    private JScrollPane buildEventListPanel() {

        DefaultListModel<String> listModel = new DefaultListModel<>();
        for (EventData ev : currentEvents) {
            listModel.addElement(ev.name);
        }

        eventList = new JList<>(listModel);
        eventList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        eventList.setFont(new Font("Serif", Font.PLAIN, 15));

        eventList.setBackground(BG_LIGHT);
        eventList.setBorder(new EmptyBorder(10, 10, 10, 10));

        eventList.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                int i = eventList.getSelectedIndex();
                if (i != -1) {
                    selectedEvent = currentEvents.get(i);
                    showEventDetails(selectedEvent);
                    deleteEventBtn.setEnabled(true);
                    editEventBtn.setEnabled(true);
                }
            }
        });

        JScrollPane scroll = new JScrollPane(eventList);
        scroll.setBorder(BorderFactory.createTitledBorder("Events"));
        return scroll;
    }

    private JPanel buildDetailsPanel() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBorder(new EmptyBorder(15, 15, 15, 15));
        panel.setBackground(BG_LIGHT);

        detailsArea = new JTextArea("Select an event...");
        detailsArea.setEditable(false);
        detailsArea.setFont(new Font("Serif", Font.PLAIN, 15));
        detailsArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        detailsArea.setBackground(Color.WHITE);

        JScrollPane detailsScroll = new JScrollPane(detailsArea);
        detailsScroll.setBorder(BorderFactory.createTitledBorder("Event Details"));

        volunteersListModel = new DefaultListModel<>();
        JList<String> volunteersList = new JList<>(volunteersListModel);
        volunteersList.setFont(new Font("Serif", Font.PLAIN, 15));
        JScrollPane volScroll = new JScrollPane(volunteersList);
        volScroll.setBorder(BorderFactory.createTitledBorder("Volunteers"));

        panel.add(detailsScroll, BorderLayout.NORTH);
        panel.add(volScroll, BorderLayout.CENTER);

        return panel;
    }

    private JPanel buildControlsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        panel.setBackground(BG_DARK);
        panel.setBorder(new EmptyBorder(10, 10, 10, 10));

        deleteEventBtn = makeRoundedButton("Delete Event");
        editEventBtn   = makeRoundedButton("Edit Event");
        JButton closeBtn = makeRoundedButton("Close");

        closeBtn.addActionListener(e -> dispose());
        deleteEventBtn.addActionListener(e -> deleteEvent());
        editEventBtn.addActionListener(e -> editEvent());

        panel.add(deleteEventBtn);
        panel.add(editEventBtn);
        panel.add(closeBtn);

        return panel;
    }

    private void showEventDetails(EventData ev) {

        String details = """
                Event Name: %s
                Category: %s
                Location: %s
                Date: %s
                Time: %s - %s
                Max Volunteers: %d

                Description:
                %s
                """.formatted(
                ev.name, ev.category, ev.location,
                ev.date, ev.startTime, ev.endTime,
                ev.volunteers, ev.description
        );

        detailsArea.setText(details);
        loadVolunteersForEvent(ev.eventId);
    }

    private void loadVolunteersForEvent(int eventId) {
        volunteersListModel.clear();

        String sql = """
                SELECT volunteer_name, status
                FROM volunteer_participations
                WHERE event_name = (SELECT name FROM events WHERE event_id = ?)
                """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, eventId);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    volunteersListModel.addElement(
                            rs.getString("volunteer_name") +
                                    " (" + rs.getString("status") + ")"
                    );
                }
            }

        } catch (Exception ex) {
            volunteersListModel.addElement("Error loading volunteers...");
        }
    }

    private void deleteEvent() {
        if (selectedEvent == null) return;

        int confirm = JOptionPane.showConfirmDialog(
                this,
                "Delete event: " + selectedEvent.name + "?",
                "Confirm",
                JOptionPane.YES_NO_OPTION
        );

        if (confirm != JOptionPane.YES_OPTION) return;

        String sql = "DELETE FROM events WHERE event_id = ? AND organizer_id = ?";

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setInt(1, selectedEvent.eventId);
            ps.setInt(2, organizerId);
            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event deleted.");

            loadEventsFromDB();
            refreshList();

            detailsArea.setText("");
            volunteersListModel.clear();
            selectedEvent = null;

            deleteEventBtn.setEnabled(false);
            editEventBtn.setEnabled(false);

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Error deleting:\n" + ex.getMessage());
        }
    }

    private void refreshList() {
        DefaultListModel<String> m = (DefaultListModel<String>) eventList.getModel();
        m.clear();
        for (EventData ev : currentEvents) {
            m.addElement(ev.name);
        }
    }

    private void editEvent() {
        if (selectedEvent == null) return;

        JTextField name       = new JTextField(selectedEvent.name);
        JTextField category   = new JTextField(selectedEvent.category);
        JTextField location   = new JTextField(selectedEvent.location);
        JTextField volunteers = new JTextField(String.valueOf(selectedEvent.volunteers));
        JTextField date       = new JTextField(selectedEvent.date);
        JTextField start      = new JTextField(selectedEvent.startTime);
        JTextField end        = new JTextField(selectedEvent.endTime);
        JTextArea  desc       = new JTextArea(selectedEvent.description);

        JPanel form = new JPanel(new GridLayout(0, 2));
        form.add(new JLabel("Name:"));        form.add(name);
        form.add(new JLabel("Category:"));    form.add(category);
        form.add(new JLabel("Location:"));    form.add(location);
        form.add(new JLabel("Volunteers:"));  form.add(volunteers);
        form.add(new JLabel("Date:"));        form.add(date);
        form.add(new JLabel("Start:"));       form.add(start);
        form.add(new JLabel("End:"));         form.add(end);
        form.add(new JLabel("Description:")); form.add(new JScrollPane(desc));

        int option = JOptionPane.showConfirmDialog(this, form,
                "Edit Event", JOptionPane.OK_CANCEL_OPTION);

        if (option != JOptionPane.OK_OPTION) return;

        String newName = name.getText().trim();

        String checkEditSql = """
                SELECT COUNT(*)
                FROM events
                WHERE organizer_id = ?
                  AND name = ?
                  AND event_id <> ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement psCheck = conn.prepareStatement(checkEditSql)) {

            psCheck.setInt(1, organizerId);
            psCheck.setString(2, newName);
            psCheck.setInt(3, selectedEvent.eventId);

            ResultSet rs = psCheck.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {

                JOptionPane.showMessageDialog(
                        this,
                        "You already have an event with this name.",
                        "Duplicate Name",
                        JOptionPane.WARNING_MESSAGE
                );

                return;
            }

        } catch (Exception e) {
            JOptionPane.showMessageDialog(this,
                    "Error checking duplicates:\n" + e.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        String sql = """
                UPDATE events
                SET name=?, category=?, location=?, volunteers=?, event_date=?, start_time=?, end_time=?, description=?
                WHERE event_id=? AND organizer_id=?
                """;

        try (Connection c = DBConnection.getConnection();
             PreparedStatement ps = c.prepareStatement(sql)) {

            ps.setString(1, name.getText().trim());
            ps.setString(2, category.getText().trim());
            ps.setString(3, location.getText().trim());
            ps.setInt(4, Integer.parseInt(volunteers.getText().trim()));
            ps.setString(5, date.getText().trim());
            ps.setString(6, start.getText().trim());
            ps.setString(7, end.getText().trim());
            ps.setString(8, desc.getText().trim());
            ps.setInt(9, selectedEvent.eventId);
            ps.setInt(10, organizerId);

            ps.executeUpdate();

            JOptionPane.showMessageDialog(this, "Event updated successfully!");

            loadEventsFromDB();
            refreshList();

        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Update error:\n" + ex.getMessage());
        }
    }

    public static class EventData {

        public int eventId;
        public String name, category, location;
        public int volunteers;
        public String date, startTime, endTime, description;

        public EventData(int id, String name, String cat, String loc, int vol,
                         String date, String st, String et, String desc) {

            this.eventId = id;
            this.name = name;
            this.category = cat;
            this.location = loc;
            this.volunteers = vol;
            this.date = date;
            this.startTime = st;
            this.endTime = et;
            this.description = desc;
        }
    }

    private JButton makeRoundedButton(String text) {
        JButton btn = new JButton(text);

        btn.setFont(new Font("Serif", Font.BOLD, 15));
        btn.setForeground(Color.WHITE);

        btn.setBackground(BTN_COLOR);
        btn.setFocusPainted(false);
        btn.setBorder(new EmptyBorder(8, 20, 8, 20));

        btn.setContentAreaFilled(false);
        btn.setOpaque(false);

        btn.setUI(new javax.swing.plaf.basic.BasicButtonUI() {
            @Override
            public void paint(Graphics g, JComponent c) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                int w = c.getWidth();
                int h = c.getHeight();

                g2.setColor(new Color(0x74835A));
                g2.fillRoundRect(0, 0, w, h, 18, 18);

                super.paint(g2, c);
            }
        });

        return btn;
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {

            int organizerId = -1;

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(
                         "SELECT organizer_id FROM organizers ORDER BY organizer_id LIMIT 1"
                 );
                 ResultSet rs = ps.executeQuery()) {

                if (rs.next()) {
                    organizerId = rs.getInt(1);
                } else {
                    JOptionPane.showMessageDialog(null,
                            "⚠ No organizers found",
                            "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }

            } catch (Exception e) {
                JOptionPane.showMessageDialog(null,
                        "Database Error:\n" + e.getMessage(),
                        "Error", JOptionPane.ERROR_MESSAGE);
                return;
            }

            new OrgClassWithEventEditor(organizerId).setVisible(true);
        });
    }
}