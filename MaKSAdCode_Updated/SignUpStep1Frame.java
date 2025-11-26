package maksadpro;

import javax.swing.*;
import java.awt.*;
import java.sql.*;
import com.toedter.calendar.JDateChooser;   // ← الكالندر
import java.text.SimpleDateFormat;

public class SignUpStep1Frame extends JFrame {

    private JTextField fullNameField;
    private JTextField emailField;
    private JTextField phoneField;
    private JPasswordField passwordField;
    private JPasswordField confirmPasswordField;

    private JRadioButton maleRadio;
    private JRadioButton femaleRadio;

    private JRadioButton onsiteRadio;
    private JRadioButton onlineRadio;

    private JDateChooser dobChooser;   // ← بدل الـ TextField

    public SignUpStep1Frame() {
        setTitle("MaKSAd – Sign Up (Step 1)");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(700, 460);
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
        header.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        container.add(header, BorderLayout.NORTH);

        JPanel form = new JPanel(new GridBagLayout());
        form.setBackground(new Color(245, 241, 225));
        form.setBorder(BorderFactory.createEmptyBorder(20, 40, 20, 40));

        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(5, 5, 5, 5);
        gc.anchor = GridBagConstraints.WEST;

        fullNameField = new JTextField(22);
        emailField    = new JTextField(22);
        phoneField    = new JTextField(22);
        passwordField = new JPasswordField(22);
        confirmPasswordField = new JPasswordField(22);

        // كالندر بدل كتابة تاريخ
        dobChooser = new JDateChooser();
        dobChooser.setDateFormatString("yyyy-MM-dd");
        dobChooser.setPreferredSize(new Dimension(150, 28));

        maleRadio   = new JRadioButton("Male");
        femaleRadio = new JRadioButton("Female");
        ButtonGroup genderGroup = new ButtonGroup();
        genderGroup.add(maleRadio);
        genderGroup.add(femaleRadio);

        onsiteRadio = new JRadioButton("On-site");
        onlineRadio = new JRadioButton("Online");
        ButtonGroup typeGroup = new ButtonGroup();
        typeGroup.add(onsiteRadio);
        typeGroup.add(onlineRadio);

        int row = 0;
        addRow(form, gc, row++, "Full Name:", fullNameField);
        addRow(form, gc, row++, "Email Address:", emailField);
        addRow(form, gc, row++, "Phone Number:", phoneField);
        addRow(form, gc, row++, "Password:", passwordField);
        addRow(form, gc, row++, "Confirm Password:", confirmPasswordField);

        // كالندر
        gc.gridx = 0; gc.gridy = row;
        form.add(new JLabel("Date of Birth:"), gc);
        gc.gridx = 1;
        form.add(dobChooser, gc);
        row++;

        // Gender
        gc.gridx = 0; gc.gridy = row;
        form.add(new JLabel("Gender:"), gc);

        JPanel genderPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        genderPanel.setBackground(new Color(245, 241, 225));
        genderPanel.add(maleRadio);
        genderPanel.add(femaleRadio);
        gc.gridx = 1;
        form.add(genderPanel, gc);
        row++;

        // Preferred Type
        gc.gridx = 0; gc.gridy = row;
        form.add(new JLabel("Preferred Type:"), gc);

        JPanel typePanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        typePanel.setBackground(new Color(245, 241, 225));
        typePanel.add(onsiteRadio);
        typePanel.add(onlineRadio);
        gc.gridx = 1;
        form.add(typePanel, gc);
        row++;

        // Next Button
        JButton nextBtn = new JButton("Next");
        nextBtn.addActionListener(e -> goToStep2());
        gc.gridx = 1; gc.gridy = row;
        gc.anchor = GridBagConstraints.EAST;
        form.add(nextBtn, gc);

        container.add(form, BorderLayout.CENTER);
        add(container);
    }

    private void addRow(JPanel panel, GridBagConstraints gc, int row,
                        String labelText, JComponent field) {

        gc.gridx = 0; gc.gridy = row;
        panel.add(new JLabel(labelText), gc);

        gc.gridx = 1;
        panel.add(field, gc);
    }

    private void goToStep2() {

        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String pass     = new String(passwordField.getPassword());
        String confirm  = new String(confirmPasswordField.getPassword());

        // نقرأ التاريخ من الكالندر
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String dob = dobChooser.getDate() != null ? sdf.format(dobChooser.getDate()) : null;

        String gender   = maleRadio.isSelected() ? "Male" :
                          (femaleRadio.isSelected() ? "Female" : null);

        String prefType = onsiteRadio.isSelected() ? "On-site" :
                          (onlineRadio.isSelected() ? "Online" : null);

        // VALIDATIONS الأساسية (باقية زي ما هي + زدنا الفحص بالتفصيل من SignUpData)
        if (fullName.isEmpty() || email.isEmpty() || pass.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please fill in required fields.");
            return;
        }
        if (!pass.equals(confirm)) {
            JOptionPane.showMessageDialog(this, "Passwords do not match.");
            return;
        }
        if (gender == null) {
            JOptionPane.showMessageDialog(this, "Please choose gender.");
            return;
        }
        if (prefType == null) {
            JOptionPane.showMessageDialog(this, "Please choose preferred volunteer type.");
            return;
        }
        if (dob == null) {
            JOptionPane.showMessageDialog(this, "Please choose your date of birth.");
            return;
        }

        // نبني SignUpData ونستخدم validate() اللي فيه:
        // - password ≥ 8
        // - email يحتوي @ و .
        // - phone يبدأ بـ 05 وطوله 10 أرقام
        // - وغيرها من الشروط
        SignUpData data = new SignUpData();
        data.setFullName(fullName);
        data.setEmail(email);
        data.setPhone(phone);
        data.setPassword(pass);
        data.setDateOfBirth(dob);
        data.setGender(gender);
        data.setPreferredType(prefType);

        try {
            data.validate();
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(
                    this,
                    ex.getMessage(),
                    "Invalid Input",
                    JOptionPane.ERROR_MESSAGE
            );
            return;
        }

        // التحقق من تكرار الإيميل/الجوال بعد ما نتأكد أن الصيغة صحيحة
        if (emailExists(email)) {
            JOptionPane.showMessageDialog(this, "This email is already registered.");
            return;
        }
        if (phoneExists(phone)) {
            JOptionPane.showMessageDialog(this, "This phone number is already registered.");
            return;
        }

        // لو كل شيء تمام → ننتقل للخطوة الثانية ومعانا SignUpData
        dispose();
        new SignUpStep2Frame(data).setVisible(true);
    }

    private boolean emailExists(String email) {
        String sql = "SELECT email FROM maksad_users WHERE email = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, email);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error while checking email.");
            return true;
        }
    }

    private boolean phoneExists(String phone) {
        String sql = "SELECT phone FROM maksad_users WHERE phone = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phone);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this, "Database error while checking phone.");
            return true;
        }
    }
}