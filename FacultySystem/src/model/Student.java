package model;

import javax.swing.*;
import java.time.LocalDate;

public class Student extends User {
    private String    depId;
    private LocalDate dob;
    private int       age;
    private String    type;

    public Student(String userId, String regNo, String role, String password,
                   String email, String fullname, String depId, LocalDate dob, int age, String type) {
        super(userId, regNo, role, password, email, fullname);
        this.depId = depId;
        this.dob   = dob;
        this.age   = age;
        this.type  = type;
    }

    public String    getStudentId()   { return getProfileId(); }
    public String    getRegNo()       { return getProfileId(); }
    public String    getDepId()       { return depId; }
    public LocalDate getDob()         { return dob; }
    public int       getAge()         { return age; }
    public String    getType()        { return type; }
    public boolean   isRepeat()       { return "Repeat".equals(type); }
    public boolean   isBatchMissed()    { return "BatchMissed".equals(type); }
    public String    getStudentType() { return type != null ? type : "Proper"; }

    @Override public void openDashboard() {
        SwingUtilities.invokeLater(() -> new gui.DashboardFrame(this));
    }
}
