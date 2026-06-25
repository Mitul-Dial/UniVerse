package com.universe.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//notification model
public class Notification {
    private String notifID;
    private String message;
    private LocalDateTime date;
    private String type;
    private boolean isRead;
    private String recipientType; //student,society,sponsor,admin
    private String recipientID;

    //constructor
    public Notification(String notifID, String message, LocalDateTime date,
                        String type, boolean isRead,
                        String recipientType, String recipientID) {
        this.notifID = notifID;
        this.message = message;
        this.date = date;
        this.type = type;
        this.isRead = isRead;
        this.recipientType = recipientType;
        this.recipientID = recipientID;
    }

    //mark read
    public void markAsRead() {
        this.isRead = true;
    }

    //formatted date
    public String getFormattedDate() {
        if (date == null) return "";
        return date.format(DateTimeFormatter.ofPattern("MMM d, yyyy · hh:mm a"));
    }

    //recent check
    public boolean isRecent(int withinHours) {
        if (date == null) return false;
        return date.isAfter(LocalDateTime.now().minusHours(withinHours));
    }

    //type color
    public String getTypeColor() {
        return switch (type) {
            case "EventApproved"    -> "#34a853";
            case "EventRejected"    -> "#ea4335";
            case "SponsorAccepted"  -> "#34a853";
            case "SponsorRejected"  -> "#ea4335";
            case "NewRegistration"  -> "#1a73e8";
            case "Announcement"     -> "#fbbc04";
            default                 -> "#5f6368";
        };
    }

    //getters setters
    public String getNotifID()        { return notifID; }
    public void setNotifID(String notifID) { this.notifID = notifID; }

    public String getMessage()        { return message; }
    public void setMessage(String message) { this.message = message; }

    public LocalDateTime getDate()    { return date; }
    public void setDate(LocalDateTime date) { this.date = date; }

    public String getType()           { return type; }
    public void setType(String type)  { this.type = type; }

    public boolean isRead()           { return isRead; }
    public void setRead(boolean read) { isRead = read; }

    public String getRecipientType()  { return recipientType; }
    public void setRecipientType(String recipientType) { this.recipientType = recipientType; }

    public String getRecipientID()    { return recipientID; }
    public void setRecipientID(String recipientID) { this.recipientID = recipientID; }

    //to string
    @Override
    public String toString() {
        return "Notification{message='" + message + "', isRead=" + isRead + "}";
    }
}