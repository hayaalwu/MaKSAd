package maksadpro;

import javax.swing.*;
import java.awt.*;
import java.net.URL;
import java.sql.SQLException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoginFrame extends JFrame {

    private JTextField idField;
    private JPasswordField passwordField;
    private JLabel statusLabel;

    public LoginFrame() {

        setTitle("MaKSAd – Login");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1280, 850);
        setLocationRelativeTo(null);
        setLayout(null);

        initUI();
    }

    private void initUI() {

        //  BACKGROUND 
        getContentPane().setBackground(Color.decode("#FFFADD"));


        //  TOP BAR 
        JPanel topBar = new JPanel();
        topBar.setBackground(Color.decode("#263717"));
        topBar.setBounds(0, 0, 1280, 130);
        topBar.setLayout(new GridBagLayout()); // نص بالمنتصف

        JLabel topText = new JLabel("We’re glad to have you as part of MaKSAd");
        topText.setForeground(Color.WHITE);
        topText.setFont(new Font("Serif", Font.BOLD, 26));

        topBar.add(topText);
        add(topBar);



        //  CENTER FORM PANEL 
        JPanel card = new RoundedPanel(30);
        card.setBackground(Color.decode("#74835A"));

        int cardWidth = 600;
        int cardHeight = 180;
        int cardX = (1280 - cardWidth) / 2;
        int cardY = (850 - cardHeight) / 2 - 60;  // رفع بسيط

        card.setBounds(cardX, cardY, cardWidth, cardHeight);
        card.setLayout(null);


        //  LABELS + FIELDS (Perfect alignment) 
        JLabel idLabel = new JLabel("Account ID:");
        idLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        idLabel.setForeground(Color.WHITE);
        idLabel.setBounds(40, 30, 150, 30);

        idField = new JTextField();
        idField.setBounds(cardWidth - 320, 30, 260, 32);
        idField.setFont(new Font("SansSerif", Font.PLAIN, 16));


        JLabel passLabel = new JLabel("Password:");
        passLabel.setFont(new Font("SansSerif", Font.BOLD, 17));
        passLabel.setForeground(Color.WHITE);
        passLabel.setBounds(40, 95, 150, 30);

        passwordField = new JPasswordField();
        passwordField.setBounds(cardWidth - 320, 95, 260, 32);
        passwordField.setFont(new Font("SansSerif", Font.PLAIN, 16));


        card.add(idLabel);
        card.add(idField);
        card.add(passLabel);
        card.add(passwordField);

        add(card);



        //  LOGIN BUTTON 
        JButton loginBtn = new JButton("Login") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(Color.decode("#263717"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                super.paintComponent(g);
            }
        };

        loginBtn.setForeground(Color.WHITE);
        loginBtn.setOpaque(false);
        loginBtn.setContentAreaFilled(false);
        loginBtn.setBorderPainted(false);
        loginBtn.setFocusPainted(false);
        loginBtn.setFont(new Font("SansSerif", Font.BOLD, 18));

        loginBtn.setBounds((1280 - 180) / 2, cardY + cardHeight + 35, 180, 48);

        loginBtn.addActionListener(e -> attemptLogin());

        add(loginBtn);



        //  LOGO (BOTTOM LEFT) 
        JLabel logo = new JLabel();
        loadImage(logo, "/maksadpro/MaKSAdPH/MaKSAdLogo.png", 130, 130);
        logo.setBounds(60, 630, 150, 150);
        add(logo);



        //  PALM (BOTTOM RIGHT) 
        JLabel palm = new JLabel();
        loadImage(palm, "/maksadpro/MaKSAdPH/MaKSAdPalm.png", 280, 280);

        palm.setBounds(1280 - 260 - 40, 850 - 260 - 20, 260, 260);

        add(palm);



        //  STATUS ERROR LABEL 
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        statusLabel.setForeground(Color.RED);
        statusLabel.setBounds(0, cardY + cardHeight + 90, 1280, 28);

        add(statusLabel);
    }


    // LOGIN LOGIC
    private void attemptLogin() {

        String idText = idField.getText().trim();
        String pass   = new String(passwordField.getPassword());

        if (idText.isEmpty() || pass.isEmpty()) {
            statusLabel.setText("Please enter your ID and password.");
            return;
        }

        if (pass.length() < 8) {
            statusLabel.setText("Password must be at least 8 characters.");
            return;
        }

        if (!idText.matches("\\d+")) {
            statusLabel.setText("ID must be numeric.");
            return;
        }

        int id = Integer.parseInt(idText);

        try {
            MaKSAdUserSystem.AuthenticatedUser user =
                    MaKSAdUserSystem.login(id, pass);

            if (user != null) {

         
                try (Connection conn = DBConnection.getConnection();
                     PreparedStatement ps = conn.prepareStatement(
                             "UPDATE admins SET last_login = NOW() WHERE admin_id = ?"
                     )) {

                    ps.setInt(1, user.getId());  
                    ps.executeUpdate();

                } catch (SQLException ex2) {
                    ex2.printStackTrace();
                }

                dispose();
                MaKSAdUserSystem.openInterfaceFor(user);

            } else {
                statusLabel.setText("Invalid ID or password.");
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
            statusLabel.setText("Database error. Please try again later.");

            JOptionPane.showMessageDialog(
                    this,
                    "A database error occurred while trying to log you in.\n"
                            + "Please check the connection and try again.\n\n"
                            + "Error: " + ex.getMessage(),
                    "Database Error",
                    JOptionPane.ERROR_MESSAGE
            );
        }
    }


    // IMAGE LOADER
    private void loadImage(JLabel label, String path, int w, int h) {
        try {
            URL url = getClass().getResource(path);
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            label.setText("Missing image.");
        }
    }


    // ROUNDED CARD PANEL
    class RoundedPanel extends JPanel {
        int radius;

        RoundedPanel(int r) {
            radius = r;
            setOpaque(false);
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

            g2.setColor(getBackground());
            g2.fillRoundRect(0, 0, getWidth(), getHeight(), radius, radius);

            super.paintComponent(g);
        }
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new LoginFrame().setVisible(true));
    }

}
