package service;

import db.DBConnection;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class MarksService {
   public boolean addMark(String studentId, String courseId, String examType, double mark) {
        String columnName = null;

        if (examType.equals("QUIZ_1"))columnName = "quiz_1";
        else if (examType.equals("QUIZ_2"))  columnName = "quiz_2";
        else if (examType.equals("QUIZ_3"))  columnName = "quiz_3";
        else if (examType.equals("ASSESSMENT")) columnName = "assesment";
        else if (examType.equals("MID_EXAM")) columnName = "mid";
        else if (examType.equals("FINAL_EXAM")) columnName = "end";

        if (columnName == null) {
            System.err.println("Unknown exam type: " + examType);
            return false;
        }

        try {
            String checkSql = "SELECT mark_id FROM Mark WHERE student_id = ? AND course_code = ?";
            PreparedStatement checkStatement = DBConnection.getConnection().prepareStatement(checkSql);
            checkStatement.setString(1, studentId);
            checkStatement.setString(2, courseId);
            ResultSet results = checkStatement.executeQuery();

            if (results.next()) {
                String updateSql = "UPDATE Mark SET " + columnName + " = ? "
                        + "WHERE student_id = ? AND course_code = ?";
                PreparedStatement updateStatement = DBConnection.getConnection().prepareStatement(updateSql);
                updateStatement.setInt   (1, (int) mark);
                updateStatement.setString(2, studentId);
                updateStatement.setString(3, courseId);
                updateStatement.executeUpdate();

            } else {
                String newMarkId = generateNewMarkId();
                String insertSql = "INSERT INTO Mark (mark_id, student_id, course_code, " + columnName + ") "
                        + "VALUES (?, ?, ?, ?)";
                PreparedStatement insertStatement = DBConnection.getConnection().prepareStatement(insertSql);
                insertStatement.setString(1, newMarkId);
                insertStatement.setString(2, studentId);
                insertStatement.setString(3, courseId);
                insertStatement.setInt   (4, (int) mark);
                insertStatement.executeUpdate();
            }

            return true;

        } catch (SQLException e) {
            System.err.println("Error in addMark: " + e.getMessage());
            return false;
        }
    }

    public double getCAMark(String studentId, String courseId) {

        String sql = "SELECT quiz_1, quiz_2, quiz_3, assesment, mid "
                + "FROM Mark "
                + "WHERE student_id = ? AND course_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);
            statement.setString(2, courseId);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                int quiz1      = results.getInt("quiz_1");
                int quiz2      = results.getInt("quiz_2");
                int quiz3      = results.getInt("quiz_3");
                int assessment = results.getInt("assesment");
                int midterm    = results.getInt("mid");

                int lowestQuiz  = Math.min(quiz1, Math.min(quiz2, quiz3));  // LEAST(quiz1,quiz2,quiz3)
                int bestTwoSum  = quiz1 + quiz2 + quiz3 - lowestQuiz;       // drop lowest quiz

                double ca = (bestTwoSum  * 0.10)
                        + (assessment  * 0.05)
                        + (midterm     * 0.20);


                return ca;
            }

        } catch (SQLException e) {
            System.err.println("Error in getCAMark: " + e.getMessage());
        }

        return 0;
    }

    public double getFinalMark(String studentId, String courseId) {

        String sql = "SELECT end FROM Mark WHERE student_id = ? AND course_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);
            statement.setString(2, courseId);

            ResultSet results = statement.executeQuery();

            if (results.next()) {
                return results.getDouble("end");
            }

        } catch (SQLException e) {
            System.err.println("Error in getFinalMark: " + e.getMessage());
        }

        return 0;

    }


    public boolean isCAEligible(String studentId, String courseId) {
        double caAverage = getCAMark(studentId, courseId);
        return caAverage >= 17;
    }

    public String getGrade(double mark) {

        if (mark >= 85 && mark <= 100) return "A+";
        if (mark >= 75) return "A";
        if (mark >= 70) return "A-";
        if (mark >= 65) return "B+";
        if (mark >= 60) return "B";
        if (mark >= 55) return "B-";
        if (mark >= 50) return "C+";
        if (mark >= 45) return "C";
        if (mark >= 40) return "C-";
        if (mark >= 35) return "D";

        return "E";
   }

    public double getGradePoint(String grade) {

        if (grade.equals("A+") || grade.equals("A")) return 4.0;
        if (grade.equals("A-"))  return 3.7;
        if (grade.equals("B+"))  return 3.3;
        if (grade.equals("B"))   return 3.0;
        if (grade.equals("B-"))  return 2.7;
        if (grade.equals("C+"))  return 2.3;
        if (grade.equals("C"))   return 2.0;
        if (grade.equals("C-"))  return 1.7;
        if (grade.equals("D"))   return 1.3;

        return 0.0;
    }
