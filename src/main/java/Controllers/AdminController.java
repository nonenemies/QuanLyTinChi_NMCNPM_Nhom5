package Controllers;

import Model.Admin;
import Model.GiangVien;
import Model.LopHoc;
import Model.LichHoc;
import Model.MonHoc;
import Model.MonTienQuyet;
import Model.SinhVien;
import Model.ThanhPhanDanhGia;
import Model.TrangThaiHoatDong;
import Model.Users;
import Model.CauHinhHocKy;
import Model.DotDangKyNganh;
import Services.AdminService;
import Services.ServiceException;
import Services.ThanhPhanDanhGiaService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.scene.control.DatePicker;
import javafx.scene.control.Label;
import javafx.scene.control.TabPane;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;

import java.sql.Date;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

public class AdminController {
    private static final String STUDENT_ROLE = "SINHVIEN";
    private static final String LECTURER_ROLE = "GIAOVIEN";
    private static final DateTimeFormatter TERM_DATE_TIME =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final AdminService adminService = new AdminService();
    private final ThanhPhanDanhGiaService thanhPhanService = new ThanhPhanDanhGiaService();
    private final ObservableList<Users> accounts = FXCollections.observableArrayList();
    private final FilteredList<Users> filteredAccounts = new FilteredList<>(accounts, item -> true);
    private final ObservableList<MonHoc> courses = FXCollections.observableArrayList();
    private final FilteredList<MonHoc> filteredCourses = new FilteredList<>(courses, item -> true);
    private final ObservableList<LopHoc> classes = FXCollections.observableArrayList();
    private final FilteredList<LopHoc> filteredClasses = new FilteredList<>(classes, item -> true);
    private final ObservableList<MonTienQuyet> prerequisites = FXCollections.observableArrayList();
    private final ObservableList<ThanhPhanDanhGia> components = FXCollections.observableArrayList();
    private final ObservableList<LichHoc> schedules = FXCollections.observableArrayList();

    @FXML private TabPane adminTabs;
    @FXML private Label lblStatus;
    @FXML private TableView<Users> tblAccounts;
    @FXML private TableColumn<Users, Integer> colUserId;
    @FXML private TableColumn<Users, String> colUserCode;
    @FXML private TableColumn<Users, String> colUserName;
    @FXML private TableColumn<Users, String> colUserEmail;
    @FXML private TableColumn<Users, String> colUserRole;
    @FXML private TableColumn<Users, String> colUserFaculty;
    @FXML private TableColumn<Users, TrangThaiHoatDong> colUserStatus;
    @FXML private TextField txtAccountSearch;
    @FXML private ComboBox<String> cmbAccountRole;
    @FXML private TextField txtUserCode;
    @FXML private TextField txtUserName;
    @FXML private TextField txtUserEmail;
    @FXML private TextField txtUserPassword;
    @FXML private TextField txtUserFaculty;
    @FXML private TextField txtStudentClass;
    @FXML private TextField txtStudentCohort;
    @FXML private TextField txtStudentDegree;
    @FXML private DatePicker dpStudentBirthDate;
    @FXML private ComboBox<String> cmbStudentGender;
    @FXML private TextField txtLecturerSpecialty;
    @FXML private TextField txtLecturerDegree;
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
    @FXML private TableView<MonTienQuyet> tblPrerequisites;
    @FXML private TableColumn<MonTienQuyet, String> colPrerequisiteCode;
    @FXML private TableColumn<MonTienQuyet, String> colPrerequisiteName;
    @FXML private ComboBox<MonHoc> cmbPrerequisite;

    @FXML private TableView<LopHoc> tblClasses;
    @FXML private TableColumn<LopHoc, String> colClassCode;
    @FXML private TableColumn<LopHoc, String> colClassCourse;
    @FXML private TableColumn<LopHoc, String> colClassLecturer;
    @FXML private TableColumn<LopHoc, String> colClassSemester;
    @FXML private TableColumn<LopHoc, String> colClassCapacity;
    @FXML private TableColumn<LopHoc, TrangThaiHoatDong> colClassStatus;
    @FXML private TextField txtClassSearch;
    @FXML private TextField txtClassCode;
    @FXML private ComboBox<MonHoc> cmbClassCourse;
    @FXML private ComboBox<GiangVien> cmbClassLecturer;
    @FXML private TextField txtClassSemester;
    @FXML private TextField txtClassCapacity;

