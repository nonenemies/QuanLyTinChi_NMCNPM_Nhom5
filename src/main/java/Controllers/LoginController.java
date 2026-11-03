package Controllers;

import Model.Users;
import Services.AuthService;
import Services.ServiceException;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import java.io.IOException;

public class LoginController {

    @FXML private TextField txtEmail;
    @FXML private PasswordField txtPassword;
    @FXML private Label lblError;

    private AuthService authService = new AuthService();

    @FXML
    protected void onBtnLoginClick(ActionEvent event) {
        String email = txtEmail.getText();
        String password = txtPassword.getText();

        if (email.isBlank() || password.isBlank()) {
            lblError.setText("Vui lòng nhập đầy đủ Email và Mật khẩu!");
            lblError.setVisible(true);
            lblError.setManaged(true);
            return;
        }

        Users user;
        try {
            user = authService.login(email, password);
        } catch (ServiceException e) {
            lblError.setText("Không thể đăng nhập do lỗi hệ thống. Vui lòng thử lại sau.");
            lblError.setVisible(true);
            lblError.setManaged(true);
            return;
        }

        if (user != null) {
            lblError.setVisible(false);
            lblError.setManaged(false);

            try {
                FXMLLoader fxmlLoader = new FXMLLoader(getClass().getResource("/views/AppShell.fxml"));
                Scene dashboardScene = new Scene(fxmlLoader.load(), 1200, 760);
                AppShellController appShell = fxmlLoader.getController();
                appShell.start(user);

                Stage currentStage = (Stage) ((Node) event.getSource()).getScene().getWindow();
                currentStage.setScene(dashboardScene);
                currentStage.setTitle("Phần mềm Quản lý Tín chỉ - " + user.getHoTen());
                currentStage.setMinWidth(1000);
                currentStage.setMinHeight(650);
                currentStage.centerOnScreen();

            } catch (IOException | RuntimeException e) {
                lblError.setText("Không thể mở màn hình tài khoản. Vui lòng thử lại.");
                lblError.setVisible(true);
                lblError.setManaged(true);
            }
        } else {
            lblError.setText("Email hoặc Mật khẩu không chính xác!");
            lblError.setVisible(true);
            lblError.setManaged(true);
        }
    }
}