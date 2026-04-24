package service;

import db.DBConnection;
import java.sql.*;
import java.util.*;

public class chAttendanceService {

    // Minimum attendance percentage needed to be eligible (80%)
    private static final double MINIMUM_ATTENDANCE = 80.0;

    // ---------------------------------------------------------------
    // ADD a new attendance record to the database
    // ---------------------------------------------------------------
    public boolean addAttendance(String studentId, String courseId, String date,
                                 String sessionType, String attendanceState, int hours) {

        // First, generate a new unique ID like "A101"
        String newAttId = generateNewAttendanceId();

        // Write the SQL query to insert one row
        String sql = "INSERT INTO Attendence (att_id, date, att_state, session_type, hour, student_id, medical_id, course_code) "
                + "VALUES (?, ?, ?, ?, ?, ?, NULL, ?)";

        try {
            // Get database connection and prepare the query
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            // Fill in the ? placeholders one by one
            statement.setString(1, newAttId);
            statement.setString(2, date);
            statement.setString(3, attendanceState);   // "Present" or "Absent"
            statement.setString(4, sessionType);        // "Lecture" or "Practical"
            statement.setInt   (5, hours);
            statement.setString(6, studentId);
            statement.setString(7, courseId);

            // Run the query
            statement.executeUpdate();
            return true; // success

        } catch (SQLException e) {
            System.err.println("Error adding attendance: " + e.getMessage());
            return false; // something went wrong
        }
    }

    // ---------------------------------------------------------------
    // GET attendance percentage for a student in a course
    // type can be: "BOTH", "THEORY", or "PRACTICAL"
    // ---------------------------------------------------------------
    public double getAttendancePercentage(String studentId, String courseId, String type) {

        // Build an extra filter depending on the type requested
        String extraFilter = "";
        if (type.equals("THEORY")) {
            extraFilter = " AND session_type = 'Lecture'";
        } else if (type.equals("PRACTICAL")) {
            extraFilter = " AND session_type = 'Practical'";
        }
        // If type is "BOTH", no extra filter is needed

        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN att_state = 'Present' THEN 1 ELSE 0 END) AS present_count "
                + "FROM Attendence "
                + "WHERE student_id = ? AND course_code = ?"
                + extraFilter;

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);
            statement.setString(2, courseId);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                int totalClasses   = results.getInt("total");
                int presentClasses = results.getInt("present_count");

                // Avoid dividing by zero
                if (totalClasses == 0) return 0;

                // Calculate percentage: (present / total) * 100
                double percentage = (double) presentClasses / totalClasses * 100;
                return percentage;
            }

        } catch (SQLException e) {
            System.err.println("Error getting attendance: " + e.getMessage());
        }

        return 0; // default if something goes wrong
    }

    // ---------------------------------------------------------------
    // GET attendance percentage INCLUDING medical leave absences
    // (medical absences count as "present" for eligibility)
    // ---------------------------------------------------------------
    public double getAttendanceWithMedicals(String studentId, String courseId) {

        String sql = "SELECT COUNT(*) AS total, "
                + "SUM(CASE WHEN att_state = 'Present' THEN 1 ELSE 0 END) AS present_count, "
                + "SUM(CASE WHEN att_state = 'Absent' AND medical_id IS NOT NULL THEN 1 ELSE 0 END) AS medical_count "
                + "FROM Attendence "
                + "WHERE student_id = ? AND course_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);
            statement.setString(2, courseId);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                int totalClasses   = results.getInt("total");
                int presentClasses = results.getInt("present_count");
                int medicalClasses = results.getInt("medical_count");

                if (totalClasses == 0) return 0;

                // Add medical absences to present count before calculating
                double percentage = (double)(presentClasses + medicalClasses) / totalClasses * 100;
                return percentage;
            }

        } catch (SQLException e) {
            System.err.println("Error getting attendance with medicals: " + e.getMessage());
        }

        return 0;
    }

    // ---------------------------------------------------------------
    // CHECK if a student is eligible to sit the exam
    // (needs 80% or more attendance including medical leave)
    // ---------------------------------------------------------------
    public boolean isEligible(String studentId, String courseId) {
        double attendance = getAttendanceWithMedicals(studentId, courseId);
        return attendance >= MINIMUM_ATTENDANCE;
    }

    // ---------------------------------------------------------------
    // GET attendance summary for ALL students in a course
    // Returns a Map like: { "TG0001" -> 85.5, "TG0002" -> 72.0 }
    // ---------------------------------------------------------------
    public Map<String, Double> getBatchSummary(String courseId) {

        // LinkedHashMap keeps the results in insertion order
        Map<String, Double> summaryMap = new LinkedHashMap<>();

        String sql = "SELECT student_id, "
                + "ROUND(SUM(CASE WHEN att_state = 'Present' THEN 1 ELSE 0 END) / COUNT(*) * 100, 2) AS percentage "
                + "FROM Attendence "
                + "WHERE course_code = ? "
                + "GROUP BY student_id";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, courseId);

            ResultSet results = statement.executeQuery();

            // Loop through each student row and add to the map
            while (results.next()) {
                String studentId   = results.getString("student_id");
                double percentage  = results.getDouble("percentage");
                summaryMap.put(studentId, percentage);
            }

        } catch (SQLException e) {
            System.err.println("Error getting batch summary: " + e.getMessage());
        }

        return summaryMap;
    }

    // ---------------------------------------------------------------
    // HELPER: Generate the next attendance ID (e.g. A1, A2, A101 ...)
    // Looks at the highest existing ID number and adds 1
    // ---------------------------------------------------------------
    private String generateNewAttendanceId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(att_id, 2) AS UNSIGNED)) AS max_id FROM Attendence";

        try {
            ResultSet results = DBConnection.getConnection().createStatement().executeQuery(sql);

            if (results.next()) {
                long highestId = results.getLong("max_id"); // e.g. 100
                return "A" + (highestId + 1);               // returns "A101"
            }

        } catch (SQLException e) {
            System.err.println("Error generating ID: " + e.getMessage());
        }

        return "A1"; // fallback if table is empty
    }
}