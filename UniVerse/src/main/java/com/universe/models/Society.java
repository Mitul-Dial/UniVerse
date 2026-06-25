package com.universe.models;

import java.time.LocalDate;
import java.time.LocalTime;

//society model
public class Society extends User {
    private String description;
    private String contactInfo;
    private String status; //pending,active,suspended
    private String universityID;

    //constructor
    public Society(String societyID, String name, String description, String email,
                   String contactInfo, String password, String status, String universityID) {
        super(societyID, name, email, password);
        this.description = description;
        this.contactInfo = contactInfo;
        this.status = status;
        this.universityID = universityID;
    }

    //login logic
    @Override
    public boolean login() {
        System.out.println("Society logged in: " + email);
        return true;
    }

    //logout logic
    @Override
    public void logout() {
        System.out.println("Society logged out: " + email);
    }

    //status checks
    public boolean isActive()    { return "Active".equals(status); }
    public boolean isPending()   { return "Pending".equals(status); }
    public boolean isSuspended() { return "Suspended".equals(status); }

    //create event
    public Event createEvent(String eventID, String title, String description,
                             LocalDate date, LocalTime time, String venue,
                             int maxSeats, double registrationFee,
                             LocalDate regDeadline, String departmentID) {
        if (!isActive())
            throw new IllegalStateException("Only active societies can create events.");

        return new Event(
                eventID, title, description,
                date, time, venue,
                maxSeats, registrationFee,
                regDeadline,
                "Pending",
                this.userID,
                departmentID
        );
    }

    //create announcement
    public Announcement createAnnouncement(String announcementID,
                                           String title, String content) {
        if (!isActive())
            throw new IllegalStateException("Only active societies can post announcements.");

        return new Announcement(
                announcementID,
                title,
                content,
                java.time.LocalDateTime.now(),
                this.userID
        );
    }

    //society id
    public String getSocietyID() { return userID; }
    public void setSocietyID(String id) { this.userID = id; }

    //getters setters
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public String getContactInfo() { return contactInfo; }
    public void setContactInfo(String contactInfo) { this.contactInfo = contactInfo; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getUniversityID() { return universityID; }
    public void setUniversityID(String universityID) { this.universityID = universityID; }

    //to string
    @Override
    public String toString() {
        return "Society{name='" + name + "', status='" + status + "'}";
    }
}