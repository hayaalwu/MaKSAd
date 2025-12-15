
package orgclass;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;

public class OrgClassWithEventEditor extends JFrame {

    private OrgClass parent;
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> categoryFilter;
    private DefaultTableModel model;
    private ArrayList<EventData> currentEvents;

    public OrgClassWithEventEditor(OrgClass parent) {
        this.parent = parent;
        setTitle("Events Viewer");
        setLayout(new BorderLayout());

        String selectedOrg = (String) parent.orgComboBox.getSelectedItem();
        currentEvents = parent.orgEvents.get(selectedOrg);

        // Top bar
        JPanel top = new JPanel(new FlowLayout());
        searchField = new JTextField(15);
        categoryFilter = new JComboBox<>(new String[]{"All", "Social", "Environmental", "Technology", "Health"});
        JButton deleteBtn = new JButton("Delete");
        JButton backBtn = new JButton("Close");

        top.add(new JLabel("Search:"));
        top.add(searchField);
        top.add(new JLabel("Category:"));
        top.add(categoryFilter);
        top.add(deleteBtn);
        top.add(backBtn);

        // Table columns
        String[] cols = {"Name", "Category", "Location", "Volunteers", "Date", "Start", "End", "Description"};
        model = new DefaultTableModel(cols, 0);

        table = new JTable(model);
        refresh();

        add(top, BorderLayout.NORTH);
        add(new JScrollPane(table), BorderLayout.CENTER);

        searchField.addKeyListener(new KeyAdapter() {
            public void keyReleased(KeyEvent e) {
                refresh();
            }
        });

        categoryFilter.addActionListener(e -> refresh());

        deleteBtn.addActionListener(e -> deleteEvent());

        backBtn.addActionListener(e -> dispose());

        // Double click to edit
        table.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent e) {
                if (e.getClickCount() == 2) {
                    int r = table.getSelectedRow();
                    if (r >= 0) editEvent(currentEvents.get(r));
                }
            }
        });

        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void refresh() {
        model.setRowCount(0);
        String search = searchField.getText().toLowerCase();
        String filter = (String) categoryFilter.getSelectedItem();

        for (EventData ev : currentEvents) {
            boolean ok =
                    ev.name.toLowerCase().contains(search) &&
                            (filter.equals("All") || ev.category.equalsIgnoreCase(filter));

            if (ok) {
                model.addRow(new Object[]{
                        ev.name, ev.category, ev.location, ev.volunteers,
                        ev.date, ev.startTime, ev.endTime, ev.description
                });
            }
        }
    }

    private void deleteEvent() {
        int sel = table.getSelectedRow();
        if (sel < 0) {
            JOptionPane.showMessageDialog(this, "Select an event first.");
            return;
        }
        int c = JOptionPane.showConfirmDialog(this, "Delete?", "Confirm", JOptionPane.YES_NO_OPTION);
        if (c == JOptionPane.YES_OPTION) {
            currentEvents.remove(sel);
            refresh();
            parent.updateOrgInfo();
        }
    }

    private void editEvent(EventData ev) {

        JTextField name = new JTextField(ev.name);
        JTextField category = new JTextField(ev.category);
        JTextField location = new JTextField(ev.location);
        JTextField volunteers = new JTextField(ev.volunteers);
        JTextField date = new JTextField(ev.date);
        JTextField start = new JTextField(ev.startTime);
        JTextField end = new JTextField(ev.endTime);
        JTextArea desc = new JTextArea(ev.description, 3, 15);

        JPanel p = new JPanel(new GridLayout(0, 1));
        p.add(new JLabel("Name:")); p.add(name);
        p.add(new JLabel("Category:")); p.add(category);
        p.add(new JLabel("Location:")); p.add(location);
        p.add(new JLabel("Volunteers:")); p.add(volunteers);
        p.add(new JLabel("Date:")); p.add(date);
        p.add(new JLabel("Start:")); p.add(start);
        p.add(new JLabel("End:")); p.add(end);
        p.add(new JLabel("Description:")); p.add(new JScrollPane(desc));

        int r = JOptionPane.showConfirmDialog(this, p, "Edit", JOptionPane.OK_CANCEL_OPTION);

        if (r == JOptionPane.OK_OPTION) {
            ev.name = name.getText();
            ev.category = category.getText();
            ev.location = location.getText();
            ev.volunteers = volunteers.getText();
            ev.date = date.getText();
            ev.startTime = start.getText();
            ev.endTime = end.getText();
            ev.description = desc.getText();

            refresh();
        }
    }

    public static class EventData {
        public String name, category, location, volunteers, date, startTime, endTime, description;

        public EventData(String name, String cat, String loc, String vol, String date,
                         String st, String et, String desc) {
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
    
}



