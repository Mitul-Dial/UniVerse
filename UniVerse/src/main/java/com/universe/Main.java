package com.universe;

import javafx.animation.FadeTransition;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.geometry.Point2D;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

//entry point
public class Main extends Application {

    //view components
    private static StackPane rootStack;
    private static Parent landingView;
    private static Parent dashboardView;
    private static CursorFx cursorFx;
    private static com.universe.controllers.DashboardController dashboardController;

    @Override
    public void start(Stage primaryStage) throws Exception {
        //load views
        FXMLLoader landingLoader = new FXMLLoader(getClass().getResource("landing.fxml"));
        landingView = landingLoader.load();

        FXMLLoader dashboardLoader = new FXMLLoader(getClass().getResource("dashboard.fxml"));
        dashboardView = dashboardLoader.load();
        dashboardController = dashboardLoader.getController();

        //initial state
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);

        //root container
        rootStack = new StackPane(landingView, dashboardView);

        //scene setup
        Scene scene = new Scene(rootStack, 1280, 800);
        scene.setCursor(Cursor.NONE);
        scene.getStylesheets().add(getClass().getResource("styles/landing.css").toExternalForm());
        scene.getStylesheets().add(getClass().getResource("styles/dashboard.css").toExternalForm());
        
        //custom cursor
        cursorFx = new CursorFx(rootStack, scene);

