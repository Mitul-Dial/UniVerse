package com.universe.controllers;

import com.universe.Main;
import com.universe.models.*;
import com.universe.services.*;
import com.universe.db.DBConnection;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.util.Duration;

import java.sql.*;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.TextStyle;
import java.util.*;

public class DashboardController {

    @FXML
    private StackPane dashboardStackRoot;
    @FXML
    private BorderPane dashboardRoot;
    @FXML
    private HBox tabBar;
    @FXML
    private StackPane contentStack;

    @FXML
    private Button tabEvents;
    @FXML
    private Button tabSocieties;
    @FXML
    private Button tabAnnouncements;
    @FXML
    private Button tabSponsors;
    @FXML
    private Button tabAdmin;

    @FXML
    private StackPane panelEventsWrapper;
    @FXML
    private StackPane panelSocietiesWrapper;
    @FXML
    private StackPane panelAnnouncementsWrapper;
    @FXML
    private StackPane panelSponsorsWrapper;
    @FXML
    private StackPane panelAdminWrapper;

    @FXML
    private HBox panelEvents;
    @FXML
    private ScrollPane panelSocieties;
    @FXML
    private ScrollPane panelAnnouncements;
    @FXML
    private ScrollPane panelSponsors;
    @FXML
    private ScrollPane panelAdmin;

    @FXML
    private Pane blobPaneEvents;
    @FXML
    private Pane blobPaneSocieties;
    @FXML
    private Pane blobPaneAnnouncements;
    @FXML
    private Pane blobPaneSponsors;
    @FXML
    private Pane blobPaneAdmin;

    @FXML
    private VBox eventFeedList;
    @FXML
    private GridPane societiesGrid;
    @FXML
    private VBox announcementsList;
    @FXML
    private GridPane sponsorsGrid;
    @FXML
    private GridPane adminGrid;

    @FXML
    private Label calMonthLabel;
    @FXML
    private GridPane calGrid;
    @FXML
    private VBox dayEventInfo;
    @FXML
    private VBox eventsFeed;

    @FXML
    private HBox welcomeBanner;
    @FXML
    private HBox welcomeActionsBox;
    @FXML
    private Label welcomeTitleLabel;
    @FXML
    private GridPane dashStatsRow;
    @FXML
    private VBox dashStat0, dashStat1, dashStat2, dashStat3;
    @FXML
    private Label statVal0, statVal1, statVal2, statVal3;
    @FXML
    private Label statDesc0, statDesc1, statDesc2, statDesc3;
    @FXML
    private Label welcomeSubtitleLabel;
    @FXML
    private Label societiesPanelHeading, societiesPanelSubheading;
    @FXML
    private VBox studentRegistrationsBox;
    @FXML
    private StackPane userAvatarBtn;
    @FXML
    private Label userAvatarText;

    private YearMonth currentMonth;
    private List<Event> allEvents;
    private Node activePanel;

    @FXML
    public void initialize() {
        initializePanelStack();
        activePanel = panelEventsWrapper;
        updateActiveTabStyles(tabEvents);
        currentMonth = YearMonth.of(2026, 4);

        initEvents();
        populateEventFeed();
        buildCalendar();
        showDayEvent(LocalDate.of(2026, 4, 21));

        populateSocieties();
        populateAnnouncements();
        populateSponsors();
        populateAdmin();

        //initialize panel background
        initPanelBlobs();
        //make blob panes
        blobPaneEvents.setMouseTransparent(true);
        blobPaneSocieties.setMouseTransparent(true);
        blobPaneAnnouncements.setMouseTransparent(true);
        blobPaneSponsors.setMouseTransparent(true);
        blobPaneAdmin.setMouseTransparent(true);
        //entrance animations
        animateWelcomeBanner();
        animateDashStats();

        wireTabClicks();
        //keep full tab
        tabBar.setPickOnBounds(true);
        tabBar.getChildren().forEach(node -> {
            node.setPickOnBounds(true);
            if (node instanceof Button) {
                ((Button) node).setMaxWidth(Double.MAX_VALUE);
                ((Button) node).setMaxHeight(Double.MAX_VALUE);
            }
        });
        registerCursorInteractions();
        contentStack.setPickOnBounds(false);
    }

    public void loadUserData() {
        User user = Session.getInstance().getLoggedInUser();
        if (user != null) {
            welcomeTitleLabel.setText("Good afternoon, " + user.getName() + " 👋");

            //divider
            int totalEvents = 0, totalRegistrations = 0, activeSocieties = 0, totalSponsors = 0;
            try (Statement stmt = DBConnection.getInstance().getConnection().createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Event");
                if (rs.next()) totalEvents = rs.getInt(1);
            } catch (SQLException ignored) {}
            try (Statement stmt = DBConnection.getInstance().getConnection().createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM EventRegistration WHERE status='Confirmed'");
                if (rs.next()) totalRegistrations = rs.getInt(1);
            } catch (SQLException ignored) {}
            try (Statement stmt = DBConnection.getInstance().getConnection().createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Society WHERE status='Active'");
                if (rs.next()) activeSocieties = rs.getInt(1);
            } catch (SQLException ignored) {}
            try (Statement stmt = DBConnection.getInstance().getConnection().createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Sponsor");
                if (rs.next()) totalSponsors = rs.getInt(1);
            } catch (SQLException ignored) {}

            //count approved events
            int approvedEvents = 0;
            try (Statement stmt = DBConnection.getInstance().getConnection().createStatement()) {
                ResultSet rs = stmt.executeQuery("SELECT COUNT(*) FROM Event WHERE status='Approved'");
                if (rs.next()) approvedEvents = rs.getInt(1);
            } catch (SQLException ignored) {}

            statVal0.setText(String.valueOf(totalEvents));
            statDesc0.setText(approvedEvents + " approved");
            statVal1.setText(String.valueOf(totalRegistrations));
            statDesc1.setText("Confirmed registrations");
            statVal2.setText(String.valueOf(activeSocieties));
            statDesc2.setText("Active this semester");
            statVal3.setText(String.valueOf(totalSponsors));
            statDesc3.setText("On platform");

            //divider
            welcomeSubtitleLabel.setText(approvedEvents + " live events · " + activeSocieties + " active societies · " + totalRegistrations + " registrations");

            //rbac tab visibility
            tabEvents.setVisible(true); tabEvents.setManaged(true);
            tabSocieties.setVisible(true); tabSocieties.setManaged(true);
            tabAnnouncements.setVisible(true); tabAnnouncements.setManaged(true);
            tabSponsors.setVisible(false); tabSponsors.setManaged(false);
            tabAdmin.setVisible(false); tabAdmin.setManaged(false);

            if (user instanceof com.universe.models.Admin) {
                tabSponsors.setVisible(true); tabSponsors.setManaged(true);
                tabAdmin.setVisible(true); tabAdmin.setManaged(true);
            } else if (user instanceof com.universe.models.Sponsor) {
                tabSocieties.setVisible(true); tabSocieties.setManaged(true);
                tabSponsors.setVisible(true); tabSponsors.setManaged(true);
            }

            //rbac action controls
            welcomeActionsBox.getChildren().clear();
            if (studentRegistrationsBox != null) {
                studentRegistrationsBox.setVisible(false);
                studentRegistrationsBox.setManaged(false);
            }

            if (user instanceof com.universe.models.Society) {
                Button addEventBtn = new Button("+ Add Event");
                addEventBtn.getStyleClass().add("btn-white");
                addEventBtn.setOnAction(e -> showCreateEventDialog(user));
                welcomeActionsBox.getChildren().add(addEventBtn);
            } else if (user instanceof com.universe.models.Student) {
                //students have no
                populateStudentRegistrations(user.getUserID());
            }

            //profile avatar initials
            if (user.getName() != null && !user.getName().trim().isEmpty()) {
                String[] parts = user.getName().trim().split("\\s+");
                String initials;
                if (parts.length > 1) {
                    initials = parts[0].substring(0, 1).toUpperCase() + parts[1].substring(0, 1).toUpperCase();
                } else {
                    initials = parts[0].substring(0, Math.min(2, parts[0].length())).toUpperCase();
                }
                if (userAvatarText != null) userAvatarText.setText(initials);
            }

            if (userAvatarBtn != null) {
                //create a sleek
                javafx.stage.Popup profilePopup = new javafx.stage.Popup();
                profilePopup.setAutoHide(true);
                
                VBox card = new VBox(8);
                card.setStyle("-fx-background-color: white; -fx-padding: 20; -fx-background-radius: 12; " +
                              "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 15, 0, 0, 4); -fx-min-width: 220;");
                card.setAlignment(Pos.CENTER);
                
                StackPane bigAvatar = new StackPane();
                bigAvatar.setStyle("-fx-background-color: #1a73e8; -fx-background-radius: 50%; -fx-min-width: 60; -fx-min-height: 60; -fx-max-width: 60; -fx-max-height: 60;");
                Label bigInitial = new Label(userAvatarText != null ? userAvatarText.getText() : "UA");
                bigInitial.setStyle("-fx-text-fill: white; -fx-font-size: 24; -fx-font-weight: bold;");
                bigAvatar.getChildren().add(bigInitial);
                VBox.setMargin(bigAvatar, new Insets(0, 0, 10, 0));
                
                Label nameLabel = new Label(user.getName());
                nameLabel.setStyle("-fx-font-size: 16; -fx-font-weight: 700; -fx-text-fill: #202124;");
                
                Label emailLabel = new Label(user.getEmail());
                emailLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #5f6368;");
                
                Label roleLabel = new Label(user.getClass().getSimpleName());
                roleLabel.setStyle("-fx-background-color: #e8f0fe; -fx-text-fill: #1a73e8; -fx-padding: 4 12; -fx-background-radius: 12; -fx-font-size: 11; -fx-font-weight: 600;");
                VBox.setMargin(roleLabel, new Insets(8, 0, 0, 0));
                
                card.getChildren().addAll(bigAvatar, nameLabel, emailLabel, roleLabel);
                profilePopup.getContent().add(card);

                userAvatarBtn.setOnMouseClicked(e -> {
                    if (profilePopup.isShowing()) {
                        profilePopup.hide();
                    } else {
                        javafx.geometry.Bounds bounds = userAvatarBtn.localToScreen(userAvatarBtn.getBoundsInLocal());
                        //position the popup
                        profilePopup.show(userAvatarBtn, bounds.getMinX() - 170, bounds.getMaxY() + 10);
                    }
                });
            }

            //check for completed
            new com.universe.services.EventService().checkAndNotifyCompletedEvents();

            //always start fresh
            switchToTab(tabEvents, panelEventsWrapper);
        }
    }

