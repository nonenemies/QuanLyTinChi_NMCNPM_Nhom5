package Controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Button;

public class LogoutConfirmController {
    @FXML private Button btnConfirm;

    private boolean confirmed;

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