    @FXML private ComboBox<MonHoc> cmbComponentCourse;
    @FXML private TableView<ThanhPhanDanhGia> tblComponents;
    @FXML private TableColumn<ThanhPhanDanhGia, Integer> colComponentOrder;
    @FXML private TableColumn<ThanhPhanDanhGia, String> colComponentName;
    @FXML private TableColumn<ThanhPhanDanhGia, Float> colComponentWeight;
    @FXML private TableColumn<ThanhPhanDanhGia, Boolean> colComponentActive;
    @FXML private TextField txtComponentName;
    @FXML private TextField txtComponentWeight;
    @FXML private TextField txtComponentOrder;
    @FXML private CheckBox chkComponentActive;
    @FXML private ComboBox<LopHoc> cmbScheduleClass;
    @FXML private TableView<LichHoc> tblSchedules;
    @FXML private TableColumn<LichHoc, Integer> colScheduleDay;
    @FXML private TableColumn<LichHoc, String> colScheduleStart;
    @FXML private TableColumn<LichHoc, String> colScheduleEnd;
    @FXML private TableColumn<LichHoc, String> colScheduleRoom;
    @FXML private TextField txtScheduleDay;
    @FXML private TextField txtScheduleStart;
    @FXML private TextField txtScheduleEnd;
    @FXML private TextField txtScheduleRoom;
    @FXML private TextField txtTermCode;
    @FXML private DatePicker dpSemesterEnd;
    @FXML private TextField txtGradeEntryDeadline;
    @FXML private ComboBox<String> cmbProgramMajor;
    @FXML private TableView<MonHoc> tblProgramCourses;
    @FXML private TableColumn<MonHoc, String> colProgramCourseCode;
    @FXML private TableColumn<MonHoc, String> colProgramCourseName;
    @FXML private DatePicker dpMajorRegistrationOpensOn;
    @FXML private DatePicker dpMajorRegistrationClosesOn;

    private Admin admin;
    private Users selectedAccount;
    private MonHoc selectedCourse;
    private LopHoc selectedClass;
    private ThanhPhanDanhGia selectedComponent;
    private LichHoc selectedSchedule;
    private List<ThanhPhanDanhGia> loadedComponents = List.of();
    private List<ThanhPhanDanhGia> newComponents = new ArrayList<>();

    @FXML
    private void initialize() {
        configureAccountTable();
        configureCourseTable();
        configureClassTable();
        configureComponentTable();
        configureScheduleTable();
        colProgramCourseCode.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colProgramCourseName.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        cmbAccountRole.setItems(FXCollections.observableArrayList(STUDENT_ROLE, LECTURER_ROLE));
        cmbAccountRole.getSelectionModel().selectFirst();
        cmbUserStatus.setItems(FXCollections.observableArrayList(TrangThaiHoatDong.values()));
        cmbUserStatus.getSelectionModel().select(TrangThaiHoatDong.HOAT_DONG);
        cmbStudentGender.setItems(FXCollections.observableArrayList("Không chỉ định", "Nam", "Nữ"));
        cmbStudentGender.getSelectionModel().selectFirst();
        tblAccounts.setItems(filteredAccounts);
        tblCourses.setItems(filteredCourses);
        tblClasses.setItems(filteredClasses);
        txtAccountSearch.textProperty().addListener((o, oldValue, newValue) -> filterAccounts());
        txtCourseSearch.textProperty().addListener((o, oldValue, newValue) -> filterCourses());
        txtClassSearch.textProperty().addListener((o, oldValue, newValue) -> filterClasses());
        cmbAccountRole.valueProperty().addListener((o, oldValue, role) -> {
            updateRoleFields(role);
            filterAccounts();
        });
        tblAccounts.getSelectionModel().selectedItemProperty().addListener(
                (o, oldValue, selected) -> populateAccountForm(selected));
        tblCourses.getSelectionModel().selectedItemProperty().addListener(
                (o, oldValue, selected) -> populateCourseForm(selected));
        tblClasses.getSelectionModel().selectedItemProperty().addListener(
                (o, oldValue, selected) -> populateClassForm(selected));
        tblComponents.getSelectionModel().selectedItemProperty().addListener(
                (o, oldValue, selected) -> populateComponentForm(selected));
        tblSchedules.getSelectionModel().selectedItemProperty().addListener(
                (o, oldValue, selected) -> populateScheduleForm(selected));
        cmbScheduleClass.valueProperty().addListener(
                (o, oldValue, selected) -> loadSchedules(selected));
        cmbComponentCourse.valueProperty().addListener((o, oldValue, selected) -> loadComponents(selected));
        cmbClassCourse.setConverter(courseConverter());
        cmbPrerequisite.setConverter(courseConverter());
        cmbComponentCourse.setConverter(courseConverter());
        cmbScheduleClass.setConverter(new StringConverter<>() {
            @Override
            public String toString(LopHoc courseClass) {
                return courseClass == null ? "" : courseClass.getMaLop() + " · "
                        + courseClass.getMaMon() + " · HK " + courseClass.getHocKy();
            }

            @Override
            public LopHoc fromString(String value) {
                return null;
            }
        });
        cmbClassLecturer.setConverter(new StringConverter<>() {
            @Override
            public String toString(GiangVien lecturer) {
                return lecturer == null ? "" : lecturer.getHoTen() + " · " + lecturer.getMaGiangVien();
            }

            @Override
            public GiangVien fromString(String value) {
                return null;
            }
        });
        updateRoleFields(cmbAccountRole.getValue());
    }

