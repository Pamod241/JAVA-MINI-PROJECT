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

    // ---------------------------------------------------------------
    // ADD a new medical record for a student
    // Returns true if successful, false if something went wrong
    // ---------------------------------------------------------------
    public boolean add(String studentId, String startDate, String endDate, String description) {

        // Generate the next ID like "MD001", "MD002", etc.
        String newMedicalId = generateNewMedicalId();

        String sql = "INSERT INTO Medical (medical_id, description, s_date, e_date, student_id) "
                + "VALUES (?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, newMedicalId);

            // If no description is given, default to "sick"
            statement.setString(2, description.isEmpty() ? "sick" : description);

            statement.setString(3, startDate);
            statement.setString(4, endDate);
            statement.setString(5, studentId);

            statement.executeUpdate();
            return true; // success

        } catch (SQLException e) {
            System.err.println("Error in add: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // DELETE a medical record by its ID
    // Returns true if successful, false if something went wrong
    // ---------------------------------------------------------------
    public boolean delete(String medicalId) {

        String sql = "DELETE FROM Medical WHERE medical_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, medicalId);
            statement.executeUpdate();
            return true; // success

        } catch (SQLException e) {
            System.err.println("Error in delete: " + e.getMessage());
            return false;
        }
    }

    // ---------------------------------------------------------------
    // GET all medical records for one specific student
    // Returns a list of Medical objects, newest first
    // ---------------------------------------------------------------
    public List<Medical> getByStudent(String studentId) {

        // This list will hold all the medical records we find
        List<Medical> medicalList = new ArrayList<>();

        String sql = "SELECT * FROM Medical WHERE student_id = ? ORDER BY s_date DESC";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);

            ResultSet results = statement.executeQuery();

            // Loop through each row and convert it to a Medical object
            while (results.next()) {
                Medical record = buildMedicalFromRow(results);
                medicalList.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error in getByStudent: " + e.getMessage());
        }

        return medicalList;
    }

    // ---------------------------------------------------------------
    // GET all medical records from every student
    // Returns a list of Medical objects, newest first
    // ---------------------------------------------------------------
    public List<Medical> getAll() {

        List<Medical> medicalList = new ArrayList<>();

        String sql = "SELECT * FROM Medical ORDER BY s_date DESC";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            // Loop through each row and convert it to a Medical object
            while (results.next()) {
                Medical record = buildMedicalFromRow(results);
                medicalList.add(record);
            }

        } catch (SQLException e) {
            System.err.println("Error in getAll: " + e.getMessage());
        }

        return medicalList;
    }

    // ---------------------------------------------------------------
    // HELPER: Reads one database row and builds a Medical object from it
    // The "throws SQLException" means if reading fails, the error
    // is passed up to the method that called this one
    // ---------------------------------------------------------------
    private Medical buildMedicalFromRow(ResultSet results) throws SQLException {

        // Read the start and end dates from the database row
        java.sql.Date startDateRaw = results.getDate("s_date");
        java.sql.Date endDateRaw   = results.getDate("e_date");

        // Convert from sql.Date to LocalDate (a simpler modern date type)
        // If the date is NULL in the database, keep it as null
        java.time.LocalDate startDate = (startDateRaw != null) ? startDateRaw.toLocalDate() : null;
        java.time.LocalDate endDate   = (endDateRaw   != null) ? endDateRaw.toLocalDate()   : null;

        // Build and return the Medical object with all the data
        return new Medical(
                results.getString("medical_id"),
                results.getString("description"),
                startDate,
                endDate,
                results.getString("student_id")
        );
    }

    // ---------------------------------------------------------------
    // HELPER: Generate the next medical ID in the format MD001, MD002 ...
    // Finds the highest existing number and adds 1
    // ---------------------------------------------------------------
    private String generateNewMedicalId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(medical_id, 3) AS UNSIGNED)) AS max_id FROM Medical";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            if (results.next()) {
                long highestNumber = results.getLong("max_id");  // e.g. 10
                // %03d pads with zeros to always give 3 digits: MD011 not MD11
                return String.format("MD%03d", highestNumber + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating medical ID: " + e.getMessage());
        }

        return "MD001"; // fallback if the table is empty
    }
}