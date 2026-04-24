package model;

import javax.swing.*;

public class Admin extends User {



    public Admin(String userId, String adminId, String role,
                 String password, String email, String fullname) {


        super(userId, adminId, role, password, email, fullname);



    }

    @Override public void openDashboard() {



        SwingUtilities.invokeLater(() -> new gui.DashboardFrame(this));




    }
}
