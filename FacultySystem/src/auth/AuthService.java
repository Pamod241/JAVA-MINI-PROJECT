package auth;

import db.DBConnection;

import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class AuthService {

    public User login(String email, String password) {
        String query = "SELECT * FROM User WHERE Email = ? AND Password = ?";
        try {
            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(query);
            stmt.setString(1, email);
            stmt.setString(2, password);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                String userId = rs.getString("User_id");
                String dbRole = rs.getString("Role");
                String pwd    = rs.getString("Password");

                PreparedStatement ls = DBConnection.getConnection().prepareStatement(
                    "SELECT profile_id FROM Login WHERE User_id = ?");
                ls.setString(1, userId);
                ResultSet lr = ls.executeQuery();
                if (!lr.next()) return null;
                String profileId = lr.getString("profile_id");

                return switch (dbRole) {
                    case "Admin"            -> loadAdmin(userId, profileId, pwd);
                    case "Lecturer"         -> loadLecturer(userId, profileId, pwd);
                    case "Student"          -> loadStudent(userId, profileId, pwd);
                    case "Technical_Officer"-> loadTechnicalOfficer(userId, profileId, pwd);
                    default -> null;
                };
            }
        } catch (SQLException e) { System.err.println("Login error: " + e.getMessage()); }
        return null;
    }

    private Admin loadAdmin(String userId, String adminId, String pwd) throws SQLException {
        PreparedStatement s = DBConnection.getConnection().prepareStatement(
            "SELECT * FROM Admin WHERE Admin_id = ?");
        s.setString(1, adminId);
        ResultSet rs = s.executeQuery();
        if (rs.next())
            return new Admin(userId, adminId, "ADMIN", pwd,
                rs.getString("Email"), rs.getString("Fullname"));
        return null;
    }

    private Lecturer loadLecturer(String userId, String lecId, String pwd) throws SQLException {
        PreparedStatement s = DBConnection.getConnection().prepareStatement(
            "SELECT * FROM Lecturer WHERE Lec_id = ?");
        s.setString(1, lecId);
        ResultSet rs = s.executeQuery();
        if (rs.next())
            return new Lecturer(userId, lecId, "LECTURER", pwd,
                rs.getString("Email"), rs.getString("Fullname"),
                rs.getString("Gender"), rs.getString("Dep_id"));
        return null;
    }

    private Student loadStudent(String userId, String regNo, String pwd) throws SQLException {
        PreparedStatement s = DBConnection.getConnection().prepareStatement(
            "SELECT * FROM Student WHERE Reg_no = ?");
        s.setString(1, regNo);
        ResultSet rs = s.executeQuery();
        if (rs.next()) {
            Date dob = rs.getDate("DOB");
            return new Student(userId, regNo, "STUDENT", pwd,
                rs.getString("Email"), rs.getString("Fullname"),
                rs.getString("Dep_id"),
                dob != null ? dob.toLocalDate() : null,
                rs.getInt("Age"), rs.getString("Type"));
        }
        return null;
    }

    private TechnicalOfficer loadTechnicalOfficer(String userId, String toId, String pwd) throws SQLException {
        PreparedStatement s = DBConnection.getConnection().prepareStatement(
            "SELECT * FROM Technical_Officer WHERE TO_id = ?");
        s.setString(1, toId);
        ResultSet rs = s.executeQuery();
        if (rs.next())
            return new TechnicalOfficer(userId, toId, "TECHNICAL_OFFICER", pwd,
                rs.getString("Email"), rs.getString("Fullname"));
        return null;
    }
}
