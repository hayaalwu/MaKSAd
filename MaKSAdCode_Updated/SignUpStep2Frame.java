package maksadpro;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class SignUpStep2Frame extends JFrame {

    private SignUpData data;

    private List<JCheckBox> interestChecks = new ArrayList<>();
    private List<JCheckBox> skillChecks    = new ArrayList<>();

    public SignUpStep2Frame(SignUpData data) {
        this.data = data;

        setTitle("MaKSAd – Sign Up (Step 2)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(750, 450);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        JPanel container = new JPanel(new BorderLayout());
        container.setBackground(new Color(245, 241, 225));

        JLabel header = new JLabel("We’re glad to have you as part of MaKSAd", SwingConstants.CENTER);
        header.setFont(new Font("Serif", Font.BOLD, 20));
        header.setOpaque(true);
        header.setBackground(new Color(39, 85, 33));
        header.setForeground(Color.WHITE);
        header.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));
        container.add(header, BorderLayout.NORTH);

        JPanel center = new JPanel();
        center.setBackground(new Color(245, 241, 225));
        center.setLayout(new BoxLayout(center, BoxLayout.Y_AXIS));

        JLabel subtitle = new JLabel("Tell us a bit about yourself!", SwingConstants.CENTER);
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setBorder(BorderFactory.createEmptyBorder(15,10,15,10));
        center.add(subtitle);

        JPanel listsPanel = new JPanel(new GridLayout(1, 2, 30, 10));
        listsPanel.setOpaque(false);

        // INTERESTS
        JPanel interestPanel = new JPanel();
        interestPanel.setLayout(new BoxLayout(interestPanel, BoxLayout.Y_AXIS));
        interestPanel.setOpaque(false);

        JLabel interestLabel = new JLabel("Areas of Interest:");
        interestLabel.setFont(interestLabel.getFont().deriveFont(Font.BOLD));
        interestPanel.add(interestLabel);

        String[] interests = {
                "Community Events", "Environmental Activities", "Health & Wellness",
                "Technology & Digital Skills", "Media & Photography", "Event Management", "Others"
        };

        for (String s : interests) {
            JCheckBox cb = new JCheckBox(s);
            cb.setOpaque(false);
            interestChecks.add(cb);
            interestPanel.add(cb);
        }

        // SKILLS
        JPanel skillsPanel = new JPanel();
        skillsPanel.setLayout(new BoxLayout(skillsPanel, BoxLayout.Y_AXIS));
        skillsPanel.setOpaque(false);

        JLabel skillsLabel = new JLabel("Skills:");
        skillsLabel.setFont(skillsLabel.getFont().deriveFont(Font.BOLD));
        skillsPanel.add(skillsLabel);

        String[] skills = {
                "Photography", "Videography", "Graphic Design", "Writing & Editing",
                "Public Speaking", "Event Organizing", "Technical Support", "Social Media Management"
        };

        for (String s : skills) {
            JCheckBox cb = new JCheckBox(s);
            cb.setOpaque(false);
            skillChecks.add(cb);
            skillsPanel.add(cb);
        }

        listsPanel.add(interestPanel);
        listsPanel.add(skillsPanel);

        center.add(listsPanel);

        //  BUTTONS (BACK + SUBMIT) 
        JPanel btnPanel = new JPanel();
        btnPanel.setOpaque(false);
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton backBtn = new JButton("Back");
        backBtn.addActionListener(e -> {
            dispose(); // close step 2
            new SignUpStep1Frame().setVisible(true); // reopen step 1
        });

        JButton submitBtn = new JButton("Submit");
        submitBtn.addActionListener(e -> submitRegistration());

        btnPanel.add(backBtn);
        btnPanel.add(submitBtn);

        center.add(Box.createVerticalStrut(15));
        center.add(btnPanel);

        container.add(center, BorderLayout.CENTER);
        add(container);
    }

    private void submitRegistration() {

        // Collect Interests / Skills
        data.setInterests(new ArrayList<>());
        data.setSkills(new ArrayList<>());

        for (JCheckBox cb : interestChecks)
            if (cb.isSelected()) data.getInterests().add(cb.getText());

        for (JCheckBox cb : skillChecks)
            if (cb.isSelected()) data.getSkills().add(cb.getText());

        //  VALIDATION
        if (data.getInterests().isEmpty() && data.getSkills().isEmpty()) {
            JOptionPane.showMessageDialog(
                    this,
                    "Please select at least one interest or one skill.",
                    "Missing Information",
                    JOptionPane.WARNING_MESSAGE
            );
            return;
        }

        String interestsJson = toJsonArray(data.getInterests());
        String skillsJson    = toJsonArray(data.getSkills());

        int volunteerId = -1;

        String insertVolunteerSQL = """
            INSERT INTO volunteers (volunteer_name, total_hours)
            VALUES (?, 0)
        """;

        String insertUserSQL = """
            INSERT INTO maksad_users
            (admin_id, organizer_id, volunteer_id,
             full_name, email, phone, password, gender,
             date_of_birth, interests, skills, role)
            VALUES (NULL, NULL, ?, ?, ?, ?, ?, ?, ?, ?, ?, 'Volunteer')
        """;

        try (Connection conn = DBConnection.getConnection()) {

            conn.setAutoCommit(false);

            // 1) INSERT INTO volunteers
            try (PreparedStatement psVol = conn.prepareStatement(insertVolunteerSQL, PreparedStatement.RETURN_GENERATED_KEYS)) {

                psVol.setString(1, data.getFullName());
                psVol.executeUpdate();

                try (ResultSet rs = psVol.getGeneratedKeys()) {
                    if (rs.next()) volunteerId = rs.getInt(1);
                }
            }

            if (volunteerId == -1)
                throw new SQLException("Failed to generate volunteer_id!");

            // 2) INSERT INTO maksad_users
            try (PreparedStatement psUser = conn.prepareStatement(insertUserSQL)) {

                psUser.setInt   (1, volunteerId);
                psUser.setString(2, data.getFullName());
                psUser.setString(3, data.getEmail());
                psUser.setString(4, data.getPhone());
                psUser.setString(5, data.getPassword());
                psUser.setString(6, data.getGender());
                psUser.setDate  (7, java.sql.Date.valueOf(data.getDateOfBirth()));
                psUser.setString(8, interestsJson);
                psUser.setString(9, skillsJson);

                psUser.executeUpdate();
            }

            conn.commit();

        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this,
                    "Error saving your data:\n" + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE);
            return;
        }

        JOptionPane.showMessageDialog(this,
                "Your account has been created successfully!\nYour Volunteer ID is: " + volunteerId,
                "Success",
                JOptionPane.INFORMATION_MESSAGE);

        dispose();
        new LoginFrame().setVisible(true);
    }

    private String toJsonArray(List<String> list) {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i).replace("\"", "\\\"")).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
