package model;

import javax.swing.*;

public class Lecturer extends User {
    private String gender;
    private String depId;

    public Lecturer(String userId, String lecId, String role, String password,
                    String email, String fullname, String gender, String depId) {
        super(userId, lecId, role, password, email, fullname);
        this.gender = gender;
        this.depId  = depId;
    }

    public String getGender()     { return gender; }
    public String getDepId()      { return depId; }
    public String getDepartment() { return depId; }

    @Override public void openDashboard() {
        SwingUtilities.invokeLater(() -> new gui.DashboardFrame(this));
    }
}
