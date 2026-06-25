package com.universe.services;

import com.universe.db.DBConnection;
import com.universe.models.Notification;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//notification service
public class NotificationService {

    private Connection conn;

    //constructor
    public NotificationService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    //send notification
    public void sendNotification(String recipientID, String recipientType,
                                 String message, String type) {
        String notifID = "N" + System.currentTimeMillis();
        String sql = "INSERT INTO Notification VALUES (?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, notifID);
            ps.setString(2, message);
            ps.setTimestamp(3, Timestamp.valueOf(LocalDateTime.now()));
            ps.setString(4, type);
            ps.setBoolean(5, false);
            ps.setString(6, recipientType);
            ps.setString(7, recipientID);
            ps.executeUpdate();
            System.out.println("Notification sent to: " + recipientID);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //get notifications
    public List<Notification> getNotifications(String recipientID, String recipientType) {
        List<Notification> list = new ArrayList<>();
        String sql = "SELECT * FROM Notification WHERE recipientID = ? " +
                "AND recipientType = ? ORDER BY date DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, recipientID);
            ps.setString(2, recipientType);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Notification(
                        rs.getString("notifID"),
                        rs.getString("message"),
                        rs.getTimestamp("date").toLocalDateTime(),
                        rs.getString("type"),
                        rs.getBoolean("isRead"),
                        rs.getString("recipientType"),
                        rs.getString("recipientID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //mark read
    public void markAsRead(String notifID) {
        String sql = "UPDATE Notification SET isRead = 1 WHERE notifID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, notifID);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}