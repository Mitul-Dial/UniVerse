package com.universe.models;

import java.util.List;

//student model
public class Student extends User {
    private String universityID;
    private String departmentID;

    //constructor
    public Student(String studentID, String name, String email,
                   String password, String universityID, String departmentID) {
        super(studentID, name, email, password);
        this.universityID = universityID;
        this.departmentID = departmentID;
    }

    //login logic
    @Override
    public boolean login() {
        System.out.println("Student logged in: " + email);
        return true;
    }

    //logout logic
    @Override
    public void logout() {
        System.out.println("Student logged out: " + email);
    }

    //registration validation
    public String canRegisterFor(Event event, List<EventRegistration> existingRegistrations) {
        if (event == null) return "Event does not exist.";

        if (!"Approved".equals(event.getStatus()))
            return "Event is not open for registration.";

        if (event.getRegDeadline() != null &&
                java.time.LocalDate.now().isAfter(event.getRegDeadline()))
            return "Registration deadline has passed.";

        for (EventRegistration reg : existingRegistrations) {
            if (reg.getEventID().equals(event.getEventID()) &&
                    "Confirmed".equals(reg.getStatus()))
                return "Already registered for this event.";
        }

        return null; //allowed
    }

    //build registration
    public EventRegistration buildRegistration(String registrationID, String eventID) {
        return new EventRegistration(
                registrationID,
                this.userID,
                eventID,
                java.time.LocalDate.now(),
                "Confirmed"
        );
    }

    //display label
    public String getDisplayLabel() {
        return name + " · " + departmentID;
    }

    //getters setters
    public String getUniversityID() { return universityID; }
    public void setUniversityID(String universityID) { this.universityID = universityID; }

    public String getDepartmentID() { return departmentID; }
    public void setDepartmentID(String departmentID) { this.departmentID = departmentID; }

    //to string
    @Override
    public String toString() {
        return "Student{universityID='" + universityID + "', " + super.toString() + "}";
    }
}