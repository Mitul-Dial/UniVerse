package com.universe.services;

import com.universe.db.DBConnection;
import com.universe.models.EventRegistration;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

//registration service
public class RegistrationService {

    private Connection conn;

    //constructor
    public RegistrationService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    //register student
    public void registerForEvent(EventRegistration reg) {
        if (isAlreadyRegistered(reg.getStudentID(), reg.getEventID())) {
            System.out.println("Already registered!");
            return;
        }
        //insert registration
        String sql = "INSERT INTO EventRegistration VALUES (?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, reg.getRegistrationID());
            ps.setString(2, reg.getStudentID());
            ps.setString(3, reg.getEventID());
            ps.setDate(4, Date.valueOf(reg.getRegistrationDate()));
            ps.setString(5, reg.getStatus());
            ps.executeUpdate();
            System.out.println("Registered successfully!");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //already registered
    public boolean isAlreadyRegistered(String studentID, String eventID) {
        //check registration
        String sql = "SELECT COUNT(*) FROM EventRegistration WHERE studentID = ? AND eventID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, studentID);
            ps.setString(2, eventID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //event registrations
    public List<EventRegistration> getRegistrationsByEvent(String eventID) {
        List<EventRegistration> list = new ArrayList<>();
        //get event registrations
        String sql = "SELECT * FROM EventRegistration WHERE eventID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, eventID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new EventRegistration(
                        rs.getString("registrationID"),
                        rs.getString("studentID"),
                        rs.getString("eventID"),
                        rs.getDate("registrationDate").toLocalDate(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //student registrations
    public List<EventRegistration> getRegistrationsByStudent(String studentID) {
        List<EventRegistration> list = new ArrayList<>();
        String sql = "SELECT * FROM EventRegistration WHERE studentID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, studentID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new EventRegistration(
                        rs.getString("registrationID"),
                        rs.getString("studentID"),
                        rs.getString("eventID"),
                        rs.getDate("registrationDate").toLocalDate(),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //cancel registration
    public void cancelRegistration(String registrationID) {
        String sql = "UPDATE EventRegistration SET status = 'Cancelled' WHERE registrationID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, registrationID);
            ps.executeUpdate();
            System.out.println("Registration cancelled.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //confirmed registrations
    public int countConfirmedRegistrations(String eventID) {
        String sql = "SELECT COUNT(*) FROM EventRegistration WHERE eventID = ? AND status = 'Confirmed'";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, eventID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //update registration
    public void updateRegistrationStatus(String registrationID, String status) {
        String sql = "UPDATE EventRegistration SET status = ? WHERE registrationID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, registrationID);
            ps.executeUpdate();
            System.out.println("Registration " + registrationID + " updated to: " + status);

            if ("Confirmed".equals(status)) {
                //check capacity
                String checkSql = "SELECT e.eventID, e.title, e.maxSeats FROM EventRegistration er JOIN Event e ON er.eventID = e.eventID WHERE er.registrationID = ?";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, registrationID);
                    ResultSet rs = psCheck.executeQuery();
                    if (rs.next()) {
                        String eID = rs.getString("eventID");
                        String title = rs.getString("title");
                        int maxSeats = rs.getInt("maxSeats");
                        int currentCount = countConfirmedRegistrations(eID);
                        if (currentCount >= maxSeats) {
                            new NotificationService().sendNotification("ALL", "GLOBAL", "Seats for event '" + title + "' are now full!", "Capacity");
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //student names
    public List<String[]> getRegistrationsWithStudentNames(String eventID) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT er.registrationID, er.studentID, s.name, er.registrationDate, er.status " +
                     "FROM EventRegistration er JOIN Student s ON er.studentID = s.studentID " +
                     "WHERE er.eventID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, eventID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("registrationID"),
                    rs.getString("studentID"),
                    rs.getString("name"),
                    rs.getDate("registrationDate").toString(),
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //event titles
    public List<String[]> getRegistrationsWithEventTitles(String studentID) {
        List<String[]> list = new ArrayList<>();
        String sql = "SELECT er.registrationID, e.title, e.date, er.status " +
                     "FROM EventRegistration er JOIN Event e ON er.eventID = e.eventID " +
                     "WHERE er.studentID = ? ORDER BY e.date DESC";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, studentID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(new String[]{
                    rs.getString("registrationID"),
                    rs.getString("title"),
                    rs.getDate("date") != null ? rs.getDate("date").toString() : "",
                    rs.getString("status")
                });
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //count members
    public int countMembersBySociety(String societyID) {
        String sql = "SELECT COUNT(DISTINCT er.studentID) " +
                "FROM EventRegistration er JOIN Event e ON er.eventID = e.eventID " +
                "WHERE e.societyID = ? AND er.status = 'Confirmed'";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, societyID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) { e.printStackTrace(); }
        return 0;
    }
}