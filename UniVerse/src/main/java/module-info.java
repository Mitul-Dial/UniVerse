module com.universe {
    requires javafx.controls;
    requires javafx.fxml;
    requires javafx.graphics;

    requires java.sql;

    opens com.universe to javafx.fxml;
    opens com.universe.controllers to javafx.fxml;
    opens com.universe.models to javafx.fxml;

    exports com.universe;
    exports com.universe.controllers;
    exports com.universe.models;
}
