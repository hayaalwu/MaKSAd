package maksadpro;

public class MaKSAd {

    public static void main(String[] args) {

        // Ensure UI uses current system theme 
        try {
            javax.swing.UIManager.setLookAndFeel(
                    javax.swing.UIManager.getSystemLookAndFeelClassName()
            );
        } catch (Exception ignored) {}

        javax.swing.SwingUtilities.invokeLater(() -> {
            new MainWelcomeFrame().setVisible(true);
        });
    }

}
