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
import javafx.scene.control.Label;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class AppShellController {
    private static final Logger LOGGER = Logger.getLogger(AppShellController.class.getName());
    private static final String OVERVIEW = "overview";
    private static final String TIMETABLE = "timetable";
    private static final String STUDENT_GRADES = "student-grades";
    private static final String REGISTRATION = "registration";
    private static final String GRADEBOOK = "gradebook";
    private static final String ADMIN_STUDENTS = "admin-students";
    private static final String ADMIN_LECTURERS = "admin-lecturers";
    private static final String ADMIN_COURSES = "admin-courses";

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

        if ("SINHVIEN".equalsIgnoreCase(user.getRole())
                || user instanceof GiangVien
                || "ADMIN".equalsIgnoreCase(user.getRole())) {
            showScreen(OVERVIEW);
        } else {
            showError("Vai trò tài khoản không được hỗ trợ.");
        }
    }

    public void showTimetable() {
        showScreen(TIMETABLE);
    }

    @FXML
    private void onLogout() {
        if (!confirmLogout()) return;

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

    private boolean confirmLogout() {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/views/LogoutConfirmView.fxml"));
            Parent view = loader.load();
            LogoutConfirmController dialog = loader.getController();
            Stage dialogStage = new Stage();
            dialogStage.initOwner(navigationItems.getScene().getWindow());
            dialogStage.initModality(Modality.WINDOW_MODAL);
            dialogStage.setTitle("Xác nhận đăng xuất");
            dialogStage.setResizable(false);
            dialogStage.setScene(new Scene(view));
            dialogStage.showAndWait();
            return dialog.isConfirmed();
        } catch (IOException | RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unable to show logout confirmation.", e);
            showError("Không thể mở hộp thoại xác nhận đăng xuất.");
            return false;
        }
    }

    private void configureNavigation() {
        navigationItems.getChildren().clear();
        navigationButtons.clear();
        if ("SINHVIEN".equalsIgnoreCase(user.getRole())) {
            addNavigationButton("Tổng quan", OVERVIEW);
            addNavigationButton("Thời khóa biểu", TIMETABLE);
            addNavigationButton("Đăng ký tín chỉ", REGISTRATION);
            addNavigationButton("Điểm tổng kết", STUDENT_GRADES);
        } else if (user instanceof GiangVien) {
            addNavigationButton("Tổng quan", OVERVIEW);
            addNavigationButton("Lớp phụ trách và bảng điểm", GRADEBOOK);
        } else if ("ADMIN".equalsIgnoreCase(user.getRole())) {
            addNavigationButton("Tổng quan", OVERVIEW);
            addNavigationButton("Quản lý sinh viên", ADMIN_STUDENTS);
            addNavigationButton("Quản lý giảng viên", ADMIN_LECTURERS);
            addNavigationButton("Quản lý môn học", ADMIN_COURSES);
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
            case OVERVIEW -> "/views/DashBoardView.fxml";
            case TIMETABLE -> "/views/TimetableView.fxml";
            case STUDENT_GRADES -> "/views/StudentGradesView.fxml";
            case REGISTRATION -> "/views/RegistrationView.fxml";
            case GRADEBOOK -> "/views/GiangVienView.fxml";
            case ADMIN_STUDENTS, ADMIN_LECTURERS, ADMIN_COURSES -> "/views/AdminView.fxml";
            default -> throw new IllegalArgumentException("Unknown application screen.");
        };
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(resource));
            Parent page = loader.load();
            Object controller = loader.getController();
            if (controller instanceof DashBoardController dashboard) {
                dashboard.setLoggedInUser(user);
                dashboard.setAppShell(this);
            } else if (controller instanceof RegistrationController registration) {
                registration.setStudent(user);
            } else if (controller instanceof TimetableController timetable) {
                timetable.setStudent(user);
            } else if (controller instanceof StudentGradesController grades) {
                grades.setStudent(user);
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
            case OVERVIEW -> "Tổng quan";
            case TIMETABLE -> "Thời khóa biểu";
            case STUDENT_GRADES -> "Điểm tổng kết";
            case REGISTRATION -> "Đăng ký tín chỉ";
            case GRADEBOOK -> "Lớp phụ trách và bảng điểm";
            case ADMIN_STUDENTS -> "Quản lý sinh viên";
            case ADMIN_LECTURERS -> "Quản lý giảng viên";
            case ADMIN_COURSES -> "Quản lý môn học";
            default -> "";
        };
    }

    private String screenTitle(String screen) {
        return switch (screen) {
            case OVERVIEW -> "Tổng quan";
            case TIMETABLE -> "Thời khóa biểu";
            case STUDENT_GRADES -> "Điểm tổng kết";
            case REGISTRATION -> "Đăng ký tín chỉ";
            case GRADEBOOK -> "Lớp phụ trách và bảng điểm";
            case ADMIN_STUDENTS -> "Quản lý sinh viên & tài khoản";
            case ADMIN_LECTURERS -> "Quản lý giảng viên & tài khoản";
            case ADMIN_COURSES -> "Quản lý môn học";
            default -> "";
        };
    }

    private String roleLabel(Users authenticatedUser) {
        if (authenticatedUser instanceof GiangVien) return "GIẢNG VIÊN";
        if ("SINHVIEN".equalsIgnoreCase(authenticatedUser.getRole())) return "SINH VIÊN";
        if ("ADMIN".equalsIgnoreCase(authenticatedUser.getRole())) return "QUẢN TRỊ VIÊN";
        return authenticatedUser.getRole();
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Không thể tiếp tục");
        alert.showAndWait();
    }
}