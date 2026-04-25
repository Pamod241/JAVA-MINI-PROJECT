package service;

import db.DBConnection;
import java.sql.*;
import java.util.*;

public class AttendanceService {


    private static final double MINIMUM_ATTENDANCE = 80.0;




    public boolean addAttendance(String studentId, String courseId, String date,
                                 String sessionType, String attendanceState, int hours) {


        String newAttId = generateNewAttendanceId();


        String sql = "INSERT INTO Attendence (att_id, date, att_state, session_type, hour, student_id, medical_id, course_code) "
                + "VALUES (?, ?, ?, ?, ?, ?, NULL, ?)";

        try {

            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);


            statement.setString(1, newAttId);
            statement.setString(2, date);
            statement.setString(3, attendanceState);
            statement.setString(4, sessionType);
            statement.setInt   (5, hours);
            statement.setString(6, studentId);
            statement.setString(7, courseId);


            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error adding attendance: " + e.getMessage());
            return false;
        }
    }


    public double getAttendancePercentage(String studentId, String courseId, String type) {


        String extraFilter = "";
        if (type.equals("THEORY")) {
            extraFilter = " AND session_type = 'Lecture'";
        } else if (type.equals("PRACTICAL")) {
            extraFilter = " AND session_type = 'Practical'";
        }

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


                if (totalClasses == 0) return 0;


                double percentage = (double) presentClasses / totalClasses * 100;
                return percentage;
            }

        } catch (SQLException e) {
            System.err.println("Error getting attendance: " + e.getMessage());
        }

        return 0;
    }


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


                double percentage = (double)(presentClasses + medicalClasses) / totalClasses * 100;
                return percentage;
            }

        } catch (SQLException e) {
            System.err.println("Error getting attendance with medicals: " + e.getMessage());
        }

        return 0;
    }


    public boolean isEligible(String studentId, String courseId) {
        double attendance = getAttendanceWithMedicals(studentId, courseId);
        return attendance >= MINIMUM_ATTENDANCE;
    }


    public Map<String, Double> getBatchSummary(String courseId) {


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



    private String generateNewAttendanceId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(att_id, 2) AS UNSIGNED)) AS max_id FROM Attendence";

        try {
            ResultSet results = DBConnection.getConnection().createStatement().executeQuery(sql);

            if (results.next()) {
                long highestId = results.getLong("max_id");
                return "A" + (highestId + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating ID: " + e.getMessage());
        }

        return "A1";
    }
}