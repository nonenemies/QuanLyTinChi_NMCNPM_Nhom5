package Controllers;

import Model.Admin;
import Model.GiangVien;
import Model.MonHoc;
import Model.Nganh;
import Model.SinhVien;
import Model.TrangThaiHoatDong;
import Model.Users;
import Services.AdminService;
import Services.ServiceException;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;

import java.sql.Date;
import java.time.LocalDate;
import java.util.Locale;
import java.util.Optional;

public class AdminController {
    private static final String STUDENT_ROLE = "SINHVIEN";
    private static final String LECTURER_ROLE = "GIAOVIEN";

    private final AdminService adminService = new AdminService();
    private final ObservableList<Users> accounts = FXCollections.observableArrayList();
    private final FilteredList<Users> filteredAccounts = new FilteredList<>(accounts, item -> true);
    private final ObservableList<MonHoc> courses = FXCollections.observableArrayList();
    private final FilteredList<MonHoc> filteredCourses = new FilteredList<>(courses, item -> true);

    @FXML private Label lblStatus;
    @FXML private ScrollPane accountManagementPane;
    @FXML private ScrollPane courseManagementPane;
    @FXML private TableView<Users> tblAccounts;
    @FXML private TableColumn<Users, Integer> colUserId;
    @FXML private TableColumn<Users, String> colUserCode;
    @FXML private TableColumn<Users, String> colUserName;
    @FXML private TableColumn<Users, String> colUserEmail;
    @FXML private TableColumn<Users, String> colUserRole;
    @FXML private TableColumn<Users, String> colUserMajor;
    @FXML private TableColumn<Users, TrangThaiHoatDong> colUserStatus;
    @FXML private TextField txtAccountSearch;
    @FXML private ComboBox<String> cmbAccountRole;
    @FXML private TextField txtUserCode;
    @FXML private TextField txtUserName;
    @FXML private TextField txtUserEmail;
    @FXML private TextField txtUserPassword;
    @FXML private TextField txtUserMajor;
    @FXML private TextField txtStudentClass;
    @FXML private TextField txtStudentCohort;
    @FXML private TextField txtStudentDegree;
    @FXML private GridPane studentFields;
    @FXML private DatePicker dpStudentBirthDate;
    @FXML private ComboBox<String> cmbStudentGender;
    @FXML private TextField txtLecturerSpecialty;
    @FXML private TextField txtLecturerDegree;
    @FXML private GridPane lecturerFields;
    @FXML private ComboBox<TrangThaiHoatDong> cmbUserStatus;

    @FXML private TableView<MonHoc> tblCourses;
    @FXML private TableColumn<MonHoc, String> colCourseCode;
    @FXML private TableColumn<MonHoc, String> colCourseName;
    @FXML private TableColumn<MonHoc, Integer> colCourseCredits;
    @FXML private TableColumn<MonHoc, String> colCourseType;
    @FXML private TableColumn<MonHoc, TrangThaiHoatDong> colCourseStatus;
    @FXML private TextField txtCourseSearch;
    @FXML private TextField txtCourseCode;
    @FXML private TextField txtCourseName;
    @FXML private TextField txtCourseCredits;
    @FXML private TextField txtCourseType;
    @FXML private TextArea txtCourseDescription;

    private Admin admin;
    private Users selectedAccount;
    private MonHoc selectedCourse;

    @FXML
    private void initialize() {
        configureAccountTable();
        configureCourseTable();
        cmbAccountRole.setItems(FXCollections.observableArrayList(STUDENT_ROLE, LECTURER_ROLE));
        cmbAccountRole.getSelectionModel().selectFirst();
        cmbUserStatus.setItems(FXCollections.observableArrayList(TrangThaiHoatDong.values()));
        cmbUserStatus.getSelectionModel().select(TrangThaiHoatDong.HOAT_DONG);
        cmbStudentGender.setItems(FXCollections.observableArrayList("Không chỉ định", "Nam", "Nữ"));
        cmbStudentGender.getSelectionModel().selectFirst();
        tblAccounts.setItems(filteredAccounts);
        tblCourses.setItems(filteredCourses);
        txtAccountSearch.textProperty().addListener((o, oldValue, newValue) -> filterAccounts());
        txtCourseSearch.textProperty().addListener((o, oldValue, newValue) -> filterCourses());
        cmbAccountRole.valueProperty().addListener((o, oldValue, role) -> {
            updateRoleFields(role);
            filterAccounts();
        });
        tblAccounts.getSelectionModel().selectedItemProperty().addListener(
                (o, oldValue, selected) -> populateAccountForm(selected));
        tblCourses.getSelectionModel().selectedItemProperty().addListener(
                (o, oldValue, selected) -> populateCourseForm(selected));
        updateRoleFields(cmbAccountRole.getValue());
    }

