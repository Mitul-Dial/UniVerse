package com.universe.controllers;

import com.universe.Main;
import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.geometry.Bounds;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.BlendMode;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.RadialGradient;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.util.Duration;
import com.universe.models.*;
import com.universe.services.UserService;
import javafx.scene.control.TextField;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.GridPane;
import javafx.geometry.Insets;

import java.util.*;

public class LandingController {
    @FXML private StackPane landingStackRoot;
    @FXML private ScrollPane landingScroll;
    @FXML private VBox landingRoot;
    @FXML private Pane blobPane;

    @FXML private StackPane heroIntroContainer;
    @FXML private Canvas heroIntroCanvas;
    @FXML private HBox heroTitleBox;
    @FXML private Label heroL0, heroL1, heroL2, heroL3, heroL4, heroL5, heroL6, heroL7;
    @FXML private Label heroSub;
    @FXML private VBox heroSection;
    @FXML private VBox statsSection;

    private double mouseX = -500, mouseY = -500;
    @FXML private Region beamBar;
    @FXML private VBox stat0, stat1, stat2, stat3;
    @FXML private VBox featuresSection;
    @FXML private VBox featureCard0, featureCard1, featureCard2;
    @FXML private VBox howSection;
    @FXML private VBox step0, step1, step2, step3;
    @FXML private StackPane stepCircle0, stepCircle1, stepCircle2, stepCircle3;
    @FXML private VBox ctaSection;
    @FXML private VBox footerSection;

    @FXML private Button btnSignIn, btnGetStarted, navFeatures, navHow, navAbout;

    private final Set<Node> animated = new HashSet<>();
    private final Set<Node> revealAnimated = new HashSet<>();
    private final List<FloatingLetter> floatingLetters = new ArrayList<>();

    @FXML
    public void initialize() {
        initHeroIntro();
        initBackgroundBlobs();
        initFeatureCardHover();
        initButtonHover();
        initStepOrbitalRings();
        wireNavLinks();
        registerCursorInteractions();
        landingScroll.vvalueProperty().addListener((obs, oldVal, newVal) -> checkScrollReveals());
        landingScroll.layoutBoundsProperty().addListener((obs, oldVal, newVal) -> checkScrollReveals());

        landingStackRoot.setOnMouseMoved(e -> {
            mouseX = e.getX();
            mouseY = e.getY();
        });

        refreshStatistics();
    }

    @FXML
    public void handleOpenDashboard() {
        TextField emailField = ThemedDialog.styledTextField("Email address");
        PasswordField passField = ThemedDialog.styledPasswordField("Password");

        VBox form = new VBox(10);
        form.getChildren().addAll(
            formLabel("Email"), emailField,
            formLabel("Password"), passField
        );

        ThemedDialog.showFormDialog(landingStackRoot, "Welcome back", "Sign in to your UniVerse account",
            form, "Sign In", "#1a73e8",
            () -> {
                String email = emailField.getText().trim();
                String password = passField.getText().trim();
                if (email.isEmpty() || password.isEmpty()) {
                    ThemedDialog.showToast(landingStackRoot, "Error", "Email and password are needed!", ThemedDialog.ToastType.ERROR);
                    return;
                }
                UserService userService = new UserService();
                User loggedInUser = userService.login(email, password);
                if (loggedInUser != null) {
                    System.out.println("Login successful: " + loggedInUser.getName());
                    ThemedDialog.dismiss(emailField);
                    Session.getInstance().setLoggedInUser(loggedInUser);
                    Main.showDashboard();
                } else {
                    ThemedDialog.showToast(landingStackRoot, "Login Failed", "Wrong email or password!\nTry again.", ThemedDialog.ToastType.ERROR);
                }
            }, null);
    }

    @FXML
    public void handleGetStarted() {
        List<String> roles = Arrays.asList("Student", "Society", "Sponsor");
        ThemedDialog.showRoleChooser(landingStackRoot, roles, role -> showRegistrationForm(role));
    }

