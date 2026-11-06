package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class RegistrationConfirmController {
    @FXML private Label lblHeader;
    @FXML private Label lblDetails;
    @FXML private Button btnConfirm;

    private boolean confirmed;

    public void setDetails(String header, String details) {
        lblHeader.setText(header);
        lblDetails.setText(details);
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    @FXML
    private void onConfirm() {
        confirmed = true;
        close();
    }

    @FXML
    private void onCancel() {
        close();
    }

    private void close() {
        btnConfirm.getScene().getWindow().hide();
    }
}
