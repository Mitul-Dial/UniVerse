package com.universe.services;

import com.universe.db.DBConnection;
import com.universe.models.Event;

import java.sql.*;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

//event service
public class EventService {

    private Connection conn;

    //constructor
    public EventService() {
        this.conn = DBConnection.getInstance().getConnection();
    }

    //approved events
    public List<Event> getApprovedEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM Event WHERE status = 'Approved'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //all events
    public List<Event> getAllEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM Event";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //pending events
    public List<Event> getPendingEvents() {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM Event WHERE status = 'Pending'";
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery(sql);
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //society events
    public List<Event> getEventsBySociety(String societyID) {
        List<Event> list = new ArrayList<>();
        String sql = "SELECT * FROM Event WHERE societyID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, societyID);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapEvent(rs));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    //venue conflict
    public boolean checkVenueConflict(String venue, LocalDate date, LocalTime time) {
        String sql = "SELECT COUNT(*) FROM Event WHERE venue = ? AND date = ? AND time = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, venue);
            ps.setDate(2, Date.valueOf(date));
            ps.setTime(3, Time.valueOf(time));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1) > 0;
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    //remaining seats
    public int getRemainingSeats(String eventID) {
        String sql = "SELECT maxSeats - " +
                "(SELECT COUNT(*) FROM EventRegistration WHERE eventID = ?) " +
                "FROM Event WHERE eventID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, eventID);
            ps.setString(2, eventID);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return rs.getInt(1);
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }

    //create event
    public void createEvent(Event event) {
        String sql = "INSERT INTO Event VALUES (?,?,?,?,?,?,?,?,?,?,?,?)";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, event.getEventID());
            ps.setString(2, event.getTitle());
            ps.setString(3, event.getDescription());
            ps.setDate(4, Date.valueOf(event.getDate()));
            ps.setTime(5, Time.valueOf(event.getTime()));
            ps.setString(6, event.getVenue());
            ps.setInt(7, event.getMaxSeats());
            ps.setDouble(8, event.getRegistrationFee());
            ps.setDate(9, Date.valueOf(event.getRegDeadline()));
            ps.setString(10, "Pending");
            ps.setString(11, event.getSocietyID());
            ps.setString(12, event.getDepartmentID());
            ps.executeUpdate();
            System.out.println("Event created: " + event.getTitle());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //update event
    public void updateEventStatus(String eventID, String status) {
        String sql = "UPDATE Event SET status = ? WHERE eventID = ?";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, status);
            ps.setString(2, eventID);
            ps.executeUpdate();
            System.out.println("Event " + eventID + " updated to: " + status);

            if ("Approved".equals(status)) {
                String eTitle = eventID;
                String socName = "Unknown Society";
                try (Statement st = conn.createStatement()) {
                    ResultSet rs = st.executeQuery("SELECT e.title, s.name FROM Event e JOIN Society s ON e.societyID = s.societyID WHERE e.eventID='" + eventID + "'");
                    if (rs.next()) {
                        eTitle = rs.getString(1);
                        socName = rs.getString(2);
                    }
                } catch (SQLException ignored) {}
                
                //notify admin global
                NotificationService ns = new NotificationService();
                ns.sendNotification("ALL", "ADMIN", "Event '" + eTitle + "' was approved for registration.", "System");
                ns.sendNotification("ALL", "GLOBAL", "New event '" + eTitle + "' by " + socName + " is now live and open for registration!", "Launch");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //check completed
    public void checkAndNotifyCompletedEvents() {
        String sql = "SELECT e.eventID, e.title, s.name FROM Event e JOIN Society s ON e.societyID = s.societyID WHERE e.date < CAST(GETDATE() AS DATE) AND e.status = 'Approved'";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            NotificationService ns = new NotificationService();
            while (rs.next()) {
                String eID = rs.getString(1);
                String title = rs.getString(2);
                String socName = rs.getString(3);
                String msg = "Event '" + title + "' by " + socName + " has been successfully completed!";
                
                //check existing notification
                String checkSql = "SELECT COUNT(*) FROM Notification WHERE message = ? AND type = 'Completion'";
                try (PreparedStatement psCheck = conn.prepareStatement(checkSql)) {
                    psCheck.setString(1, msg);
                    ResultSet rsCheck = psCheck.executeQuery();
                    if (rsCheck.next() && rsCheck.getInt(1) == 0) {
                        ns.sendNotification("ALL", "GLOBAL", msg, "Completion");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    //map event
    private Event mapEvent(ResultSet rs) throws SQLException {
        return new Event(
                rs.getString("eventID"),
                rs.getString("title"),
                rs.getString("description"),
                rs.getDate("date").toLocalDate(),
                rs.getTime("time").toLocalTime(),
                rs.getString("venue"),
                rs.getInt("maxSeats"),
                rs.getDouble("registrationFee"),
                rs.getDate("regDeadline").toLocalDate(),
                rs.getString("status"),
                rs.getString("societyID"),
                rs.getString("departmentID")
        );
    }
}