    private void showRegistrationForm(String role) {
        TextField nameField = ThemedDialog.styledTextField("Full name");
        TextField emailField = ThemedDialog.styledTextField("Email address");
        PasswordField passField = ThemedDialog.styledPasswordField("Password");

        TextField extra1Field = ThemedDialog.styledTextField(
            role.equals("Student") ? "Registration Number" :
            role.equals("Society") ? "Description" : "Organization Name"
        );
        TextField extra2Field = ThemedDialog.styledTextField("Contact Info");

        VBox form = new VBox(10);
        form.getChildren().addAll(
            formLabel("Name"), nameField,
            formLabel("Email"), emailField,
            formLabel("Password"), passField,
            formLabel(role.equals("Student") ? "Reg Number" :
                      role.equals("Society") ? "Description" : "Organization"), extra1Field
        );
        if (role.equals("Society") || role.equals("Sponsor")) {
            form.getChildren().addAll(formLabel("Contact Info"), extra2Field);
        }

        String accent = role.equals("Student") ? "#1a73e8" :
                        role.equals("Society") ? "#34a853" : "#fbbc04";

        ThemedDialog.showFormDialog(landingStackRoot, "Create " + role + " Account",
            "Fill in your details to join UniVerse", form, "Create Account", accent,
            () -> {
                String name = nameField.getText().trim();
                String email = emailField.getText().trim();
                String pass = passField.getText().trim();
                String extra1 = extra1Field.getText().trim();
                String extra2 = extra2Field.getText().trim();

                if (name.isEmpty() || email.isEmpty() || pass.isEmpty() || extra1.isEmpty()) {
                    ThemedDialog.showToast(landingStackRoot, "Error", "All required fields must be filled!", ThemedDialog.ToastType.ERROR);
                    return;
                }

                UserService userService = new UserService();
                if (userService.emailExists(email)) {
                    ThemedDialog.showToast(landingStackRoot, "Error", "Email is already registered!", ThemedDialog.ToastType.ERROR);
                    return;
                }

                ThemedDialog.dismiss(nameField);

                String userID = role.substring(0, 1).toUpperCase() + System.currentTimeMillis();
                if (role.equals("Student")) {
                    Student student = new Student(userID, name, email, pass, "U1", "D001");
                    userService.registerStudent(student);
                } else if (role.equals("Society")) {
                    Society society = new Society(userID, name, extra1, email, extra2, pass, "Pending", "U1");
                    new com.universe.services.SocietyService().registerSociety(society);
                } else if (role.equals("Sponsor")) {
                    Sponsor sponsor = new Sponsor(userID, name, extra1, email, extra2, pass);
                    userService.registerSponsor(sponsor);
                }

                ThemedDialog.showToast(landingStackRoot, "Registration Successful",
                    "Account created successfully! Please sign in.", ThemedDialog.ToastType.SUCCESS);
            }, null);
    }

    private Label formLabel(String text) {
        Label lbl = new Label(text);
        lbl.setStyle("-fx-font-size: 13; -fx-font-weight: 600; -fx-text-fill: #5f6368; -fx-font-family: 'Segoe UI';");
        return lbl;
    }

