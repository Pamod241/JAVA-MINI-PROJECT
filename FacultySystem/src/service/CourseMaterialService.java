package service;

import db.DBConnection;

import java.sql.*;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CourseMaterialService {


    public List<Map<String, Object>> getByCourse(String courseId) {


        List<Map<String, Object>> materialList = new ArrayList<>();


        String sql = "SELECT cm.*, l.Fullname AS uploader_name "
                + "FROM Course_Material cm "
                + "LEFT JOIN Lecturer l ON cm.uploaded_by = l.Lec_id "
                + "WHERE cm.C_code = ? "
                + "ORDER BY cm.uploaded_at DESC";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, courseId);

            ResultSet results = statement.executeQuery();


            addResultsToList(materialList, results);

        } catch (SQLException e) {
            System.err.println("Error in getByCourse: " + e.getMessage());
        }

        return materialList;
    }


    public List<Map<String, Object>> getAll() {

        List<Map<String, Object>> materialList = new ArrayList<>();

        String sql = "SELECT cm.*, l.Fullname AS uploader_name "
                + "FROM Course_Material cm "
                + "LEFT JOIN Lecturer l ON cm.uploaded_by = l.Lec_id "
                + "ORDER BY cm.uploaded_at DESC";  // newest first

        try {
            Statement statement  = DBConnection.getConnection().createStatement();
            ResultSet results    = statement.executeQuery(sql);

            addResultsToList(materialList, results);

        } catch (SQLException e) {
            System.err.println("Error in getAll: " + e.getMessage());
        }

        return materialList;
    }


    private void addResultsToList(List<Map<String, Object>> materialList, ResultSet results) throws SQLException {

        while (results.next()) {


            Map<String, Object> material = new LinkedHashMap<>();

            material.put("material_id",   results.getString("mat_id"));
            material.put("course_id",     results.getString("C_code"));
            material.put("uploaded_by",   results.getString("uploaded_by"));
            material.put("uploader_name", results.getString("uploader_name"));
            material.put("title",         results.getString("title"));
            material.put("description",   results.getString("description"));
            material.put("file_url",      results.getString("file_url"));
            material.put("material_type", results.getString("material_type"));


            Timestamp uploadTime = results.getTimestamp("uploaded_at");
            if (uploadTime != null) {
                material.put("uploaded_at", uploadTime.toLocalDateTime().toLocalDate().toString());
            } else {
                material.put("uploaded_at", "");
            }


            materialList.add(material);
        }
    }


    public String add(String courseId, String uploadedBy, String title,
                      String description, String fileUrl, String materialType) {


        if (title == null || title.isEmpty()) {
            return "Title is required.";
        }


        String newMaterialId = generateNewMaterialId();

        String sql = "INSERT INTO Course_Material (mat_id, C_code, uploaded_by, title, description, file_url, material_type) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, newMaterialId);
            statement.setString(2, courseId);
            statement.setString(3, uploadedBy);
            statement.setString(4, title);


            statement.setString(5, description.isEmpty() ? null : description);
            statement.setString(6, fileUrl.isEmpty()     ? null : fileUrl);

            statement.setString(7, materialType);

            statement.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("Error in add: " + e.getMessage());
            return "DB error: " + e.getMessage();
        }
    }


    public String update(String materialId, String title, String description,
                         String fileUrl, String materialType) {

        String sql = "UPDATE Course_Material "
                + "SET title = ?, description = ?, file_url = ?, material_type = ? "
                + "WHERE mat_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, title);
            statement.setString(2, description.isEmpty() ? null : description);
            statement.setString(3, fileUrl.isEmpty()     ? null : fileUrl);
            statement.setString(4, materialType);
            statement.setString(5, materialId);  // the WHERE condition

            statement.executeUpdate();
            return null;

        } catch (SQLException e) {
            System.err.println("Error in update: " + e.getMessage());
            return "DB error: " + e.getMessage();
        }
    }


    public String delete(String materialId, String lecturerId) {


        String sql = "DELETE FROM Course_Material WHERE mat_id = ? AND uploaded_by = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, materialId);
            statement.setString(2, lecturerId);

            int rowsDeleted = statement.executeUpdate();


            if (rowsDeleted == 0) {
                return "Not found or you don't have permission to delete this.";
            }

            return null;

        } catch (SQLException e) {
            System.err.println("Error in delete: " + e.getMessage());
            return "DB error: " + e.getMessage();
        }
    }


    public List<String> getAllCourseIds() {

        List<String> courseIdList = new ArrayList<>();

        String sql = "SELECT C_code FROM Course ORDER BY C_code";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            while (results.next()) {
                courseIdList.add(results.getString(1));
            }

        } catch (SQLException e) {
            System.err.println("Error in getAllCourseIds: " + e.getMessage());
        }

        return courseIdList;
    }


    private String generateNewMaterialId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(mat_id, 3) AS UNSIGNED)) AS max_id FROM Course_Material";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            if (results.next()) {
                long highestNumber = results.getLong("max_id");

                return String.format("CM%03d", highestNumber + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating material ID: " + e.getMessage());
        }

        return "CM001";
    }
}