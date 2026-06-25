package com.universe.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

//announcement model
public class Announcement {
    private String announcementID;
    private String title;
    private String content;
    private LocalDateTime dateTime;
    private String societyID;

    //constructor
    public Announcement(String announcementID, String title, String content,
                        LocalDateTime dateTime, String societyID) {
        this.announcementID = announcementID;
        this.title = title;
        this.content = content;
        this.dateTime = dateTime;
        this.societyID = societyID;
    }

    //formatted date
    public String getFormattedDate() {
        if (dateTime == null) return "";
        return dateTime.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
    }

    //content preview
    public String getContentPreview() {
        if (content == null) return "";
        return content.length() > 120 ? content.substring(0, 117) + "..." : content;
    }

    //recent check
    public boolean isRecent(int withinDays) {
        if (dateTime == null) return false;
        return dateTime.isAfter(LocalDateTime.now().minusDays(withinDays));
    }

    //getters setters
    public String getAnnouncementID() { return announcementID; }
    public void setAnnouncementID(String id) { this.announcementID = id; }

    public String getTitle()          { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getContent()        { return content; }
    public void setContent(String content) { this.content = content; }

    public LocalDateTime getDateTime() { return dateTime; }
    public void setDateTime(LocalDateTime dateTime) { this.dateTime = dateTime; }

    public String getSocietyID()      { return societyID; }
    public void setSocietyID(String societyID) { this.societyID = societyID; }

    //to string
    @Override
    public String toString() {
        return "Announcement{title='" + title + "', societyID='" + societyID + "'}";
    }
}