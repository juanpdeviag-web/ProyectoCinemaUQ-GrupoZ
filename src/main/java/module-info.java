module co.edu.uq.cinemauq {
    requires javafx.controls;
    requires javafx.fxml;

    requires org.controlsfx.controls;
    requires net.synedra.validatorfx;
    requires org.kordamp.bootstrapfx.core;

    opens co.edu.uq.cinemauq to javafx.fxml;
    exports co.edu.uq.cinemauq;
}