    public void setAdmin(Users user, String section) {
        if (!(user instanceof Admin authenticatedAdmin)
                || authenticatedAdmin.getTrangThai() != TrangThaiHoatDong.HOAT_DONG) {
            setStatus("Không xác định được tài khoản admin đang hoạt động.", true);
            return;
        }
        admin = authenticatedAdmin;
        boolean coursesSection = "admin-courses".equals(section);
        accountManagementPane.setVisible(!coursesSection);
        accountManagementPane.setManaged(!coursesSection);
        courseManagementPane.setVisible(coursesSection);
        courseManagementPane.setManaged(coursesSection);
        if (!coursesSection) {
            cmbAccountRole.setValue("admin-lecturers".equals(section) ? LECTURER_ROLE : STUDENT_ROLE);
        }
        loadAllData();
    }

    @FXML
    private void onRefresh() {
        if (accountManagementPane.isVisible()) onClearAccountForm();
        else onClearCourseForm();
        loadAllData();
    }

    @FXML
    private void onClearAccountForm() {
        tblAccounts.getSelectionModel().clearSelection();
        selectedAccount = null;
        txtUserCode.clear();
        txtUserCode.setDisable(false);
        txtUserName.clear();
        txtUserEmail.clear();
        txtUserPassword.clear();
        txtUserPassword.setPromptText("Mật khẩu (bắt buộc khi tạo)");
        txtUserMajor.clear();
        txtStudentClass.clear();
        txtStudentCohort.clear();
        txtStudentDegree.clear();
        dpStudentBirthDate.setValue(null);
        cmbStudentGender.getSelectionModel().selectFirst();
        txtLecturerSpecialty.clear();
        txtLecturerDegree.clear();
        cmbAccountRole.setDisable(false);
        cmbUserStatus.getSelectionModel().select(TrangThaiHoatDong.HOAT_DONG);
    }

