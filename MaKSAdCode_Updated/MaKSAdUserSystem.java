package maksadpro;

import javax.swing.*;
import java.sql.*;

public class MaKSAdUserSystem {

    public enum Role { ADMIN, ORGANIZER, VOLUNTEER }

    public static class AuthenticatedUser {
        private final int id;
        private final String name;
        private final String email;
        private final Role role;

        public AuthenticatedUser(int id, String name, String email, Role role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }

        public int getId()      { return id; }
        public String getName() { return name; }
        public String getEmail(){ return email; }
        public Role getRole()   { return role; }
    }

    // UNIFIED LOGIN USING maksad_users
    public static AuthenticatedUser login(int id, String password) throws SQLException {

        String sql = """
                SELECT admin_id, organizer_id, volunteer_id,
                       full_name, email, role
                FROM maksad_users
                WHERE 
                    (admin_id = ? OR organizer_id = ? OR volunteer_id = ?) 
                    AND password = ?
                """;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, id);
            ps.setInt(2, id);
            ps.setInt(3, id);
            ps.setString(4, password);

            ResultSet rs = ps.executeQuery();

            if (rs.next()) {

                String roleStr = rs.getString("role");
                Role role = Role.valueOf(roleStr.toUpperCase());

                int realId = switch (role) {
                    case ADMIN     -> rs.getInt("admin_id");
                    case ORGANIZER -> rs.getInt("organizer_id");
                    case VOLUNTEER -> rs.getInt("volunteer_id");
                };

                return new AuthenticatedUser(
                        realId,
                        rs.getString("full_name"),
                        rs.getString("email"),
                        role
                );
            }
        }
        return null;
    }

    // INTERFACE SWITCHER
    public static void openInterfaceFor(AuthenticatedUser user) {

        if (user == null) {
            JOptionPane.showMessageDialog(null, "User is null!", "Error", JOptionPane.ERROR_MESSAGE);
            return;
        }

        switch (user.getRole()) {

            case ADMIN -> openAdminInterface(user);

            case ORGANIZER -> openOrganizerInterface(user);

            case VOLUNTEER -> openVolunteerInterface(user);
        }
    }

    //  OPEN ADMIN 
    private static void openAdminInterface(AuthenticatedUser user) {
        SwingUtilities.invokeLater(() -> {

            Admin adminObj = getAdminData(user.getId());

            new AdminDashboardFrame(adminObj);
        });
    }

    private static Admin getAdminData(int adminId) {
        String sql = "SELECT * FROM admins WHERE admin_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setInt(1, adminId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                return new Admin(
                        rs.getInt("admin_id"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getBoolean("is_super_admin"),
                        rs.getString("role_title")
                );
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return null;
    }

    //  OPEN ORGANIZER 
    private static void openOrganizerInterface(AuthenticatedUser user) {
        SwingUtilities.invokeLater(() -> {
            new OrgClass(user.getId()).setVisible(true);
        });
    }

    //  OPEN VOLUNTEER 
    private static void openVolunteerInterface(AuthenticatedUser user) {
        SwingUtilities.invokeLater(() -> {
            new Volunteer(user).setVisible(true);
        });
    }

}
