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


    public List<Map<String, Object>> getAll() {


        List<Map<String, Object>> courseList = new ArrayList<>();

        String sql = "SELECT * FROM Course ORDER BY C_code ASC";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);


            while (results.next()) {


                Map<String, Object> course = new LinkedHashMap<>();

                course.put("course_id",   results.getString("C_code"));
                course.put("course_name", results.getString("C_name"));
                course.put("credits",     results.getInt("Credit"));
                course.put("type",        results.getString("Type"));
                course.put("lec_id",      results.getString("Lec_id"));


                courseList.add(course);
            }

        } catch (SQLException e) {
            System.err.println("Error in getAll: " + e.getMessage());
        }

        return courseList;
    }


    public String create(String courseId, String courseName, int credits,
                         String type, String lecId) {


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


            statement.setString(5, lecId.isEmpty() ? null : lecId);

            statement.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("Error in create: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }


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


            statement.setString(4, lecId.isEmpty() ? null : lecId);

            statement.setString(5, courseId);

            statement.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("Error in update: " + e.getMessage());
            return "Database error: " + e.getMessage();
        }
    }


    public String delete(String courseId) {

        String sql = "DELETE FROM Course WHERE C_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, courseId);
            statement.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("Error in delete: " + e.getMessage());
            return "Cannot delete — course may have related attendance or marks records.";
        }
    }


    public boolean courseIdExists(String courseId) {

        String sql = "SELECT 1 FROM Course WHERE C_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, courseId);

            ResultSet results = statement.executeQuery();


            return results.next();

        } catch (SQLException e) {
            System.err.println("Error in courseIdExists: " + e.getMessage());
            return false;
        }
    }
}