    private void initHeroIntro() {
        heroIntroCanvas.widthProperty().bind(heroIntroContainer.widthProperty());
        heroIntroCanvas.heightProperty().bind(heroIntroContainer.heightProperty());
        heroSub.setOpacity(0);
        heroSub.setTranslateY(24);

        Label[] labels = { heroL0, heroL1, heroL2, heroL3, heroL4, heroL5, heroL6, heroL7 };
        Color[] colors = { Color.web("#ea4335"), Color.web("#1a73e8"), Color.web("#fbbc04"), Color.web("#34a853"),
                Color.web("#ea4335"), Color.web("#4285f4"), Color.web("#fbbc04"), Color.web("#34a853") };
        for (int i = 0; i < labels.length; i++) {
            floatingLetters.add(new FloatingLetter(labels[i].getText(), colors[i], i));
        }

        final int phase1 = 160, phase2 = 90, phase3 = 40, phase4 = 60;
        final int total = phase1 + phase2 + phase3 + phase4;
        GraphicsContext g = heroIntroCanvas.getGraphicsContext2D();
        new AnimationTimer() {
            int frame = 0;
            @Override public void handle(long now) {
                double w = heroIntroCanvas.getWidth();
                double h = heroIntroCanvas.getHeight();
                double cx = w / 2.0, cy = h / 2.0;
                g.clearRect(0, 0, w, h);
                if (frame <= total) {
                    drawAtom(g, frame, phase1, phase2, phase3, cx, cy);
                }
                if (frame < total) {
                    frame++;
                } else {
                    stop();
                }
            }

            private void drawAtom(GraphicsContext g, int f, int p1, int p2, int p3, double cx, double cy) {
                g.setFill(new RadialGradient(0, 0, cx, cy, 65, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(115, 205, 255, 0.95)), new Stop(1, Color.rgb(26, 115, 232, 0.08))));
                g.fillOval(cx - 70, cy - 70, 140, 140);
                for (int i = 0; i < 8; i++) {
                    double rot = (f * (0.8 + i * 0.12)) % 360;
                    g.save();
                    g.translate(cx, cy);
                    g.rotate((i * 22.5) + rot * 0.3);
                    g.setStroke(Color.rgb(26, 115, 232, 0.35));
                    g.strokeOval(-110 - i * 3, -34 - i, (220 + i * 6), (68 + i * 3));
                    g.restore();
                }

                for (FloatingLetter fl : floatingLetters) {
                    fl.update(f, p1, p2, p3, cx, cy);
                    double letterSize = f < p1 ? 42 : (f < p1 + p2 ? 42 + ((f - p1) / (double) p2) * 70 : 112);
                    g.setFont(Font.font("Segoe UI", FontWeight.EXTRA_BOLD, letterSize));
                    g.setFill(Color.rgb(0, 0, 0, 0.22));
                    g.fillText(fl.letter, fl.x + 4, fl.y + 5);
                    g.setFill(fl.color);
                    g.fillText(fl.letter, fl.x, fl.y);
                    g.setStroke(new Color(1, 1, 1, 0.25));
                    g.setLineWidth(0.8);
                    g.strokeText(fl.letter, fl.x, fl.y);
                    if (f > p1 && f < p1 + p2 + p3) {
                        //lighter softer magnet
                        g.setStroke(fl.color.deriveColor(0, 0.8, 1.3, 0.5));
                        g.setLineWidth(1.8);
                        drawBolt(g, cx, cy, fl.x, fl.y);
                    }
                }
                if (f >= p1 + p2 + p3) {
                    double alpha = 1.0 - Math.min(1.0, (f - (p1 + p2 + p3)) / 60.0);
                    heroIntroCanvas.setOpacity(alpha);
                    if (alpha <= 0.05) {
                        heroTitleBox.setOpacity(1);
                        animateSubIn();
                    }
                }
            }
        }.start();
    }

    private void animateSubIn() {
        if (heroSub.getOpacity() > 0.1) return;
        FadeTransition fadeIn = new FadeTransition(Duration.millis(600), heroSub);
        fadeIn.setFromValue(0);
        fadeIn.setToValue(1);
        fadeIn.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition moveUp = new TranslateTransition(Duration.millis(600), heroSub);
        moveUp.setFromY(24);
        moveUp.setToY(0);
        moveUp.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(fadeIn, moveUp).play();
    }

    private void drawBolt(GraphicsContext g, double sx, double sy, double ex, double ey) {
        g.beginPath();
        g.moveTo(sx, sy);
        for (int i = 1; i <= 4; i++) {
            double t = i / 5.0;
            double x = sx + (ex - sx) * t + (Math.random() - 0.5) * 12;
            double y = sy + (ey - sy) * t + (Math.random() - 0.5) * 12;
            g.lineTo(x, y);
        }
        g.lineTo(ex, ey);
        g.stroke();
    }

    //divider
    //divider
    //animation starry background
    //divider
    private static final int NODE_COUNT = 55;
    private double[] nx, ny, nvx, nvy;
    private static final double MAX_DIST = 160;
    private double elapsedNodes = 0;

    private void initBackgroundBlobs() {
        blobPane.getChildren().clear();

        Canvas nodeCanvas = new Canvas();
        nodeCanvas.widthProperty().bind(blobPane.widthProperty());
        nodeCanvas.heightProperty().bind(blobPane.heightProperty());
        blobPane.getChildren().add(nodeCanvas);
        
        GraphicsContext gc = nodeCanvas.getGraphicsContext2D();

        nx  = new double[NODE_COUNT];
        ny  = new double[NODE_COUNT];
        nvx = new double[NODE_COUNT];
        nvy = new double[NODE_COUNT];
        Random rng = new Random(42);
        
        double spawnW = 1280, spawnH = 720;
        for (int i = 0; i < NODE_COUNT; i++) {
            nx[i]  = rng.nextDouble() * spawnW;
            ny[i]  = rng.nextDouble() * spawnH;
            double speed = 10 + rng.nextDouble() * 20;
            double angle = rng.nextDouble() * Math.PI * 2;
            nvx[i] = Math.cos(angle) * speed;
            nvy[i] = Math.sin(angle) * speed;
        }

        final long[] lastNs = {-1};
        new AnimationTimer() {
            @Override public void handle(long now) {
                if (lastNs[0] < 0) { lastNs[0] = now; return; }
                double dt = Math.min((now - lastNs[0]) / 1_000_000_000.0, 0.05);
                lastNs[0] = now;
                elapsedNodes += dt;
                
                double w = nodeCanvas.getWidth();
                double h = nodeCanvas.getHeight();
                if (w == 0 || h == 0) return;

                //update
                for (int i = 0; i < NODE_COUNT; i++) {
                    nx[i] += nvx[i] * dt;
                    ny[i] += nvy[i] * dt;
                    if (nx[i] < 0) { nx[i] = 0; nvx[i] =  Math.abs(nvx[i]); }
                    if (nx[i] > w) { nx[i] = w; nvx[i] = -Math.abs(nvx[i]); }
                    if (ny[i] < 0) { ny[i] = 0; nvy[i] =  Math.abs(nvy[i]); }
                    if (ny[i] > h) { ny[i] = h; nvy[i] = -Math.abs(nvy[i]); }
                }

                //render background gradient
                gc.clearRect(0, 0, w, h);
                gc.setFill(new LinearGradient(0, 0, 1, 1, true, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.web("#080f25")),
                        new Stop(0.5, Color.web("#050a18")),
                        new Stop(1, Color.web("#02040c"))));
                gc.fillRect(0, 0, w, h);

                //torch glow
                gc.setGlobalBlendMode(BlendMode.ADD);
                gc.setFill(new RadialGradient(0, 0, mouseX, mouseY, 350, false, CycleMethod.NO_CYCLE,
                        new Stop(0, Color.rgb(26, 115, 232, 0.15)),
                        new Stop(1, Color.TRANSPARENT)));
                gc.fillRect(0, 0, w, h);
                gc.setGlobalBlendMode(BlendMode.SRC_OVER);

                //render lines
                for (int i = 0; i < NODE_COUNT; i++) {
                    for (int j = i + 1; j < NODE_COUNT; j++) {
                        double dx = nx[j] - nx[i], dy = ny[j] - ny[i];
                        double dist = Math.sqrt(dx*dx + dy*dy);
                        if (dist < MAX_DIST) {
                            double alpha = (1.0 - dist / MAX_DIST) * 0.35;
                            gc.setStroke(Color.color(0.50, 0.70, 1.0, alpha));
                            gc.setLineWidth(0.8);
                            gc.strokeLine(nx[i], ny[i], nx[j], ny[j]);
                        }
                    }
                }
                
                //render nodes
                for (int i = 0; i < NODE_COUNT; i++) {
                    double pulse = 0.6 + 0.4 * Math.sin(elapsedNodes * 1.5 + i * 0.7);
                    double r = 2.5 + pulse * 1.2;
                    gc.setFill(Color.color(0.55, 0.78, 1.0, 0.85 * pulse));
                    gc.fillOval(nx[i] - r, ny[i] - r, r * 2, r * 2);
                    gc.setFill(Color.color(0.75, 0.90, 1.0, 0.45 * pulse));
                    gc.fillOval(nx[i] - r*2, ny[i] - r*2, r*4, r*4);
                }
            }
        }.start();
    }

    //divider
    //animation scroll triggered
    //divider
    private void checkScrollReveals() {
        Bounds scrollBounds = landingScroll.localToScene(landingScroll.getBoundsInLocal());
        if (scrollBounds == null)
            return;

        double viewTop = scrollBounds.getMinY();
        double viewBottom = scrollBounds.getMaxY();

        //stats section
        //stats section
        if (!animated.contains(statsSection) && isNodeVisible(statsSection, viewTop, viewBottom)) {
            animated.add(statsSection);
            animateBeamAndStats();
        }

        //features section
        if (!animated.contains(featuresSection) && isNodeVisible(featuresSection, viewTop, viewBottom)) {
            animated.add(featuresSection);
            animateFeaturesSection();
        }

        //how it works
        if (!animated.contains(howSection) && isNodeVisible(howSection, viewTop, viewBottom)) {
            animated.add(howSection);
            animateReveal(howSection, 24, 500);
            animateSteps();
        }

        //cta section
        if (!animated.contains(ctaSection) && isNodeVisible(ctaSection, viewTop, viewBottom)) {
            animated.add(ctaSection);
            animateReveal(ctaSection, 24, 500);
        }

        if (!animated.contains(footerSection) && isNodeVisible(footerSection, viewTop, viewBottom)) {
            animated.add(footerSection);
            animateReveal(footerSection, 24, 400);
        }

        for (Node node : landingRoot.lookupAll(".reveal")) {
            if (!revealAnimated.contains(node) && isNodeVisible(node, viewTop, viewBottom)) {
                revealAnimated.add(node);
                animateReveal(node, 24, 500);
            }
        }
    }

    private boolean isNodeVisible(Node node, double viewTop, double viewBottom) {
        try {
            Bounds nodeBounds = node.localToScene(node.getBoundsInLocal());
            if (nodeBounds == null)
                return false;
            return nodeBounds.getMinY() < viewBottom && nodeBounds.getMaxY() > viewTop;
        } catch (Exception e) {
            return false;
        }
    }

    private void animateReveal(Node node, double fromY, double durationMs) {
        FadeTransition ft = new FadeTransition(Duration.millis(durationMs), node);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.setInterpolator(Interpolator.EASE_OUT);

        TranslateTransition tt = new TranslateTransition(Duration.millis(durationMs), node);
        tt.setFromY(fromY);
        tt.setToY(0);
        tt.setInterpolator(Interpolator.EASE_OUT);

        new ParallelTransition(ft, tt).play();
    }

    //stats beam then
    private void animateBeamAndStats() {
        //animate the beam
        beamBar.setScaleX(0);
        ScaleTransition beamScale = new ScaleTransition(Duration.millis(1800), beamBar);
        beamScale.setFromX(0);
        beamScale.setToX(1);
        beamScale.setInterpolator(Interpolator.EASE_OUT);
        beamScale.play();

        //after ms stagger
        VBox[] stats = { stat0, stat1, stat2, stat3 };
        PauseTransition delay = new PauseTransition(Duration.millis(1400));
        delay.setOnFinished(e -> {
            for (int i = 0; i < stats.length; i++) {
                VBox stat = stats[i];
                PauseTransition stagger = new PauseTransition(Duration.millis(i * 120));
                stagger.setOnFinished(ev -> {
                    FadeTransition ft = new FadeTransition(Duration.millis(600), stat);
                    ft.setFromValue(0);
                    ft.setToValue(1);
                    ft.setInterpolator(Interpolator.EASE_OUT);

                    TranslateTransition tt = new TranslateTransition(Duration.millis(600), stat);
                    tt.setFromY(30);
                    tt.setToY(0);
                    tt.setInterpolator(Interpolator.EASE_OUT);

                    new ParallelTransition(ft, tt).play();
                });
                stagger.play();
            }
        });
        delay.play();
    }

    //feature section cards
    private void animateFeaturesSection() {
        animateReveal(featuresSection, 24, 500);
        VBox[] cards = { featureCard0, featureCard1, featureCard2 };
        double[] yOffsets = { 0, 18, 36 };
        for (int i = 0; i < cards.length; i++) {
            cards[i].setTranslateX(-160);
            cards[i].setTranslateY(yOffsets[i]);
            cards[i].setScaleX(0.84);
            cards[i].setScaleY(0.84);
            cards[i].setOpacity(0);
        }
        for (int i = 0; i < cards.length; i++) {
            VBox card = cards[i];
            double startY = yOffsets[i];
            PauseTransition stagger = new PauseTransition(Duration.millis(i * 280));
            stagger.setOnFinished(ev -> {
                Timeline tl = new Timeline(
                        new KeyFrame(Duration.ZERO,
                                new KeyValue(card.translateXProperty(), -160),
                                new KeyValue(card.translateYProperty(), startY),
                                new KeyValue(card.scaleXProperty(), 0.84),
                                new KeyValue(card.scaleYProperty(), 0.84),
                                new KeyValue(card.opacityProperty(), 0)),
                        new KeyFrame(Duration.millis(1100),
                                new KeyValue(card.translateXProperty(), 0, Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0)),
                                new KeyValue(card.translateYProperty(), 0, Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0)),
                                new KeyValue(card.scaleXProperty(), 1.0, Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0)),
                                new KeyValue(card.scaleYProperty(), 1.0, Interpolator.SPLINE(0.22, 1.0, 0.36, 1.0)),
                                new KeyValue(card.opacityProperty(), 1, Interpolator.EASE_OUT)));
                tl.play();
            });
            stagger.play();
        }
    }

    //steps with circle
    private void animateSteps() {
        VBox[] steps = { step0, step1, step2, step3 };
        StackPane[] circles = { stepCircle0, stepCircle1, stepCircle2, stepCircle3 };
        String[] colors = { "#1a73e8", "#ea4335", "#34a853", "#fbbc04" };

        for (int i = 0; i < steps.length; i++) {
            VBox step = steps[i];
            StackPane circle = circles[i];
            String color = colors[i];
            int idx = i;

            PauseTransition stagger = new PauseTransition(Duration.millis(i * 280));
            stagger.setOnFinished(ev -> {
                //scale circle in
                ScaleTransition sc = new ScaleTransition(Duration.millis(500), circle);
                sc.setFromX(0);
                sc.setFromY(0);
                sc.setToX(1);
                sc.setToY(1);
                sc.setInterpolator(Interpolator.EASE_OUT);
                sc.setOnFinished(ev2 -> {
                    addPulseRing(circle, color);
                    Point2D scenePoint = circle.localToScene(circle.getBoundsInLocal().getWidth() / 2,
                            circle.getBoundsInLocal().getHeight() / 2);
                    Main.burstParticles(scenePoint.getX(), scenePoint.getY(), 10);
                });

                //fade translate the
                FadeTransition ft = new FadeTransition(Duration.millis(500), step);
                ft.setFromValue(0);
                ft.setToValue(1);
                ft.setInterpolator(Interpolator.EASE_OUT);

                TranslateTransition tt = new TranslateTransition(Duration.millis(500), step);
                tt.setFromY(20);
                tt.setToY(0);
                tt.setInterpolator(Interpolator.EASE_OUT);

                new ParallelTransition(sc, ft, tt).play();
            });
            stagger.play();
        }
    }

    //divider
    //animation feature card
    //divider
    private void initFeatureCardHover() {
        VBox[] cards = { featureCard0, featureCard1, featureCard2 };
        Color[] glowColors = { Color.web("#1a73e8"), Color.web("#34a853"), Color.web("#ea4335") };

        for (int i = 0; i < cards.length; i++) {
            VBox card = cards[i];
            Color glow = glowColors[i];
            DropShadow ds = new DropShadow(14, glow);
            Timeline[] cycle = new Timeline[1];

            card.setOnMouseEntered(e -> {
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
                tt.setToY(-6);
                tt.play();

                ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                st.setToX(1.01);
                st.setToY(1.01);
                st.play();
                cycle[0] = new Timeline(
                        new KeyFrame(Duration.ZERO, new KeyValue(ds.colorProperty(), Color.web("#ea4335"))),
                        new KeyFrame(Duration.millis(260), new KeyValue(ds.colorProperty(), Color.web("#fbbc04"))),
                        new KeyFrame(Duration.millis(520), new KeyValue(ds.colorProperty(), Color.web("#34a853"))),
                        new KeyFrame(Duration.millis(780), new KeyValue(ds.colorProperty(), Color.web("#1a73e8"))));
                cycle[0].setCycleCount(Timeline.INDEFINITE);
                card.setEffect(ds);
                cycle[0].play();
            });

            card.setOnMouseExited(e -> {
                TranslateTransition tt = new TranslateTransition(Duration.millis(200), card);
                tt.setToY(0);
                tt.play();

                ScaleTransition st = new ScaleTransition(Duration.millis(200), card);
                st.setToX(1.0);
                st.setToY(1.0);
                st.play();
                if (cycle[0] != null) cycle[0].stop();
                card.setEffect(null);
            });
        }
    }

    //divider
    //animation button hover
    //divider
    private void initButtonHover() {
        if (btnGetStarted != null)
            addButtonHoverEffect(btnGetStarted);
        if (btnSignIn != null)
            addButtonHoverEffect(btnSignIn);
    }

    private void wireNavLinks() {
        navFeatures.setOnAction(e -> scrollToNode(featuresSection));
        navHow.setOnAction(e -> scrollToNode(howSection));
        navAbout.setOnAction(e -> scrollToNode(footerSection));
    }

    private void scrollToNode(Node target) {
        double nodeY = target.getBoundsInParent().getMinY();
        double contentHeight = landingRoot.getBoundsInLocal().getHeight();
        double scrollPaneHeight = landingScroll.getViewportBounds().getHeight();
        double vValue = nodeY / (contentHeight - scrollPaneHeight);
        Timeline tl = new Timeline(new KeyFrame(Duration.millis(600),
                new KeyValue(landingScroll.vvalueProperty(), Math.min(1.0, vValue), Interpolator.EASE_BOTH)));
        tl.play();
    }

    private void addButtonHoverEffect(Button btn) {
        btn.setOnMouseEntered(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), btn);
            tt.setToY(-1);
            tt.play();
        });
        btn.setOnMouseExited(e -> {
            TranslateTransition tt = new TranslateTransition(Duration.millis(150), btn);
            tt.setToY(0);
            tt.play();
        });
    }

    //divider
    //animation step circle
    //divider
    private void addPulseRing(StackPane circlePane, String color) {
        Circle pulseRing = new Circle(28);
        pulseRing.setFill(Color.TRANSPARENT);
        pulseRing.setStroke(Color.web(color));
        pulseRing.setStrokeWidth(2);
        pulseRing.setOpacity(0.4);
        pulseRing.setMouseTransparent(true);

        circlePane.getChildren().add(0, pulseRing);

        Timeline pulse = new Timeline(
                new KeyFrame(Duration.ZERO,
                        new KeyValue(pulseRing.opacityProperty(), 0.4, Interpolator.EASE_BOTH),
                        new KeyValue(pulseRing.radiusProperty(), 28.0, Interpolator.EASE_BOTH)),
                new KeyFrame(Duration.millis(2000),
                        new KeyValue(pulseRing.opacityProperty(), 0.0, Interpolator.EASE_BOTH),
                        new KeyValue(pulseRing.radiusProperty(), 36.0, Interpolator.EASE_BOTH)));
        pulse.setCycleCount(Timeline.INDEFINITE);
        pulse.setAutoReverse(true);
        pulse.play();
    }

    //divider
    //animation orbital rings
    //divider
    private void initStepOrbitalRings() {
        StackPane[] circles = { stepCircle0, stepCircle1, stepCircle2, stepCircle3 };
        String[] colors = { "#1a73e8", "#ea4335", "#34a853", "#fbbc04" };

        for (int i = 0; i < circles.length; i++) {
            StackPane circlePane = circles[i];
            String color = colors[i];

            Circle ring1 = new Circle(32);
            ring1.setFill(Color.TRANSPARENT);
            ring1.setStroke(Color.web(color));
            ring1.setStrokeWidth(2);
            ring1.setOpacity(0.3);
            ring1.getStrokeDashArray().addAll(6.0, 4.0);
            ring1.setMouseTransparent(true);

            RotateTransition rt1 = new RotateTransition(Duration.millis(4000), ring1);
            rt1.setByAngle(360);
            rt1.setCycleCount(Timeline.INDEFINITE);
            rt1.setInterpolator(Interpolator.LINEAR);
            rt1.play();

            Circle ring2 = new Circle(40);
            ring2.setFill(Color.TRANSPARENT);
            ring2.setStroke(Color.web(color));
            ring2.setStrokeWidth(1);
            ring2.setOpacity(0.2);
            ring2.getStrokeDashArray().addAll(4.0, 5.0);
            ring2.setMouseTransparent(true);

            RotateTransition rt2 = new RotateTransition(Duration.millis(6000), ring2);
            rt2.setByAngle(-360);
            rt2.setCycleCount(Timeline.INDEFINITE);
            rt2.setInterpolator(Interpolator.LINEAR);
            rt2.play();

            //add rings behind
            circlePane.getChildren().add(0, ring2);
            circlePane.getChildren().add(1, ring1);
        }
    }

    private void registerCursorInteractions() {
        List<Node> interactive = new ArrayList<>();
        interactive.add(btnGetStarted);
        interactive.add(btnSignIn);
        interactive.add(navFeatures);
        interactive.add(navHow);
        interactive.add(navAbout);
        interactive.add(featureCard0);
        interactive.add(featureCard1);
        interactive.add(featureCard2);
        for (Node node : interactive) {
            Main.registerInteractiveCursorNode(node);
        }
    }

    private void refreshStatistics() {
        UserService userService = new UserService();
        Map<String, Integer> stats = userService.getLandingStats();

        updateStatLabel(stat0, stats.getOrDefault("universities", 0));
        updateStatLabel(stat1, stats.getOrDefault("societies", 0));
        updateStatLabel(stat2, stats.getOrDefault("students", 0));
        updateStatLabel(stat3, stats.getOrDefault("events", 0));
    }

    private void updateStatLabel(VBox statBox, int count) {
        if (statBox == null) return;
        //search inside the
        Label numLabel = (Label) statBox.lookup(".stat-number");
        if (numLabel != null) {
            numLabel.setText(String.valueOf(count));
        }
    }

    private static class FloatingLetter {
        String letter;
        Color color;
        double x, y;
        final int index;

        FloatingLetter(String letter, Color color, int idx) {
            this.letter = letter;
            this.color = color;
            this.index = idx;
        }

        void update(int frame, int p1, int p2, int p3, double cx, double cy) {
            int totalPullStart = p1;
            int totalPullEnd = p1 + p2;
            
            //variance seed for
            double seed = index * 0.8 + 1;
            
            double angle = (seed * 45) + frame * 0.8;
            double fx = cx + Math.cos(Math.toRadians(angle)) * (165);
            double fy = cy + Math.sin(Math.toRadians(angle * 1.3)) * (92 + seed * 8);

            
            double spacing = 68;
            double totalWidth = (8-1) * spacing;
            double tx = cx - (totalWidth / 2.0) + index * spacing;
            double ty = cy + 36;

            if (frame < totalPullStart) {
                x = fx;
                y = fy;
            } else if (frame < totalPullEnd) {
                double t = (frame - totalPullStart) / (double) p2;
                double ease = t * t * (3 - 2 * t);
                x = fx + (tx - fx) * ease;
                y = fy + (ty - fy) * ease;
            } else {
                x = tx;
                y = ty;
            }
        }
    }
}
