package model;

import javax.swing.SwingUtilities;

public class TechnicalOfficer extends User {
    public TechnicalOfficer(String userId, String toId, String role,
                             String password, String email, String fullname) {
        super(userId, toId, role, password, email, fullname);
    }

    public String getDepartment() { return null; }

    @Override public void openDashboard() {
        SwingUtilities.invokeLater(() -> new gui.DashboardFrame(this));
    }
}
