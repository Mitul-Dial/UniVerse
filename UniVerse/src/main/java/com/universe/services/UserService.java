package com.universe.services;

import com.universe.db.DBConnection;
import com.universe.models.*;

import java.sql.*;

//user service
public class UserService {

    private Connection conn;

    //constructor
    public UserService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    //login user
    public User login(String email, String password) {
        User user = checkStudent(email, password);
        if (user != null) return user;

        user = checkAdmin(email, password);
        if (user != null) return user;

        user = checkSponsor(email, password);
        if (user != null) return user;

        //check society
        return checkSociety(email, password);
    }

    //check student
    private Student checkStudent(String email, String password) {
        String sql = "SELECT * FROM Student WHERE email = ? AND password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Student(
                        rs.getString("studentID"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("universityID"),
                        rs.getString("departmentID")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //check admin
    private Admin checkAdmin(String email, String password) {
        String sql = "SELECT * FROM Admin WHERE email = ? AND password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Admin(
                        rs.getString("adminID"),
                        rs.getString("name"),
                        rs.getString("email"),
                        rs.getString("password"),
                        rs.getString("universityID")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //check sponsor
    private Sponsor checkSponsor(String email, String password) {
        String sql = "SELECT * FROM Sponsor WHERE email = ? AND password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Sponsor(
                        rs.getString("sponsorID"),
                        rs.getString("name"),
                        rs.getString("organization"),
                        rs.getString("email"),
                        rs.getString("contactInfo"),
                        rs.getString("password")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //check society
    private Society checkSociety(String email, String password) {
        String sql = "SELECT * FROM Society WHERE email = ? AND password = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, email);
            ps.setString(2, password);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
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
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return null;
    }

    //register student
    public void registerStudent(Student s) {
        String sql = "INSERT INTO Student VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, s.getUserID());
            ps.setString(2, s.getName());
            ps.setString(3, s.getEmail());
            ps.setString(4, s.getPassword());
            ps.setString(5, s.getUniversityID());
            ps.setString(6, s.getDepartmentID());
            ps.executeUpdate();
            System.out.println("Student registered: " + s.getName());
            new NotificationService().sendNotification("ALL", "ADMIN", "New student registered: " + s.getName(), "System");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //register sponsor
    public void registerSponsor(Sponsor s) {
        String sql = "INSERT INTO Sponsor VALUES (?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, s.getUserID());
            ps.setString(2, s.getName());
            ps.setString(3, s.getOrganization());
            ps.setString(4, s.getEmail());
            ps.setString(5, s.getContactInfo());
            ps.setString(6, s.getPassword());
            ps.executeUpdate();
            System.out.println("Sponsor registered: " + s.getName());
            new NotificationService().sendNotification("ALL", "ADMIN", "New sponsor registered: " + s.getName(), "System");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //get sponsors
    public java.util.List<Sponsor> getAllSponsors() {
        java.util.List<Sponsor> list = new java.util.ArrayList<>();
        String sql = "SELECT * FROM Sponsor";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Sponsor(
                        rs.getString("sponsorID"),
                        rs.getString("name"),
                        rs.getString("organization"),
                        rs.getString("email"),
                        rs.getString("contactInfo"),
                        rs.getString("password")
                ));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //check email
    public boolean emailExists(String email) {
        String[] tables = {"Student", "Admin", "Sponsor", "Society"};
        for (String table : tables) {
            String sql = "SELECT COUNT(*) FROM " + table + " WHERE email = ?";
            try {
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setString(1, email);
                ResultSet rs = ps.executeQuery();
                if (rs.next() && rs.getInt(1) > 0) return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }
    
    //landing stats
    public java.util.Map<String, Integer> getLandingStats() {
        java.util.Map<String, Integer> stats = new java.util.HashMap<>();
        stats.put("universities", getCount("SELECT COUNT(DISTINCT universityID) FROM Student"));
        stats.put("societies", getCount("SELECT COUNT(*) FROM Society"));
        stats.put("students", getCount("SELECT COUNT(*) FROM Student"));
        stats.put("events", getCount("SELECT COUNT(*) FROM Event"));
        return stats;
    }

    //get count
    private int getCount(String sql) {
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}