    @FXML
    private void onSaveAccount() {
        if (admin == null) return;
        try {
            Users account = buildAccountFromForm();
            boolean saved;
            if (selectedAccount == null) {
                saved = adminService.taoTaiKhoan(admin, account);
            } else {
                account.setUserID(selectedAccount.getUserID());
                if (account.getPasswordHash().isBlank()) {
                    account.setPasswordHash(selectedAccount.getPasswordHash());
                }
                saved = adminService.capNhatTaiKhoan(admin, account);
            }
            if (!saved) {
                setStatus("Không thể lưu tài khoản. Dữ liệu có thể đã thay đổi.", true);
                return;
            }
            onClearAccountForm();
            loadAccounts();
            setStatus("Đã lưu hồ sơ và tài khoản.", false);
        } catch (IllegalArgumentException | ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onDeactivateAccount() {
        if (admin == null || selectedAccount == null) {
            setStatus("Chọn một sinh viên hoặc giảng viên cần ngừng tài khoản.", true);
            return;
        }
        if (!confirm("Ngừng tài khoản " + selectedAccount.getMaSo() + "?",
                "Tài khoản sẽ không thể đăng nhập. Hồ sơ và dữ liệu học vụ được giữ lại.")) return;
        try {
            if (!adminService.ngungTaiKhoan(admin, selectedAccount.getUserID())) {
                setStatus("Tài khoản không còn hoạt động hoặc đã thay đổi.", true);
                return;
            }
            onClearAccountForm();
            loadAccounts();
            setStatus("Đã ngừng hoạt động tài khoản.", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onClearCourseForm() {
        tblCourses.getSelectionModel().clearSelection();
        selectedCourse = null;
        txtCourseCode.clear();
        txtCourseCode.setDisable(false);
        txtCourseName.clear();
        txtCourseCredits.clear();
        txtCourseType.clear();
        txtCourseDescription.clear();
    }

    @FXML
    private void onSaveCourse() {
        try {
            String code = required(txtCourseCode.getText(), "Mã môn học");
            String name = required(txtCourseName.getText(), "Tên môn học");
            int credits = parsePositiveInt(txtCourseCredits.getText(), "Số tín chỉ");
            String type = required(txtCourseType.getText(), "Loại môn");
            if (code.length() > 20 || name.length() > 200 || type.length() > 20) {
                throw new IllegalArgumentException("Mã môn, tên môn hoặc loại môn vượt quá giới hạn.");
            }
            MonHoc course = new MonHoc(code, name, credits, type, optional(txtCourseDescription.getText()),
                    selectedCourse == null ? TrangThaiHoatDong.HOAT_DONG : selectedCourse.getTrangThai());
            boolean saved = selectedCourse == null
                    ? adminService.taoMonHoc(admin, course) : adminService.capNhatMonHoc(admin, course);
            if (!saved) {
                setStatus("Không thể lưu môn học.", true);
                return;
            }
            onClearCourseForm();
            loadCourses();
            setStatus("Đã lưu môn học.", false);
        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onDeactivateCourse() {
        if (selectedCourse == null) {
            setStatus("Chọn môn học cần ngừng hoạt động.", true);
            return;
        }
        if (!confirm("Ngừng môn " + selectedCourse.getMaMon() + "?", "Môn học sẽ được giữ lại để bảo toàn dữ liệu lịch sử.")) return;
        try {
            if (!adminService.ngungMonHoc(admin, selectedCourse.getMaMon())) {
                setStatus("Môn học không còn hoạt động hoặc đã thay đổi.", true);
                return;
            }
            onClearCourseForm();
            loadCourses();
            setStatus("Đã ngừng hoạt động môn học.", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    private void configureAccountTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userID"));
        colUserCode.setCellValueFactory(new PropertyValueFactory<>("maSo"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserMajor.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue() instanceof SinhVien student && student.getNganh() != null
                        ? student.getNganh().getMaNganh() : ""));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
    }

    private void configureCourseTable() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colCourseCredits.setCellValueFactory(new PropertyValueFactory<>("soTinChi"));
        colCourseType.setCellValueFactory(new PropertyValueFactory<>("loaiMon"));
        colCourseStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
    }

    private void loadAllData() {
        if (admin == null) return;
        if (accountManagementPane.isVisible()) loadAccounts();
        else loadCourses();
    }

    private void loadAccounts() {
        try {
            accounts.setAll(adminService.layDanhSachTaiKhoan(admin));
            filterAccounts();
        } catch (ServiceException e) {
            setStatus("Không thể tải tài khoản. Vui lòng kiểm tra kết nối cơ sở dữ liệu.", true);
        }
    }

    private void loadCourses() {
        try {
            courses.setAll(adminService.layDanhSachMonHoc(admin));
            filterCourses();
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    private void filterAccounts() {
        String query = normalized(txtAccountSearch.getText());
        String role = cmbAccountRole.getValue();
        filteredAccounts.setPredicate(account -> (role == null || role.equalsIgnoreCase(account.getRole()))
                && (account.getMaSo() + " " + account.getHoTen() + " "
                + account.getEmail() + " " + account.getRole() + " " + accountNganh(account))
                .toLowerCase(Locale.ROOT).contains(query));
    }

    private String accountNganh(Users account) {
        return account instanceof SinhVien student && student.getNganh() != null
                ? optional(student.getNganh().getMaNganh()) : "";
    }

    private void filterCourses() {
        String query = normalized(txtCourseSearch.getText());
        filteredCourses.setPredicate(course -> (course.getMaMon() + " " + course.getTenMon() + " "
                + course.getLoaiMon()).toLowerCase(Locale.ROOT).contains(query));
    }

    private void populateAccountForm(Users selected) {
        selectedAccount = selected;
        if (selected == null) return;
        cmbAccountRole.setValue(selected instanceof SinhVien ? STUDENT_ROLE : LECTURER_ROLE);
        cmbAccountRole.setDisable(true);
        txtUserCode.setText(selected.getMaSo());
        txtUserCode.setDisable(true);
        txtUserName.setText(selected.getHoTen());
        txtUserEmail.setText(selected.getEmail());
        txtUserPassword.clear();
        txtUserPassword.setPromptText("Để trống để giữ mật khẩu");
        txtUserMajor.setText(selected instanceof SinhVien student && student.getNganh() != null
                ? optional(student.getNganh().getMaNganh()) : "");
        cmbUserStatus.setValue(selected.getTrangThai());
        if (selected instanceof SinhVien student) {
            txtStudentClass.setText(student.getLop());
            txtStudentCohort.setText(student.getKhoaHoc() == null ? "" : student.getKhoaHoc().toString());
            txtStudentDegree.setText(student.getHeDaoTao());
            dpStudentBirthDate.setValue(student.getNgaySinh() == null ? null
                    : new Date(student.getNgaySinh().getTime()).toLocalDate());
            cmbStudentGender.setValue(student.getGioiTinh() == null ? "Không chỉ định"
                    : student.getGioiTinh() ? "Nam" : "Nữ");
        } else if (selected instanceof GiangVien lecturer) {
            txtLecturerSpecialty.setText(lecturer.getChuyenNganh());
            txtLecturerDegree.setText(lecturer.getHocVi());
        }
        updateRoleFields(cmbAccountRole.getValue());
    }

    private Users buildAccountFromForm() {
        String role = cmbAccountRole.getValue();
        String code = required(txtUserCode.getText(), "Mã tài khoản");
        String name = required(txtUserName.getText(), "Họ tên");
        String email = required(txtUserEmail.getText(), "Email");
        String password = txtUserPassword.getText() == null ? "" : txtUserPassword.getText().trim();
        if (selectedAccount == null && password.isBlank()) {
            throw new IllegalArgumentException("Cần nhập mật khẩu cho tài khoản mới.");
        }
        if (selectedAccount != null && password.isBlank()) password = selectedAccount.getPasswordHash();
        String major = optional(txtUserMajor.getText());
        TrangThaiHoatDong status = cmbUserStatus.getValue();
        if (role == null || status == null) throw new IllegalArgumentException("Chọn vai trò và trạng thái tài khoản.");
        if (STUDENT_ROLE.equals(role)) {
            Integer cohort = txtStudentCohort.getText().isBlank() ? null
                    : parsePositiveInt(txtStudentCohort.getText(), "Khóa học");
            String genderText = cmbStudentGender.getValue();
            Boolean gender = "Nam".equals(genderText) ? Boolean.TRUE
                    : "Nữ".equals(genderText) ? Boolean.FALSE : null;
            LocalDate birth = dpStudentBirthDate.getValue();
            return new SinhVien(selectedAccount == null ? null : selectedAccount.getUserID(),
                    name, email, password, STUDENT_ROLE, code,
                    major == null ? null : new Nganh(major, null), null, status,
                    code, birth == null ? null : Date.valueOf(birth), gender,
                    optional(txtStudentClass.getText()), cohort, optional(txtStudentDegree.getText()));
        }
        if (LECTURER_ROLE.equals(role)) {
            return new GiangVien(selectedAccount == null ? null : selectedAccount.getUserID(),
                    name, email, password, LECTURER_ROLE, code, null, status,
                    code, optional(txtLecturerSpecialty.getText()), optional(txtLecturerDegree.getText()));
        }
        throw new IllegalArgumentException("Vai trò tài khoản không hợp lệ.");
    }

    private void populateCourseForm(MonHoc selected) {
        selectedCourse = selected;
        if (selected == null) return;
        txtCourseCode.setText(selected.getMaMon());
        txtCourseCode.setDisable(true);
        txtCourseName.setText(selected.getTenMon());
        txtCourseCredits.setText(selected.getSoTinChi().toString());
        txtCourseType.setText(selected.getLoaiMon());
        txtCourseDescription.setText(selected.getMoTa());
    }

    private void updateRoleFields(String role) {
        boolean student = STUDENT_ROLE.equals(role);
        boolean lecturer = LECTURER_ROLE.equals(role);
        studentFields.setManaged(student);
        studentFields.setVisible(student);
        lecturerFields.setManaged(lecturer);
        lecturerFields.setVisible(lecturer);
    }

    private boolean confirm(String title, String detail) {
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận thao tác");
        alert.setHeaderText(title);
        alert.setContentText(detail);
        Optional<ButtonType> response = alert.showAndWait();
        return response.isPresent() && response.get() == ButtonType.OK;
    }

    private String required(String value, String field) {
        if (value == null || value.isBlank()) throw new IllegalArgumentException(field + " không được để trống.");
        return value.trim();
    }

    private String optional(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private int parsePositiveInt(String value, String field) {
        try {
            int result = Integer.parseInt(value.trim());
            if (result > 0) return result;
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException(field + " phải là số nguyên dương.");
    }

    private String normalized(String text) {
        return text == null ? "" : text.trim().toLowerCase(Locale.ROOT);
    }

    private void setStatus(String message, boolean error) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("teacher-status-error", "teacher-status-info");
        lblStatus.getStyleClass().add(error ? "teacher-status-error" : "teacher-status-info");
    }
}