    private void showCreateEventDialog(User user) {
        TextField title = ThemedDialog.styledTextField("Title");
        TextArea desc = new TextArea();
        desc.setPromptText("Description");
        desc.setPrefRowCount(3);
        desc.setStyle("-fx-background-color: #f8f9fa; -fx-border-color: #dadce0; -fx-border-radius: 10; -fx-background-radius: 10; -fx-font-size: 14; -fx-font-family: 'Segoe UI';");
        DatePicker date = new DatePicker();
        TextField time = ThemedDialog.styledTextField("HH:mm (e.g. 14:30)");
        TextField venue = ThemedDialog.styledTextField("Venue");
        TextField seats = ThemedDialog.styledTextField("Max Seats");
        TextField fee = ThemedDialog.styledTextField("Registration Fee");
        DatePicker deadline = new DatePicker();

        VBox form = new VBox(8);
        form.getChildren().addAll(
            formLabel("Title"), title,
            formLabel("Description"), desc,
            formLabel("Date"), date,
            formLabel("Time"), time,
            formLabel("Venue"), venue,
            formLabel("Max Seats"), seats,
            formLabel("Fee ($)"), fee,
            formLabel("Registration Deadline"), deadline
        );

        javafx.scene.control.ScrollPane formScroll = new javafx.scene.control.ScrollPane(form);
        formScroll.setFitToWidth(true);
        formScroll.setMaxHeight(400);
        formScroll.setStyle("-fx-background-color: transparent; -fx-background: transparent;");

        ThemedDialog.showFormDialog(dashboardStackRoot, "Create New Event",
            "Enter event details below", formScroll, "Create", "#34a853",
            () -> {
                try {
                    String eventID = "E" + System.currentTimeMillis();
                    Event event = ((Society) user).createEvent(
                        eventID, title.getText(), desc.getText(),
                        date.getValue(), java.time.LocalTime.parse(time.getText()),
                        venue.getText(), Integer.parseInt(seats.getText()),
                        Double.parseDouble(fee.getText()), deadline.getValue(), "D001"
                    );
                    ThemedDialog.dismiss(title);
                    new EventService().createEvent(event);
                    ThemedDialog.showToast(dashboardStackRoot, "Event Created",
                        "Event submitted for Admin approval!", ThemedDialog.ToastType.SUCCESS);
                    populateEventFeed();
                } catch (Exception ex) {
                    ThemedDialog.showToast(dashboardStackRoot, "Error",
                        "Invalid input. Please check the formats.", ThemedDialog.ToastType.ERROR);
                }
            }, null);
    }

