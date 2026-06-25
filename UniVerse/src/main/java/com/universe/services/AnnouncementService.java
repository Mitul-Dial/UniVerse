package com.universe.services;

import com.universe.db.DBConnection;
import com.universe.models.Announcement;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

//announcement service
public class AnnouncementService {

    private Connection conn;

    //constructor
    public AnnouncementService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    //post announcement
    public void postAnnouncement(Announcement a) {
        String sql = "INSERT INTO Announcement VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, a.getAnnouncementID());
            ps.setString(2, a.getTitle());
            ps.setString(3, a.getContent());
            ps.setTimestamp(4, Timestamp.valueOf(a.getDateTime()));
            ps.setString(5, a.getSocietyID());
            ps.executeUpdate();
            System.out.println("Announcement posted: " + a.getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //get all
    public List<Announcement> getAllAnnouncements() {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT * FROM Announcement ORDER BY dateTime DESC";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(new Announcement(
                        rs.getString("announcementID"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getTimestamp("dateTime").toLocalDateTime(),
                        rs.getString("societyID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //society announcements
    public List<Announcement> getAnnouncementsBySociety(String societyID) {
        List<Announcement> list = new ArrayList<>();
        String sql = "SELECT * FROM Announcement WHERE societyID = ? ORDER BY dateTime DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, societyID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new Announcement(
                        rs.getString("announcementID"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getTimestamp("dateTime").toLocalDateTime(),
                        rs.getString("societyID")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }
}