private int getSemesterFromCourseCode(String courseCode) {
        try {
            if (courseCode == null || courseCode.length() < 5) return 1;
            int year = Character.getNumericValue(courseCode.charAt(3)); // 4th char
            int sem  = Character.getNumericValue(courseCode.charAt(4)); // 5th char
            return (year - 1) * 2 + sem; // overall semester number
        } catch (Exception e) {
            return 1; // default to semester 1 if pattern doesn't match
        }
    }

    public double calculateSGPA(String studentId, int semesterNumber) {

        double totalPoints  = 0;
        int    totalCredits = 0;

        String sql = "SELECT c.Credit, m.end AS final_mark, m.course_code "
                + "FROM Mark m "
                + "JOIN Course c ON m.course_code = c.C_code "
                + "WHERE m.student_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String courseCode = results.getString("course_code");

                if (getSemesterFromCourseCode(courseCode) != semesterNumber) continue;

                int    credits   = results.getInt("Credit");
                double finalMark = results.getDouble("final_mark");

                String grade      = getGrade(finalMark);
                double gradePoint = getGradePoint(grade);

                totalPoints  += gradePoint * credits;
                totalCredits += credits;
            }

        } catch (SQLException e) {
            System.err.println("Error in calculateSGPA: " + e.getMessage());
        }

        if (totalCredits == 0) return 0;
        return Math.round((totalPoints / totalCredits) * 100.0) / 100.0;
    }
    public double calculateSGPA(String studentId) {

        double totalPoints  = 0;
        int    totalCredits = 0;

        String sql = "SELECT c.Credit, m.end AS final_mark "
                + "FROM Mark m "
                + "JOIN Course c ON m.course_code = c.C_code "
                + "WHERE m.student_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                int    credits   = results.getInt("Credit");
                double finalMark = results.getDouble("final_mark");

                String grade      = getGrade(finalMark);
                double gradePoint = getGradePoint(grade);

                totalPoints  += gradePoint * credits;
                totalCredits += credits;
            }

        } catch (SQLException e) {
            System.err.println("Error in calculateSGPA: " + e.getMessage());
        }

        if (totalCredits == 0) return 0;
        return Math.round((totalPoints / totalCredits) * 100.0) / 100.0;
    }

    public double calculateCGPA(String studentId) {

        Set<Integer> semesters = new HashSet<>();

        String sql = "SELECT DISTINCT m.course_code "
                + "FROM Mark m "
                + "WHERE m.student_id = ? AND m.end > 0";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, studentId);
            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String courseCode = results.getString("course_code");
                semesters.add(getSemesterFromCourseCode(courseCode));
            }

        } catch (SQLException e) {
            System.err.println("Error in calculateCGPA: " + e.getMessage());
        }

        if (semesters.isEmpty()) return 0;
        double totalSGPA = 0;
        for (int sem : semesters) {
            totalSGPA += calculateSGPA(studentId, sem);
        }

        double cgpa = totalSGPA / semesters.size();
        return Math.round(cgpa * 100.0) / 100.0;
    }
    public List<Integer> getStudentSemesters(String studentId) {
        Set<Integer> semSet = new TreeSet<>();

        String sql = "SELECT DISTINCT course_code FROM Mark WHERE student_id = ? AND end > 0";
        try {
            PreparedStatement stmt = DBConnection.getConnection().prepareStatement(sql);
            stmt.setString(1, studentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                semSet.add(getSemesterFromCourseCode(rs.getString("course_code")));
            }
        } catch (SQLException e) {
            System.err.println("Error in getStudentSemesters: " + e.getMessage());
        }

        return new ArrayList<>(semSet);
    }
    public List<Map<String, Object>> getBatchSummary(String courseId) {

        List<Map<String, Object>> summaryList = new ArrayList<>();

        String sql = "SELECT student_id, end AS final_mark FROM Mark WHERE course_code = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);
            statement.setString(1, courseId);

            ResultSet results = statement.executeQuery();

            while (results.next()) {
                String studentId = results.getString("student_id");
                double finalMark = results.getDouble("final_mark");


                Map<String, Object> studentSummary = new HashMap<>();
                studentSummary.put("studentId",  studentId);
                studentSummary.put("finalMark",  finalMark);
                studentSummary.put("grade",      getGrade(finalMark));
                studentSummary.put("caEligible", isCAEligible(studentId, courseId));

                summaryList.add(studentSummary);
            }

        } catch (SQLException e) {
            System.err.println("Error in getBatchSummary: " + e.getMessage());
        }

        return summaryList;
    }
    private String generateNewMarkId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(mark_id, 2) AS UNSIGNED)) AS max_id FROM Mark";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            if (results.next()) {
                long highestId = results.getLong("max_id"); // e.g. 120
                return "M" + (highestId + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating mark ID: " + e.getMessage());
        }

        return "M1";
    }
}