        //stage config
        primaryStage.setTitle("UniVerse — University Event & Society Platform");
        primaryStage.setScene(scene);
        primaryStage.setMinWidth(1024);
        primaryStage.setMinHeight(700);
        primaryStage.show();
    }

    //switch dashboard
    public static void showDashboard() {
        if (dashboardController != null) {
            dashboardController.loadUserData();
        }
        landingView.setVisible(false);
        landingView.setManaged(false);
        dashboardView.setVisible(true);
        dashboardView.setManaged(true);
        dashboardView.setOpacity(0);
        
        //transition animation
        FadeTransition ft = new FadeTransition(Duration.millis(400), dashboardView);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    //switch landing
    public static void showLanding() {
        dashboardView.setVisible(false);
        dashboardView.setManaged(false);
        landingView.setVisible(true);
        landingView.setManaged(true);
        landingView.setOpacity(0);
        
        //transition animation
        FadeTransition ft = new FadeTransition(Duration.millis(400), landingView);
        ft.setFromValue(0);
        ft.setToValue(1);
        ft.play();
    }

    //launch app
    public static void main(String[] args) {
        launch(args);
    }

    //register interactions
    public static void registerInteractiveCursorNode(Node node) {
        if (cursorFx != null && node != null) {
            cursorFx.registerInteractiveNode(node);
        }
    }

    //emit particles
    public static void burstParticles(double sceneX, double sceneY, int count) {
        if (cursorFx != null) {
            cursorFx.emitBurst(sceneX, sceneY, count);
        }
    }

    //custom cursor
    private static final class CursorFx {
        private static final Color[] GOOGLE_COLORS = {
                Color.web("#ea4335"), Color.web("#fbbc04"), Color.web("#34a853"), Color.web("#1a73e8")
        };
        private final Circle dot = new Circle(6, Color.web("#1a73e8"));
        private final Circle ring = new Circle(18, Color.TRANSPARENT);
        private final Canvas particleCanvas = new Canvas();
        private final GraphicsContext gc = particleCanvas.getGraphicsContext2D();
        private final List<Particle> particles = new ArrayList<>();
        private double mouseX;
        private double mouseY;
        private double ringX;
        private double ringY;
        private long frameCount = 0;
        private double prevEmitX;
        private double prevEmitY;
        private boolean enlarged = false;

        private CursorFx(StackPane root, Scene scene) {
            //setup layers
            root.setPickOnBounds(false);

            ring.setStroke(Color.rgb(26, 115, 232, 0.65));
            ring.setStrokeWidth(2);
            dot.setManaged(false);
            ring.setManaged(false);

            particleCanvas.widthProperty().bind(root.widthProperty());
            particleCanvas.heightProperty().bind(root.heightProperty());

            Pane cursorOverlay = new Pane();
            cursorOverlay.setMouseTransparent(true);
            cursorOverlay.setPickOnBounds(false);
            cursorOverlay.setManaged(false);
            cursorOverlay.prefWidthProperty().bind(root.widthProperty());
            cursorOverlay.prefHeightProperty().bind(root.heightProperty());

            cursorOverlay.getChildren().addAll(particleCanvas, ring, dot);
            root.getChildren().add(cursorOverlay);

            dot.setMouseTransparent(true);
            ring.setMouseTransparent(true);
            particleCanvas.setMouseTransparent(true);

            //mouse events
            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_MOVED, e -> {
                mouseX = e.getSceneX();
                mouseY = e.getSceneY();
            });
            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_DRAGGED, e -> {
                mouseX = e.getSceneX();
                mouseY = e.getSceneY();
            });
            scene.addEventFilter(javafx.scene.input.MouseEvent.MOUSE_CLICKED, e -> {
                Point2D p = root.sceneToLocal(e.getSceneX(), e.getSceneY());
                emitBurst(p.getX(), p.getY(), 12);
            });

            //animation loop
            new AnimationTimer() {
                private long lastNs = 0;

                @Override
                public void handle(long now) {
                    if (lastNs == 0) {
                        lastNs = now;
                        ringX = mouseX;
                        ringY = mouseY;
                    }
                    double dt = (now - lastNs) / 1_000_000_000.0;
                    lastNs = now;
                    frameCount++;

                    Point2D local = root.sceneToLocal(mouseX, mouseY);
                    double targetX = local.getX();
                    double targetY = local.getY();

                    //smooth movement
                    dot.setLayoutX(targetX);
                    dot.setLayoutY(targetY);
                    ringX += (targetX - ringX) * 0.12;
                    ringY += (targetY - ringY) * 0.12;
                    ring.setLayoutX(ringX);
                    ring.setLayoutY(ringY);

                    //emit trail
                    if (frameCount % 3 == 0) {
                        double speed = Math.hypot(targetX - prevEmitX, targetY - prevEmitY);
                        if (speed > 8) {
                            emitTrail(targetX, targetY, speed);
                            prevEmitX = targetX;
                            prevEmitY = targetY;
                        }
                    }
                    updateParticles(dt);
                }
            }.start();
        }

        //register hover
        private void registerInteractiveNode(Node node) {
            node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_ENTERED, e -> setEnlarged(true));
            node.addEventHandler(javafx.scene.input.MouseEvent.MOUSE_EXITED, e -> setEnlarged(false));
        }

        //cursor scaling
        private void setEnlarged(boolean value) {
            if (enlarged == value)
                return;
            enlarged = value;
            dot.setRadius(value ? 10 : 6);
            ring.setRadius(value ? 26 : 18);
            dot.setFill(value ? Color.rgb(26, 115, 232, 0.65) : Color.web("#1a73e8"));
        }

        //create trail
        private void emitTrail(double x, double y, double speed) {
            int count = Math.min(4, Math.max(1, (int) (speed / 12)));
            for (int i = 0; i < count; i++) {
                double vx = (Math.random() - 0.5) * 90;
                double vy = (Math.random() - 0.5) * 90 - 20;
                particles.add(
                        new Particle(x, y, vx, vy, GOOGLE_COLORS[(int) (Math.random() * GOOGLE_COLORS.length)], 0.8));
            }
        }

        //create burst
        private void emitBurst(double x, double y, int count) {
            for (int i = 0; i < count; i++) {
                double a = (Math.PI * 2 * i) / count;
                double speed = 120 + Math.random() * 120;
                particles.add(new Particle(x, y, Math.cos(a) * speed, Math.sin(a) * speed - 30,
                        GOOGLE_COLORS[(int) (Math.random() * GOOGLE_COLORS.length)], 1.0));
            }
        }

        //render particles
        private void updateParticles(double dt) {
            gc.clearRect(0, 0, particleCanvas.getWidth(), particleCanvas.getHeight());
            Iterator<Particle> it = particles.iterator();
            while (it.hasNext()) {
                Particle p = it.next();
                p.life -= dt * 1.4;
                p.vy += 260 * dt;
                p.x += p.vx * dt;
                p.y += p.vy * dt;
                if (p.life <= 0) {
                    it.remove();
                    continue;
                }
                gc.setFill(new Color(p.color.getRed(), p.color.getGreen(), p.color.getBlue(), Math.max(0, p.life)));
                gc.fillOval(p.x - 2.5, p.y - 2.5, 5, 5);
            }
        }

        //particle model
        private static final class Particle {
            private double x, y, vx, vy, life;
            private final Color color;

            private Particle(double x, double y, double vx, double vy, Color color, double life) {
                this.x = x;
                this.y = y;
                this.vx = vx;
                this.vy = vy;
                this.color = color;
                this.life = life;
            }
        }
    }
}
