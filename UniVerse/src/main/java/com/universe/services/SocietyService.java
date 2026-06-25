package com.universe.services;

import com.universe.db.DBConnection;
import com.universe.models.Society;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//society service
public class SocietyService {

    private Connection conn;

    //constructor
    public SocietyService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    //all societies
    public List<Society> getAllSocieties() {
        List<Society> list = new ArrayList<>();
        String sql = "SELECT * FROM Society";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapSociety(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //active societies
    public List<Society> getActiveSocieties() {
        List<Society> list = new ArrayList<>();
        String sql = "SELECT * FROM Society WHERE status = 'Active'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapSociety(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //pending societies
    public List<Society> getPendingSocieties() {
        List<Society> list = new ArrayList<>();
        String sql = "SELECT * FROM Society WHERE status = 'Pending'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) list.add(mapSociety(rs));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //get society
    public Society getSocietyByID(String societyID) {
        String sql = "SELECT * FROM Society WHERE societyID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, societyID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapSociety(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //approve society
    public void approveSociety(String societyID) {
        updateStatus(societyID, "Active");
    }

    //suspend society
    public void suspendSociety(String societyID) {
        updateStatus(societyID, "Suspended");
    }

    //society login
    public Society login(String email, String password) {
        String sql = "SELECT * FROM Society WHERE email = ? AND password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapSociety(rs);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //register society
    public void registerSociety(Society s) {
        String sql = "INSERT INTO Society VALUES (?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, s.getSocietyID());
            ps.setString(2, s.getName());
            ps.setString(3, s.getDescription());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getContactInfo());
            ps.setString(6, s.getPassword());
            ps.setString(7, "Active");
            ps.setString(8, s.getUniversityID());
            ps.executeUpdate();
            System.out.println("Society registered: " + s.getName());
            new NotificationService().sendNotification("ALL", "ADMIN", "New society registered: " + s.getName(), "System");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //update status
    private void updateStatus(String societyID, String status) {
        String sql = "UPDATE Society SET status = ? WHERE societyID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, societyID);
            ps.executeUpdate();
            System.out.println("Society " + societyID + " status: " + status);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //map society
    private Society mapSociety(ResultSet rs) throws SQLException {
        return new Society(
                rs.getString("societyID"),
                rs.getString("name"),
                rs.getString("description"),
                rs.getString("email"),
                rs.getString("contactInfo"),
                rs.getString("password"),
                rs.getString("status"),
                rs.getString("universityID")
        );
    }
}