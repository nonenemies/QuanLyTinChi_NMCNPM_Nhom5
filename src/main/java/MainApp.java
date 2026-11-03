import atlantafx.base.theme.PrimerLight;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class MainApp extends Application {
    @Override
    public void start(Stage stage) throws Exception {
        Application.setUserAgentStylesheet(new PrimerLight().getUserAgentStylesheet());

        FXMLLoader fxmlLoader = new FXMLLoader(MainApp.class.getResource("/views/LoginView.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1080, 680);

        stage.setTitle("Phần mềm Quản lý Tín chỉ - Nhóm 5");
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(600);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args); // Lệnh bắt buộc để chạy JavaFX
    }
}