package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class RegistrationFailureController {
    @FXML private Label lblHeader;
    @FXML private Label lblMessage;
    @FXML private Button btnClose;

    public void setDetails(String header, String message) {
        lblHeader.setText(header);
        lblMessage.setText(message);
    }

    @FXML
    private void onClose() {
        btnClose.getScene().getWindow().hide();
    }
}