    public void setAdmin(Users user, String section) {
        if (!(user instanceof Admin authenticatedAdmin)
                || authenticatedAdmin.getTrangThai() != TrangThaiHoatDong.HOAT_DONG) {
            setStatus("Không xác định được tài khoản admin đang hoạt động.", true);
            return;
        }
        admin = authenticatedAdmin;
        adminTabs.getSelectionModel().select(sectionIndex(section));
        if ("admin-students".equals(section)) cmbAccountRole.setValue(STUDENT_ROLE);
        else if ("admin-lecturers".equals(section)) cmbAccountRole.setValue(LECTURER_ROLE);
        loadAllData();
    }

    @FXML
    private void onRefresh() {
        onClearAccountForm();
        onClearCourseForm();
        onClearClassForm();
        onClearComponentForm();
        loadAllData();
        onLoadTermSettings();
    }

    @FXML
    private void onLoadTermSettings() {
        String hocKy = txtTermCode.getText() == null ? "" : txtTermCode.getText().trim();
        if (admin == null || hocKy.isBlank()) return;
        try {
            CauHinhHocKy config = adminService.layCauHinhHocKy(admin, hocKy);
            if (config == null) {
                dpSemesterEnd.setValue(null);
                txtGradeEntryDeadline.clear();
                setStatus("Học kỳ chưa có cấu hình điểm; nhập ngày kết thúc và hạn nhập điểm.", false);
                return;
            }
            dpSemesterEnd.setValue(config.ketThucHocKy());
            txtGradeEntryDeadline.setText(TERM_DATE_TIME.format(config.hanNhapDiem()));
            setStatus("Đã tải mốc điểm của học kỳ " + hocKy + ".", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onSaveTermSettings() {
        try {
            LocalDateTime gradeDeadline = LocalDateTime.parse(
                    required(txtGradeEntryDeadline.getText(), "Hạn nhập điểm"), TERM_DATE_TIME);
            LocalDate semesterEnd = dpSemesterEnd.getValue();
            if (semesterEnd == null) throw new IllegalArgumentException("Chọn ngày kết thúc học kỳ.");
            boolean saved = adminService.luuCauHinhHocKy(admin, required(txtTermCode.getText(), "Mã học kỳ"),
                    semesterEnd, gradeDeadline);
            setStatus(saved ? "Đã lưu mốc nhập điểm cho học kỳ." : "Không thể lưu cấu hình điểm.", !saved);
        } catch (DateTimeParseException e) {
            setStatus("Hạn nhập điểm cần theo định dạng yyyy-MM-dd HH:mm, ví dụ 2026-11-02 08:00.", true);
        } catch (IllegalArgumentException | ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onLoadMajorRegistration() {
            String major = cmbProgramMajor.getValue() == null ? "" : cmbProgramMajor.getValue().trim();
            if (admin == null || major.isBlank()) return;
            try {
                tblProgramCourses.setItems(FXCollections.observableArrayList(
                        adminService.layMonTheoKhoa(admin, major)));
                DotDangKyNganh period = adminService.layDotDangKyNganh(admin, major);
                if (period == null) {
                    dpMajorRegistrationOpensOn.setValue(null);
                    dpMajorRegistrationClosesOn.setValue(null);
                    setStatus("Chưa có lịch đăng ký cho khoa/ngành này.", false);
                } else {
                    dpMajorRegistrationOpensOn.setValue(period.moDangKy().toLocalDate());
                    dpMajorRegistrationClosesOn.setValue(period.dongDangKy().toLocalDate());
                    setStatus("Đã tải chương trình và lịch đăng ký của " + major + ".", false);
                }
            } catch (ServiceException e) {
                setStatus(e.getMessage(), true);
            }
        }

    @FXML
    private void onSaveMajorRegistration() {
            try {
                String major = required(cmbProgramMajor.getValue(), "Khoa/ngành");
                LocalDate opensOn = dpMajorRegistrationOpensOn.getValue();
                LocalDate closesOn = dpMajorRegistrationClosesOn.getValue();
                if (opensOn == null) throw new IllegalArgumentException("Chọn ngày mở đăng ký.");
                if (closesOn == null) throw new IllegalArgumentException("Chọn ngày đóng đăng ký.");
                LocalDateTime opensAt = opensOn.atTime(12, 0);
                LocalDateTime closesAt = closesOn.atTime(12, 0);
                boolean saved = adminService.luuDotDangKyNganh(admin, major, opensAt, closesAt);
                setStatus(saved ? "Đã lưu lịch đăng ký cho " + major + "." : "Không thể lưu lịch đăng ký.", !saved);
            } catch (IllegalArgumentException | ServiceException e) {
                setStatus(e.getMessage(), true);
            }
        }

    @FXML
    private void onAddAllProgramCourses() {
            String major = cmbProgramMajor.getValue() == null ? "" : cmbProgramMajor.getValue().trim();
            if (major.isBlank()) {
                setStatus("Chọn khoa/ngành trước khi thêm môn.", true);
                return;
            }
            try {
                int addedCount = adminService.themTatCaMonVaoChuongTrinh(admin, major);
                onLoadMajorRegistration();
                setStatus(addedCount == 0
                        ? "Tất cả môn đang hoạt động đã có trong chương trình."
                        : "Đã thêm " + addedCount + " môn vào chương trình " + major + ".", false);
            } catch (ServiceException e) {
                setStatus(e.getMessage(), true);
            }
        }

    @FXML
    private void onRemoveProgramCourse() {
            MonHoc course = tblProgramCourses.getSelectionModel().getSelectedItem();
            String major = cmbProgramMajor.getValue() == null ? "" : cmbProgramMajor.getValue().trim();
            if (course == null) {
                setStatus("Chọn môn cần gỡ khỏi chương trình đào tạo.", true);
                return;
            }
            try {
                if (adminService.xoaMonKhoiChuongTrinh(admin, major, course.getMaMon())) {
                    onLoadMajorRegistration();
                    setStatus("Đã gỡ môn khỏi chương trình.", false);
                }
            } catch (ServiceException e) {
                setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onClearAccountForm() {
        tblAccounts.getSelectionModel().clearSelection();
        selectedAccount = null;
        txtUserCode.clear();
        txtUserName.clear();
        txtUserEmail.clear();
        txtUserPassword.clear();
        txtUserPassword.setPromptText("Mật khẩu (bắt buộc khi tạo)");
        txtUserFaculty.clear();
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
            loadLecturers();
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
        tblPrerequisites.getItems().clear();
        cmbPrerequisite.getItems().clear();
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
                    ? adminService.themMonHoc(admin, course) : adminService.suaMonHoc(admin, course);
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

    @FXML
    private void onAddPrerequisite() {
        MonHoc prerequisite = cmbPrerequisite.getValue();
        if (selectedCourse == null || prerequisite == null) {
            setStatus("Chọn môn học và môn tiên quyết trước.", true);
            return;
        }
        if (selectedCourse.getMaMon().equals(prerequisite.getMaMon())) {
            setStatus("Môn học không thể là tiên quyết của chính nó.", true);
            return;
        }
        try {
            if (!adminService.themMonTienQuyet(admin, selectedCourse.getMaMon(), prerequisite.getMaMon())) {
                setStatus("Không thêm được môn tiên quyết.", true);
                return;
            }
            loadPrerequisites();
            setStatus("Đã thêm môn tiên quyết.", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onRemovePrerequisite() {
        MonTienQuyet selected = tblPrerequisites.getSelectionModel().getSelectedItem();
        if (selectedCourse == null || selected == null) {
            setStatus("Chọn môn tiên quyết cần gỡ.", true);
            return;
        }
        try {
            if (!adminService.xoaMonTienQuyet(
                    admin, selectedCourse.getMaMon(), selected.getMaMonTienQuyet())) {
                setStatus("Quan hệ tiên quyết đã thay đổi.", true);
                return;
            }
            loadPrerequisites();
            setStatus("Đã gỡ môn tiên quyết.", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onClearClassForm() {
        tblClasses.getSelectionModel().clearSelection();
        selectedClass = null;
        txtClassCode.clear();
        txtClassCode.setDisable(false);
        cmbClassCourse.getSelectionModel().clearSelection();
        cmbClassLecturer.getSelectionModel().clearSelection();
        txtClassSemester.clear();
        txtClassCapacity.clear();
    }

    @FXML
    private void onSaveClass() {
        try {
            String code = required(txtClassCode.getText(), "Mã lớp");
            MonHoc course = cmbClassCourse.getValue();
            GiangVien lecturer = cmbClassLecturer.getValue();
            String semester = required(txtClassSemester.getText(), "Học kỳ");
            int capacity = parsePositiveInt(txtClassCapacity.getText(), "Sĩ số tối đa");
            if (code.length() > 20 || semester.length() > 20 || course == null || lecturer == null) {
                throw new IllegalArgumentException("Mã lớp/học kỳ không hợp lệ hoặc chưa chọn môn, giảng viên.");
            }
            LopHoc classItem = new LopHoc(code, course.getMaMon(), lecturer.getMaGiangVien(),
                    semester, capacity, selectedClass == null ? 0 : selectedClass.getSiSoHienTai(),
                    selectedClass == null ? TrangThaiHoatDong.HOAT_DONG : selectedClass.getTrangThai());
            boolean saved = selectedClass == null
                    ? adminService.themLopHoc(admin, classItem) : adminService.suaLopHoc(admin, classItem);
            if (!saved) {
                setStatus("Không thể lưu lớp. Sĩ số tối đa không được thấp hơn số sinh viên đã đăng ký.", true);
                return;
            }
            onClearClassForm();
            loadClasses();
            setStatus("Đã lưu lớp học phần.", false);
        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onDeactivateClass() {
        if (selectedClass == null) {
            setStatus("Chọn lớp học phần cần ngừng hoạt động.", true);
            return;
        }
        if (!confirm("Ngừng lớp " + selectedClass.getMaLop() + "?", "Dữ liệu đăng ký và điểm sẽ được giữ lại.")) return;
        try {
            if (!adminService.ngungLopHoc(admin, selectedClass.getMaLop())) {
                setStatus("Lớp đã ngừng hoạt động hoặc đã thay đổi.", true);
                return;
            }
            onClearClassForm();
            loadClasses();
            setStatus("Đã ngừng hoạt động lớp học phần.", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onAddComponent() {
        MonHoc course = cmbComponentCourse.getValue();
        if (course == null) {
            setStatus("Chọn môn học trước khi thêm thành phần đánh giá.", true);
            return;
        }
        if (hasPersistedActiveComponents()) {
            setStatus("Môn đã có cấu hình hoạt động; hãy chỉnh sửa cấu hình hiện có.", true);
            return;
        }
        try {
            String name = required(txtComponentName.getText(), "Tên thành phần");
            float weight = parseWeight(txtComponentWeight.getText());
            int order = txtComponentOrder.getText().isBlank()
                    ? nextComponentOrder() : parsePositiveInt(txtComponentOrder.getText(), "Thứ tự");
            if (name.length() > 200) throw new IllegalArgumentException("Tên thành phần tối đa 200 ký tự.");
            ThanhPhanDanhGia newComponent = new ThanhPhanDanhGia(
                    0, course.getMaMon(), name, weight, order, true);
            newComponents.add(newComponent);
            components.add(newComponent);
            tblComponents.refresh();
            txtComponentName.clear();
            txtComponentWeight.clear();
            txtComponentOrder.clear();
            setStatus("Đã thêm vào cấu hình tạm. Có thể thêm tiếp; tổng trọng số phải bằng 1 trước khi lưu.", false);
        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onApplyComponentEdit() {
        if (selectedComponent == null) {
            setStatus("Chọn thành phần cần chỉnh sửa.", true);
            return;
        }
        try {
            String name = required(txtComponentName.getText(), "Tên thành phần");
            float weight = parseWeight(txtComponentWeight.getText());
            int order = parsePositiveInt(txtComponentOrder.getText(), "Thứ tự");
            if (name.length() > 200) throw new IllegalArgumentException("Tên thành phần tối đa 200 ký tự.");
            selectedComponent.setTenTP(name);
            selectedComponent.setTrongSo(weight);
            selectedComponent.setThuTu(order);
            selectedComponent.setTrangThai(chkComponentActive.isSelected());
            tblComponents.refresh();
            setStatus("Đã cập nhật dữ liệu trong bảng. Bấm lưu để kiểm tra tổng trọng số.", false);
        } catch (IllegalArgumentException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onSaveComponents() {
        MonHoc course = cmbComponentCourse.getValue();
        if (admin == null || course == null) {
            setStatus("Chọn môn học trước khi lưu cấu hình đánh giá.", true);
            return;
        }
        try {
            List<ThanhPhanDanhGia> toCreate = newComponents.stream()
                    .filter(component -> component.getMaTP() == 0).toList();
            if (!toCreate.isEmpty()) {
                if (hasPersistedActiveComponents()) {
                    setStatus("Môn đã có cấu hình hoạt động. Không thể thêm cấu hình mới.", true);
                    return;
                }
                thanhPhanService.themThanhPhan(admin, course.getMaMon(), toCreate);
            } else {
                List<ThanhPhanDanhGia> toUpdate = components.stream()
                        .filter(component -> component.getMaTP() > 0)
                        .map(this::copyComponent).toList();
                thanhPhanService.suaThanhPhan(admin, course.getMaMon(), toUpdate);
            }
            loadComponents(course);
            setStatus("Đã lưu cấu hình thành phần đánh giá.", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onClearComponentForm() {
        tblComponents.getSelectionModel().clearSelection();
        selectedComponent = null;
        txtComponentName.clear();
        txtComponentWeight.clear();
        txtComponentOrder.clear();
        chkComponentActive.setSelected(true);
    }

    private void configureAccountTable() {
        colUserId.setCellValueFactory(new PropertyValueFactory<>("userID"));
        colUserCode.setCellValueFactory(new PropertyValueFactory<>("maSo"));
        colUserName.setCellValueFactory(new PropertyValueFactory<>("hoTen"));
        colUserEmail.setCellValueFactory(new PropertyValueFactory<>("email"));
        colUserRole.setCellValueFactory(new PropertyValueFactory<>("role"));
        colUserFaculty.setCellValueFactory(new PropertyValueFactory<>("khoa"));
        colUserStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
    }

    private void configureCourseTable() {
        colCourseCode.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colCourseCredits.setCellValueFactory(new PropertyValueFactory<>("soTinChi"));
        colCourseType.setCellValueFactory(new PropertyValueFactory<>("loaiMon"));
        colCourseStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        colPrerequisiteCode.setCellValueFactory(new PropertyValueFactory<>("maMonTienQuyet"));
        colPrerequisiteName.setCellValueFactory(new PropertyValueFactory<>("tenMonTienQuyet"));
        tblPrerequisites.setItems(prerequisites);
    }

    private void configureClassTable() {
        colClassCode.setCellValueFactory(new PropertyValueFactory<>("maLop"));
        colClassCourse.setCellValueFactory(new PropertyValueFactory<>("maMon"));
        colClassLecturer.setCellValueFactory(new PropertyValueFactory<>("maGV"));
        colClassSemester.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
        colClassCapacity.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(
                data.getValue().getSiSoHienTai() + "/" + data.getValue().getSiSoToiDa()));
        colClassStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
    }

    private void configureComponentTable() {
        colComponentOrder.setCellValueFactory(new PropertyValueFactory<>("thuTu"));
        colComponentName.setCellValueFactory(new PropertyValueFactory<>("tenTP"));
        colComponentWeight.setCellValueFactory(new PropertyValueFactory<>("trongSo"));
        colComponentActive.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        tblComponents.setItems(components);
    }

    private void configureScheduleTable() {
        colScheduleDay.setCellValueFactory(data ->
                new javafx.beans.property.ReadOnlyObjectWrapper<>(data.getValue().getThu()));
        colScheduleStart.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(
                data.getValue().getGioBatDau().toString()));
        colScheduleEnd.setCellValueFactory(data -> new javafx.beans.property.ReadOnlyStringWrapper(
                data.getValue().getGioKetThuc().toString()));
        colScheduleRoom.setCellValueFactory(new PropertyValueFactory<>("phongHoc"));
        tblSchedules.setItems(schedules);
    }

    private void loadAllData() {
        if (admin == null) return;
        loadAccounts();
        loadCourses();
        loadClasses();
        loadLecturers();
        if (cmbComponentCourse.getValue() == null && !cmbComponentCourse.getItems().isEmpty()) {
            cmbComponentCourse.getSelectionModel().selectFirst();
        }
    }

    private void loadAccounts() {
        try {
            accounts.setAll(adminService.layDanhSachTaiKhoan(admin));
            String selectedMajor = cmbProgramMajor.getValue();
            List<String> majors = accounts.stream()
                    .map(Users::getKhoa)
                    .filter(khoa -> khoa != null && !khoa.isBlank())
                    .map(String::trim)
                    .distinct()
                    .sorted(String.CASE_INSENSITIVE_ORDER)
                    .toList();
            cmbProgramMajor.setItems(FXCollections.observableArrayList(majors));
            if (selectedMajor != null && majors.contains(selectedMajor)) {
                cmbProgramMajor.setValue(selectedMajor);
            } else if (!majors.isEmpty()) {
                cmbProgramMajor.getSelectionModel().selectFirst();
            }
            filterAccounts();
        } catch (ServiceException e) {
            setStatus("Không thể tải tài khoản. Vui lòng kiểm tra kết nối cơ sở dữ liệu.", true);
        }
    }

    private void loadCourses() {
        try {
            courses.setAll(adminService.layDanhSachMonHoc(admin));
            filterCourses();
            List<MonHoc> activeCourses = courses.stream()
                    .filter(course -> course.getTrangThai() == TrangThaiHoatDong.HOAT_DONG).toList();
            cmbClassCourse.setItems(FXCollections.observableArrayList(activeCourses));
            cmbComponentCourse.setItems(FXCollections.observableArrayList(activeCourses));
            updatePrerequisiteChoices();
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    private void loadClasses() {
        try {
            classes.setAll(adminService.layDanhSachLopHoc(admin));
            filterClasses();
            cmbScheduleClass.setItems(FXCollections.observableArrayList(classes));
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    private void loadSchedules(LopHoc courseClass) {
        schedules.clear();
        onClearScheduleForm();
        if (courseClass == null || admin == null) return;
        try {
            schedules.setAll(adminService.layLichHocTheoLop(admin, courseClass.getMaLop()));
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onRefreshSchedules() {
        loadSchedules(cmbScheduleClass.getValue());
    }

    @FXML
    private void onClearScheduleForm() {
        tblSchedules.getSelectionModel().clearSelection();
        selectedSchedule = null;
        txtScheduleDay.clear();
        txtScheduleStart.clear();
        txtScheduleEnd.clear();
        txtScheduleRoom.clear();
    }

    private void populateScheduleForm(LichHoc selected) {
        selectedSchedule = selected;
        if (selected == null) return;
        txtScheduleDay.setText(Integer.toString(selected.getThu()));
        txtScheduleStart.setText(selected.getGioBatDau().toString());
        txtScheduleEnd.setText(selected.getGioKetThuc().toString());
        txtScheduleRoom.setText(selected.getPhongHoc());
    }

    @FXML
    private void onSaveSchedule() {
        LopHoc courseClass = cmbScheduleClass.getValue();
        if (courseClass == null) {
            setStatus("Chọn lớp học phần trước khi xếp lịch.", true);
            return;
        }
        try {
            int day = parsePositiveInt(txtScheduleDay.getText(), "Thứ");
            LocalTime start = LocalTime.parse(required(txtScheduleStart.getText(), "Giờ bắt đầu"));
            LocalTime end = LocalTime.parse(required(txtScheduleEnd.getText(), "Giờ kết thúc"));
            LichHoc schedule = new LichHoc(selectedSchedule == null ? 0 : selectedSchedule.getMaLich(),
                    courseClass.getMaLop(), day, start, end,
                    required(txtScheduleRoom.getText(), "Phòng học"));
            if (!adminService.luuLichHoc(admin, schedule)) {
                setStatus("Lịch đã thay đổi hoặc không thể lưu.", true);
                return;
            }
            loadSchedules(courseClass);
            setStatus("Đã lưu lịch học.", false);
        } catch (DateTimeParseException e) {
            setStatus("Giờ học cần theo định dạng HH:mm, ví dụ 08:00.", true);
        } catch (IllegalArgumentException | ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    @FXML
    private void onDeleteSchedule() {
        if (selectedSchedule == null) {
            setStatus("Chọn lịch học cần xóa.", true);
            return;
        }
        LopHoc courseClass = cmbScheduleClass.getValue();
        try {
            if (!adminService.xoaLichHoc(admin, selectedSchedule.getMaLich())) {
                setStatus("Lịch học đã thay đổi hoặc không tồn tại.", true);
                return;
            }
            loadSchedules(courseClass);
            setStatus("Đã xóa lịch học.", false);
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    private void loadLecturers() {
        try {
            List<GiangVien> lecturers = adminService.layDanhSachTaiKhoan(admin).stream()
                    .filter(GiangVien.class::isInstance)
                    .map(GiangVien.class::cast)
                    .filter(lecturer -> lecturer.getTrangThai() == TrangThaiHoatDong.HOAT_DONG)
                    .toList();
            cmbClassLecturer.setItems(FXCollections.observableArrayList(lecturers));
        } catch (ServiceException e) {
            setStatus("Không thể tải danh sách giảng viên.", true);
        }
    }

    private void filterAccounts() {
        String query = normalized(txtAccountSearch.getText());
        String role = cmbAccountRole.getValue();
        filteredAccounts.setPredicate(account -> (role == null || role.equalsIgnoreCase(account.getRole()))
                && (account.getMaSo() + " " + account.getHoTen() + " "
                + account.getEmail() + " " + account.getRole() + " " + account.getKhoa())
                .toLowerCase(Locale.ROOT).contains(query));
    }

    private void filterCourses() {
        String query = normalized(txtCourseSearch.getText());
        filteredCourses.setPredicate(course -> (course.getMaMon() + " " + course.getTenMon() + " "
                + course.getLoaiMon()).toLowerCase(Locale.ROOT).contains(query));
    }

    private void filterClasses() {
        String query = normalized(txtClassSearch.getText());
        filteredClasses.setPredicate(item -> (item.getMaLop() + " " + item.getMaMon() + " "
                + item.getMaGV() + " " + item.getHocKy()).toLowerCase(Locale.ROOT).contains(query));
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
        txtUserFaculty.setText(selected.getKhoa());
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
        String faculty = optional(txtUserFaculty.getText());
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
                    name, email, password, STUDENT_ROLE, code, faculty, null, status,
                    code, birth == null ? null : Date.valueOf(birth), gender,
                    optional(txtStudentClass.getText()), cohort, optional(txtStudentDegree.getText()));
        }
        if (LECTURER_ROLE.equals(role)) {
            return new GiangVien(selectedAccount == null ? null : selectedAccount.getUserID(),
                    name, email, password, LECTURER_ROLE, code, faculty, null, status,
                    code, optional(txtLecturerSpecialty.getText()), optional(txtLecturerDegree.getText()));
        }
        throw new IllegalArgumentException("Vai trò tài khoản không hợp lệ.");
    }

    private void populateCourseForm(MonHoc selected) {
        selectedCourse = selected;
        if (selected == null) {
            tblPrerequisites.getItems().clear();
            return;
        }
        txtCourseCode.setText(selected.getMaMon());
        txtCourseCode.setDisable(true);
        txtCourseName.setText(selected.getTenMon());
        txtCourseCredits.setText(selected.getSoTinChi().toString());
        txtCourseType.setText(selected.getLoaiMon());
        txtCourseDescription.setText(selected.getMoTa());
        loadPrerequisites();
        updatePrerequisiteChoices();
    }

    private void loadPrerequisites() {
        if (selectedCourse == null) {
            prerequisites.clear();
            return;
        }
        try {
            prerequisites.setAll(adminService.layMonTienQuyet(admin, selectedCourse.getMaMon()));
            updatePrerequisiteChoices();
        } catch (ServiceException e) {
            setStatus(e.getMessage(), true);
        }
    }

    private void updatePrerequisiteChoices() {
        String courseCode = selectedCourse == null ? null : selectedCourse.getMaMon();
        List<String> assigned = prerequisites.stream().map(MonTienQuyet::getMaMonTienQuyet).toList();
        cmbPrerequisite.setItems(FXCollections.observableArrayList(courses.stream()
                .filter(course -> course.getTrangThai() == TrangThaiHoatDong.HOAT_DONG)
                .filter(course -> !course.getMaMon().equals(courseCode))
                .filter(course -> !assigned.contains(course.getMaMon()))
                .toList()));
    }

    private void populateClassForm(LopHoc selected) {
        selectedClass = selected;
        if (selected == null) return;
        txtClassCode.setText(selected.getMaLop());
        txtClassCode.setDisable(true);
        cmbClassCourse.getItems().stream()
                .filter(course -> course.getMaMon().equals(selected.getMaMon()))
                .findFirst().ifPresent(cmbClassCourse.getSelectionModel()::select);
        cmbClassLecturer.getItems().stream()
                .filter(lecturer -> lecturer.getMaGiangVien().equals(selected.getMaGV()))
                .findFirst().ifPresent(cmbClassLecturer.getSelectionModel()::select);
        txtClassSemester.setText(selected.getHocKy());
        txtClassCapacity.setText(selected.getSiSoToiDa().toString());
    }

    private void loadComponents(MonHoc course) {
        components.clear();
        newComponents.clear();
        loadedComponents = List.of();
        selectedComponent = null;
        onClearComponentForm();
        if (course == null) return;
        try {
            loadedComponents = thanhPhanService.layTheoMon(course.getMaMon());
            components.setAll(loadedComponents.stream()
                    .sorted((first, second) -> Integer.compare(first.getThuTu(), second.getThuTu()))
                    .map(this::copyComponent).toList());
        } catch (ServiceException e) {
            setStatus("Không thể tải thành phần đánh giá của môn.", true);
        }
    }

    private void populateComponentForm(ThanhPhanDanhGia selected) {
        selectedComponent = selected;
        if (selected == null) return;
        txtComponentName.setText(selected.getTenTP());
        txtComponentWeight.setText(Float.toString(selected.getTrongSo()));
        txtComponentOrder.setText(Integer.toString(selected.getThuTu()));
        chkComponentActive.setSelected(selected.getTrangThai());
    }

    private boolean hasPersistedActiveComponents() {
        return loadedComponents.stream().anyMatch(ThanhPhanDanhGia::getTrangThai);
    }

    private int nextComponentOrder() {
        return components.stream().mapToInt(ThanhPhanDanhGia::getThuTu).max().orElse(0) + 1;
    }

    private ThanhPhanDanhGia copyComponent(ThanhPhanDanhGia source) {
        return new ThanhPhanDanhGia(source.getMaTP(), source.getMaMon(), source.getTenTP(),
                source.getTrongSo(), source.getThuTu(), source.getTrangThai());
    }

    private void updateRoleFields(String role) {
        boolean student = STUDENT_ROLE.equals(role);
        for (javafx.scene.Node node : List.of(txtStudentClass, txtStudentCohort, txtStudentDegree,
                dpStudentBirthDate, cmbStudentGender)) {
            node.setManaged(student);
            node.setVisible(student);
        }
        for (javafx.scene.Node node : List.of(txtLecturerSpecialty, txtLecturerDegree)) {
            boolean lecturer = LECTURER_ROLE.equals(role);
            node.setManaged(lecturer);
            node.setVisible(lecturer);
        }
    }

    private StringConverter<MonHoc> courseConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(MonHoc course) {
                return course == null ? "" : course.getMaMon() + " · " + course.getTenMon();
            }

            @Override
            public MonHoc fromString(String value) {
                return null;
            }
        };
    }

    private int sectionIndex(String section) {
        return switch (section) {
            case "admin-courses" -> 1;
            case "admin-classes" -> 2;
            case "admin-components" -> 4;
            default -> 0;
        };
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

    private float parseWeight(String value) {
        try {
            float weight = Float.parseFloat(value.trim().replace(',', '.'));
            if (Float.isFinite(weight) && weight > 0 && weight <= 1) return weight;
        } catch (NumberFormatException ignored) {
        }
        throw new IllegalArgumentException("Trọng số phải thuộc khoảng (0, 1].");
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
