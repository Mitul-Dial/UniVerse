package com.universe.models;

import java.time.LocalDate;

//event registration
public class EventRegistration {
    private String registrationID;
    private String studentID;
    private String eventID;
    private LocalDate registrationDate;
    private String status; //confirmed,cancelled

    //constructor
    public EventRegistration(String registrationID, String studentID,
                             String eventID, LocalDate registrationDate, String status) {
        this.registrationID = registrationID;
        this.studentID = studentID;
        this.eventID = eventID;
        this.registrationDate = registrationDate;
        this.status = status;
    }

    //status checks
    public boolean isConfirmed() { return "Confirmed".equals(status); }
    public boolean isCancelled() { return "Cancelled".equals(status); }

    //cancel registration
    public void cancel() {
        if (isCancelled())
            throw new IllegalStateException("Registration is already cancelled.");
        this.status = "Cancelled";
    }

    //reinstate registration
    public void reinstate() {
        if (isConfirmed())
            throw new IllegalStateException("Registration is already confirmed.");
        this.status = "Confirmed";
    }

    //getters setters
    public String getRegistrationID() { return registrationID; }
    public void setRegistrationID(String id) { this.registrationID = id; }

    public String getStudentID()      { return studentID; }
    public void setStudentID(String studentID) { this.studentID = studentID; }

    public String getEventID()        { return eventID; }
    public void setEventID(String eventID) { this.eventID = eventID; }

    public LocalDate getRegistrationDate() { return registrationDate; }
    public void setRegistrationDate(LocalDate registrationDate) { this.registrationDate = registrationDate; }

    public String getStatus()         { return status; }
    public void setStatus(String status) { this.status = status; }

    //to string
    @Override
    public String toString() {
        return "EventRegistration{regID='" + registrationID + "', studentID='" + studentID + "'}";
    }
}