<div align="center">

# 🌌 UniVerse

### University Event & Society Management Platform

A feature-rich **JavaFX desktop application** that centralizes university event management, society coordination, sponsorship workflows, and real-time notifications — all wrapped in a polished, animated UI.

[![Java](https://img.shields.io/badge/Java-17-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://openjdk.org/)
[![JavaFX](https://img.shields.io/badge/JavaFX-17.0.2-3178C6?style=for-the-badge&logo=java&logoColor=white)](https://openjfx.io/)
[![Maven](https://img.shields.io/badge/Maven-3.x-C71A36?style=for-the-badge&logo=apachemaven&logoColor=white)](https://maven.apache.org/)
[![SQL Server](https://img.shields.io/badge/SQL_Server-2019+-CC2927?style=for-the-badge&logo=microsoftsqlserver&logoColor=white)](https://www.microsoft.com/en-us/sql-server)

---

*Streamlining campus life — from event creation to sponsorship deals, all in one place.*

</div>

---

## 📋 Table of Contents

- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Database Schema](#database-schema)
- [Getting Started](#getting-started)
- [Project Structure](#project-structure)
- [Design Patterns](#design-patterns)
- [Screenshots](#screenshots)
- [Contributors](#contributors)
- [License](#license)

---

## Overview

**UniVerse** is a comprehensive platform designed to solve the fragmented nature of university event management. It brings together four distinct user roles — **Students**, **Societies**, **Sponsors**, and **Admins** — into a single cohesive system with role-based access control (RBAC).

The platform handles the complete lifecycle of university events: from society registration and event proposal, through admin approval workflows, to student registration and sponsor deal management, with a real-time notification system tying it all together.

### The Problem

University campuses often struggle with:
- Scattered event information across multiple channels
- No centralized system for society management and approval
- Manual, error-prone event registration processes
- Lack of transparency in sponsorship dealings
- No unified notification system for stakeholders

### The Solution

UniVerse addresses these pain points with a **role-based desktop application** that provides each stakeholder with tailored dashboards, automated workflows, and real-time updates.

---

## Features

### 🎓 Student Portal
- Browse and register for approved events with seat availability tracking
- View registration history with status tracking (Confirmed / Cancelled)
- Expandable event detail cards with venue, date, deadline, and capacity info
- Real-time notifications for event updates and capacity alerts

### 🏛️ Society Management
- Create and submit events for admin approval
- Post announcements visible to all platform users
- Manage event lifecycle (Pending → Approved → Completed)
- Review and respond to incoming sponsorship proposals
- Track member engagement through registration metrics

### 💼 Sponsor Dashboard
- Browse live events and submit sponsorship proposals
- Track deal status (Pending / Accepted / Rejected)
- One-proposal-per-event enforcement to prevent duplicates

### 🔐 Admin Panel
- Approve, reject, or cancel events with a single click
- Manage society statuses (Activate / Suspend)
- Platform-wide statistics dashboard (events, registrations, societies, sponsors)
- Automated notifications dispatched on every approval action

### 🎨 UI/UX Highlights
- **Animated landing page** with a particle-based atom intro sequence, floating letters, and constellation background
- **Custom cursor system** with particle trails, click burst effects, and interactive hover states
- **Smooth transitions** between views with fade and translate animations
- **Themed modal dialogs** for login, registration, event creation, and sponsorship proposals
- **Interactive calendar** with event markers and day-detail view
- **Toast notifications** for real-time feedback
- **Animated background blobs** with ambient drift motion on every dashboard panel

---

## Architecture

The project follows a **layered MVC architecture** with clear separation of concerns:

```
┌─────────────────────────────────────────────────────┐
│                   Presentation Layer                 │
│  ┌─────────────┐  ┌──────────────┐  ┌────────────┐  │
│  │   FXML      │  │ Controllers  │  │    CSS     │  │
│  │   Views     │  │  (JavaFX)    │  │  Stylesheets│ │
│  └─────────────┘  └──────────────┘  └────────────┘  │
├─────────────────────────────────────────────────────┤
│                   Business Logic Layer               │
│  ┌──────────────────────────────────────────────┐    │
│  │              Service Classes                  │    │
│  │  UserService · EventService · SocietyService  │   │
│  │  RegistrationService · SponsorshipService     │   │
│  │  AnnouncementService · NotificationService    │   │
│  └──────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────┤
│                    Domain Model Layer                │
│  ┌──────────────────────────────────────────────┐    │
│  │  User (abstract) → Student, Society,          │   │
│  │                     Sponsor, Admin             │   │
│  │  Event · EventRegistration · Announcement     │   │
│  │  SponsorshipDeal · Notification · Session     │   │
│  └──────────────────────────────────────────────┘    │
├─────────────────────────────────────────────────────┤
│                  Data Access Layer                   │
│  ┌──────────────────────────────────────────────┐    │
│  │  DBConnection (Singleton) → MSSQL via JDBC   │    │
│  └──────────────────────────────────────────────┘    │
└─────────────────────────────────────────────────────┘
```

---

## Tech Stack

| Layer | Technology |
|-------|-----------|
| **Language** | Java 17 |
| **UI Framework** | JavaFX 17.0.2 (FXML + CSS) |
| **Build Tool** | Apache Maven |
| **Database** | Microsoft SQL Server (JDBC) |
| **Authentication** | Windows Integrated Security |
| **Module System** | Java Platform Module System (JPMS) |

---

## Database Schema

The application uses a normalized relational schema with **13 tables**:

```
University ──┬── Department
             ├── Admin
             ├── Student ──── EventRegistration ──── Event
             └── Society ──┬── Event ──── SponsorshipDeal ──── Sponsor
                           └── Announcement

Notification (polymorphic recipient: Student | Society | Sponsor | Admin)
SharedCalendar ──── Event
EventSummary ──── Event
```

Key design decisions:
- **Polymorphic notifications** via `recipientType` + `recipientID` columns
- **Constraint-enforced status enums** using `CHECK` constraints
- **Referential integrity** with foreign keys across all relationships
- **Unique constraints** preventing duplicate event registrations

> The full SQL schema is available in [`universe_db.sql`](universe_db.sql).

---

## Getting Started

### Prerequisites

| Requirement | Version |
|---|---|
| **JDK** | 17 or higher |
| **Maven** | 3.x |
| **SQL Server** | 2019+ (with Windows Authentication enabled) |

### 1. Clone the Repository

```bash
git clone https://github.com/<your-username>/UniVerse.git
cd UniVerse
```

### 2. Set Up the Database

1. Open **SQL Server Management Studio (SSMS)**
2. Execute the schema script:
   ```sql
   -- Run the contents of universe_db.sql
   ```
3. This creates the `universe_db` database with all 13 tables, constraints, and relationships.

### 3. Configure Database Connection

The app uses **Windows Integrated Authentication** by default. If needed, update the connection string in `src/main/java/com/universe/db/DBConnection.java`:

```java
private static final String URL =
    "jdbc:sqlserver://localhost;databaseName=universe_db;integratedSecurity=true;encrypt=false;";
```

> **Note:** The `mssql-jdbc_auth-12.6.1.x64.dll` and `mssql-jdbc_auth-13.4.0.x64.dll` files are included in the project root for Windows Integrated Authentication support.

### 4. Build & Run

```bash
# Install dependencies and compile
mvn clean install

# Run the application
mvn javafx:run
```

The application launches at **1280×800** resolution (minimum 1024×700).

---

## Project Structure

```
UniVerse/
├── pom.xml                          # Maven project configuration
├── universe_db.sql                  # Database schema script
├── mssql-jdbc_auth-*.dll            # JDBC auth libraries (Windows)
│
└── src/main/
    ├── java/
    │   ├── module-info.java         # JPMS module descriptor
    │   └── com/universe/
    │       ├── Main.java            # App entry point, view switching, cursor FX
    │       ├── controllers/
    │       │   ├── DashboardController.java   # Main dashboard (1929 lines)
    │       │   ├── LandingController.java     # Landing page + animations
    │       │   └── ThemedDialog.java          # Reusable modal dialog system
    │       ├── db/
    │       │   └── DBConnection.java          # Singleton DB connection
    │       ├── models/
    │       │   ├── User.java                  # Abstract base user
    │       │   ├── Student.java               # Student entity
    │       │   ├── Society.java               # Society entity
    │       │   ├── Sponsor.java               # Sponsor entity
    │       │   ├── Admin.java                 # Admin entity
    │       │   ├── Event.java                 # Event entity
    │       │   ├── EventRegistration.java     # Registration entity
    │       │   ├── Announcement.java          # Announcement entity
    │       │   ├── SponsorshipDeal.java        # Sponsorship deal entity
    │       │   ├── Notification.java          # Notification entity
    │       │   └── Session.java               # Session singleton
    │       └── services/
    │           ├── UserService.java           # Auth & user management
    │           ├── EventService.java          # Event CRUD & lifecycle
    │           ├── SocietyService.java        # Society management
    │           ├── RegistrationService.java   # Event registration logic
    │           ├── SponsorshipService.java    # Sponsorship deal handling
    │           ├── AnnouncementService.java   # Announcement operations
    │           └── NotificationService.java   # Notification dispatch
    │
    └── resources/com/universe/
        ├── landing.fxml             # Landing page layout
        ├── dashboard.fxml           # Dashboard layout
        └── styles/
            ├── landing.css          # Landing page styles
            └── dashboard.css        # Dashboard styles
```

---

## Design Patterns

| Pattern | Usage |
|---------|-------|
| **Singleton** | `DBConnection` ensures a single database connection instance; `Session` manages the logged-in user globally |
| **MVC** | FXML views + Controllers + Service/Model separation |
| **Template Method** | Abstract `User` class defines `login()` / `logout()` contracts; subclasses provide role-specific implementations |
| **Observer-like** | Notification system dispatches alerts to relevant recipients on state changes |
| **Factory Method** | `Society.createEvent()` and `Sponsor.submitProposal()` encapsulate entity creation with validation |
| **RBAC** | Tab visibility and action controls dynamically adapt based on the logged-in user's role |

---

## Screenshots

> *Screenshots can be added here showing the landing page, login dialog, student dashboard, admin panel, and event creation flow.*

<!-- 
![Landing Page](screenshots/landing.png)
![Dashboard](screenshots/dashboard.png)
![Event Creation](screenshots/create-event.png)
-->

---

## Contributors

| Name | Role |
|------|------|
| **Contributor 1** | Development & Design |
| **Contributor 2** | Development & Design |
| **Contributor 3** | Development & Design |

---

## License

This project is licensed under the [MIT License](LICENSE).

---

<div align="center">

**Built with ☕ Java & 💙 JavaFX**

</div>
