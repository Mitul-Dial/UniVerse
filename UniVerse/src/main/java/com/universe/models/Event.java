package com.universe.models;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

//event model
public class Event {
    private String eventID;
    private String title;
    private String description;
    private LocalDate date;
    private LocalTime time;
    private String venue;
    private int maxSeats;
    private double registrationFee;
    private LocalDate regDeadline;
    private String status; //pending,approved,rejected
    private String societyID;
    private String departmentID;

    //constructor
    public Event(String eventID, String title, String description, LocalDate date,
                 LocalTime time, String venue, int maxSeats, double registrationFee,
                 LocalDate regDeadline, String status, String societyID, String departmentID) {
        this.eventID = eventID;
        this.title = title;
        this.description = description;
        this.date = date;
        this.time = time;
        this.venue = venue;
        this.maxSeats = maxSeats;
        this.registrationFee = registrationFee;
        this.regDeadline = regDeadline;
        this.status = status;
        this.societyID = societyID;
        this.departmentID = departmentID;
    }

    //status checks
    public boolean isApproved()   { return "Approved".equals(status); }
    public boolean isPending()    { return "Pending".equals(status); }
    public boolean isRejected()   { return "Rejected".equals(status); }
    public boolean isCancelled()  { return "Cancelled".equals(status); }

    //deadline check
    public boolean isRegistrationClosed() {
        if (regDeadline == null) return false;
        return LocalDate.now().isAfter(regDeadline);
    }

    //over check
    public boolean isOver() {
        if (date == null) return false;
        return LocalDate.now().isAfter(date);
    }

    //registration check
    public boolean isOpenForRegistration() {
        return isApproved() && !isRegistrationClosed() && !isOver();
    }

    //seat check
    public boolean hasSeatsAvailable(int totalConfirmed) {
        return totalConfirmed < maxSeats;
    }

    //remaining seats
    public int remainingSeats(int totalConfirmed) {
        return Math.max(0, maxSeats - totalConfirmed);
    }

    //month label
    public String getMonthLabel() {
        if (date == null) return "";
        return date.getMonth().getDisplayName(
                java.time.format.TextStyle.SHORT, java.util.Locale.ENGLISH
        ).toUpperCase();
    }

    //day number
    public int getDayNumber() {
        return date != null ? date.getDayOfMonth() : 0;
    }

    //deadline label
    public String getDeadlineLabel() {
        if (regDeadline == null) return "";
        return "Deadline " + regDeadline.format(DateTimeFormatter.ofPattern("MMM d"));
    }

    //fee label
    public String getFeeLabel() {
        if (registrationFee == 0) return "Free";
        return "PKR " + String.format("%.0f", registrationFee);
    }

    //getters setters
    public String getEventID()         { return eventID; }
    public void setEventID(String eventID) { this.eventID = eventID; }

    public String getTitle()           { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription()     { return description; }
    public void setDescription(String description) { this.description = description; }

    public LocalDate getDate()         { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public LocalTime getTime()         { return time; }
    public void setTime(LocalTime time) { this.time = time; }

    public String getVenue()           { return venue; }
    public void setVenue(String venue) { this.venue = venue; }

    public int getMaxSeats()           { return maxSeats; }
    public void setMaxSeats(int maxSeats) { this.maxSeats = maxSeats; }

    public double getRegistrationFee() { return registrationFee; }
    public void setRegistrationFee(double registrationFee) { this.registrationFee = registrationFee; }

    public LocalDate getRegDeadline()  { return regDeadline; }
    public void setRegDeadline(LocalDate regDeadline) { this.regDeadline = regDeadline; }

    public String getStatus()          { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getSocietyID()       { return societyID; }
    public void setSocietyID(String societyID) { this.societyID = societyID; }

    public String getDepartmentID()    { return departmentID; }
    public void setDepartmentID(String departmentID) { this.departmentID = departmentID; }

    //to string
    @Override
    public String toString() {
        return "Event{title='" + title + "', date=" + date + ", status='" + status + "'}";
    }
}