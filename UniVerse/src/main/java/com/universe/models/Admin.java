package com.universe.models;

//admin model
public class Admin extends User {
    private String universityID;

    //constructor
    public Admin(String adminID, String name, String email,
                 String password, String universityID) {
        super(adminID, name, email, password);
        this.universityID = universityID;
    }

    //login logic
    @Override
    public boolean login() {
        System.out.println("Admin logged in: " + email);
        return true;
    }

    //logout logic
    @Override
    public void logout() {
        System.out.println("Admin logged out: " + email);
    }

    //approve event
    public void approveEvent(Event event) {
        if (event == null) throw new IllegalArgumentException("Event cannot be null.");
        if (!"Pending".equals(event.getStatus()))
            throw new IllegalStateException("Only Pending events can be approved.");
        event.setStatus("Approved");
    }

    //reject event
    public void rejectEvent(Event event) {
        if (event == null) throw new IllegalArgumentException("Event cannot be null.");
        if (!"Pending".equals(event.getStatus()))
            throw new IllegalStateException("Only Pending events can be rejected.");
        event.setStatus("Rejected");
    }

    //cancel event
    public void cancelEvent(Event event) {
        if (event == null) throw new IllegalArgumentException("Event cannot be null.");
        event.setStatus("Cancelled");
    }

    //approve society
    public void approveSociety(Society society) {
        if (society == null) throw new IllegalArgumentException("Society cannot be null.");
        if (!"Pending".equals(society.getStatus()))
            throw new IllegalStateException("Only Pending societies can be approved.");
        society.setStatus("Active");
    }

    //suspend society
    public void suspendSociety(Society society) {
        if (society == null) throw new IllegalArgumentException("Society cannot be null.");
        society.setStatus("Suspended");
    }

    //reject society
    public void rejectSociety(Society society) {
        if (society == null) throw new IllegalArgumentException("Society cannot be null.");
        society.setStatus("Suspended");
    }

    //build notification
    public Notification buildNotification(String notifID, String message,
                                          String type, String recipientType,
                                          String recipientID) {
        return new Notification(
                notifID,
                message,
                java.time.LocalDateTime.now(),
                type,
                false,
                recipientType,
                recipientID
        );
    }

    //getters setters
    public String getUniversityID() { return universityID; }
    public void setUniversityID(String universityID) { this.universityID = universityID; }

    //to string
    @Override
    public String toString() {
        return "Admin{universityID='" + universityID + "', " + super.toString() + "}";
    }
}