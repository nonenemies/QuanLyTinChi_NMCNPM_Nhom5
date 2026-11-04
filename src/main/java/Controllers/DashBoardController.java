package Controllers;

import Model.Admin;
import Model.GiangVien;
import Model.LopHoc;
import Model.LopHocDangKy;
import Model.SinhVien;
import Model.TrangThaiDangKy;
import Model.TrangThaiHoatDong;
import Model.Users;
import Services.AdminService;
import Services.DiemService;
import Services.ServiceException;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DashBoardController {
    private static final Logger LOGGER = Logger.getLogger(DashBoardController.class.getName());
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.##");

    private final DiemService diemService = new DiemService();
    private final AdminService adminService = new AdminService();

    @FXML private Label lblWelcome;
    @FXML private Label lblRoleSummary;
    @FXML private Label lblRoleBadge;
    @FXML private Label lblAccountStatus;
    @FXML private Label lblStatus;
    @FXML private Label lblProfileTitle;
    @FXML private Label lblActivityTitle;
    @FXML private Label lblActivitySubtitle;
    @FXML private HBox summaryCards;
    @FXML private VBox profileDetails;
    @FXML private VBox activityList;
    @FXML private Button btnTimetable;

    private Users loggedInUser;
    private AppShellController appShell;

    @FXML
    private void initialize() {
        lblAccountStatus.setText("");
        lblStatus.setVisible(false);
        lblStatus.setManaged(false);
    }

    public void setLoggedInUser(Users user) {
        loggedInUser = user;
        if (user == null) {
            showError("Không xác định được tài khoản đang đăng nhập.");
            return;
        }
        lblWelcome.setText("Xin chào, " + valueOrDash(user.getHoTen()));
        lblRoleBadge.setText(roleLabel(user));
        lblAccountStatus.setText(user.getTrangThai() == TrangThaiHoatDong.HOAT_DONG
                ? "Tài khoản đang hoạt động" : "Tài khoản đã ngừng hoạt động");
        loadOverview();
    }

    public void setAppShell(AppShellController appShell) {
        this.appShell = appShell;
    }

    @FXML
    private void onRefresh() {
        loadOverview();
    }

    @FXML
    private void onOpenTimetable() {
        if (appShell != null && loggedInUser instanceof SinhVien) {
            appShell.showTimetable();
        }
    }

    private void loadOverview() {
        if (loggedInUser == null) return;
        summaryCards.getChildren().clear();
        profileDetails.getChildren().clear();
        activityList.getChildren().clear();
        btnTimetable.setVisible(loggedInUser instanceof SinhVien);
        btnTimetable.setManaged(loggedInUser instanceof SinhVien);

        try {
            if (loggedInUser instanceof SinhVien student) {
                loadStudentOverview(student);
            } else if (loggedInUser instanceof GiangVien lecturer) {
                loadLecturerOverview(lecturer);
            } else if (loggedInUser instanceof Admin admin) {
                loadAdminOverview(admin);
            } else {
                showError("Vai trò tài khoản không được hỗ trợ.");
            }
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Unable to load dashboard for role " + loggedInUser.getRole(), e);
            showError("Không thể tải dữ liệu tổng quan. Vui lòng thử tải lại.");
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while rendering dashboard.", e);
            showError("Đã xảy ra lỗi khi hiển thị dữ liệu tổng quan. Vui lòng tải lại.");
        }
    }

    private void loadStudentOverview(SinhVien student) {
        lblRoleSummary.setText("Thông tin học tập và tiến độ các học phần của bạn.");
        lblProfileTitle.setText("Thông tin sinh viên");
        lblActivityTitle.setText("Học phần đang theo học");
        lblActivitySubtitle.setText("Danh sách được lấy từ các đăng ký hiện còn hiệu lực.");

        List<LopHocDangKy> registrations = diemService.layKetQuaHocTap(student);
        long activeCourses = registrations.stream()
                .filter(item -> item.getTrangThaiDangKy() == TrangThaiDangKy.DANG_HOC).count();
        long earnedCredits = 0;
        Set<String> passedCourses = new LinkedHashSet<>();
        for (LopHocDangKy registration : registrations) {
            int credits = registration.getSoTinChi() == null ? 0 : registration.getSoTinChi();
            if (registration.getTrangThaiDangKy() == TrangThaiDangKy.DA_HOAN_THANH
                    && registration.getDiemTongKet() != null && registration.getDiemTongKet() >= 4
                    && passedCourses.add(registration.getMaMon())) {
                earnedCredits += credits;
            }
        }
        double gpa = diemService.tinhGpaTichLuy(registrations, null);
        String average = Double.isNaN(gpa) ? "—" : SCORE_FORMAT.format(gpa) + " / 10";
        addMetric("HỌC PHẦN ĐANG HỌC", Long.toString(activeCourses), "HP", "stat-icon-violet");
        addMetric("TÍN CHỈ ĐÃ TÍCH LŨY", Long.toString(earnedCredits), "TC", "stat-icon-blue");
        addMetric("ĐIỂM TRUNG BÌNH", average, "TB", "stat-icon-green");

        addProfileDetail("Email", valueOrDash(student.getEmail()));
        addProfileDetail("Mã sinh viên", valueOrDash(student.getMaSinhVien()));
        addProfileDetail("Lớp", valueOrDash(student.getLop()));
        addProfileDetail("Khoa", valueOrDash(student.getKhoa()));
        addProfileDetail("Khóa học", student.getKhoaHoc() == null ? "—" : student.getKhoaHoc().toString());
        addProfileDetail("Hệ đào tạo", valueOrDash(student.getHeDaoTao()));

        List<LopHocDangKy> activeRegistrations = registrations.stream()
                .filter(item -> item.getTrangThaiDangKy() == TrangThaiDangKy.DANG_HOC)
                .toList();
        if (activeRegistrations.isEmpty()) {
            addEmptyActivity("Hiện bạn chưa có học phần đang theo học.");
        } else {
            activeRegistrations.forEach(item -> addActivity(
                    item.getTenMon(),
                    item.getMaMon() + " · Lớp " + item.getMaLop() + " · Học kỳ " + item.getHocKy()
                            + " · " + item.getLichHoc(),
                    item.getDiemTongKet() == null
                            ? "Đang học" : "Điểm " + SCORE_FORMAT.format(item.getDiemTongKet())));
        }
        showInfo("Điểm trung bình tích lũy lấy điểm cao nhất của mỗi môn, có trọng số theo tín chỉ.");
    }

    private void loadLecturerOverview(GiangVien lecturer) {
        lblRoleSummary.setText("Thông tin giảng dạy và các lớp học phần được phân công.");
        lblProfileTitle.setText("Thông tin giảng viên");
        lblActivityTitle.setText("Lớp học phần phụ trách");
        lblActivitySubtitle.setText("Các lớp đang hoạt động và sĩ số hiện tại.");

        List<LopHoc> classes = diemService.layLopPhuTrach(lecturer).stream()
                .filter(item -> item.getTrangThai() == TrangThaiHoatDong.HOAT_DONG)
                .toList();
        int enrolledStudents = classes.stream()
                .map(LopHoc::getSiSoHienTai).filter(value -> value != null)
                .mapToInt(Integer::intValue).sum();
        addMetric("LỚP ĐANG PHỤ TRÁCH", Integer.toString(classes.size()), "LỚP", "stat-icon-violet");
        addMetric("SINH VIÊN ĐANG HỌC", Integer.toString(enrolledStudents), "SV", "stat-icon-blue");
        addMetric("HỌC KỲ TRONG PHÂN CÔNG", Long.toString(
                classes.stream().map(LopHoc::getHocKy).filter(value -> value != null).distinct().count()),
                "HK", "stat-icon-green");

        addProfileDetail("Email", valueOrDash(lecturer.getEmail()));
        addProfileDetail("Mã giảng viên", valueOrDash(lecturer.getMaGiangVien()));
        addProfileDetail("Khoa", valueOrDash(lecturer.getKhoa()));
        addProfileDetail("Chuyên ngành", valueOrDash(lecturer.getChuyenNganh()));
        addProfileDetail("Học vị", valueOrDash(lecturer.getHocVi()));

        if (classes.isEmpty()) {
            addEmptyActivity("Hiện chưa có lớp học phần đang hoạt động được phân công.");
        } else {
            classes.forEach(item -> addActivity(
                    "Lớp " + item.getMaLop() + " · " + item.getMaMon(),
                    "Học kỳ " + item.getHocKy() + " · Sĩ số "
                            + item.getSiSoHienTai() + "/" + item.getSiSoToiDa(),
                    "Đang hoạt động"));
        }
        showInfo("Mở mục “Lớp phụ trách và bảng điểm” trên thanh điều hướng để quản lý điểm.");
    }

    private void loadAdminOverview(Admin admin) {
        lblRoleSummary.setText("Tình hình tài khoản, môn học và lớp học phần trong hệ thống.");
        lblProfileTitle.setText("Thông tin quản trị viên");
        lblActivityTitle.setText("Lớp học phần đang hoạt động");
        lblActivitySubtitle.setText("Tối đa 8 lớp; tình trạng và sĩ số lấy từ dữ liệu hiện tại.");

        List<Users> accounts = adminService.layDanhSachTaiKhoan(admin);
        long activeStudents = accounts.stream()
                .filter(SinhVien.class::isInstance)
                .filter(user -> user.getTrangThai() == TrangThaiHoatDong.HOAT_DONG).count();
        long activeLecturers = accounts.stream()
                .filter(GiangVien.class::isInstance)
                .filter(user -> user.getTrangThai() == TrangThaiHoatDong.HOAT_DONG).count();
        List<LopHoc> classes = adminService.layDanhSachLopHoc(admin).stream()
                .filter(item -> item.getTrangThai() == TrangThaiHoatDong.HOAT_DONG)
                .toList();
        long activeCourses = adminService.layDanhSachMonHoc(admin).stream()
                .filter(item -> item.getTrangThai() == TrangThaiHoatDong.HOAT_DONG).count();

        addMetric("SINH VIÊN HOẠT ĐỘNG", Long.toString(activeStudents), "SV", "stat-icon-blue");
        addMetric("GIẢNG VIÊN HOẠT ĐỘNG", Long.toString(activeLecturers), "GV", "stat-icon-violet");
        addMetric("MÔN / LỚP ĐANG HOẠT ĐỘNG",
                activeCourses + " / " + classes.size(), "HP", "stat-icon-green");

        addProfileDetail("Email", valueOrDash(admin.getEmail()));
        addProfileDetail("Tài khoản", valueOrDash(admin.getMaSo()));
        addProfileDetail("Họ tên", valueOrDash(admin.getHoTen()));
        addProfileDetail("Khoa", valueOrDash(admin.getKhoa()));
        addProfileDetail("Trạng thái", admin.getTrangThai() == TrangThaiHoatDong.HOAT_DONG
                ? "Đang hoạt động" : "Ngừng hoạt động");

        if (classes.isEmpty()) {
            addEmptyActivity("Hiện chưa có lớp học phần đang hoạt động.");
        } else {
            classes.stream().limit(8).forEach(item -> addActivity(
                    "Lớp " + item.getMaLop() + " · " + item.getMaMon(),
                    "Học kỳ " + item.getHocKy() + " · Sĩ số "
                            + item.getSiSoHienTai() + "/" + item.getSiSoToiDa(),
                    "Đang hoạt động"));
        }
        showInfo("Các mục quản trị khác nằm trên thanh điều hướng bên trái.");
    }

    private void addMetric(String title, String value, String icon, String iconStyle) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("stat-label");
        Label iconLabel = new Label(icon);
        iconLabel.getStyleClass().add("stat-icon-label");
        VBox iconBox = new VBox(iconLabel);
        iconBox.getStyleClass().addAll("stat-icon", iconStyle);
        HBox heading = new HBox(titleLabel, new javafx.scene.layout.Region(), iconBox);
        heading.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(heading.getChildren().get(1), Priority.ALWAYS);

        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("stat-value");
        VBox card = new VBox(14, heading, valueLabel);
        card.getStyleClass().add("stat-card");
        HBox.setHgrow(card, Priority.ALWAYS);
        summaryCards.getChildren().add(card);
    }

    private void addProfileDetail(String label, String value) {
        HBox row = new HBox(12);
        Label key = new Label(label);
        key.getStyleClass().add("profile-detail-label");
        Label detail = new Label(value);
        detail.getStyleClass().add("profile-detail-value");
        detail.setWrapText(true);
        row.getChildren().addAll(key, new javafx.scene.layout.Region(), detail);
        HBox.setHgrow(row.getChildren().get(1), Priority.ALWAYS);
        profileDetails.getChildren().add(row);
    }

    private void addActivity(String title, String detail, String badgeText) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("course-name");
        Label detailLabel = new Label(detail);
        detailLabel.getStyleClass().add("course-meta");
        detailLabel.setWrapText(true);
        VBox text = new VBox(5, titleLabel, detailLabel);
        HBox row = new HBox(14, text, badge(badgeText));
        row.getStyleClass().add("schedule-row");
        row.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        HBox.setHgrow(text, Priority.ALWAYS);
        activityList.getChildren().add(row);
    }

    private void addEmptyActivity(String message) {
        Label empty = new Label(message);
        empty.getStyleClass().add("section-subtitle");
        empty.setWrapText(true);
        activityList.getChildren().add(empty);
    }

    private Label badge(String text) {
        Label label = new Label(text);
        label.getStyleClass().add("course-status");
        label.setWrapText(true);
        return label;
    }

    private String roleLabel(Users user) {
        if (user instanceof SinhVien) return "SINH VIÊN";
        if (user instanceof GiangVien) return "GIẢNG VIÊN";
        if (user instanceof Admin) return "QUẢN TRỊ VIÊN";
        return user.getRole();
    }

    private String valueOrDash(String value) {
        return value == null || value.isBlank() ? "—" : value;
    }

    private void showInfo(String message) {
        lblStatus.getStyleClass().removeAll("teacher-status-error", "teacher-status-info");
        lblStatus.getStyleClass().add("teacher-status-info");
        lblStatus.setText(message);
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

    private void showError(String message) {
        lblStatus.getStyleClass().removeAll("teacher-status-error", "teacher-status-info");
        lblStatus.getStyleClass().add("teacher-status-error");
        lblStatus.setText(message);
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }
}
