import java.awt.*;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.*;
import javax.swing.*;
import javax.swing.border.*;

/* Event class: represents a single volunteering event  */
class Event {
    int id;
    String title;
    LocalDateTime start;
    LocalDateTime end;

    // Constructor: initialize event with id, title, and auto start/end time [**jood calss**]
    Event(int id, String title) {
        this.id = id;
        this.title = title;
        this.start = LocalDateTime.now();      // event starts now
        this.end = start.plusHours(2);         // default duration = 2 hours
    }

    @Override
    public String toString() {
        // Used when showing the event in JList / logs
        return title + " (" + start.toLocalDate() + ")";
    }

    // Getters used by other classes (VolunteerParticipation, UI, reports)
    public int getId()                 { return id; }
    public String getName()            { return title; }
    public LocalDate getDate()         { return start.toLocalDate(); }
    public LocalTime getStartTime()    { return start.toLocalTime(); }
    public LocalTime getEndTime()      { return end.toLocalTime(); }
}

/* VolunteerParticipation: link between Volunteer and Event */
class VolunteerParticipation { // [**Reemas**]
    Event event;          // which event the volunteer joined
    boolean pending = true;   // request is pending approval
    boolean approved = false; // true if organizer approves the participation
    double hours = 0.0;       // actual hours earned in this event

    // Constructor: create participation for a specific event
    VolunteerParticipation(Event e) {
        this.event = e;
    }
}

/* VolunteerData: data for a single volunteer */
class VolunteerData {
    double totalHours;  // total volunteering hours across all events
    java.util.List<String> interests = new ArrayList<>();                  // tags/fields of interest
    java.util.List<VolunteerParticipation> participations = new ArrayList<>(); // all joined events
    java.util.List<Certificate> certificates = new ArrayList<>();          // certificates earned
    java.util.List<Event> availableEvents = new ArrayList<>();             // events available to join

    // Constructor: seed with some sample events (for demo/testing)
    VolunteerData() {
        // [**Lama**]
        availableEvents.add(new Event(1, "Beach Cleanup"));
        availableEvents.add(new Event(2, "Book Fair Volunteering"));
        availableEvents.add(new Event(3, "Tech Workshop Support"));

    }

    // Request to join an event (if no time conflict)
    void requestJoin(Event e) {
        if (canJoin(e)) {
            participations.add(new VolunteerParticipation(e));
        }
    }

    // Cancel last participation for a specific event [**DB choosing!!!**]
    void cancelJoin(Event e) {
        // removeIf will remove all participations where vp.event == e
        participations.removeIf(vp -> vp.event == e);
    }

    // Check if the new event overlaps with any existing joined event
    boolean hasConflict(Event e) {
        for (VolunteerParticipation vp : participations) {
            Event ev = vp.event;
            if (ev == null) continue;
            // time overlap: start is before other's end AND end is after other's start
            boolean overlap = !e.start.isAfter(ev.end) && !e.end.isBefore(ev.start);
            if (overlap) return true;
        }
        return false;
    }

    // Can the volunteer join this event? (no conflict)
    boolean canJoin(Event e) {
        return !hasConflict(e);
    }

    // Number of earned certificates
    int getCertificatesCount() {
        return certificates.size();
    }

    // Number of joined events (participations)
    int getParticipationCount() {
        return participations.size();
    }

    // Completion rate = certificates / total participations
    double attendRate() {
        int total = getParticipationCount();
        if (total == 0) return 0.0;
        return (double) getCertificatesCount() / total;
    }

    // Return all participations (used by ParticipationViewFrame)
    java.util.List<VolunteerParticipation> viewParticipation() {
        return participations;
    }

    // Return all certificates (used by CertificatesView)
    java.util.List<Certificate> getCertificates() {
        return certificates;
    }
}

/* ParticipationViewFrame: simple table to display participations */
class ParticipationViewFrame extends JFrame { // [**Reemas DB**]
    ParticipationViewFrame(java.util.List<VolunteerParticipation> data) {
        setTitle("Volunteer Participation");
        setSize(750, 400);
        setLayout(new BorderLayout());
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        // Table column headers
        String[] cols = {"Event ID", "Event Title", "Start", "End", "Pending", "Approved", "Hours"};
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
        Object[][] rows = new Object[data.size()][cols.length];

        // Fill table rows from participation list
        for (int i = 0; i < data.size(); i++) {
            VolunteerParticipation vp = data.get(i);
            rows[i][0] = vp.event.getId();
            rows[i][1] = vp.event.getName();
            rows[i][2] = vp.event.start.format(fmt);
            rows[i][3] = vp.event.end.format(fmt);
            rows[i][4] = vp.pending;
            rows[i][5] = vp.approved;
            rows[i][6] = vp.hours;
        }

        JTable table = new JTable(rows, cols);
        add(new JScrollPane(table), BorderLayout.CENTER);
        setVisible(true);
    }
}

