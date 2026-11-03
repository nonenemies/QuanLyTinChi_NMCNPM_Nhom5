package Controllers;

import Model.GiangVien;
import Model.Users;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class AppShellController {
    private static final Logger LOGGER = Logger.getLogger(AppShellController.class.getName());
    private static final String DASHBOARD = "dashboard";
    private static final String TIMETABLE = "timetable";
    private static final String STUDENT_GRADES = "student-grades";
    private static final String CHATBOT = "chatbot";
    private static final String REGISTRATION = "registration";
    private static final String EXCHANGE = "exchange";
    private static final String GRADEBOOK = "gradebook";
    private static final String ADMIN_STUDENTS = "admin-students";
    private static final String ADMIN_LECTURERS = "admin-lecturers";
    private static final String ADMIN_COURSES = "admin-courses";
    private static final String ADMIN_CLASSES = "admin-classes";
    private static final String ADMIN_COMPONENTS = "admin-components";

    @FXML private Label lblUserName;
    @FXML private Label lblUserRole;
    @FXML private Label lblContentTitle;
    @FXML private VBox navigationItems;
    @FXML private VBox contentHost;

    private Users user;
    private String currentScreen;
    private final List<Button> navigationButtons = new ArrayList<>();

    public void start(Users authenticatedUser) throws IOException {
        if (authenticatedUser == null) {
            throw new IllegalArgumentException("Authenticated user is required.");
        }
        user = authenticatedUser;
        lblUserName.setText(user.getHoTen());
        lblUserRole.setText(roleLabel(user));
        configureNavigation();

        if (user instanceof GiangVien
                || "SINHVIEN".equalsIgnoreCase(user.getRole())
                || "ADMIN".equalsIgnoreCase(user.getRole())) {
            showScreen(DASHBOARD);
        } else {
            showError("Vai trò tài khoản không được hỗ trợ.");
        }
    }

    @FXML
    private void onLogout() {
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận đăng xuất");
        confirmation.setHeaderText("Bạn muốn đăng xuất khỏi tài khoản?");
        confirmation.setContentText("Bạn sẽ được đưa về màn hình đăng nhập.");
        Optional<ButtonType> response = confirmation.showAndWait();
        if (response.isEmpty() || response.get() != ButtonType.OK) return;

        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LoginView.fxml"));
            Scene scene = new Scene(loader.load(), 1080, 680);
            Stage stage = (Stage) navigationItems.getScene().getWindow();
            stage.setScene(scene);
            stage.setTitle("Đăng nhập - Credit Hub");
            stage.setMinWidth(900);
            stage.setMinHeight(600);
            stage.centerOnScreen();
            user = null;
        } catch (IOException | RuntimeException e) {
            showError("Không thể đăng xuất lúc này. Vui lòng thử lại.");
        }
    }

    public void showTimetable() {
        if (isStudent()) showScreen(TIMETABLE);
    }

    private void configureNavigation() {
        navigationItems.getChildren().clear();
        navigationButtons.clear();
        if ("SINHVIEN".equalsIgnoreCase(user.getRole())) {
            addNavigationButton("Tổng quan", DASHBOARD);
            addNavigationButton("Thời khóa biểu", TIMETABLE);
            addNavigationButton("Đăng ký tín chỉ", REGISTRATION);
            addNavigationButton("Điểm tổng kết", STUDENT_GRADES);
            addNavigationButton("Trợ lý AI học tập", CHATBOT);
            addNavigationButton("Yêu cầu đổi lớp", EXCHANGE);
        } else if (user instanceof GiangVien) {
            addNavigationButton("Tổng quan", DASHBOARD);
            addNavigationButton("Lớp phụ trách và bảng điểm", GRADEBOOK);
        } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            addNavigationButton("Tổng quan", DASHBOARD);
            addNavigationButton("Quản lý sinh viên", ADMIN_STUDENTS);
            addNavigationButton("Quản lý giảng viên", ADMIN_LECTURERS);
            addNavigationButton("Quản lý môn học", ADMIN_COURSES);
            addNavigationButton("Quản lý lớp học phần", ADMIN_CLASSES);
            addNavigationButton("Thành phần đánh giá", ADMIN_COMPONENTS);
        }
    }

    private void addNavigationButton(String title, String screen) {
        Button button = new Button(title);
        button.setMaxWidth(Double.MAX_VALUE);
        button.getStyleClass().add("nav-item");
        button.setOnAction(event -> showScreen(screen));
        navigationItems.getChildren().add(button);
        navigationButtons.add(button);
    }

    private void showScreen(String screen) {
        String resource = switch (screen) {
            case DASHBOARD -> "/views/DashBoardView.fxml";
            case TIMETABLE -> "/views/TimetableView.fxml";
            case STUDENT_GRADES -> "/views/StudentGradesView.fxml";
            case CHATBOT -> "/views/ChatbotUI.fxml";
            case REGISTRATION -> "/views/RegistrationView.fxml";
            case EXCHANGE -> "/views/ExchangeView.fxml";
            case GRADEBOOK -> "/views/GiangVienView.fxml";
            case ADMIN_STUDENTS, ADMIN_LECTURERS, ADMIN_COURSES,
                    ADMIN_CLASSES, ADMIN_COMPONENTS -> "/views/AdminView.fxml";
            default -> throw new IllegalArgumentException("Unknown application screen.");
        };
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent page = loader.load();
            Object controller = loader.getController();
            if (controller instanceof DashBoardController dashboard) {
                dashboard.setAppShell(this);
                dashboard.setLoggedInUser(user);
            } else if (controller instanceof RegistrationController registration) {
                registration.setStudent(user);
            } else if (controller instanceof TimetableController timetable) {
                timetable.setStudent(user);
            } else if (controller instanceof StudentGradesController grades) {
                grades.setStudent(user);
            } else if (controller instanceof ChatbotController chatbot) {
                chatbot.setStudent(user);
            } else if (controller instanceof ExchangeController exchange) {
                exchange.setStudent(user);
            } else if (controller instanceof GiangVienController lecturer) {
                lecturer.setLecturer(user);
            } else if (controller instanceof AdminController administrator) {
                administrator.setAdmin(user, screen);
            }
            VBox.setVgrow(page, Priority.ALWAYS);
            if (page instanceof Region region) {
                region.setMaxSize(Double.MAX_VALUE, Double.MAX_VALUE);
            }
            contentHost.getChildren().setAll(page);
            currentScreen = screen;
            lblContentTitle.setText(screenTitle(screen));
            updateNavigationSelection();
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unable to open screen: " + screen, e);
            showError("Không thể mở chức năng này: " + rootMessage(e));
        }
    }

    private String rootMessage(Throwable error) {
        Throwable cause = error;
        while (cause.getCause() != null) {
            cause = cause.getCause();
        }
        String message = cause.getMessage();
        return message == null || message.isBlank() ? "lỗi không xác định" : message;
    }

    private void updateNavigationSelection() {
        for (int i = 0; i < navigationButtons.size(); i++) {
            Button button = navigationButtons.get(i);
            button.getStyleClass().remove("nav-item-active");
            if (button.getOnAction() != null
                    && button.getText().equals(navigationTitleForScreen(currentScreen))) {
                button.getStyleClass().add("nav-item-active");
            }
        }
    }

    private String navigationTitleForScreen(String screen) {
        return switch (screen) {
            case DASHBOARD -> "Tổng quan";
            case TIMETABLE -> "Thời khóa biểu";
            case STUDENT_GRADES -> "Điểm tổng kết";
            case CHATBOT -> "Trợ lý AI học tập";
            case REGISTRATION -> "Đăng ký tín chỉ";
            case EXCHANGE -> "Yêu cầu đổi lớp";
            case GRADEBOOK -> "Lớp phụ trách và bảng điểm";
            case ADMIN_STUDENTS -> "Quản lý sinh viên";
            case ADMIN_LECTURERS -> "Quản lý giảng viên";
            case ADMIN_COURSES -> "Quản lý môn học";
            case ADMIN_CLASSES -> "Quản lý lớp học phần";
            case ADMIN_COMPONENTS -> "Thành phần đánh giá";
            default -> "";
        };
    }

    private String screenTitle(String screen) {
        return switch (screen) {
            case DASHBOARD -> "Tổng quan";
            case TIMETABLE -> "Thời khóa biểu";
            case STUDENT_GRADES -> "Điểm tổng kết";
            case CHATBOT -> "Trợ lý AI tư vấn học tập";
            case REGISTRATION -> "Đăng ký tín chỉ";
            case EXCHANGE -> "Yêu cầu đổi lớp";
            case GRADEBOOK -> "Lớp phụ trách và bảng điểm";
            case ADMIN_STUDENTS -> "Quản lý sinh viên & tài khoản";
            case ADMIN_LECTURERS -> "Quản lý giảng viên & tài khoản";
            case ADMIN_COURSES -> "Quản lý môn học và môn tiên quyết";
            case ADMIN_CLASSES -> "Quản lý lớp học phần";
            case ADMIN_COMPONENTS -> "Cấu hình thành phần đánh giá";
            default -> "";
        };
    }

    private String roleLabel(Users authenticatedUser) {
        if (authenticatedUser instanceof GiangVien) return "GIẢNG VIÊN";
        if ("SINHVIEN".equalsIgnoreCase(authenticatedUser.getRole())) return "SINH VIÊN";
        if ("ADMIN".equalsIgnoreCase(authenticatedUser.getRole())) return "QUẢN TRỊ VIÊN";
        return authenticatedUser.getRole();
    }

    private boolean isStudent() {
        return user != null && "SINHVIEN".equalsIgnoreCase(user.getRole());
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Không thể tiếp tục");
        alert.showAndWait();
    }
}
