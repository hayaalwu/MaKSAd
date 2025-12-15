package maksadpro;

import javax.swing.*;
import java.awt.*;
import java.net.URL;

public class MainWelcomeFrame extends JFrame {

    public MainWelcomeFrame() {

        setTitle("MaKSAd – Welcome");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setSize(1280, 850);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        initUI();
    }

    private void initUI() {

        //  LEFT SIDE RECTANGLE 
        JPanel leftPanel = new JPanel();
        leftPanel.setBackground(Color.decode("#FFFADD"));
        leftPanel.setPreferredSize(new Dimension(140, getHeight()));
        add(leftPanel, BorderLayout.WEST);


        //  MAIN CENTER PANEL 
        JPanel centerPanel = new JPanel();
        centerPanel.setBackground(Color.decode("#263717"));
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBorder(BorderFactory.createEmptyBorder(60, 40, 40, 40));
        add(centerPanel, BorderLayout.CENTER);


        //  ABOUT BUTTON (TOP RIGHT) 
        JPanel topBar = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        topBar.setOpaque(false);

        JButton aboutTopBtn = createSmallButton("About MaKSAd");
        aboutTopBtn.addActionListener(e -> showAboutScreen());

        topBar.add(aboutTopBtn);
        centerPanel.add(topBar);
        centerPanel.add(Box.createVerticalStrut(50));


        //  LOGO 
        JLabel logo = new JLabel();
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadImage(logo, "/maksadpro/MaKSAdPH/MaKSAdLogo.png", 200, 200);
        centerPanel.add(logo);

        centerPanel.add(Box.createVerticalStrut(45));


        //  TEXT 
        JLabel textImg = new JLabel();
        textImg.setAlignmentX(Component.CENTER_ALIGNMENT);
        loadImage(textImg, "/maksadpro/MaKSAdPH/Text.png", 880, 68);     
        centerPanel.add(textImg);

        centerPanel.add(Box.createVerticalStrut(70));


        //  BUTTONS 
        JButton loginBtn  = createButton("Login");
        JButton signupBtn = createButton("SignUp");

        loginBtn.addActionListener(e -> {
            dispose();
            new LoginFrame().setVisible(true);
        });

        signupBtn.addActionListener(e -> {
            dispose();
            new SignUpStep1Frame().setVisible(true);
        });

        centerPanel.add(loginBtn);
        centerPanel.add(Box.createVerticalStrut(25));
        centerPanel.add(signupBtn);


        //  NEW TEXT UNDER SIGNUP 
        JLabel noAccountLabel = new JLabel("Don’t have an account yet? Create one now");
        noAccountLabel.setForeground(Color.decode("#74835A"));
        noAccountLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
        noAccountLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        centerPanel.add(Box.createVerticalStrut(20));
        centerPanel.add(noAccountLabel);


        centerPanel.add(Box.createVerticalGlue());
    }


    //  IMAGE LOADING 
    private void loadImage(JLabel label, String path, int w, int h) {
        try {
            URL url = getClass().getResource(path);
            ImageIcon icon = new ImageIcon(url);
            Image scaled = icon.getImage().getScaledInstance(w, h, Image.SCALE_SMOOTH);
            label.setIcon(new ImageIcon(scaled));
        } catch (Exception e) {
            label.setText("Image not found: " + path);
            label.setForeground(Color.WHITE);
        }
    }


    //  MAIN BUTTON STYLE 
    private JButton createButton(String text) {

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#74835A"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 25, 25);

                super.paintComponent(g);
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setPreferredSize(new Dimension(190, 44));
        btn.setMaximumSize(new Dimension(190, 44));
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));

        return btn;
    }


    //  SMALL BUTTON STYLE 
    private JButton createSmallButton(String text) {

        JButton btn = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {

                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(Color.decode("#74835A"));
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);

                super.paintComponent(g);
            }
        };

        btn.setForeground(Color.WHITE);
        btn.setOpaque(false);
        btn.setContentAreaFilled(false);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);

        btn.setPreferredSize(new Dimension(160, 40));
        btn.setFont(new Font("SansSerif", Font.BOLD, 15));

        return btn;
    }


    //  ABOUT SCREEN 
    private void showAboutScreen() {

        JFrame about = new JFrame("About MaKSAd");
        about.setSize(600, 750);
        about.setLocationRelativeTo(this);

        JLabel img = new JLabel();
        img.setHorizontalAlignment(SwingConstants.CENTER);

        loadImage(img, "/maksadpro/MaKSAdPH/AboutMaKSAd.png", 600, 750);

        about.add(img);
        about.setVisible(true);
    }


    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new MainWelcomeFrame().setVisible(true));
    }

}
