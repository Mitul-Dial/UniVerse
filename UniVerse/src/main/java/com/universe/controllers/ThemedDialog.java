package com.universe.controllers;

import javafx.animation.*;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.List;
import java.util.function.Consumer;

/**
 * Reusable themed dialog overlays that match the UniVerse design system.
 * Replaces default JavaFX Alert/Dialog boxes with custom styled modals.
 */
public class ThemedDialog {

    // ── Toast / Alert replacement ─────────────────────────────────────
    public static void showToast(StackPane owner, String title, String message, ToastType type) {
        // Overlay
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(32,33,36,0.45);");
        overlay.setOnMouseClicked(e -> dismissOverlay(overlay, owner));

        // Content Box
        VBox cardContent = new VBox(12);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(32, 36, 28, 36));

        // Icon
        String icon = type == ToastType.SUCCESS ? "✓" : type == ToastType.ERROR ? "✕" : "ℹ";
        String iconBg = type == ToastType.SUCCESS ? "#e6f4ea" : type == ToastType.ERROR ? "#fce8e6" : "#e8f0fe";
        String iconFg = type == ToastType.SUCCESS ? "#34a853" : type == ToastType.ERROR ? "#ea4335" : "#1a73e8";

        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(52, 52);
        iconCircle.setMaxSize(52, 52);
        iconCircle.setStyle("-fx-background-color: " + iconBg + "; -fx-background-radius: 50%;");
        Label iconLabel = new Label(icon);
        iconLabel.setStyle("-fx-font-size: 24; -fx-font-weight: 700; -fx-text-fill: " + iconFg + "; -fx-font-family: 'Segoe UI';");
        iconCircle.getChildren().add(iconLabel);

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 18; -fx-font-weight: 600; -fx-text-fill: #202124; -fx-font-family: 'Segoe UI';");

        // Message
        Label msgLabel = new Label(message);
        msgLabel.setWrapText(true);
        msgLabel.setMaxWidth(310);
        msgLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #5f6368; -fx-font-family: 'Segoe UI'; -fx-text-alignment: center;");
        msgLabel.setAlignment(Pos.CENTER);

        // OK Button
        Button okBtn = new Button("OK");
        okBtn.setStyle(
            "-fx-background-color: " + iconFg + ";" +
            "-fx-text-fill: white;" +
            "-fx-background-radius: 100;" +
            "-fx-padding: 10 40;" +
            "-fx-font-size: 14; -fx-font-weight: 500;" +
            "-fx-cursor: hand; -fx-font-family: 'Segoe UI';"
        );
        okBtn.setOnAction(e -> dismissOverlay(overlay, owner));
        VBox.setMargin(okBtn, new Insets(6, 0, 0, 0));

        cardContent.getChildren().addAll(iconCircle, titleLabel, msgLabel, okBtn);
        
        StackPane card = createAnimatedCard(cardContent, 420);
        overlay.getChildren().add(card);