/* BackgroundPanel: panel that can draw a background image or solid color */
class BackgroundPanel extends JPanel {
    private final Image bg;

    BackgroundPanel(String imagePath, Color fallback) {
        setOpaque(false);
        if (imagePath != null && !imagePath.isEmpty())
            bg = new ImageIcon(imagePath).getImage();   // load background image
        else {
            bg = null;
            setBackground(fallback); // use solid color if no image
            setOpaque(true);
        }
    }

    @Override protected void paintComponent(Graphics g) {
        if (bg != null)
            g.drawImage(bg, 0, 0, getWidth(), getHeight(), this); // stretch image to fill panel
        else
            super.paintComponent(g);
    }
}

/* Volunteer GUI: main dashboard for a single volunteer */
public class Volunteer extends JFrame {
    // Single data object representing this volunteer's stats
    final VolunteerData v = new VolunteerData();

    // Metric labels
    final JLabel hoursLbl = new JLabel("0.0");
    final JLabel certLbl  = new JLabel("0");
    final JLabel partLbl  = new JLabel("0");
    final JLabel rateLbl  = new JLabel("0%");

    // Interests UI
    final DefaultListModel<String> interestsModel = new DefaultListModel<>();
    final JList<String> interestsList = new JList<>(interestsModel);
    final JTextField interestInput = new JTextField();

    // Activity log area
    final JTextArea log = new JTextArea(7, 28);

    // Shared fonts and button size
    static final Font TITLE = new Font("Segoe UI", Font.BOLD, 16);
    static final Font TEXT  = new Font("Segoe UI", Font.PLAIN, 14);
    static final Dimension BTN_SIZE = new Dimension(220, 44);

    public Volunteer() {
        setTitle("Volunteer GUI");
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setSize(860, 540);
        setLocationRelativeTo(null);

        // Root panel with background (image or color)
        BackgroundPanel root = new BackgroundPanel("MaKSAd_logo.png", new Color(245, 248, 250));
        root.setLayout(new BorderLayout());
        root.setBorder(new EmptyBorder(16, 16, 16, 16));
        setContentPane(root);

        // Left panel: KPI cards + interests [**Khawlah?**]
        JPanel left = new JPanel();
        left.setOpaque(false);
        left.setLayout(new BoxLayout(left, BoxLayout.Y_AXIS));
        left.setBorder(new EmptyBorder(8, 8, 8, 16));

        left.add(cardMetric("Total Hours", hoursLbl));
        left.add(Box.createVerticalStrut(10));
        left.add(cardMetric("Certificates", certLbl));
        left.add(Box.createVerticalStrut(10));
        left.add(cardMetric("Joined Events", partLbl));
        left.add(Box.createVerticalStrut(10));
        left.add(cardMetric("Completion Rate", rateLbl));
        left.add(Box.createVerticalStrut(10));

        // Card for interests list + add field/button
        JPanel interestsCard = cardBase();
        interestsCard.setLayout(new BorderLayout(8, 8));
        JLabel intTitle = new JLabel("Interests");
        intTitle.setFont(TITLE);
        interestsList.setFont(TEXT);

        JButton addInt = new JButton("Add interest");
        addInt.addActionListener(e -> {
            String s = interestInput.getText().trim();
            if (!s.isEmpty()) {
                v.interests.add(s);          // add to data
                interestsModel.addElement(s); // add to UI list
                interestInput.setText("");
            }
        });

        interestsCard.add(intTitle, BorderLayout.NORTH);
        interestsCard.add(new JScrollPane(interestsList), BorderLayout.CENTER);

        JPanel inputRow = new JPanel(new BorderLayout(8, 8));
        inputRow.setOpaque(false);
        inputRow.add(interestInput, BorderLayout.CENTER);
        inputRow.add(addInt, BorderLayout.EAST);
        interestsCard.add(inputRow, BorderLayout.SOUTH);

        left.add(interestsCard);

        //Right panel: buttons + log 
        JPanel right = new JPanel();
        right.setOpaque(false);
        right.setLayout(new BoxLayout(right, BoxLayout.Y_AXIS));
        right.setBorder(new EmptyBorder(8, 16, 8, 8));

        JButton req      = wideButton("requestJoin");
        JButton cancel   = wideButton("cancelJoin");
        JButton viewPart = wideButton("View Participation");
        JButton viewCert = wideButton("View Certificates");

        // Open dialog to select which event to join
        req.addActionListener(e -> new EventSelectionDialog());

        // Cancel last participation (simple behavior for demo)
        cancel.addActionListener(e -> {
            if (!v.participations.isEmpty()) {
                Event ev = v.participations.get(v.participations.size() - 1).event;
                v.cancelJoin(ev);
                log.append("[CANCEL] " + ev.getName() + "\n");
            }
            refresh();
        });

        // Open participation table window
        viewPart.addActionListener(e ->
                new ParticipationViewFrame(v.viewParticipation())
        );

        // Open certificates window
        viewCert.addActionListener(e ->
                new CertificatesView(v.getCertificates())
        );

        right.add(req);
        right.add(Box.createVerticalStrut(12));
        right.add(cancel);
        right.add(Box.createVerticalStrut(12));
        right.add(viewPart);
        right.add(Box.createVerticalStrut(12));
        right.add(viewCert);
        right.add(Box.createVerticalStrut(12));

        // Activity log card
        JPanel logCard = cardBase();
        logCard.setLayout(new BorderLayout(8, 8));
        JLabel logTitle = new JLabel("Activity Log");
        logTitle.setFont(TITLE);
        log.setEditable(false);
        log.setFont(new Font("Consolas", Font.PLAIN, 13));
        logCard.add(logTitle, BorderLayout.NORTH);
        logCard.add(new JScrollPane(log), BorderLayout.CENTER);
        right.add(logCard);

        // Place left + right panels in root
        root.add(left, BorderLayout.CENTER);
        root.add(right, BorderLayout.EAST);

        // Initialize KPI labels
        refresh();
    }

