module co.edu.uq.cinemauq {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.junit.jupiter.api;

    opens co.edu.uq.cinemauq to javafx.fxml;
    opens co.edu.uq.cinemauq.ui.controllers to javafx.fxml;
    opens co.edu.uq.cinemauq.ui.utils to javafx.fxml;
    opens co.edu.uq.cinemauq.ui.models to javafx.fxml;
    opens co.edu.uq.cinemauq.core.domain to javafx.fxml;
    opens co.edu.uq.cinemauq.core.state to javafx.fxml;
    opens co.edu.uq.cinemauq.patterns.decorator to javafx.fxml;
    opens co.edu.uq.cinemauq.patterns.composite to javafx.fxml;
    opens co.edu.uq.cinemauq.patterns.facade to javafx.fxml;
    exports co.edu.uq.cinemauq;
    exports co.edu.uq.cinemauq.ui.controllers;
    exports co.edu.uq.cinemauq.ui.utils;
    exports co.edu.uq.cinemauq.ui.models;
    exports co.edu.uq.cinemauq.core.domain;
    exports co.edu.uq.cinemauq.core.state;
    exports co.edu.uq.cinemauq.patterns.decorator;
    exports co.edu.uq.cinemauq.patterns.composite;
    exports co.edu.uq.cinemauq.patterns.facade;
}