    private Label formLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #5f6368; -fx-font-family: 'Segoe UI';");
        return lbl;
    }

    @FXML
    public void handleBackToSite() {
        Main.showLanding();
    }

    private void switchToTab(Button source, Node target) {
        contentStack.getChildren().setAll(target);
        
        //reload data on
        if (target == panelEventsWrapper) {
            populateEventFeed();
            buildCalendar();
        }
        else if (target == panelSocietiesWrapper) populateSocieties();
        else if (target == panelAnnouncementsWrapper) populateAnnouncements();
        else if (target == panelSponsorsWrapper) populateSponsors();
        else if (target == panelAdminWrapper) populateAdmin();

        if (target == activePanel) {
            updateActiveTabStyles(source);
            return;
        }
        target.setOpacity(0);
        target.setTranslateY(10);

        FadeTransition ft = new FadeTransition(Duration.millis(250), target);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition tt = new TranslateTransition(Duration.millis(250), target);
        tt.setFromY(10);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
        activePanel = target;
        updateActiveTabStyles(source);
    }

    private void initializePanelStack() {
        blobPaneEvents.setMouseTransparent(true);
        blobPaneSocieties.setMouseTransparent(true);
        blobPaneAnnouncements.setMouseTransparent(true);
        blobPaneSponsors.setMouseTransparent(true);
        blobPaneAdmin.setMouseTransparent(true);

        panelEventsWrapper.setManaged(true);
        panelSocietiesWrapper.setManaged(true);
        panelAnnouncementsWrapper.setManaged(true);
        panelSponsorsWrapper.setManaged(true);
        panelAdminWrapper.setManaged(true);

        panelEventsWrapper.setPickOnBounds(false);
        panelSocietiesWrapper.setPickOnBounds(false);
        panelAnnouncementsWrapper.setPickOnBounds(false);
        panelSponsorsWrapper.setPickOnBounds(false);
        panelAdminWrapper.setPickOnBounds(false);

        panelEventsWrapper.setVisible(true);
        panelSocietiesWrapper.setVisible(true);
        panelAnnouncementsWrapper.setVisible(true);
        panelSponsorsWrapper.setVisible(true);
        panelAdminWrapper.setVisible(true);

        contentStack.getChildren().setAll(panelEventsWrapper);
        panelEventsWrapper.setOpacity(1);
        panelEventsWrapper.setTranslateY(0);
    }

    private void updateActiveTabStyles(Button selected) {
        Button[] tabs = { tabEvents, tabSocieties, tabAnnouncements, tabSponsors, tabAdmin };
        for (Button tab : tabs) {
            tab.getStyleClass().remove("dash-tab-active");
        }
        selected.getStyleClass().add("dash-tab-active");
    }

    private void wireTabClicks() {
        Button[] tabs = { tabEvents, tabSocieties, tabAnnouncements, tabSponsors, tabAdmin };
        Node[] panels = { panelEventsWrapper, panelSocietiesWrapper, panelAnnouncementsWrapper, panelSponsorsWrapper, panelAdminWrapper };

        for (int i = 0; i < tabs.length; i++) {
            Button tab = tabs[i];
            Node panel = panels[i];

            tab.setMaxWidth(Double.MAX_VALUE);
            tab.setMaxHeight(Double.MAX_VALUE);
            tab.setPickOnBounds(true);
            tab.setMnemonicParsing(false);
            tab.setCursor(Cursor.HAND);

            tab.setOnAction(e -> switchToTab(tab, panel));
        }
    }

    private void registerCursorInteractions() {
        Button[] tabs = { tabEvents, tabSocieties, tabAnnouncements, tabSponsors, tabAdmin };
        for (Button tab : tabs) {
            Main.registerInteractiveCursorNode(tab);
        }
    }

    @FXML
    public void handleCalPrev() {
        currentMonth = currentMonth.minusMonths(1);
        buildCalendar();
    }

    @FXML
    public void handleCalNext() {
        currentMonth = currentMonth.plusMonths(1);
        buildCalendar();
    }

    //divider
    //dashboard entrance animations
    //divider
    private void animateWelcomeBanner() {
        PauseTransition delay = new PauseTransition(Duration.millis(200));
        delay.setOnFinished(e -> {
            FadeTransition ft = new FadeTransition(Duration.millis(500), welcomeBanner);
            ft.setFromValue(0);
            ft.setToValue(1);
            ft.setInterpolator(Interpolator.EASE_OUT);
            ft.play();
        });
        delay.play();
    }

    private void animateDashStats() {
        VBox[] stats = { dashStat0, dashStat1, dashStat2, dashStat3 };
        for (int i = 0; i < stats.length; i++) {
            VBox stat = stats[i];
            PauseTransition delay = new PauseTransition(Duration.millis(400 + i * 120));
            delay.setOnFinished(e -> {
                FadeTransition ft = new FadeTransition(Duration.millis(500), stat);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.setInterpolator(Interpolator.EASE_OUT);

                TranslateTransition tt = new TranslateTransition(Duration.millis(500), stat);
                tt.setFromY(20);
                tt.setToY(0);
                tt.setInterpolator(Interpolator.EASE_OUT);

                new ParallelTransition(ft, tt).play();
            });
            delay.play();
        }
    }

    //divider
    //panel background blobs
    //divider
    private void initPanelBlobs() {
        String[][] eventBlobs = {
                { "rgba(26,115,232,0.18)", "rgba(66,133,244,0.14)", "rgba(100,181,246,0.13)" }
        };
        String[][] socBlobs = {
                { "rgba(52,168,83,0.18)", "rgba(0,151,167,0.15)", "rgba(129,199,132,0.14)" }
        };
        String[][] annBlobs = {
                { "rgba(251,188,4,0.20)", "rgba(234,67,53,0.13)", "rgba(239,154,154,0.15)" }
        };
        String[][] sponBlobs = {
                { "rgba(123,31,162,0.16)", "rgba(52,168,83,0.15)", "rgba(26,115,232,0.13)" }
        };
        String[][] admBlobs = {
                { "rgba(26,115,232,0.17)", "rgba(234,67,53,0.14)", "rgba(251,188,4,0.14)" }
        };

        double[][] positions = { { -60, -40 }, { -40, 200 }, { 300, 120 } };
        double[][] sizes = { { 480, 400 }, { 380, 340 }, { 300, 280 } };

        addBlobsToPane(blobPaneEvents, eventBlobs[0], positions, sizes);
        addBlobsToPane(blobPaneSocieties, socBlobs[0], positions, sizes);
        addBlobsToPane(blobPaneAnnouncements, annBlobs[0], positions, sizes);
        addBlobsToPane(blobPaneSponsors, sponBlobs[0], positions, sizes);
        addBlobsToPane(blobPaneAdmin, admBlobs[0], positions, sizes);
    }

    private void addBlobsToPane(Pane pane, String[] colors, double[][] positions, double[][] sizes) {
        double[][] driftValues = { { 14, -16 }, { -10, 12 }, { 8, -8 } };
        double[] durations = { 9000, 11000, 13000 };

        for (int i = 0; i < colors.length; i++) {
            Region blob = new Region();
            blob.setStyle("-fx-background-color: " + colors[i] + "; -fx-background-radius: 250;");
            blob.setPrefWidth(sizes[i][0]);
            blob.setPrefHeight(sizes[i][1]);
            blob.setMinWidth(Region.USE_PREF_SIZE);
            blob.setMinHeight(Region.USE_PREF_SIZE);
            blob.setMaxWidth(Region.USE_PREF_SIZE);
            blob.setMaxHeight(Region.USE_PREF_SIZE);
            blob.setTranslateX(positions[i][0]);
            blob.setTranslateY(positions[i][1]);
            blob.setMouseTransparent(true);
            blob.setOpacity(0.8);
            pane.getChildren().add(blob);

            double baseX = positions[i][0];
            double baseY = positions[i][1];
            double dx = driftValues[i][0];
            double dy = driftValues[i][1];
            double dur = durations[i];

            Timeline tl = new Timeline(
                    new KeyFrame(Duration.ZERO,
                            new KeyValue(blob.translateXProperty(), baseX, Interpolator.EASE_BOTH),
                            new KeyValue(blob.translateYProperty(), baseY, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(dur / 2),
                            new KeyValue(blob.translateXProperty(), baseX + dx, Interpolator.EASE_BOTH),
                            new KeyValue(blob.translateYProperty(), baseY + dy, Interpolator.EASE_BOTH)),
                    new KeyFrame(Duration.millis(dur),
                            new KeyValue(blob.translateXProperty(), baseX, Interpolator.EASE_BOTH),
                            new KeyValue(blob.translateYProperty(), baseY, Interpolator.EASE_BOTH)));
            tl.setCycleCount(Timeline.INDEFINITE);
            //offset each blob
            tl.jumpTo(Duration.millis(i * 3000));
            tl.play();
        }
    }

    //divider
    //event data
    //divider
    private void initEvents() {
        EventService eventService = new EventService();
        allEvents = eventService.getApprovedEvents();
    }

    //divider
    //event feed
    //divider
    private void populateEventFeed() {
        allEvents = new EventService().getApprovedEvents();
        eventFeedList.getChildren().clear();
        for (Event ev : allEvents) {
            //event row main
            VBox eventContainer = new VBox(0);
            eventContainer.getStyleClass().add("soc-event-card");
            eventContainer.setPadding(new Insets(0));

            HBox eventRow = new HBox(14);
            eventRow.setAlignment(Pos.TOP_LEFT);
            eventRow.setPadding(new Insets(14, 16, 14, 16));

            //hover animation
            eventContainer.setOnMouseEntered(e -> {
                TranslateTransition tt = new TranslateTransition(Duration.millis(150), eventContainer);
                tt.setToX(2);
                tt.play();
            });
            eventContainer.setOnMouseExited(e -> {
                TranslateTransition tt = new TranslateTransition(Duration.millis(150), eventContainer);
                tt.setToX(0);
                tt.play();
            });

            //date column
            VBox dateCol = new VBox(0);
            dateCol.setAlignment(Pos.CENTER);
            dateCol.setMinWidth(44);
            dateCol.setPrefWidth(44);
            Label monthLabel = new Label(ev.getMonthLabel());
            monthLabel.getStyleClass().add("feed-month");
            monthLabel.setStyle("-fx-text-fill: #1a73e8;");
            Label dayLabel = new Label(String.valueOf(ev.getDayNumber()));
            dayLabel.getStyleClass().add("feed-day");
            dateCol.getChildren().addAll(monthLabel, dayLabel);

            //info column
            VBox infoCol = new VBox(3);
            HBox.setHgrow(infoCol, Priority.ALWAYS);
            Label titleLabel = new Label(ev.getTitle());
            titleLabel.getStyleClass().add("feed-event-title");
            Label venueLabel = new Label(ev.getVenue());
            venueLabel.getStyleClass().add("feed-event-venue");
            HBox chipRow = new HBox(6);
            chipRow.getStyleClass().add("feed-chips");
            String societyName = ev.getSocietyID();
            try (Statement st = DBConnection.getInstance().getConnection().createStatement()) {
                ResultSet rs = st.executeQuery("SELECT name FROM Society WHERE societyID='" + ev.getSocietyID() + "'");
                if (rs.next()) societyName = rs.getString("name");
            } catch (SQLException ignored) {}

            java.util.List<String> tags = java.util.List.of(societyName != null ? societyName : "General");
            for (int i = 0; i < tags.size(); i++) {
                Label chip = new Label(tags.get(i));
                chip.getStyleClass().addAll("chip", "chip-blue");
                chipRow.getChildren().add(chip);
            }
            infoCol.getChildren().addAll(titleLabel, venueLabel, chipRow);
            VBox.setMargin(chipRow, new Insets(5, 0, 0, 0));

            //action column
            VBox actionCol = new VBox(6);
            actionCol.setAlignment(Pos.TOP_RIGHT);
            
            User user = Session.getInstance().getLoggedInUser();
            VBox statsDropdown = null;
            Button toggleBtn = null;

            if (user instanceof com.universe.models.Student) {
                //determine registration state
                RegistrationService regService = new RegistrationService();
                boolean isRegistered = regService.isAlreadyRegistered(user.getUserID(), ev.getEventID());
                int regCount = regService.countConfirmedRegistrations(ev.getEventID());
                int maxSeats = ev.getMaxSeats();
                
                if (ev.isRegistrationClosed() || ev.isOver()) {
                    Button regBtn = new Button("Closed (Past)");
                    regBtn.getStyleClass().addAll("btn-register", "btn-register-disabled");
                    regBtn.setDisable(true);
                    actionCol.getChildren().add(regBtn);
                } else if (isRegistered) {
                    Button regBtn = new Button("Registered ✓");
                    regBtn.getStyleClass().add("btn-registered");
                    regBtn.setDisable(true);
                    actionCol.getChildren().add(regBtn);
                } else if (regCount >= maxSeats) {
                    Button fullBtn = new Button("Seats Full");
                    fullBtn.getStyleClass().addAll("btn-register", "btn-register-disabled");
                    fullBtn.setDisable(true);
                    actionCol.getChildren().add(fullBtn);
                } else {
                    Button regBtn = new Button("Register");
                    regBtn.getStyleClass().add("btn-register");
                    regBtn.setOnAction(e -> {
                        com.universe.models.EventRegistration newReg = new com.universe.models.EventRegistration(
                                "REG" + System.currentTimeMillis(), user.getUserID(), ev.getEventID(),
                                java.time.LocalDate.now(), "Pending");
                        regService.registerForEvent(newReg);
                        regBtn.setText("Pending ✓");
                        regBtn.getStyleClass().add("btn-registered");
                        regBtn.setDisable(true);
                        populateStudentRegistrations(user.getUserID()); //refresh the sidebar
                    });
                    actionCol.getChildren().add(regBtn);
                    if (!ev.getDeadlineLabel().isEmpty()) {
                        Label deadlineLabel = new Label(ev.getDeadlineLabel());
                        deadlineLabel.getStyleClass().add("feed-deadline");
                        actionCol.getChildren().add(deadlineLabel);
                    }
                }

                //expandable stats dropdown
                statsDropdown = new VBox(8);
                statsDropdown.setPadding(new Insets(0, 16, 14, 74));
                statsDropdown.setVisible(false);
                statsDropdown.setManaged(false);

                String evDate = ev.getDate() != null ? ev.getDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) : "TBD";
                String evTime = ev.getTime() != null ? ev.getTime().format(DateTimeFormatter.ofPattern("hh:mm a")) : "TBD";
                String evDeadline = ev.getRegDeadline() != null ? ev.getRegDeadline().format(DateTimeFormatter.ofPattern("MMM d, yyyy")) : "No Deadline";

                GridPane statsGrid = new GridPane();
                statsGrid.setHgap(10);
                statsGrid.setVgap(10);
                statsGrid.add(createStatBox(evDate + " at " + evTime, "Event Start", "#e8f0fe", "#1a73e8"), 0, 0);
                statsGrid.add(createStatBox(societyName, "Organizing Society", "#fef7e0", "#b06000"), 1, 0);
                statsGrid.add(createStatBox(Math.max(0, maxSeats - regCount) + " left", "Seats Remaining", "#e6f4ea", "#34a853"), 0, 1);
                statsGrid.add(createStatBox(evDeadline, "Registration Deadline", "#fce8e6", "#ea4335"), 1, 1);
                ColumnConstraints col50 = new ColumnConstraints();
                col50.setHgrow(Priority.ALWAYS);
                col50.setPercentWidth(50);
                statsGrid.getColumnConstraints().addAll(col50, col50);

                statsDropdown.getChildren().add(statsGrid);

                toggleBtn = new Button("▼ View Details");
                toggleBtn.getStyleClass().add("soc-toggle-btn");
                VBox finalStatsDropdown = statsDropdown;
                Button finalToggleBtn = toggleBtn;
                toggleBtn.setOnAction(e -> {
                    boolean show = !finalStatsDropdown.isVisible();
                    finalStatsDropdown.setVisible(show);
                    finalStatsDropdown.setManaged(show);
                    finalToggleBtn.setText(show ? "▲ Hide Details" : "▼ View Details");
                });
            } else if (user instanceof com.universe.models.Sponsor) {
                com.universe.models.Sponsor sponsor = (com.universe.models.Sponsor) user;
                List<SponsorshipDeal> existingDeals = new SponsorshipService().getDealsBySponsor(sponsor.getSponsorID());
                
                if (sponsor.hasActiveProposalFor(ev.getEventID(), existingDeals)) {
                    Button dealBtn = new Button("Deal Sent ✓");
                    dealBtn.getStyleClass().addAll("btn-white-outline", "btn-registered");
                    dealBtn.setDisable(true);
                    actionCol.getChildren().add(dealBtn);
                } else {
                    Button dealBtn = new Button("Sponsor Event");
                    dealBtn.getStyleClass().add("btn-white-outline");
                    dealBtn.setOnAction(e -> {
                        ThemedDialog.showTextInput(dashboardStackRoot,
                            "Sponsorship Proposal", "Sponsor " + ev.getTitle(),
                            "Enter your proposal message...",
                            msg -> {
                                SponsorshipDeal deal = new SponsorshipDeal(
                                    "D" + System.currentTimeMillis(), user.getUserID(), ev.getEventID(),
                                    msg, java.time.LocalDate.now(), "Pending", "");
                                new SponsorshipService().submitProposal(deal);
                                dealBtn.setText("Deal Sent ✓");
                                dealBtn.getStyleClass().add("btn-registered");
                                dealBtn.setDisable(true);
                            });
                    });
                    actionCol.getChildren().add(dealBtn);
                }
            } else {
                Label viewOnlyLabel = new Label("View Only");
                viewOnlyLabel.getStyleClass().add("feed-deadline");
                actionCol.getChildren().add(viewOnlyLabel);
            }

            eventRow.getChildren().addAll(dateCol, infoCol, actionCol);
            eventContainer.getChildren().add(eventRow);
            
            if (toggleBtn != null && statsDropdown != null) {
                HBox toggleRow = new HBox();
                toggleRow.setPadding(new Insets(0, 16, 8, 74));
                toggleRow.getChildren().add(toggleBtn);
                eventContainer.getChildren().addAll(toggleRow, statsDropdown);
            }
            
            eventFeedList.getChildren().add(eventContainer);
        }
    }

    //divider
    //calendar
    //divider
    private void buildCalendar() {
        calGrid.getChildren().clear();
        calMonthLabel.setText(
                currentMonth.getMonth().getDisplayName(TextStyle.FULL, Locale.ENGLISH) + " " + currentMonth.getYear());

        String[] days = { "S", "M", "T", "W", "T", "F", "S" };
        for (int i = 0; i < 7; i++) {
            Label header = new Label(days[i]);
            header.getStyleClass().addAll("cal-cell", "cal-header");
            header.setMaxWidth(Double.MAX_VALUE);
            header.setAlignment(Pos.CENTER);
            calGrid.add(header, i, 0);
        }

        LocalDate first = currentMonth.atDay(1);
        int startDow = first.getDayOfWeek().getValue() % 7;
        int daysInMonth = currentMonth.lengthOfMonth();

        LocalDate today = LocalDate.of(2026, 4, 21);
        Set<Integer> eventDays = new HashSet<>();
        for (Event ev : allEvents) {
            if (ev.getDate().getYear() == currentMonth.getYear()
                    && ev.getDate().getMonthValue() == currentMonth.getMonthValue()) {
                eventDays.add(ev.getDate().getDayOfMonth());
            }
        }

        YearMonth prevMonth = currentMonth.minusMonths(1);
        int prevDays = prevMonth.lengthOfMonth();
        for (int i = startDow - 1; i >= 0; i--) {
            int day = prevDays - i;
            Label cell = new Label(String.valueOf(day));
            cell.getStyleClass().addAll("cal-cell", "cal-other-month");
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setAlignment(Pos.CENTER);
            calGrid.add(cell, startDow - 1 - i, 1);
        }

        int row = 1;
        int col = startDow;
        for (int day = 1; day <= daysInMonth; day++) {
            Label cell = new Label(String.valueOf(day));
            cell.getStyleClass().add("cal-cell");
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setAlignment(Pos.CENTER);

            if (today.getYear() == currentMonth.getYear() && today.getMonthValue() == currentMonth.getMonthValue()
                    && today.getDayOfMonth() == day) {
                cell.getStyleClass().add("cal-today");
            } else if (eventDays.contains(day)) {
                cell.getStyleClass().add("cal-has-event");
            }

            final int clickedDay = day;
            cell.setOnMouseClicked(e -> {
                calGrid.getChildren().forEach(n -> n.getStyleClass().remove("cal-selected"));
                cell.getStyleClass().add("cal-selected");
                showDayEvent(LocalDate.of(currentMonth.getYear(), currentMonth.getMonthValue(), clickedDay));
            });

            calGrid.add(cell, col, row);
            col++;
            if (col > 6) {
                col = 0;
                row++;
            }
        }

        int nextDay = 1;
        while (col <= 6 && col > 0) {
            Label cell = new Label(String.valueOf(nextDay++));
            cell.getStyleClass().addAll("cal-cell", "cal-other-month");
            cell.setMaxWidth(Double.MAX_VALUE);
            cell.setAlignment(Pos.CENTER);
            calGrid.add(cell, col, row);
            col++;
        }
    }

    private void showDayEvent(LocalDate date) {
        dayEventInfo.getChildren().clear();
        Event found = null;
        for (Event ev : allEvents) {
            if (ev.getDate().equals(date)) {
                found = ev;
                break;
            }
        }

        if (found != null) {
            Label title = new Label(found.getTitle());
            title.getStyleClass().add("day-event-title");
            title.setWrapText(true);
            Label meta = new Label(found.getVenue());
            meta.getStyleClass().add("day-event-meta");
            meta.setWrapText(true);
            HBox chips = new HBox(6);
            java.util.List<String> tags = java.util.List.of(found.getSocietyID() != null ? found.getSocietyID() : "General");
            for (int i = 0; i < tags.size() && i < 2; i++) {
                Label chip = new Label(tags.get(i));
                chip.getStyleClass().addAll("day-event-chip", "chip-blue");
                chips.getChildren().add(chip);
            }
            dayEventInfo.getChildren().addAll(title, meta, chips);
        } else {
            Label noEvent = new Label("No events on this day");
            noEvent.getStyleClass().add("day-event-empty");
            dayEventInfo.getChildren().add(noEvent);
        }
    }

    //divider
    //societies
    //divider
    private void populateSocieties() {
        societiesGrid.getChildren().clear();
        User user = Session.getInstance().getLoggedInUser();

        if (user instanceof com.universe.models.Society) {
            societiesPanelHeading.setText("My Society Dashboard");
            societiesPanelSubheading.setText("Manage your events and sponsorship deals");
            populateMySocietyDashboard();
            return;
        }

        SocietyService societyService = new SocietyService();
        List<Society> societies = societyService.getActiveSocieties();

        //dynamic subheading from
        societiesPanelHeading.setText("All Societies");
        societiesPanelSubheading.setText(societies.size() + " active societies on the platform");

        String[][] palette = {
                {"#1a73e8","#1557b0"}, {"#34a853","#1b7a3d"}, {"#7b1fa2","#4a148c"},
                {"#ea4335","#b71c1c"}, {"#f9a825","#e65100"}, {"#0097a7","#006064"}
        };

        int col = 0, row = 0;
        for (int i = 0; i < societies.size(); i++) {
            Society soc = societies.get(i);
            String[] colors = palette[i % palette.length];
            String initial = soc.getName().substring(0, 1).toUpperCase();
            
            //total active events
            int activeEventsCount = 0;
            try (Statement st = DBConnection.getInstance().getConnection().createStatement()) {
                ResultSet rs = st.executeQuery("SELECT COUNT(*) FROM Event WHERE societyID='" + soc.getSocietyID() + "' AND status='Approved'");
                if (rs.next()) activeEventsCount = rs.getInt(1);
            } catch (SQLException ignored) {}
            String eventsLabel = activeEventsCount + " active events";

            VBox card = createSocietyCard(
                    soc.getName(), colors[0], colors[1],
                    initial, soc.getDescription(), eventsLabel, false
            );
            societiesGrid.add(card, col, row);
            col++;
            if (col > 2) { col = 0; row++; }
        }
    }

    private void populateMySocietyDashboard() {
        Society soc = (Society) Session.getInstance().getLoggedInUser();
        EventService eventService = new EventService();
        SponsorshipService sponsorshipService = new SponsorshipService();
        RegistrationService registrationService = new RegistrationService();

        VBox dash = new VBox(24);
        dash.setPadding(new Insets(0));

        //divider
        VBox eventsSection = new VBox(6);
        eventsSection.getStyleClass().add("admin-card");
        eventsSection.setPadding(new Insets(20));

        Label eventsTitle = new Label("📅 My Events");
        eventsTitle.getStyleClass().add("admin-card-title");
        Label eventsDesc = new Label("All events created by your society — manage registrations for approved events.");
        eventsDesc.getStyleClass().add("admin-card-desc");
        eventsDesc.setWrapText(true);

        VBox eventsList = new VBox(10);
        List<Event> myEvents = eventService.getEventsBySociety(soc.getSocietyID());

        if (myEvents.isEmpty()) {
            Label noEvents = new Label("No events created yet. Click '+ Add Event' in the banner above.");
            noEvents.setStyle("-fx-text-fill: #9aa0a6; -fx-padding: 12;");
            eventsList.getChildren().add(noEvents);
        } else {
            for (Event ev : myEvents) {
                VBox eventCard = new VBox(0);
                eventCard.getStyleClass().add("soc-event-card");

                HBox eventRow = new HBox(14);
                eventRow.setPadding(new Insets(14, 16, 14, 16));
                eventRow.setAlignment(Pos.CENTER_LEFT);

                //icon column
                StackPane icon = new StackPane();
                icon.setMinWidth(40); icon.setMinHeight(40);
                icon.setMaxWidth(40); icon.setMaxHeight(40);
                String iconBg = ev.isApproved() ? "#e6f4ea" : ev.isPending() ? "#fef7e0" : "#fce8e6";
                icon.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 10;");
                String iconEmoji = ev.isApproved() ? "✅" : ev.isPending() ? "⏳" : "❌";
                Label iconLabel = new Label(iconEmoji);
                iconLabel.setStyle("-fx-font-size: 18px;");
                icon.getChildren().add(iconLabel);

                //info column
                VBox info = new VBox(3);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label eTitle = new Label(ev.getTitle());
                eTitle.getStyleClass().add("feed-event-title");
                HBox metaRow = new HBox(8);
                Label eVenue = new Label("📍 " + ev.getVenue());
                eVenue.getStyleClass().add("feed-event-venue");
                Label eDate = new Label("🗓 " + ev.getDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
                eDate.getStyleClass().add("feed-event-venue");
                metaRow.getChildren().addAll(eVenue, eDate);
                info.getChildren().addAll(eTitle, metaRow);

                //status badge stats
                VBox rightCol = new VBox(5);
                rightCol.setAlignment(Pos.CENTER_RIGHT);
                Label statusBadge = new Label(ev.getStatus());
                String badgeStyle = ev.isApproved() ? "status-badge-green"
                        : ev.isPending() ? "status-badge-yellow" : "status-badge-red";
                statusBadge.getStyleClass().add(badgeStyle);

                rightCol.getChildren().add(statusBadge);
                if (ev.isApproved()) {
                    int regCount = registrationService.countConfirmedRegistrations(ev.getEventID());
                    int remaining = ev.getMaxSeats() - regCount;
                    Label seatsLabel = new Label("👥 " + regCount + " registered · " + remaining + " seats left");
                    seatsLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #5f6368;");
                    rightCol.getChildren().add(seatsLabel);
                }

                eventRow.getChildren().addAll(icon, info, rightCol);
                eventCard.getChildren().add(eventRow);

                //expandable registration list
                if (ev.isApproved()) {
                    VBox regDropdown = new VBox(6);
                    regDropdown.setPadding(new Insets(0, 16, 14, 70));
                    regDropdown.setVisible(false);
                    regDropdown.setManaged(false);

                    List<String[]> regs = registrationService.getRegistrationsWithStudentNames(ev.getEventID());
                    if (regs.isEmpty()) {
                        Label noRegs = new Label("No students registered yet.");
                        noRegs.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 12;");
                        regDropdown.getChildren().add(noRegs);
                    } else {
                        for (String[] reg : regs) {
                            HBox regRow = new HBox(10);
                            regRow.setAlignment(Pos.CENTER_LEFT);
                            regRow.setPadding(new Insets(6, 10, 6, 10));
                            regRow.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

                            Label studentIcon = new Label("🎓");
                            studentIcon.setStyle("-fx-font-size: 14;");
                            VBox studentInfo = new VBox(1);
                            HBox.setHgrow(studentInfo, Priority.ALWAYS);
                            Label sName = new Label(reg[2]);
                            sName.setStyle("-fx-font-size: 13; -fx-font-weight: 500; -fx-text-fill: #202124;");
                            Label sDate = new Label("Registered: " + reg[3]);
                            sDate.setStyle("-fx-font-size: 11; -fx-text-fill: #9aa0a6;");
                            studentInfo.getChildren().addAll(sName, sDate);

                            Label regStatus = new Label(reg[4]);
                            regStatus.getStyleClass().add("Confirmed".equals(reg[4]) ? "status-badge-green" : "status-badge-red");

                            //dropdown actions approve
                            MenuButton actionMenu = new MenuButton("⋮");
                            actionMenu.getStyleClass().add("soc-action-menu");
                            String regID = reg[0];
                            MenuItem approveItem = new MenuItem("✓ Confirm");
                            approveItem.setOnAction(ae -> {
                                registrationService.updateRegistrationStatus(regID, "Confirmed");
                                populateSocieties();
                            });
                            MenuItem rejectItem = new MenuItem("✗ Cancel");
                            rejectItem.setOnAction(ae -> {
                                registrationService.updateRegistrationStatus(regID, "Cancelled");
                                populateSocieties();
                            });
                            actionMenu.getItems().addAll(approveItem, rejectItem);

                            regRow.getChildren().addAll(studentIcon, studentInfo, regStatus, actionMenu);
                            regDropdown.getChildren().add(regRow);
                        }
                    }

                    //toggle button
                    Button toggleBtn = new Button("▼ View Registrations (" + regs.size() + ")");
                    toggleBtn.getStyleClass().add("soc-toggle-btn");
                    toggleBtn.setOnAction(e -> {
                        boolean show = !regDropdown.isVisible();
                        regDropdown.setVisible(show);
                        regDropdown.setManaged(show);
                        toggleBtn.setText((show ? "▲ Hide" : "▼ View") + " Registrations (" + regs.size() + ")");
                    });

                    HBox toggleRow = new HBox();
                    toggleRow.setPadding(new Insets(0, 16, 8, 70));
                    toggleRow.getChildren().add(toggleBtn);

                    eventCard.getChildren().addAll(toggleRow, regDropdown);
                }

                //hover animation
                eventCard.setOnMouseEntered(e -> {
                    TranslateTransition tt = new TranslateTransition(Duration.millis(150), eventCard);
                    tt.setToX(2); tt.play();
                });
                eventCard.setOnMouseExited(e -> {
                    TranslateTransition tt = new TranslateTransition(Duration.millis(150), eventCard);
                    tt.setToX(0); tt.play();
                });

                eventsList.getChildren().add(eventCard);
            }
        }
        eventsSection.getChildren().addAll(eventsTitle, eventsDesc, eventsList);

        //divider
        VBox dealsSection = new VBox(6);
        dealsSection.getStyleClass().add("admin-card");
        dealsSection.setPadding(new Insets(20));

        Label dealsTitle = new Label("💼 Incoming Sponsorship Offers");
        dealsTitle.getStyleClass().add("admin-card-title");
        Label dealsDesc = new Label("Sponsorship proposals from sponsors for your events.");
        dealsDesc.getStyleClass().add("admin-card-desc");
        dealsDesc.setWrapText(true);

        VBox dealsList = new VBox(10);
        List<SponsorshipDeal> deals = sponsorshipService.getDealsBySociety(soc.getSocietyID());

        if (deals.isEmpty()) {
            Label noDeals = new Label("No incoming sponsorship offers yet.");
            noDeals.setStyle("-fx-text-fill: #9aa0a6; -fx-padding: 12;");
            dealsList.getChildren().add(noDeals);
        } else {
            for (SponsorshipDeal deal : deals) {
                HBox dealRow = new HBox(14);
                dealRow.getStyleClass().add("soc-event-card");
                dealRow.setPadding(new Insets(14, 16, 14, 16));
                dealRow.setAlignment(Pos.CENTER_LEFT);

                //icon
                StackPane dealIcon = new StackPane();
                dealIcon.setMinWidth(40); dealIcon.setMinHeight(40);
                dealIcon.setMaxWidth(40); dealIcon.setMaxHeight(40);
                String dIconBg = deal.isPending() ? "#e8f0fe" : deal.isAccepted() ? "#e6f4ea" : "#fce8e6";
                dealIcon.setStyle("-fx-background-color: " + dIconBg + "; -fx-background-radius: 10;");
                Label dIconLabel = new Label(deal.isPending() ? "📩" : deal.isAccepted() ? "🤝" : "❌");
                dIconLabel.setStyle("-fx-font-size: 18px;");
                dealIcon.getChildren().add(dIconLabel);

                //info
                VBox dealInfo = new VBox(3);
                HBox.setHgrow(dealInfo, Priority.ALWAYS);

                //fetch sponsor name
                String sponsorName = deal.getSponsorID();
                try (Statement st = DBConnection.getInstance().getConnection().createStatement()) {
                    ResultSet rs = st.executeQuery("SELECT name FROM Sponsor WHERE sponsorID='" + deal.getSponsorID() + "'");
                    if (rs.next()) sponsorName = rs.getString("name");
                } catch (SQLException ignored) {}

                Label dTitle = new Label("From: " + sponsorName);
                dTitle.getStyleClass().add("feed-event-title");
                Label dEvent = new Label("Event: " + deal.getEventID());
                dEvent.getStyleClass().add("feed-event-venue");
                Label dMsg = new Label("\"" + deal.getProposalMessage() + "\"");
                dMsg.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 12; -fx-font-style: italic;");
                dMsg.setWrapText(true);
                dealInfo.getChildren().addAll(dTitle, dEvent, dMsg);

                //status actions
                VBox dealActions = new VBox(6);
                dealActions.setAlignment(Pos.CENTER_RIGHT);
                Label dealStatus = new Label(deal.getStatus());
                dealStatus.getStyleClass().add(deal.getStatusBadgeStyle());
                dealActions.getChildren().add(dealStatus);

                if (deal.isPending()) {
                    HBox btns = new HBox(6);
                    Button acceptBtn = new Button("✓ Accept");
                    acceptBtn.getStyleClass().add("btn-approve");
                    acceptBtn.setOnAction(e -> {
                        sponsorshipService.updateDealStatus(deal.getDealID(), "Accepted", "Deal accepted!");
                        populateSocieties();
                    });
                    Button rejectBtn = new Button("✗ Reject");
                    rejectBtn.getStyleClass().add("btn-reject");
                    rejectBtn.setOnAction(e -> {
                        sponsorshipService.updateDealStatus(deal.getDealID(), "Rejected", "Deal declined.");
                        populateSocieties();
                    });
                    btns.getChildren().addAll(acceptBtn, rejectBtn);
                    dealActions.getChildren().add(btns);
                }

                dealRow.getChildren().addAll(dealIcon, dealInfo, dealActions);
                dealsList.getChildren().add(dealRow);
            }
        }
        dealsSection.getChildren().addAll(dealsTitle, dealsDesc, dealsList);

        dash.getChildren().addAll(eventsSection, dealsSection);
        societiesGrid.add(dash, 0, 0);
        GridPane.setColumnSpan(dash, 3);
    }

    private VBox createSocietyCard(String name, String color, String colorDark, String initial,
                                   String desc, String members, boolean following) {
        VBox card = new VBox(0);
        card.getStyleClass().add("soc-card");

        //hover animation
        card.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
            tt.setToY(-3);
            tt.play();
        });
        card.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
            tt.setToY(0);
            tt.play();
        });

        Region banner = new Region();
        banner.getStyleClass().add("soc-card-banner");
        banner.setMinHeight(80);
        banner.setPrefHeight(80);
        banner.setStyle("-fx-background-color: linear-gradient(to bottom right, " + color + ", " + colorDark + ");");

        VBox body = new VBox(3);
        body.setPadding(new Insets(16));

        StackPane avatar = new StackPane();
        avatar.getStyleClass().add("soc-card-avatar");
        avatar.setStyle("-fx-background-color: " + color + ";");
        avatar.setMaxWidth(48);
        avatar.setMaxHeight(48);
        avatar.setMinWidth(48);
        avatar.setMinHeight(48);
        Label initLabel = new Label(initial);
        initLabel.setStyle("-fx-text-fill: white; -fx-font-size: 20px; -fx-font-weight: 700;");
        avatar.getChildren().add(initLabel);
        VBox.setMargin(avatar, new Insets(-28, 0, 8, 0));

        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("soc-card-name");
        Label descLabel = new Label(desc);
        descLabel.getStyleClass().add("soc-card-desc");
        descLabel.setWrapText(true);

        HBox footer = new HBox(10);
        footer.setAlignment(Pos.CENTER_LEFT);
        VBox.setMargin(footer, new Insets(12, 0, 0, 0));
        Label membersLabel = new Label(members);
        membersLabel.getStyleClass().add("soc-members");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button followBtn = new Button(following ? "Following ✓" : "Follow");
        followBtn.getStyleClass().add("btn-follow");
        if (following) {
            followBtn.getStyleClass().add("btn-following");
        }
        followBtn.setOnAction(e -> {
            if (followBtn.getStyleClass().contains("btn-following")) {
                followBtn.getStyleClass().remove("btn-following");
                followBtn.setText("Follow");
            } else {
                followBtn.getStyleClass().add("btn-following");
                followBtn.setText("Following ✓");
            }
        });

        footer.getChildren().addAll(membersLabel, spacer, followBtn);
        body.getChildren().addAll(avatar, nameLabel, descLabel, footer);
        card.getChildren().addAll(banner, body);
        return card;
    }

    //divider
    //announcements
    //divider
    private void populateAnnouncements() {
        announcementsList.getChildren().clear();
        User user = Session.getInstance().getLoggedInUser();
        if (user == null) return;
        
        com.universe.services.NotificationService notifService = new com.universe.services.NotificationService();
        String roleType = user.getClass().getSimpleName();
        java.util.List<com.universe.models.Notification> notifications = notifService.getNotifications(user.getUserID(), roleType);
        
        //merge global announcements
        java.util.List<com.universe.models.Notification> globalNotifs = notifService.getNotifications("ALL", "GLOBAL");
        notifications.addAll(globalNotifs);
        notifications.sort((n1, n2) -> n2.getDate().compareTo(n1.getDate()));

        //icon color cycling
        String[] icons   = {"📌","📘","🎭","🏆","📣","🔔"};
        String[] bgColors= {"#fef7e0" ,"#e8f0fe","#e6f4ea","#fce8e6","#f3e5f5","#e0f7fa"};

        if (notifications.isEmpty()) {
            Label noNotif = new Label("No new notifications");
            noNotif.setStyle("-fx-padding: 20; -fx-text-fill: #5f6368;");
            announcementsList.getChildren().add(noNotif);
            return;
        }

        for (int i = 0; i < notifications.size(); i++) {
            com.universe.models.Notification notif = notifications.get(i);

            HBox item = new HBox(14);
            item.getStyleClass().add("announce-item");
            item.setPadding(new Insets(16));

            StackPane icon = new StackPane();
            icon.getStyleClass().add("announce-icon");
            icon.setStyle("-fx-background-color: " + bgColors[i % bgColors.length] + ";");
            icon.setMinWidth(40); icon.setMinHeight(40);
            icon.setMaxWidth(40); icon.setMaxHeight(40);
            Label iconLabel = new Label(icons[i % icons.length]);
            iconLabel.setStyle("-fx-font-size: 18px;");
            icon.getChildren().add(iconLabel);

            VBox content = new VBox(3);
            HBox.setHgrow(content, Priority.ALWAYS);
            Label title = new Label(notif.getType() + " Notification");
            title.getStyleClass().add("announce-title");
            title.setWrapText(true);
            Label body = new Label(notif.getMessage());
            body.getStyleClass().add("announce-body");
            body.setWrapText(true);

            HBox meta = new HBox(12);
            meta.getStyleClass().add("announce-meta");
            VBox.setMargin(meta, new Insets(6, 0, 0, 0));
            Label source = new Label("System Message"); 
            source.getStyleClass().add("announce-meta-text");
            //format date
            java.time.format.DateTimeFormatter formatter = java.time.format.DateTimeFormatter.ofPattern("MMM dd, yyyy");
            Label date = new Label(notif.getDate().format(formatter));
            date.getStyleClass().add("announce-meta-text");

            meta.getChildren().addAll(source, date);
            content.getChildren().addAll(title, body, meta);
            item.getChildren().addAll(icon, content);
            announcementsList.getChildren().add(item);
        }
    }

    //divider
    //sponsors
    //divider
    private void populateSponsors() {
        SponsorshipService sponsorshipService = new SponsorshipService();
        EventService eventService = new EventService();
        RegistrationService registrationService = new RegistrationService();
        User user = Session.getInstance().getLoggedInUser();

        sponsorsGrid.getChildren().clear();

        if (user instanceof com.universe.models.Admin) {
            VBox adminSponsorsCard = new VBox(6);
            adminSponsorsCard.getStyleClass().add("admin-card");
            adminSponsorsCard.setPadding(new Insets(20));
            Label asTitle = new Label("🏢 Registered Sponsors");
            asTitle.getStyleClass().add("admin-card-title");
            Label asDesc = new Label("Overview of all registered sponsors and their sent proposals.");
            asDesc.getStyleClass().add("admin-card-desc");
            asDesc.setWrapText(true);

            VBox sponsorList = new VBox(10);
            List<com.universe.models.Sponsor> allSponsors = new com.universe.services.UserService().getAllSponsors();

            if (allSponsors.isEmpty()) {
                Label noSponsors = new Label("No sponsors registered on the platform yet.");
                noSponsors.setStyle("-fx-text-fill: #9aa0a6; -fx-padding: 12;");
                sponsorList.getChildren().add(noSponsors);
            } else {
                for (com.universe.models.Sponsor sponsor : allSponsors) {
                    VBox sCard = new VBox(0);
                    sCard.getStyleClass().add("soc-event-card");

                    HBox sRow = new HBox(14);
                    sRow.setPadding(new Insets(14, 16, 14, 16));
                    sRow.setAlignment(Pos.CENTER_LEFT);

                    //icon
                    StackPane sIcon = new StackPane();
                    sIcon.setMinWidth(40); sIcon.setMinHeight(40);
                    sIcon.setMaxWidth(40); sIcon.setMaxHeight(40);
                    sIcon.setStyle("-fx-background-color: #fce8e6; -fx-background-radius: 10;");
                    Label sIconLabel = new Label("🏢");
                    sIconLabel.setStyle("-fx-font-size: 18px;");
                    sIcon.getChildren().add(sIconLabel);

                    //info
                    VBox sInfo = new VBox(3);
                    HBox.setHgrow(sInfo, Priority.ALWAYS);
                    Label sTitle = new Label(sponsor.getName());
                    sTitle.getStyleClass().add("feed-event-title");
                    Label sOrg = new Label("Org: " + sponsor.getOrganization());
                    sOrg.getStyleClass().add("feed-event-venue");
                    Label sContact = new Label("Contact: " + sponsor.getEmail() + " | " + sponsor.getContactInfo());
                    sContact.setStyle("-fx-font-size: 11; -fx-text-fill: #9aa0a6;");
                    sInfo.getChildren().addAll(sTitle, sOrg, sContact);

                    sRow.getChildren().addAll(sIcon, sInfo);
                    sCard.getChildren().add(sRow);

                    //dropdown for proposals
                    List<com.universe.models.SponsorshipDeal> deals = sponsorshipService.getDealsBySponsor(sponsor.getSponsorID());
                    if (!deals.isEmpty()) {
                        VBox dealsDropdown = new VBox(6);
                        dealsDropdown.setPadding(new Insets(0, 16, 14, 70));
                        dealsDropdown.setVisible(false);
                        dealsDropdown.setManaged(false);

                        for (com.universe.models.SponsorshipDeal deal : deals) {
                            HBox dealRow = new HBox(10);
                            dealRow.setAlignment(Pos.CENTER_LEFT);
                            dealRow.setPadding(new Insets(6, 10, 6, 10));
                            dealRow.setStyle("-fx-background-color: #f8f9fa; -fx-background-radius: 8;");

                            Label dealIcon = new Label("📄");
                            
                            VBox dInfo = new VBox(1);
                            HBox.setHgrow(dInfo, Priority.ALWAYS);
                            
                            //get event title
                            String evTitle = deal.getEventID();
                            String socName = "Unknown Society";
                            try (java.sql.Statement st = com.universe.db.DBConnection.getInstance().getConnection().createStatement()) {
                                java.sql.ResultSet rs = st.executeQuery("SELECT e.title, s.name FROM Event e JOIN Society s ON e.societyID = s.societyID WHERE e.eventID='" + deal.getEventID() + "'");
                                if (rs.next()) {
                                    evTitle = rs.getString(1);
                                    socName = rs.getString(2);
                                }
                            } catch (java.sql.SQLException ignored) {}

                            Label dEvTitle = new Label(evTitle);
                            dEvTitle.setStyle("-fx-font-size: 13; -fx-font-weight: 500; -fx-text-fill: #202124;");
                            Label dSocName = new Label("Society: " + socName);
                            dSocName.setStyle("-fx-font-size: 11; -fx-text-fill: #9aa0a6;");
                            dInfo.getChildren().addAll(dEvTitle, dSocName);

                            Label dStatus = new Label(deal.getStatus());
                            dStatus.getStyleClass().add(deal.getStatusBadgeStyle());

                            dealRow.getChildren().addAll(dealIcon, dInfo, dStatus);
                            dealsDropdown.getChildren().add(dealRow);
                        }

                        Button toggleBtn = new Button("▼ View Proposals (" + deals.size() + ")");
                        toggleBtn.getStyleClass().add("soc-toggle-btn");
                        toggleBtn.setOnAction(e -> {
                            boolean show = !dealsDropdown.isVisible();
                            dealsDropdown.setVisible(show);
                            dealsDropdown.setManaged(show);
                            toggleBtn.setText((show ? "▲ Hide Proposals" : "▼ View Proposals") + " (" + deals.size() + ")");
                        });

                        HBox toggleRow = new HBox();
                        toggleRow.setPadding(new Insets(0, 16, 8, 70));
                        toggleRow.getChildren().add(toggleBtn);
                        sCard.getChildren().addAll(toggleRow, dealsDropdown);
                    }

                    sponsorList.getChildren().add(sCard);
                }
            }
            adminSponsorsCard.getChildren().addAll(asTitle, asDesc, sponsorList);
            sponsorsGrid.add(adminSponsorsCard, 0, 0);
            GridPane.setColumnSpan(adminSponsorsCard, 2);
            return;
        }

        //get sponsor s
        String currentSponsorID = (user instanceof com.universe.models.Sponsor) ? user.getUserID() : null;
        List<com.universe.models.SponsorshipDeal> myDeals = (currentSponsorID != null)
                ? sponsorshipService.getDealsBySponsor(currentSponsorID) : new ArrayList<>();
        Set<String> appliedEventIDs = new HashSet<>();
        for (com.universe.models.SponsorshipDeal d : myDeals) {
            if (!"Rejected".equals(d.getStatus())) appliedEventIDs.add(d.getEventID());
        }

        //divider
        VBox dealsCard = new VBox(6);
        dealsCard.getStyleClass().add("admin-card");
        dealsCard.setPadding(new Insets(20));
        Label dealsTitle = new Label("📋 Available Events");
        dealsTitle.getStyleClass().add("admin-card-title");
        Label dealsDesc = new Label("Approved events actively seeking sponsorship this semester.");
        dealsDesc.getStyleClass().add("admin-card-desc");
        dealsDesc.setWrapText(true);

        VBox dealsList = new VBox(10);
        List<Event> approvedEvents = eventService.getApprovedEvents();

        if (approvedEvents.isEmpty()) {
            Label noEvents = new Label("No approved events available right now.");
            noEvents.setStyle("-fx-text-fill: #9aa0a6; -fx-padding: 12;");
            dealsList.getChildren().add(noEvents);
        } else {
            for (Event ev : approvedEvents) {
                HBox eventRow = new HBox(14);
                eventRow.getStyleClass().add("soc-event-card");
                eventRow.setPadding(new Insets(14, 16, 14, 16));
                eventRow.setAlignment(Pos.CENTER_LEFT);

                //icon
                StackPane icon = new StackPane();
                icon.setMinWidth(40); icon.setMinHeight(40);
                icon.setMaxWidth(40); icon.setMaxHeight(40);
                icon.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 10;");
                Label iconLabel = new Label("🎪");
                iconLabel.setStyle("-fx-font-size: 18px;");
                icon.getChildren().add(iconLabel);

                //info
                VBox info = new VBox(3);
                HBox.setHgrow(info, Priority.ALWAYS);
                Label eTitle = new Label(ev.getTitle());
                eTitle.getStyleClass().add("feed-event-title");
                HBox metaRow = new HBox(10);
                Label eVenue = new Label("📍 " + ev.getVenue());
                eVenue.getStyleClass().add("feed-event-venue");
                Label eDate = new Label("🗓 " + ev.getDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
                eDate.getStyleClass().add("feed-event-venue");
                Label eFee = new Label("💰 " + ev.getFeeLabel());
                eFee.getStyleClass().add("feed-event-venue");
                metaRow.getChildren().addAll(eVenue, eDate, eFee);
                Label eSociety = new Label("Society: " + ev.getSocietyID());
                eSociety.setStyle("-fx-font-size: 11; -fx-text-fill: #9aa0a6;");
                info.getChildren().addAll(eTitle, metaRow, eSociety);

                //action
                VBox actionCol = new VBox(4);
                actionCol.setAlignment(Pos.CENTER_RIGHT);

                if (appliedEventIDs.contains(ev.getEventID())) {
                    Button appliedBtn = new Button("Applied ✓");
                    appliedBtn.getStyleClass().addAll("btn-approve");
                    appliedBtn.setDisable(true);
                    actionCol.getChildren().add(appliedBtn);
                } else {
                    Button sponsorBtn = new Button("Send Proposal");
                    sponsorBtn.getStyleClass().add("btn-register");
                    sponsorBtn.setOnAction(e -> {
                        ThemedDialog.showTextInput(dashboardStackRoot,
                            "Sponsorship Proposal", "Sponsor " + ev.getTitle(),
                            "Enter your proposal message...",
                            msg -> {
                                SponsorshipDeal deal = new SponsorshipDeal(
                                    "D" + System.currentTimeMillis(), currentSponsorID, ev.getEventID(),
                                    msg, java.time.LocalDate.now(), "Pending", "");
                                sponsorshipService.submitProposal(deal);
                                populateSponsors();
                            });
                    });
                    actionCol.getChildren().add(sponsorBtn);
                }

                eventRow.getChildren().addAll(icon, info, actionCol);
                dealsList.getChildren().add(eventRow);
            }
        }
        dealsCard.getChildren().addAll(dealsTitle, dealsDesc, dealsList);

        //divider
        VBox proposalsCard = new VBox(6);
        proposalsCard.getStyleClass().add("admin-card");
        proposalsCard.setPadding(new Insets(20));
        Label proposalsTitle = new Label("📊 Your Proposals (" + myDeals.size() + ")");
        proposalsTitle.getStyleClass().add("admin-card-title");
        Label proposalsDesc = new Label("Track the status of your submitted sponsorship proposals.");
        proposalsDesc.getStyleClass().add("admin-card-desc");
        proposalsDesc.setWrapText(true);

        VBox proposalsList = new VBox(10);
        if (myDeals.isEmpty()) {
            Label noProps = new Label("You haven't submitted any proposals yet.");
            noProps.setStyle("-fx-text-fill: #9aa0a6; -fx-padding: 12;");
            proposalsList.getChildren().add(noProps);
        } else {
            for (SponsorshipDeal deal : myDeals) {
                VBox propCard = new VBox(0);
                propCard.getStyleClass().add("soc-event-card");

                HBox propRow = new HBox(14);
                propRow.setPadding(new Insets(14, 16, 14, 16));
                propRow.setAlignment(Pos.CENTER_LEFT);

                //icon
                StackPane propIcon = new StackPane();
                propIcon.setMinWidth(40); propIcon.setMinHeight(40);
                propIcon.setMaxWidth(40); propIcon.setMaxHeight(40);
                String pIconBg = deal.isPending() ? "#fef7e0" : deal.isAccepted() ? "#e6f4ea" : "#fce8e6";
                propIcon.setStyle("-fx-background-color: " + pIconBg + "; -fx-background-radius: 10;");
                String pEmoji = deal.isPending() ? "⏳" : deal.isAccepted() ? "✅" : "❌";
                Label pIconLabel = new Label(pEmoji);
                pIconLabel.setStyle("-fx-font-size: 18px;");
                propIcon.getChildren().add(pIconLabel);

                //info fetch event
                VBox propInfo = new VBox(3);
                HBox.setHgrow(propInfo, Priority.ALWAYS);

                String eventTitle = deal.getEventID();
                try (Statement st = DBConnection.getInstance().getConnection().createStatement()) {
                    ResultSet rs = st.executeQuery("SELECT title FROM Event WHERE eventID='" + deal.getEventID() + "'");
                    if (rs.next()) eventTitle = rs.getString("title");
                } catch (SQLException ignored) {}

                Label pTitle = new Label(eventTitle);
                pTitle.getStyleClass().add("feed-event-title");
                Label pMsg = new Label("\"" + deal.getProposalMessage() + "\"");
                pMsg.setStyle("-fx-text-fill: #5f6368; -fx-font-size: 12; -fx-font-style: italic;");
                pMsg.setWrapText(true);
                Label pDate = new Label("Submitted: " + deal.getDateSubmitted().format(DateTimeFormatter.ofPattern("MMM d, yyyy")));
                pDate.setStyle("-fx-font-size: 11; -fx-text-fill: #9aa0a6;");
                propInfo.getChildren().addAll(pTitle, pMsg, pDate);

                //status badge
                Label statusBadge = new Label(deal.getStatus());
                statusBadge.getStyleClass().add(deal.getStatusBadgeStyle());

                propRow.getChildren().addAll(propIcon, propInfo, statusBadge);
                propCard.getChildren().add(propRow);

                //expandable stats dropdown
                if (deal.isAccepted()) {
                    VBox statsDropdown = new VBox(8);
                    statsDropdown.setPadding(new Insets(0, 16, 14, 70));
                    statsDropdown.setVisible(false);
                    statsDropdown.setManaged(false);

                    //fetch event details
                    try (Statement st = DBConnection.getInstance().getConnection().createStatement()) {
                        ResultSet rs = st.executeQuery("SELECT * FROM Event WHERE eventID='" + deal.getEventID() + "'");
                        if (rs.next()) {
                            int regCount = registrationService.countConfirmedRegistrations(deal.getEventID());
                            double fee = rs.getDouble("registrationFee");
                            double totalRevenue = regCount * fee;
                            int maxSeats = rs.getInt("maxSeats");
                            String evDate = rs.getDate("date").toLocalDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));
                            String evDeadline = rs.getDate("regDeadline").toLocalDate().format(DateTimeFormatter.ofPattern("MMM d, yyyy"));

                            //build stat boxes
                            GridPane statsGrid = new GridPane();
                            statsGrid.setHgap(10);
                            statsGrid.setVgap(10);
                            statsGrid.add(createStatBox(String.valueOf(regCount), "Students Registered", "#e8f0fe", "#1a73e8"), 0, 0);
                            statsGrid.add(createStatBox(maxSeats - regCount + " left", "Seats Remaining", "#e6f4ea", "#34a853"), 1, 0);
                            statsGrid.add(createStatBox(fee == 0 ? "Free" : "PKR " + String.format("%.0f", totalRevenue), "Est. Revenue", "#fef7e0", "#b06000"), 0, 1);
                            statsGrid.add(createStatBox(evDate, "Event Date", "#fce8e6", "#ea4335"), 1, 1);
                            ColumnConstraints col50 = new ColumnConstraints();
                            col50.setHgrow(Priority.ALWAYS);
                            col50.setPercentWidth(50);
                            statsGrid.getColumnConstraints().addAll(col50, col50);

                            Label deadlineInfo = new Label("📅 Registration Deadline: " + evDeadline);
                            deadlineInfo.setStyle("-fx-font-size: 12; -fx-text-fill: #5f6368;");

                            statsDropdown.getChildren().addAll(statsGrid, deadlineInfo);
                        }
                    } catch (SQLException ignored) {}

                    //toggle
                    Button toggleBtn = new Button("▼ View Event Stats");
                    toggleBtn.getStyleClass().add("soc-toggle-btn");
                    toggleBtn.setOnAction(e -> {
                        boolean show = !statsDropdown.isVisible();
                        statsDropdown.setVisible(show);
                        statsDropdown.setManaged(show);
                        toggleBtn.setText(show ? "▲ Hide Event Stats" : "▼ View Event Stats");
                    });

                    HBox toggleRow = new HBox();
                    toggleRow.setPadding(new Insets(0, 16, 8, 70));
                    toggleRow.getChildren().add(toggleBtn);

                    propCard.getChildren().addAll(toggleRow, statsDropdown);
                }

                proposalsList.getChildren().add(propCard);
            }
        }
        proposalsCard.getChildren().addAll(proposalsTitle, proposalsDesc, proposalsList);

        sponsorsGrid.getChildren().clear();
        sponsorsGrid.add(dealsCard, 0, 0);
        sponsorsGrid.add(proposalsCard, 0, 1);
        GridPane.setColumnSpan(dealsCard, 2);
        GridPane.setColumnSpan(proposalsCard, 2);
    }

    //divider
    //admin
    //divider
    private void populateAdmin() {
        VBox pendingCard = new VBox(6);
        pendingCard.getStyleClass().add("admin-card");
        pendingCard.setPadding(new Insets(20));
        Label pendingTitle = new Label("⏳ Pending Event Approvals");
        pendingTitle.getStyleClass().add("admin-card-title");
        Label pendingDesc = new Label("Events submitted by societies awaiting admin approval.");
        pendingDesc.getStyleClass().add("admin-card-desc");
        pendingDesc.setWrapText(true);

        VBox pendingList = new VBox(8);
        com.universe.services.EventService eventService = new com.universe.services.EventService();
        java.util.List<com.universe.models.Event> pendingEvents = eventService.getPendingEvents();
        for (com.universe.models.Event ev : pendingEvents) {
            pendingList.getChildren().add(createPendingItem(ev.getEventID(), ev.getTitle(), ev.getSocietyID(), "#fbbc04", "✓ Approve", true, "Event"));
        }
        pendingCard.getChildren().addAll(pendingTitle, pendingDesc, pendingList);

        VBox statsCard = new VBox(6);
        statsCard.getStyleClass().add("admin-card");
        statsCard.setPadding(new Insets(20));
        Label statsTitle = new Label("📊 Platform Stats");
        statsTitle.getStyleClass().add("admin-card-title");
        Label statsDesc = new Label("Live overview of platform activity this semester.");
        statsDesc.getStyleClass().add("admin-card-desc");
        statsDesc.setWrapText(true);

        GridPane statsBoxes = new GridPane();
        statsBoxes.setHgap(10);
        statsBoxes.setVgap(10);
        VBox.setMargin(statsBoxes, new Insets(8, 0, 0, 0));
        statsBoxes.getColumnConstraints().addAll(
                new ColumnConstraints() {
                    {
                        setHgrow(Priority.ALWAYS);
                        setPercentWidth(50);
                    }
                },
                new ColumnConstraints() {
                    {
                        setHgrow(Priority.ALWAYS);
                        setPercentWidth(50);
                    }
                });

        int totalEvents = eventService.getAllEvents().size();
        int activeSocieties = new com.universe.services.SocietyService().getActiveSocieties().size();
        int totalStudents = 0;
        int activeSponsors = 0;
        try (java.sql.Statement stmt = com.universe.db.DBConnection.getInstance().getConnection().createStatement()) {
            java.sql.ResultSet rsStudent = stmt.executeQuery("SELECT COUNT(*) FROM Student");
            if (rsStudent.next()) totalStudents = rsStudent.getInt(1);
        } catch (java.sql.SQLException e) { }
        try (java.sql.Statement stmt = com.universe.db.DBConnection.getInstance().getConnection().createStatement()) {
            java.sql.ResultSet rsSponsor = stmt.executeQuery("SELECT COUNT(*) FROM Sponsor");
            if (rsSponsor.next()) activeSponsors = rsSponsor.getInt(1);
        } catch (java.sql.SQLException e) { }

        statsBoxes.add(createStatBox(String.valueOf(totalEvents), "Total Events", "#e8f0fe", "#1a73e8"), 0, 0);
        statsBoxes.add(createStatBox(String.valueOf(totalStudents), "Total Students", "#e6f4ea", "#34a853"), 1, 0);
        statsBoxes.add(createStatBox(String.valueOf(activeSocieties), "Active Societies", "#fce8e6", "#ea4335"), 0, 1);
        statsBoxes.add(createStatBox(String.valueOf(activeSponsors), "Active Sponsors", "#fef7e0", "#b06000"), 1, 1);

        statsCard.getChildren().addAll(statsTitle, statsDesc, statsBoxes);

        VBox activityCard = new VBox(6);
        activityCard.getStyleClass().add("admin-card");
        activityCard.setPadding(new Insets(20));
        Label activityTitle = new Label("🔔 Recent Activity");
        activityTitle.getStyleClass().add("admin-card-title");
        Label activityDesc = new Label("Latest actions taken on the platform.");
        activityDesc.getStyleClass().add("admin-card-desc");
        activityDesc.setWrapText(true);

        VBox activityList = new VBox(8);
        try (java.sql.Statement stmt = com.universe.db.DBConnection.getInstance().getConnection().createStatement()) {
            java.sql.ResultSet rs = stmt.executeQuery("SELECT TOP 5 message FROM Notification WHERE recipientType = 'ADMIN' ORDER BY date DESC");
            while (rs.next()) {
                activityList.getChildren().add(createActivityItem(rs.getString("message"), "#1a73e8"));
            }
        } catch (java.sql.SQLException e) { 
            e.printStackTrace(); 
        }
        activityCard.getChildren().addAll(activityTitle, activityDesc, activityList);

        adminGrid.getChildren().clear();
        adminGrid.add(pendingCard, 0, 0);
        GridPane.setColumnSpan(pendingCard, 2);
        adminGrid.add(statsCard, 0, 1);
        adminGrid.add(activityCard, 1, 1);
    }

    private HBox createPendingItem(String id, String name, String secondary, String dotColor,
                                   String btnText, boolean hasReject, String type) {
        HBox item = new HBox(10);
        item.getStyleClass().add("pending-item");
        item.setPadding(new Insets(8, 10, 8, 10));
        item.setAlignment(Pos.CENTER_LEFT);

        Circle dot;
        try {
            dot = new Circle(4, Color.web(dotColor));
        } catch (Exception e) {
            dot = new Circle(4, Color.GRAY);
            System.out.println("Skipped invalid color: " + dotColor);
        }
        Label nameLabel = new Label(name);
        nameLabel.getStyleClass().add("pending-item-name");
        Label secLabel = new Label(secondary);
        secLabel.getStyleClass().add("pending-item-sec");
        Region sp = new Region();
        HBox.setHgrow(sp, Priority.ALWAYS);

        HBox actions = new HBox(6);
        Button approveBtn = new Button(btnText);
        approveBtn.getStyleClass().add("btn-approve");
        approveBtn.setOnAction(e -> {
            if ("Society".equals(type)) {
                new com.universe.services.SocietyService().approveSociety(id);
            } else if ("Event".equals(type)) {
                new com.universe.services.EventService().updateEventStatus(id, "Approved");
            }
            VBox parent = (VBox) item.getParent();
            if (parent != null) {
                FadeTransition ft = new FadeTransition(Duration.millis(200), item);
                ft.setToValue(0);
                ft.setOnFinished(ev -> parent.getChildren().remove(item));
                ft.play();
            }
        });
        actions.getChildren().add(approveBtn);

        if (hasReject) {
            Button rejectBtn = new Button("✗");
            rejectBtn.getStyleClass().add("btn-reject");
            rejectBtn.setOnAction(e -> {
                if ("Society".equals(type)) {
                    new com.universe.services.SocietyService().suspendSociety(id);
                } else if ("Event".equals(type)) {
                    new com.universe.services.EventService().updateEventStatus(id, "Rejected");
                }
                VBox parent = (VBox) item.getParent();
                if (parent != null) {
                    FadeTransition ft = new FadeTransition(Duration.millis(200), item);
                    ft.setToValue(0);
                    ft.setOnFinished(ev -> parent.getChildren().remove(item));
                    ft.play();
                }
            });
            actions.getChildren().add(rejectBtn);
        }

        item.getChildren().addAll(dot, nameLabel, secLabel, sp, actions);
        return item;
    }

    private VBox createStatBox(String value, String label, String bgColor, String textColor) {
        VBox box = new VBox(2);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(10));
        box.setStyle("-fx-background-color: " + bgColor + "; -fx-background-radius: 8;");
        Label valLabel = new Label(value);
        valLabel.setStyle("-fx-font-size: 22; -fx-font-weight: 500; -fx-text-fill: " + textColor + ";");
        Label lblLabel = new Label(label);
        lblLabel.setStyle("-fx-font-size: 11; -fx-text-fill: #5f6368;");
        box.getChildren().addAll(valLabel, lblLabel);
        return box;
    }

    private HBox createActivityItem(String text, String dotColor) {
        HBox item = new HBox(8);
        item.setAlignment(Pos.TOP_LEFT);
        Label bullet = new Label("•");
        bullet.setStyle("-fx-text-fill: " + dotColor + "; -fx-font-size: 14;");
        Label content = new Label(text);
        content.setStyle("-fx-font-size: 13; -fx-text-fill: #5f6368;");
        content.setWrapText(true);
        item.getChildren().addAll(bullet, content);
        return item;
    }

    private void populateStudentRegistrations(String studentID) {
        studentRegistrationsBox.getChildren().clear();
        studentRegistrationsBox.setManaged(true);
        studentRegistrationsBox.setVisible(true);

        Label title = new Label("📋 My Registrations");
        title.getStyleClass().add("mini-cal-title");
        title.setPadding(new Insets(0, 0, 8, 0));
        studentRegistrationsBox.getChildren().add(title);

        List<String[]> regs = new RegistrationService().getRegistrationsWithEventTitles(studentID);
        if (regs.isEmpty()) {
            Label noRegs = new Label("You haven't registered for any events yet.");
            noRegs.setStyle("-fx-text-fill: #9aa0a6; -fx-font-size: 12;");
            noRegs.setWrapText(true);
            studentRegistrationsBox.getChildren().add(noRegs);
        } else {
            VBox list = new VBox(8);
            for (String[] reg : regs) {
                HBox row = new HBox(6);
                row.setAlignment(Pos.CENTER_LEFT);
                String dotColor = "Confirmed".equals(reg[3]) ? "#34a853" : "Pending".equals(reg[3]) ? "#fbbc04" : "#ea4335";
                Circle dot = new Circle(4, Color.web(dotColor));
                
                VBox info = new VBox(2);
                Label evTitle = new Label(reg[1]);
                evTitle.setStyle("-fx-font-size: 12; -fx-font-weight: bold; -fx-text-fill: #202124;");
                Label evDate = new Label(reg[2]);
                evDate.setStyle("-fx-font-size: 10; -fx-text-fill: #5f6368;");
                info.getChildren().addAll(evTitle, evDate);
                
                row.getChildren().addAll(dot, info);
                list.getChildren().add(row);
            }
            studentRegistrationsBox.getChildren().add(list);
        }
    }
}