    // Inner dialog for selecting an Event to join 
    class EventSelectionDialog extends JDialog {
        EventSelectionDialog() {
            super(Volunteer.this, "Select Event", true);
            setSize(400, 300);
            setLocationRelativeTo(Volunteer.this);
            setLayout(new BorderLayout(8, 8));

            // List model for available events
            DefaultListModel<Event> model = new DefaultListModel<>();
            for (Event ev : v.availableEvents) model.addElement(ev);

            JList<Event> list = new JList<>(model);
            list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

            JButton joinBtn   = new JButton("Join Event");
            JButton cancelBtn = new JButton("Cancel");

            // When user clicks "Join Event"
            joinBtn.addActionListener(e -> {
                Event selected = list.getSelectedValue();
                if (selected == null) {
                    JOptionPane.showMessageDialog(this, "Please select an event.");
                    return;
                }
                if (v.canJoin(selected)) {
                    v.requestJoin(selected);
                    log.append("[JOIN] " + selected.getName() + "\n");
                    refresh();
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(this, "You already have an event at this time.");
                }
            });

            cancelBtn.addActionListener(e -> dispose());

            JPanel bottom = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            bottom.add(cancelBtn);
            bottom.add(joinBtn);

            add(new JScrollPane(list), BorderLayout.CENTER);
            add(bottom, BorderLayout.SOUTH);
            setVisible(true);
        }
    }

    // Helper to create a wide button with unified style
    private JButton wideButton(String text) {
        JButton b = new JButton(text);
        b.setFont(TEXT);
        b.setPreferredSize(BTN_SIZE);
        b.setMaximumSize(BTN_SIZE);
        b.setMinimumSize(BTN_SIZE);
        b.setAlignmentX(Component.CENTER_ALIGNMENT);
        return b;
    }

    // Helper to create a KPI card with title + value 
    private JPanel cardMetric(String title, JLabel value) {
        JPanel p = cardBase();
        p.setLayout(new BorderLayout(8, 8));
        JLabel t = new JLabel(title);
        t.setFont(TITLE);
        value.setFont(new Font("Segoe UI", Font.BOLD, 18));
        value.setHorizontalAlignment(SwingConstants.RIGHT);
        p.add(t, BorderLayout.WEST);
        p.add(value, BorderLayout.EAST);
        return p;
    }

    // Base style for "card" panels 
    private JPanel cardBase() {
        JPanel p = new JPanel();
        p.setOpaque(true);
        p.setBackground(new Color(255, 255, 255, 210));
        p.setBorder(new CompoundBorder(
                new LineBorder(new Color(230, 233, 236), 1, true),
                new EmptyBorder(12, 12, 12, 12)
        ));
        return p;
    }

    //  Refresh UI KPIs based on current data 
    void refresh() {
        hoursLbl.setText(String.format("%.1f", v.totalHours));
        certLbl.setText(String.valueOf(v.getCertificatesCount()));
        partLbl.setText(String.valueOf(v.getParticipationCount()));
        double rate = v.attendRate() * 100.0;
        rateLbl.setText(String.format("%.0f%%", rate));
    }

    // Entry point to run this GUI alone 
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Volunteer().setVisible(true));
    }
}