        // Animate in
        owner.getChildren().add(overlay);
        overlay.setOpacity(0);
        card.setScaleX(0.85);
        card.setScaleY(0.85);
        FadeTransition fade = new FadeTransition(Duration.millis(200), overlay);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), card);
        scale.setToX(1); scale.setToY(1);
        scale.setInterpolator(Interpolator.SPLINE(0.25, 1, 0.5, 1));
        new ParallelTransition(fade, scale).play();
    }

    // ── Role chooser (replaces ChoiceDialog) ──────────────────────────
    public static void showRoleChooser(StackPane owner, List<String> roles, Consumer<String> onSelected) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(32,33,36,0.45);");
        overlay.setOnMouseClicked(e -> dismissOverlay(overlay, owner));

        VBox cardContent = new VBox(16);
        cardContent.setAlignment(Pos.CENTER);
        cardContent.setPadding(new Insets(32, 36, 28, 36));

        // Icon
        StackPane iconCircle = new StackPane();
        iconCircle.setMinSize(52, 52); iconCircle.setMaxSize(52, 52);
        iconCircle.setStyle("-fx-background-color: #e8f0fe; -fx-background-radius: 50%;");
        Label iconLabel = new Label("👤");
        iconLabel.setStyle("-fx-font-size: 24;");
        iconCircle.getChildren().add(iconLabel);

        Label titleLabel = new Label("Join UniVerse");
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: 600; -fx-text-fill: #202124; -fx-font-family: 'Segoe UI';");

        Label subLabel = new Label("Select your role to get started");
        subLabel.setStyle("-fx-font-size: 14; -fx-text-fill: #5f6368; -fx-font-family: 'Segoe UI';");

        VBox btnBox = new VBox(10);
        btnBox.setAlignment(Pos.CENTER);
        VBox.setMargin(btnBox, new Insets(8, 0, 0, 0));

        String[] colors = {"#1a73e8", "#34a853", "#fbbc04"};
        String[] bgColors = {"#e8f0fe", "#e6f4ea", "#fef7e0"};
        String[] icons = {"🎓", "🏛", "💼"};

        for (int i = 0; i < roles.size(); i++) {
            String role = roles.get(i);
            String color = colors[i % colors.length];
            String bgColor = bgColors[i % bgColors.length];
            String emoji = icons[i % icons.length];

            Button roleBtn = new Button(emoji + "  " + role);
            roleBtn.setMaxWidth(Double.MAX_VALUE);
            roleBtn.setStyle(
                "-fx-background-color: " + bgColor + ";" +
                "-fx-text-fill: " + color + ";" +
                "-fx-background-radius: 12;" +
                "-fx-padding: 14 20;" +
                "-fx-font-size: 15; -fx-font-weight: 600;" +
                "-fx-cursor: hand; -fx-font-family: 'Segoe UI';" +
                "-fx-alignment: center-left;"
            );
            final String fColor = color;
            roleBtn.setOnMouseEntered(e -> roleBtn.setStyle(roleBtn.getStyle() + "-fx-effect: dropshadow(gaussian, " + fColor + "33, 8, 0, 0, 2);"));
            roleBtn.setOnMouseExited(e -> roleBtn.setStyle(roleBtn.getStyle().replaceAll("-fx-effect:[^;]*;", "")));
            roleBtn.setOnAction(e -> {
                dismissOverlay(overlay, owner);
                onSelected.accept(role);
            });
            btnBox.getChildren().add(roleBtn);
        }

        // Cancel
        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #5f6368;" +
            "-fx-font-size: 13; -fx-font-weight: 500; -fx-cursor: hand;" +
            "-fx-font-family: 'Segoe UI'; -fx-padding: 8 20;"
        );
        cancelBtn.setOnAction(e -> dismissOverlay(overlay, owner));
        VBox.setMargin(cancelBtn, new Insets(4, 0, 0, 0));

        cardContent.getChildren().addAll(iconCircle, titleLabel, subLabel, btnBox, cancelBtn);
        
        StackPane card = createAnimatedCard(cardContent, 420);
        overlay.getChildren().add(card);

        owner.getChildren().add(overlay);
        animateIn(overlay, card);
    }

    // ── Form dialog (replaces Dialog for login/signup/event creation) ─
    public static void showFormDialog(StackPane owner, String title, String subtitle,
                                       Node formContent, String submitText, String accentColor,
                                       Runnable onSubmit, Runnable onCancel) {
        StackPane overlay = new StackPane();
        overlay.setStyle("-fx-background-color: rgba(32,33,36,0.45);");
        overlay.setOnMouseClicked(e -> {
            dismissOverlay(overlay, owner);
            if (onCancel != null) onCancel.run();
        });

        VBox cardContent = new VBox(6);
        cardContent.setAlignment(Pos.CENTER_LEFT);
        cardContent.setPadding(new Insets(32, 36, 28, 36));

        // Title
        Label titleLabel = new Label(title);
        titleLabel.setStyle("-fx-font-size: 20; -fx-font-weight: 600; -fx-text-fill: #202124; -fx-font-family: 'Segoe UI';");

        // Subtitle
        Label subLabel = new Label(subtitle);
        subLabel.setStyle("-fx-font-size: 13; -fx-text-fill: #5f6368; -fx-font-family: 'Segoe UI';");
        VBox.setMargin(subLabel, new Insets(0, 0, 12, 0));

        // Buttons row
        HBox btnRow = new HBox(10);
        btnRow.setAlignment(Pos.CENTER_RIGHT);
        VBox.setMargin(btnRow, new Insets(16, 0, 0, 0));

        Button cancelBtn = new Button("Cancel");
        cancelBtn.setStyle(
            "-fx-background-color: transparent; -fx-text-fill: #5f6368;" +
            "-fx-border-color: #dadce0; -fx-border-width: 1; -fx-border-radius: 100;" +
            "-fx-background-radius: 100; -fx-padding: 10 22;" +
            "-fx-font-size: 14; -fx-font-weight: 500; -fx-cursor: hand; -fx-font-family: 'Segoe UI';"
        );
        cancelBtn.setOnAction(e -> {
            dismissOverlay(overlay, owner);
            if (onCancel != null) onCancel.run();
        });

        Button submitBtn = new Button(submitText);
        submitBtn.setStyle(
            "-fx-background-color: " + accentColor + ";" +
            "-fx-text-fill: white; -fx-background-radius: 100;" +
            "-fx-padding: 10 28; -fx-font-size: 14; -fx-font-weight: 500;" +
            "-fx-cursor: hand; -fx-font-family: 'Segoe UI';"
        );
        submitBtn.setOnAction(e -> onSubmit.run());

        btnRow.getChildren().addAll(cancelBtn, submitBtn);
        cardContent.getChildren().addAll(titleLabel, subLabel, formContent, btnRow);

        StackPane card = createAnimatedCard(cardContent, 520);

        // Store overlay ref on card for later dismissal
        card.getProperties().put("overlay", overlay);
        card.getProperties().put("owner", owner);

        overlay.getChildren().add(card);
        owner.getChildren().add(overlay);
        animateIn(overlay, card);
    }

    /** Dismiss overlay from within a form submit handler. Call with any node inside the card. */
    public static void dismiss(Node anyChildOfCard) {
        Node node = anyChildOfCard;
        while (node != null) {
            if (node.getProperties().containsKey("overlay")) {
                StackPane overlay = (StackPane) node.getProperties().get("overlay");
                StackPane owner = (StackPane) node.getProperties().get("owner");
                dismissOverlay(overlay, owner);
                return;
            }
            node = node.getParent();
        }
    }

    /** Dismiss a specific overlay directly. */
    public static void dismissOverlay(StackPane overlay, StackPane owner) {
        FadeTransition fade = new FadeTransition(Duration.millis(150), overlay);
        fade.setToValue(0);
        fade.setOnFinished(e -> owner.getChildren().remove(overlay));
        fade.play();
    }

    // ── Text input dialog (replaces TextInputDialog) ──────────────────
    public static void showTextInput(StackPane owner, String title, String subtitle,
                                      String promptText, Consumer<String> onSubmit) {
        TextField inputField = new TextField();
        inputField.setPromptText(promptText);
        inputField.setStyle(
            "-fx-background-color: #f8f9fa; -fx-background-radius: 10;" +
            "-fx-border-color: #dadce0; -fx-border-radius: 10; -fx-border-width: 1;" +
            "-fx-padding: 12 14; -fx-font-size: 14; -fx-font-family: 'Segoe UI';"
        );

        VBox content = new VBox(8);
        content.getChildren().add(inputField);

        showFormDialog(owner, title, subtitle, content, "Send", "#1a73e8",
            () -> {
                String val = inputField.getText().trim();
                if (!val.isEmpty()) {
                    dismiss(inputField);
                    onSubmit.accept(val);
                }
            }, null);
    }

    // ── Styled text field helper ──────────────────────────────────────
    public static TextField styledTextField(String prompt) {
        TextField tf = new TextField();
        tf.setPromptText(prompt);
        tf.setStyle(
            "-fx-background-color: #f8f9fa; -fx-background-radius: 10;" +
            "-fx-border-color: #dadce0; -fx-border-radius: 10; -fx-border-width: 1;" +
            "-fx-padding: 10 14; -fx-font-size: 14; -fx-font-family: 'Segoe UI';"
        );
        return tf;
    }

    public static PasswordField styledPasswordField(String prompt) {
        PasswordField pf = new PasswordField();
        pf.setPromptText(prompt);
        pf.setStyle(
            "-fx-background-color: #f8f9fa; -fx-background-radius: 10;" +
            "-fx-border-color: #dadce0; -fx-border-radius: 10; -fx-border-width: 1;" +
            "-fx-padding: 10 14; -fx-font-size: 14; -fx-font-family: 'Segoe UI';"
        );
        return pf;
    }

    private static void animateIn(StackPane overlay, StackPane card) {
        overlay.setOpacity(0);
        card.setScaleX(0.85);
        card.setScaleY(0.85);
        FadeTransition fade = new FadeTransition(Duration.millis(200), overlay);
        fade.setToValue(1);
        ScaleTransition scale = new ScaleTransition(Duration.millis(250), card);
        scale.setToX(1); scale.setToY(1);
        scale.setInterpolator(Interpolator.SPLINE(0.25, 1, 0.5, 1));
        new ParallelTransition(fade, scale).play();
    }

    private static StackPane createAnimatedCard(VBox contentBox, double maxWidth) {
        StackPane card = new StackPane();
        card.setMaxWidth(maxWidth);
        card.setMaxHeight(Region.USE_PREF_SIZE);
        card.setStyle(
            "-fx-background-color: white;" +
            "-fx-background-radius: 20;" +
            "-fx-effect: dropshadow(gaussian, rgba(0,0,0,0.18), 24, 0, 0, 8);"
        );
        card.setOnMouseClicked(e -> e.consume());

        Pane bgContainer = new Pane();
        bgContainer.setMinSize(0, 0);
        bgContainer.setPrefSize(0, 0);
        bgContainer.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);

        javafx.scene.shape.Rectangle clip = new javafx.scene.shape.Rectangle();
        clip.widthProperty().bind(card.widthProperty());
        clip.heightProperty().bind(card.heightProperty());
        clip.setArcWidth(40);
        clip.setArcHeight(40);
        bgContainer.setClip(clip);

        Region bgGradient = new Region();
        bgGradient.setPrefSize(2000, 2000);
        // Brighter Google shades: red, blue, green, yellow, red (for seamless look)
        bgGradient.setStyle(
            "-fx-background-color: linear-gradient(to bottom right, #799299ff, #c8deffff, #c9f0d4ff, #f9eaa4ff, #ac8d8bff);"
        );
        bgGradient.setOpacity(0.95);

        TranslateTransition tt = new TranslateTransition(Duration.seconds(8), bgGradient);
        tt.setFromX(0); tt.setFromY(0);
        tt.setToX(-600); tt.setToY(-600);
        tt.setAutoReverse(true);
        tt.setCycleCount(Animation.INDEFINITE);
        tt.play();

        bgContainer.getChildren().add(bgGradient);
        contentBox.setStyle("-fx-background-color: transparent;");

        card.getChildren().addAll(bgContainer, contentBox);
        return card;
    }

    public enum ToastType { SUCCESS, ERROR, INFO }
}
