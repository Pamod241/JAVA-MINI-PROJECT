package service;

import db.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CourseService {

    // ---------------------------------------------------------------
    // GET all courses from the database
    // Returns a list where each item is one course's details
    // ---------------------------------------------------------------
    public List<Map<String, Object>> getAll() {

        // This list will hold all the courses we find
        List<Map<String, Object>> courseList = new ArrayList<>();

        String sql = "SELECT * FROM Course ORDER BY C_code ASC";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            // Loop through each row returned from the database
            while (results.next()) {

                // A Map holds one course's details as key-value pairs
                Map<String, Object> course = new LinkedHashMap<>();

                course.put("course_id",   results.getString("C_code"));
                course.put("course_name", results.getString("C_name"));
                course.put("credits",     results.getInt("Credit"));
                course.put("type",        results.getString("Type"));
                course.put("lec_id",      results.getString("Lec_id"));

                // Add this course to the main list
                courseList.add(course);
            }

        } catch (SQLException e) {
            System.err.println("Error in getAll: " + e.getMessage());
        }

        return courseList;
    }

    // ---------------------------------------------------------------
    // CREATE a new course and save it to the database
    // Returns null if successful, or an error message if it fails
    // ---------------------------------------------------------------
    public String create(String courseId, String courseName, int credits,
                         String type, String lecId) {

        // First check if this course ID is already taken
        if (courseIdExists(courseId)) {
            return "Course ID already exists.";
        }

        String sql = "INSERT INTO Course (C_code, C_name, Credit, Type, Lec_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, courseId);
            statement.setString(2, courseName);
            statement.setInt   (3, credits);
            statement.setString(4, type);

            // If no lecturer is assigned, store NULL in the database
            statement.setString(5, lecId.isEmpty() ? null : lecId);

            statement.executeUpdate();
            return null; // null means success

        } catch (SQLException e) {
            System.err.println("Error in create: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }

    // ---------------------------------------------------------------
    // UPDATE an existing course's details
    // Returns null if successful, or an error message if it fails
    // ---------------------------------------------------------------
    public String update(String courseId, String courseName, int credits,
                         String type, String lecId) {

        String sql = "UPDATE Course "
                + "SET C_name = ?, Credit = ?, Type = ?, Lec_id = ? "
                + "WHERE C_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, courseName);
            statement.setInt   (2, credits);
            statement.setString(3, type);

            // If no lecturer is assigned, store NULL in the database
            statement.setString(4, lecId.isEmpty() ? null : lecId);

            statement.setString(5, courseId);  // the WHERE condition

            statement.executeUpdate();
            return null; // null means success

        } catch (SQLException e) {
            System.err.println("Error in update: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }

    // ---------------------------------------------------------------
    // DELETE a course from the database
    // Returns null if successful, or an error message if it fails
    // Note: delete will fail if the course has attendance or marks
    //       records linked to it (database foreign key protection)
    // ---------------------------------------------------------------
    public String delete(String courseId) {

        String sql = "DELETE FROM Course WHERE C_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, courseId);
            statement.executeUpdate();
            return null; // null means success

        } catch (SQLException e) {
            System.err.println("Error in delete: " + e.getMessage());
            return "Cannot delete — course may have related attendance or marks records.";
        }
    }

    // ---------------------------------------------------------------
    // CHECK if a course ID already exists in the database
    // Returns true if found, false if not found
    // Used by create() to prevent duplicate course IDs
    // ---------------------------------------------------------------
    public boolean courseIdExists(String courseId) {

        // "SELECT 1" is a quick way to check existence — it returns one row if found
        String sql = "SELECT 1 FROM Course WHERE C_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, courseId);

            ResultSet results = statement.executeQuery();

            // results.next() returns true if at least one row was found
            return results.next();

        } catch (SQLException e) {
            System.err.println("Error in courseIdExists: " + e.getMessage());
            return false;
        }
    }
}