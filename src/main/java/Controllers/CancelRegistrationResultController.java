package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;

public class CancelRegistrationResultController {
    @FXML private Label lblHeader;
    @FXML private Label lblMessage;
    @FXML private Button btnClose;

    public void setDetails(String context, String header, String message) {
        lblHeader.setText(header);
        lblMessage.setText(context + System.lineSeparator() + message);
    }

    @FXML
    private void onClose() {
        btnClose.getScene().getWindow().hide();
    }
}
