CREATE DATABASE universe_db;
USE universe_db;

-- 1. University
CREATE TABLE University (
    universityID VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    location VARCHAR(100)
);

-- 2. Department
CREATE TABLE Department (
    departmentID VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    universityID VARCHAR(20) NOT NULL,
    FOREIGN KEY (universityID) REFERENCES University(universityID)
);

-- 3. Admin
CREATE TABLE Admin (
    adminID VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    universityID VARCHAR(20) NOT NULL,
    FOREIGN KEY (universityID) REFERENCES University(universityID)
);

-- 4. Student
CREATE TABLE Student (
    studentID VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    universityID VARCHAR(20) NOT NULL,
    departmentID VARCHAR(20),
    FOREIGN KEY (universityID) REFERENCES University(universityID),
    FOREIGN KEY (departmentID) REFERENCES Department(departmentID)
);

-- 5. Society
CREATE TABLE Society (
    societyID VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    description VARCHAR(500),
    email VARCHAR(100) UNIQUE NOT NULL,
    contactInfo VARCHAR(200),
    password VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    universityID VARCHAR(20) NOT NULL,
    CONSTRAINT chk_soc_status CHECK (status IN ('Pending','Active','Suspended')),
    FOREIGN KEY (universityID) REFERENCES University(universityID)
);

-- 6. Sponsor
CREATE TABLE Sponsor (
    sponsorID VARCHAR(20) PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    organization VARCHAR(200),
    email VARCHAR(100) UNIQUE NOT NULL,
    contactInfo VARCHAR(200),
    password VARCHAR(255) NOT NULL
);

-- 7. Event
CREATE TABLE Event (
    eventID VARCHAR(20) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    description VARCHAR(1000),
    date DATE NOT NULL,
    time TIME NOT NULL,
    venue VARCHAR(200) NOT NULL,
    maxSeats INT NOT NULL,
    registrationFee DECIMAL(10,2) DEFAULT 0,
    regDeadline DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    societyID VARCHAR(20) NOT NULL,
    departmentID VARCHAR(20),
    CONSTRAINT chk_event_status CHECK (status IN ('Pending','Approved','Rejected','Cancelled')),
    CONSTRAINT chk_seats CHECK (maxSeats > 0),
    CONSTRAINT chk_fee CHECK (registrationFee >= 0),
    FOREIGN KEY (societyID) REFERENCES Society(societyID),
    FOREIGN KEY (departmentID) REFERENCES Department(departmentID)
);

-- 8. Event Registration
CREATE TABLE EventRegistration (
    registrationID VARCHAR(20) PRIMARY KEY,
    studentID VARCHAR(20) NOT NULL,
    eventID VARCHAR(20) NOT NULL,
    registrationDate DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'Confirmed',
    CONSTRAINT chk_reg_status CHECK (status IN ('Confirmed','Cancelled')),
    UNIQUE (studentID, eventID),
    FOREIGN KEY (studentID) REFERENCES Student(studentID),
    FOREIGN KEY (eventID) REFERENCES Event(eventID)
);

-- 9. Announcement
CREATE TABLE Announcement (
    announcementID VARCHAR(20) PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    content VARCHAR(2000) NOT NULL,
    dateTime DATETIME NOT NULL,
    societyID VARCHAR(20) NOT NULL,
    FOREIGN KEY (societyID) REFERENCES Society(societyID)
);

-- 10. Sponsorship Deal
CREATE TABLE SponsorshipDeal (
    dealID VARCHAR(20) PRIMARY KEY,
    sponsorID VARCHAR(20) NOT NULL,
    eventID VARCHAR(20) NOT NULL,
    proposalMessage VARCHAR(2000),
    dateSubmitted DATE NOT NULL,
    status VARCHAR(20) DEFAULT 'Pending',
    responseMessage VARCHAR(1000),
    CONSTRAINT chk_deal_status CHECK (status IN ('Pending','Accepted','Rejected')),
    FOREIGN KEY (sponsorID) REFERENCES Sponsor(sponsorID),
    FOREIGN KEY (eventID) REFERENCES Event(eventID)
);

-- 11. Notification
CREATE TABLE Notification (
    notifID VARCHAR(20) PRIMARY KEY,
    message VARCHAR(500) NOT NULL,
    date DATETIME NOT NULL,
    type VARCHAR(50) NOT NULL,
    isRead BIT DEFAULT 0,
    recipientType VARCHAR(20) NOT NULL,
    recipientID VARCHAR(20) NOT NULL,
    CONSTRAINT chk_recip_type CHECK (recipientType IN ('Student','Society','Sponsor','Admin'))
);

-- 12. Shared Calendar
CREATE TABLE SharedCalendar (
    calendarID VARCHAR(20) PRIMARY KEY,
    eventID VARCHAR(20) NOT NULL,
    month INT NOT NULL,
    year INT NOT NULL,
    CONSTRAINT chk_month CHECK (month BETWEEN 1 AND 12),
    CONSTRAINT chk_year CHECK (year >= 2024),
    FOREIGN KEY (eventID) REFERENCES Event(eventID)
);

-- 13. Event Summary
CREATE TABLE EventSummary (
    summaryID VARCHAR(20) PRIMARY KEY,
    eventID VARCHAR(20) NOT NULL,
    totalRegistrations INT DEFAULT 0,
    sponsorCount INT DEFAULT 0,
    generatedDate DATE NOT NULL,
    CONSTRAINT chk_total_reg CHECK (totalRegistrations >= 0),
    CONSTRAINT chk_sponsor_count CHECK (sponsorCount >= 0),
    FOREIGN KEY (eventID) REFERENCES Event(eventID)
);

