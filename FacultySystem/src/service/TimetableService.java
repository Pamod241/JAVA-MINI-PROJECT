package service;

import db.DBConnection;
import model.Timetable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class TimetableService {
    public boolean add(String departmentId, String dayOfWeek, String courseCode,
                       String startTime, String endTime, String location, String sessionType) {
    String newTimetableId = generateNewTimetableId();

        String sql = "INSERT INTO Timetable (tt_id, Dep_id, day_of_week, C_code, start_time, end_time, location, session_type) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, newTimetableId);
            statement.setString(2, departmentId);
            statement.setString(3, dayOfWeek);
            statement.setString(4, courseCode);
            statement.setString(5, startTime);
            statement.setString(6, endTime);
            statement.setString(7, location);
            statement.setString(8, sessionType);

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error in add: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String timetableId) {

        String sql = "DELETE FROM Timetable WHERE tt_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, timetableId);
            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error in delete: " + e.getMessage());
            return false;
        }
    }

    public List<Timetable> getByDept(String departmentId) {


        List<Timetable> timetableList = new ArrayList<>();

                String sql = "SELECT * FROM Timetable "
                + "WHERE Dep_id = ? "
                + "ORDER BY FIELD(day_of_week, 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday'), "
                + "start_time";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, departmentId);

            ResultSet results = statement.executeQuery();

            while (results.next()) {

                String timetableId = results.getString("tt_id");
                String depId  = results.getString("Dep_id");
                String day   = results.getString("day_of_week");
                String courseCode  = results.getString("C_code");
                String location = results.getString("location");
                String sessionType = results.getString("session_type");

                java.time.LocalTime startTime = results.getTime("start_time").toLocalTime();
                java.time.LocalTime endTime   = results.getTime("end_time").toLocalTime();

                Timetable entry = new Timetable(
                        timetableId, depId, day, courseCode,
                        startTime, endTime, location, sessionType
                );

                timetableList.add(entry);
            }

        } catch (SQLException e) {
            System.err.println("Error in getByDept: " + e.getMessage());
        }

        return timetableList;
    }

    private String generateNewTimetableId() {
        String sql = "SELECT MAX(CAST(SUBSTRING(tt_id, 3) AS UNSIGNED)) AS max_id FROM Timetable";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            if (results.next()) {
                long highestNumber = results.getLong("max_id");
                return String.format("TT%03d", highestNumber + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating timetable ID: " + e.getMessage());
        }

        return "TT001";
    }
}