
package orgclass;

import javax.swing.*;
import java.awt.*;
import java.util.*;

public class OrgClass extends JFrame {

    JComboBox<String> orgComboBox;
    JTextArea descriptionArea;
    JLabel eventCountLabel;
    JButton addEventButton, showEventsButton;

    public HashMap<String, String> orgDescriptions = new HashMap<>();
    public HashMap<String, ArrayList<OrgClassWithEventEditor.EventData>> orgEvents = new HashMap<>();

    public OrgClass() {
        setTitle("Organization Manager");
        setSize(500, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        // Organizations
        orgDescriptions.put("Org1", "Helping the community with social events.");
        orgDescriptions.put("Org2", "Environmental protection and awareness.");
        orgDescriptions.put("Org3", "Technology and innovation programs.");

        for (String org : orgDescriptions.keySet()) {
            orgEvents.put(org, new ArrayList<>());
        }

        // Top
        JPanel topPanel = new JPanel();
        orgComboBox = new JComboBox<>(orgDescriptions.keySet().toArray(new String[0]));
        orgComboBox.addActionListener(e -> updateOrgInfo());
        topPanel.add(new JLabel("Select Organization:"));
        topPanel.add(orgComboBox);

        // Center
        JPanel centerPanel = new JPanel(new BorderLayout());
        descriptionArea = new JTextArea(5, 30);
        descriptionArea.setEditable(false);
        eventCountLabel = new JLabel("Events: 0");
        centerPanel.add(new JScrollPane(descriptionArea), BorderLayout.CENTER);
        centerPanel.add(eventCountLabel, BorderLayout.SOUTH);

        // Bottom
        JPanel bottomPanel = new JPanel();
        addEventButton = new JButton("Add Event");
        addEventButton.addActionListener(e -> addEvent());
        showEventsButton = new JButton("Show All Events");
        showEventsButton.addActionListener(e -> new OrgClassWithEventEditor(this));
        bottomPanel.add(addEventButton);
        bottomPanel.add(showEventsButton);

        add(topPanel, BorderLayout.NORTH);
        add(centerPanel, BorderLayout.CENTER);
        add(bottomPanel, BorderLayout.SOUTH);

        updateOrgInfo();
        setVisible(true);
    }

    void updateOrgInfo() {
        String selectedOrg = (String) orgComboBox.getSelectedItem();
        descriptionArea.setText(orgDescriptions.get(selectedOrg));
        eventCountLabel.setText("Events: " + orgEvents.get(selectedOrg).size());
    }

    private void addEvent() {

        // Fields
        JTextField nameField = new JTextField(15);
        JTextField categoryField = new JTextField(15);
        JTextField locationField = new JTextField(15);
        JTextField volunteersField = new JTextField(15);
        JTextArea descField = new JTextArea(3, 15);

        // Date spinner (no time)
        JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor dateEditor = new JSpinner.DateEditor(dateSpinner, "yyyy-MM-dd");
        dateSpinner.setEditor(dateEditor);
        dateEditor.getTextField().setEditable(false);

        // Start time spinner
        JSpinner startTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor sEditor = new JSpinner.DateEditor(startTime, "HH:mm");
        startTime.setEditor(sEditor);
        sEditor.getTextField().setEditable(false);

        // End time spinner
        JSpinner endTime = new JSpinner(new SpinnerDateModel());
        JSpinner.DateEditor eEditor = new JSpinner.DateEditor(endTime, "HH:mm");
        endTime.setEditor(eEditor);
        eEditor.getTextField().setEditable(false);

        // Panel using GridBag
        JPanel panel = new JPanel(new GridBagLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.insets = new Insets(5, 5, 5, 5);
        c.fill = GridBagConstraints.HORIZONTAL;

        int y = 0;

        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Event Name:"), c);
        c.gridx = 1; panel.add(nameField, c);

        y++;
        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Category:"), c);
        c.gridx = 1; panel.add(categoryField, c);

        y++;
        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Location:"), c);
        c.gridx = 1; panel.add(locationField, c);

        y++;
        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Volunteers (max 100):"), c);
        c.gridx = 1; panel.add(volunteersField, c);

        y++;
        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Date:"), c);
        c.gridx = 1; panel.add(dateSpinner, c);

        y++;
        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Start Time:"), c);
        c.gridx = 1; panel.add(startTime, c);

        y++;
        c.gridx = 0; c.gridy = y; panel.add(new JLabel("End Time:"), c);
        c.gridx = 1; panel.add(endTime, c);

        y++;
        c.gridx = 0; c.gridy = y; panel.add(new JLabel("Description:"), c);
        c.gridx = 1; panel.add(new JScrollPane(descField), c);

        boolean valid = false;

        while (!valid) {
            int result = JOptionPane.showConfirmDialog(this, panel, "Add New Event", JOptionPane.OK_CANCEL_OPTION);

            if (result != JOptionPane.OK_OPTION) return;

            try {
                String name = nameField.getText().trim();
                String category = categoryField.getText().trim();
                String location = locationField.getText().trim();
                String description = descField.getText().trim();
                int volunteers = Integer.parseInt(volunteersField.getText().trim());

                if (name.isEmpty() || category.isEmpty() || location.isEmpty() || description.isEmpty())
                    throw new IllegalArgumentException("Please fill all fields.");

                if (volunteers < 1 || volunteers > 100)
                    throw new IllegalArgumentException("Volunteers must be between 1 and 100.");

                Date date = (Date) dateSpinner.getValue();
                Date st = (Date) startTime.getValue();
                Date et = (Date) endTime.getValue();

                Calendar today = Calendar.getInstance();
                Calendar selected = Calendar.getInstance();
                selected.setTime(date);

                if (selected.before(today))
                    throw new IllegalArgumentException("Date must be in the future.");

                if (!st.before(et))
                    throw new IllegalArgumentException("Start time must be earlier than end time.");

                // Save
                String dateStr = dateEditor.getFormat().format(date);
                String stStr = sEditor.getFormat().format(st);
                String etStr = eEditor.getFormat().format(et);

                OrgClassWithEventEditor.EventData ev =
                        new OrgClassWithEventEditor.EventData(name, category, location,
                                String.valueOf(volunteers), dateStr, stStr, etStr, description);

                orgEvents.get((String) orgComboBox.getSelectedItem()).add(ev);

                updateOrgInfo();
                valid = true;

            } catch (NumberFormatException ex) {
                JOptionPane.showMessageDialog(this, "Volunteers must be a number.", "Error", JOptionPane.ERROR_MESSAGE);
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(OrgClass::new);
    }
}
