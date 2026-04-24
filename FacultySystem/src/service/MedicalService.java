package service;

import db.DBConnection;
import model.Medical;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MedicalService {
    public boolean add(String studentId, String startDate, String endDate, String description) {

        String newMedicalId = generateNewMedicalId();

        String sql = "INSERT INTO Medical (medical_id, description, s_date, e_date, student_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, newMedicalId);

            statement.setString(2, description.isEmpty() ? "sick" : description);

            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.setString(5, studentId);

            statement.executeUpdate();
            return true;
        } catch (SQLException e) {
            System.err.println("Error in add: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String medicalId) {

        String sql = "DELETE FROM Medical WHERE medical_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, medicalId);
            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error in delete: " + e.getMessage());
            return false;
        }
    }
  public List<Medical> getByStudent(String studentId) {

        List<Medical> medicalList = new ArrayList<>();

        String sql = "SELECT * FROM Medical WHERE student_id = ? ORDER BY s_date DESC";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);

            ResultSet results = statement.executeQuery();

            while (results.next()) {
                Medical record = buildMedicalFromRow(results);
                medicalList.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error in getByStudent: " + e.getMessage());
        }

        return medicalList;
    }
    public List<Medical> getAll() {

        List<Medical> medicalList = new ArrayList<>();

        String sql = "SELECT * FROM Medical ORDER BY s_date DESC";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            while (results.next()) {
                Medical record = buildMedicalFromRow(results);
                medicalList.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error in getAll: " + e.getMessage());
        }

        return medicalList;
    }
    private Medical buildMedicalFromRow(ResultSet results) throws SQLException {

        java.sql.Date startDateRaw = results.getDate("s_date");
        java.sql.Date endDateRaw   = results.getDate("e_date");

        java.time.LocalDate startDate = (startDateRaw != null) ? startDateRaw.toLocalDate() : null;
        java.time.LocalDate endDate   = (endDateRaw   != null) ? endDateRaw.toLocalDate()   : null;
        return new Medical(
                results.getString("medical_id"),
                results.getString("description"),
                startDate,
                endDate,
                results.getString("student_id")
        );
    }

    private String generateNewMedicalId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(medical_id, 3) AS UNSIGNED)) AS max_id FROM Medical";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            if (results.next()) {
                long highestNumber = results.getLong("max_id");
                return String.format("MD%03d", highestNumber + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating medical ID: " + e.getMessage());
        }

        return "MD001";
    }
}