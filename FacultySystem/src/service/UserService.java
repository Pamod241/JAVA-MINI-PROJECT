package service;

import db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class UserService {


    public List<Map<String, Object>> getAll() {


        List<Map<String, Object>> allUsers = new ArrayList<>();


        allUsers.addAll(getAllByRole("STUDENT"));
        allUsers.addAll(getAllByRole("LECTURER"));
        allUsers.addAll(getAllByRole("TECHNICAL_OFFICER"));
        allUsers.addAll(getAllByRole("ADMIN"));

        return allUsers;
    }


    public List<Map<String, Object>> getAllByRole(String role) {

        List<Map<String, Object>> userList = new ArrayList<>();

        try {


            if (role.equals("STUDENT")) {

                String sql = "SELECT u.User_id, u.Password, l.profile_id, "
                        + "st.Fullname, st.Email, st.Dep_id, st.Type "
                        + "FROM User u "
                        + "JOIN Login   l  ON u.User_id    = l.User_id "
                        + "JOIN Student st ON l.profile_id = st.Reg_no "
                        + "WHERE u.Role = 'Student' "
                        + "ORDER BY st.Reg_no";

                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    Map<String, Object> user = new LinkedHashMap<>();
                    user.put("user_id",    results.getString("User_id"));
                    user.put("profile_id", results.getString("profile_id"));
                    user.put("role",       "STUDENT");
                    user.put("fullname",   results.getString("Fullname"));
                    user.put("email",      results.getString("Email"));
                    user.put("password",   results.getString("Password"));
                    user.put("dep_id",     results.getString("Dep_id"));
                    user.put("type",       results.getString("Type"));
                    user.put("gender",     null);  // students don't have a gender field
                    userList.add(user);
                }
            }

            // ── LECTURERS ─────────────────────────────────────────
            // Join User → Login → Lecturer to get full lecturer details
            else if (role.equals("LECTURER")) {

                String sql = "SELECT u.User_id, u.Password, l.profile_id, "
                        + "lc.Fullname, lc.Email, lc.Dep_id, lc.Gender "
                        + "FROM User u "
                        + "JOIN Login    l  ON u.User_id    = l.User_id "
                        + "JOIN Lecturer lc ON l.profile_id = lc.Lec_id "
                        + "WHERE u.Role = 'Lecturer' "
                        + "ORDER BY lc.Lec_id";

                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    Map<String, Object> user = new LinkedHashMap<>();
                    user.put("user_id",    results.getString("User_id"));
                    user.put("profile_id", results.getString("profile_id"));
                    user.put("role",       "LECTURER");
                    user.put("fullname",   results.getString("Fullname"));
                    user.put("email",      results.getString("Email"));
                    user.put("password",   results.getString("Password"));
                    user.put("dep_id",     results.getString("Dep_id"));
                    user.put("type",       null);    // lecturers don't have a type field
                    user.put("gender",     results.getString("Gender"));
                    userList.add(user);
                }
            }

            // ── TECHNICAL OFFICERS ────────────────────────────────
            // Join User → Login → Technical_Officer
            else if (role.equals("TECHNICAL_OFFICER")) {

                String sql = "SELECT u.User_id, u.Password, l.profile_id, "
                        + "t.Fullname, t.Email "
                        + "FROM User u "
                        + "JOIN Login            l ON u.User_id    = l.User_id "
                        + "JOIN Technical_Officer t ON l.profile_id = t.TO_id "
                        + "WHERE u.Role = 'Technical_Officer' "
                        + "ORDER BY t.TO_id";

                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    Map<String, Object> user = new LinkedHashMap<>();
                    user.put("user_id",    results.getString("User_id"));
                    user.put("profile_id", results.getString("profile_id"));
                    user.put("role",       "TECHNICAL_OFFICER");
                    user.put("fullname",   results.getString("Fullname"));
                    user.put("email",      results.getString("Email"));
                    user.put("password",   results.getString("Password"));
                    user.put("dep_id",     null);  // technical officers have no department
                    user.put("type",       null);
                    user.put("gender",     null);
                    userList.add(user);
                }
            }

            // ── ADMINS ────────────────────────────────────────────
            // Join User → Login → Admin
            else if (role.equals("ADMIN")) {

                String sql = "SELECT u.User_id, u.Password, l.profile_id, "
                        + "a.Fullname, a.Email "
                        + "FROM User u "
                        + "JOIN Login l ON u.User_id    = l.User_id "
                        + "JOIN Admin a ON l.profile_id = a.Admin_id "
                        + "WHERE u.Role = 'Admin' "
                        + "ORDER BY a.Admin_id";

                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                ResultSet results = statement.executeQuery();

                while (results.next()) {
                    Map<String, Object> user = new LinkedHashMap<>();
                    user.put("user_id",    results.getString("User_id"));
                    user.put("profile_id", results.getString("profile_id"));
                    user.put("role",       "ADMIN");
                    user.put("fullname",   results.getString("Fullname"));
                    user.put("email",      results.getString("Email"));
                    user.put("password",   results.getString("Password"));
                    user.put("dep_id",     null);
                    user.put("type",       null);
                    user.put("gender",     null);
                    userList.add(user);
                }
            }

        } catch (SQLException e) {
            System.err.println("Error in getAllByRole: " + e.getMessage());
        }

        return userList;
    }


    // CREATE a brand new user account

    public String createUser(String role, String profileId, String fullname, String email,
                             String password, String depId, String type, String gender) {

        // Basic validation before touching the database
        if (profileId == null || profileId.isEmpty()) {
            return "Profile ID is required.";
        }
        if (profileIdExists(profileId)) {
            return "Profile ID already exists.";
        }


        String newUserId = generateNewUserId();

        String databaseRole = "";
        if      (role.equals("STUDENT"))           databaseRole = "Student";
        else if (role.equals("LECTURER"))          databaseRole = "Lecturer";
        else if (role.equals("TECHNICAL_OFFICER")) databaseRole = "Technical_Officer";
        else                                       databaseRole = "Admin";

        try {
            Connection connection = DBConnection.getConnection();

            // Turn off auto-commit so we can group all inserts into one transaction
            // This means: either ALL inserts succeed, or NONE of them are saved
            connection.setAutoCommit(false);

            try {
                // ── Step 1: Insert into the User table ──────────────
                String userSql = "INSERT INTO User (User_id, Email, Password, Role) "
                        + "VALUES (?, ?, ?, ?)";
                PreparedStatement userStatement = connection.prepareStatement(userSql);
                userStatement.setString(1, newUserId);
                userStatement.setString(2, email);
                userStatement.setString(3, password);
                userStatement.setString(4, databaseRole);
                userStatement.executeUpdate();

                // ── Step 2: Insert into the Login table ─────────────
                // This links the User account to their role-specific profile
                String loginSql = "INSERT INTO Login (User_id, profile_id) VALUES (?, ?)";
                PreparedStatement loginStatement = connection.prepareStatement(loginSql);
                loginStatement.setString(1, newUserId);
                loginStatement.setString(2, profileId);
                loginStatement.executeUpdate();

                // ── Step 3: Insert into the role-specific profile table ──
                if (role.equals("STUDENT")) {

                    String sql = "INSERT INTO Student (Reg_no, Email, Fullname, Dep_id, Type) "
                            + "VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, profileId);
                    statement.setString(2, email);
                    statement.setString(3, fullname);
                    statement.setString(4, depId.isEmpty() ? null : depId);
                    statement.setString(5, type.isEmpty() ? "Proper" : type); // default to "Proper"
                    statement.executeUpdate();

                } else if (role.equals("LECTURER")) {

                    String sql = "INSERT INTO Lecturer (Lec_id, Email, Fullname, Gender, Dep_id) "
                            + "VALUES (?, ?, ?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, profileId);
                    statement.setString(2, email);
                    statement.setString(3, fullname);
                    statement.setString(4, gender.isEmpty() ? null : gender);
                    statement.setString(5, depId.isEmpty() ? null : depId);
                    statement.executeUpdate();

                } else if (role.equals("TECHNICAL_OFFICER")) {

                    String sql = "INSERT INTO Technical_Officer (TO_id, Email, Fullname) "
                            + "VALUES (?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, profileId);
                    statement.setString(2, email);
                    statement.setString(3, fullname);
                    statement.executeUpdate();

                } else if (role.equals("ADMIN")) {

                    String sql = "INSERT INTO Admin (Admin_id, Email, Fullname) "
                            + "VALUES (?, ?, ?)";
                    PreparedStatement statement = connection.prepareStatement(sql);
                    statement.setString(1, profileId);
                    statement.setString(2, email);
                    statement.setString(3, fullname);
                    statement.executeUpdate();
                }


                connection.commit();
                return null;

            } catch (SQLException e) {

                connection.rollback();
                System.err.println("Error in createUser: " + e.getMessage());
                return "Database error: " + e.getMessage();

            } finally {

                connection.setAutoCommit(true);
            }

        } catch (SQLException e) {
            return "Connection error: " + e.getMessage();
        }
    }



    public String updateUser(String userId, String fullname, String email, String password,
                             String depId, String type, String gender) {

        try {
            // ── Step 1: Update email and password in the User table ──
            String updateUserSql = "UPDATE User SET Email = ?, Password = ? WHERE User_id = ?";
            PreparedStatement updateUserStatement = DBConnection.getConnection().prepareStatement(updateUserSql);
            updateUserStatement.setString(1, email);
            updateUserStatement.setString(2, password);
            updateUserStatement.setString(3, userId);
            updateUserStatement.executeUpdate();

            // ── Step 2: Find the profile_id from the Login table ────
            // We need it to know which row to update in the profile table
            String loginSql = "SELECT profile_id FROM Login WHERE User_id = ?";
            PreparedStatement loginStatement = DBConnection.getConnection().prepareStatement(loginSql);
            loginStatement.setString(1, userId);
            ResultSet loginResults = loginStatement.executeQuery();

            if (!loginResults.next()) return null; // no login row found, nothing more to do
            String profileId = loginResults.getString("profile_id");

            // ── Step 3: Find the role from the User table ────────────
            // We need it to know which profile table to update
            String roleSql = "SELECT Role FROM User WHERE User_id = ?";
            PreparedStatement roleStatement = DBConnection.getConnection().prepareStatement(roleSql);
            roleStatement.setString(1, userId);
            ResultSet roleResults = roleStatement.executeQuery();

            if (!roleResults.next()) return null; // no user row found
            String databaseRole = roleResults.getString("Role");

            // ── Step 4: Update the correct profile table ─────────────
            if (databaseRole.equals("Student")) {

                String sql = "UPDATE Student "
                        + "SET Fullname = ?, Email = ?, Dep_id = ?, Type = ? "
                        + "WHERE Reg_no = ?";
                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                statement.setString(1, fullname);
                statement.setString(2, email);
                statement.setString(3, depId.isEmpty() ? null : depId);
                statement.setString(4, type.isEmpty() ? "Proper" : type);
                statement.setString(5, profileId);
                statement.executeUpdate();

            } else if (databaseRole.equals("Lecturer")) {

                String sql = "UPDATE Lecturer "
                        + "SET Fullname = ?, Email = ?, Gender = ?, Dep_id = ? "
                        + "WHERE Lec_id = ?";
                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                statement.setString(1, fullname);
                statement.setString(2, email);
                statement.setString(3, gender.isEmpty() ? null : gender);
                statement.setString(4, depId.isEmpty() ? null : depId);
                statement.setString(5, profileId);
                statement.executeUpdate();

            } else if (databaseRole.equals("Technical_Officer")) {

                String sql = "UPDATE Technical_Officer "
                        + "SET Fullname = ?, Email = ? "
                        + "WHERE TO_id = ?";
                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                statement.setString(1, fullname);
                statement.setString(2, email);
                statement.setString(3, profileId);
                statement.executeUpdate();

            } else if (databaseRole.equals("Admin")) {

                String sql = "UPDATE Admin "
                        + "SET Fullname = ?, Email = ? "
                        + "WHERE Admin_id = ?";
                PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
                statement.setString(1, fullname);
                statement.setString(2, email);
                statement.setString(3, profileId);
                statement.executeUpdate();
            }

            return null; // null means success

        } catch (SQLException e) {
            System.err.println("Error in updateUser: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }

    // ---------------------------------------------------------------
    // DELETE a user by their User ID
    // The database will automatically delete their Login row too
    // (because of the CASCADE rule set up in the database schema)
    // Returns null if successful, or an error message if it fails
    // ---------------------------------------------------------------
    public String deleteUser(String userId) {

        String sql = "DELETE FROM User WHERE User_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, userId);
            statement.executeUpdate();
            return null; // null means success

        } catch (SQLException e) {
            System.err.println("Error in deleteUser: " + e.getMessage());
            return "Cannot delete — user may have related records.";
        }
    }


    // CHECK if a profile ID already exists in the Login table

    public boolean profileIdExists(String profileId) {

        // "SELECT 1" is a quick existence check — returns one row if found
        String sql = "SELECT 1 FROM Login WHERE profile_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, profileId);

            ResultSet results = statement.executeQuery();

            // results.next() returns true if at least one row was found
            return results.next();

        } catch (SQLException e) {
            System.err.println("Error in profileIdExists: " + e.getMessage());
            return false;
        }
    }


    private String generateNewUserId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(User_id, 2) AS UNSIGNED)) AS max_id FROM User";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            if (results.next()) {
                long highestNumber = results.getLong("max_id");

                return String.format("U%03d", highestNumber + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating user ID: " + e.getMessage());
        }

        return "U001";
    }
}