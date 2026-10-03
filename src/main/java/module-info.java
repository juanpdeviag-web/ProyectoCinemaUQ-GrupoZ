module co.edu.uq.cinemauq {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;
    requires org.junit.jupiter.api;

    opens co.edu.uq.cinemauq to javafx.fxml;
    exports co.edu.uq.cinemauq;
}