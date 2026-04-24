package service;

import db.DBConnection;
import model.Notice;
import java.sql.*;
import java.util.*;

public class NoticeService {


    public boolean add(String title, String content, String createdBy) {


        String newNoticeId = generateNewNoticeId();

        String sql = "INSERT INTO Notice (notice_id, title, content, created_by) "
                + "VALUES (?, ?, ?, ?)";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, newNoticeId);
            statement.setString(2, title);
            statement.setString(3, content);
            statement.setString(4, createdBy);

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error in add: " + e.getMessage());
            return false;
        }
    }

    public boolean delete(String noticeId) {

        String sql = "DELETE FROM Notice WHERE notice_id = ?";

        try {
            PreparedStatement statement = DBConnection.getConnection().prepareStatement(sql);

            statement.setString(1, noticeId);
            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            System.err.println("Error in delete: " + e.getMessage());
            return false;
        }
    }

    public List<Notice> getAll() {


        List<Notice> noticeList = new ArrayList<>();

        String sql = "SELECT * FROM Notice ORDER BY created_at DESC";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            while (results.next()) {

                String noticeId  = results.getString("notice_id");
                String title     = results.getString("title");
                String content   = results.getString("content");
                String createdBy = results.getString("created_by");


                java.time.LocalDateTime createdAt =
                        results.getTimestamp("created_at").toLocalDateTime();


                Notice notice = new Notice(noticeId, title, content, createdBy, createdAt);
                noticeList.add(notice);
            }

        } catch (SQLException e) {
            System.err.println("Error in getAll: " + e.getMessage());
        }

        return noticeList;
    }

    private String generateNewNoticeId() {

        String sql = "SELECT MAX(CAST(SUBSTRING(notice_id, 2) AS UNSIGNED)) AS max_id FROM Notice";

        try {
            Statement statement = DBConnection.getConnection().createStatement();
            ResultSet results   = statement.executeQuery(sql);

            if (results.next()) {
                long highestNumber = results.getLong("max_id");
                return String.format("N%03d", highestNumber + 1);
            }

        } catch (SQLException e) {
            System.err.println("Error generating notice ID: " + e.getMessage());
        }

        return "N001";
    }
}