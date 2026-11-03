package Controllers;

import Model.LopHocDangKy;
import Model.KetQuaDangKy;
import Model.QuyDinhTinChi;
import Model.TinhTrangTinChi;
import Model.Users;
import Services.DangKyHocService;
import Services.ServiceException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.css.PseudoClass;

import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.logging.Level;
import java.util.logging.Logger;

public class RegistrationController {
    private static final Logger LOGGER = Logger.getLogger(RegistrationController.class.getName());
    private static final PseudoClass REGISTERED_ROW = PseudoClass.getPseudoClass("registered");
    private final DangKyHocService registrationService = new DangKyHocService();
    private final ObservableList<LopHocDangKy> classes = FXCollections.observableArrayList();
    private final FilteredList<LopHocDangKy> filteredClasses = new FilteredList<>(classes, item -> true);
    private Users student;

    @FXML private Label lblStatus;
    @FXML private Label lblEmpty;
    @FXML private Label lblCreditStatus;
    @FXML private Label lblCreditPolicy;
    @FXML private TextField txtSearch;
    @FXML private TableView<LopHocDangKy> tblClasses;
    @FXML private TableColumn<LopHocDangKy, String> colCourseCode;
    @FXML private TableColumn<LopHocDangKy, String> colCourseName;
    @FXML private TableColumn<LopHocDangKy, String> colClassSchedule;
    @FXML private TableColumn<LopHocDangKy, String> colClassCapacity;
    @FXML private TableColumn<LopHocDangKy, Integer> colCredits;
    @FXML private TableView<LopHocDangKy> tblRegistered;
    @FXML private TableColumn<LopHocDangKy, String> colRegisteredClass;
    @FXML private TableColumn<LopHocDangKy, String> colRegisteredCourse;
    @FXML private TableColumn<LopHocDangKy, String> colRegisteredSemester;
    @FXML private TableColumn<LopHocDangKy, String> colRegisteredSchedule;
    @FXML private TableColumn<LopHocDangKy, Integer> colRegisteredCredits;
    @FXML private TableColumn<LopHocDangKy, String> colRegisteredStatus;
    @FXML private Button btnConfirm;
    @FXML private Button btnCancelRegistration;

    @FXML
    private void initialize() {
        lblCreditPolicy.setText("Mức tối thiểu " + QuyDinhTinChi.MIN_TIN_CHI
                + " tín chỉ chỉ dùng để cảnh báo; vượt mức tối đa "
                + QuyDinhTinChi.MAX_TIN_CHI + " tín chỉ sẽ bị từ chối.");
        colCourseName.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colCourseCode.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getMaMon() + " · " + cell.getValue().getMaLop()));
        colClassCapacity.setCellValueFactory(new PropertyValueFactory<>("siSo"));
        colClassSchedule.setCellValueFactory(cell -> new ReadOnlyStringWrapper(
                cell.getValue().getLichHoc() + " · " + cell.getValue().getGiangVien()));
        colCredits.setCellValueFactory(new PropertyValueFactory<>("soTinChi"));
        colRegisteredClass.setCellValueFactory(new PropertyValueFactory<>("maLop"));
        colRegisteredCourse.setCellValueFactory(new PropertyValueFactory<>("tenMon"));
        colRegisteredSemester.setCellValueFactory(new PropertyValueFactory<>("hocKy"));
        colRegisteredSchedule.setCellValueFactory(new PropertyValueFactory<>("lichHoc"));
        colRegisteredCredits.setCellValueFactory(new PropertyValueFactory<>("soTinChi"));
        colRegisteredStatus.setCellValueFactory(new PropertyValueFactory<>("trangThai"));
        tblClasses.setItems(filteredClasses);
        tblClasses.setRowFactory(table -> new TableRow<>() {
            @Override
            protected void updateItem(LopHocDangKy item, boolean empty) {
                super.updateItem(item, empty);
                pseudoClassStateChanged(REGISTERED_ROW, !empty && item.isDaDangKy());
            }
        });
        tblClasses.setFixedCellSize(32);
        tblRegistered.setFixedCellSize(32);
        filteredClasses.addListener((ListChangeListener<LopHocDangKy>)
                change -> fitTableHeight(tblClasses));
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applyFilters());
        tblClasses.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) -> updateConfirmButton(selected));
        tblRegistered.getSelectionModel().selectedItemProperty().addListener(
                (observable, oldValue, selected) ->
                        btnCancelRegistration.setDisable(selected == null || !selected.isCoTheHuy()));
    }

    public void setStudent(Users student) {
        this.student = student;
        if (student == null || !"SINHVIEN".equalsIgnoreCase(student.getRole())
                || student.getMaSo() == null || student.getMaSo().isBlank()) {
            setStatus("Không xác định được mã sinh viên. Vui lòng quay lại và đăng nhập lại.", true);
            return;
        }
        loadClasses();
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        loadClasses();
    }

    @FXML
    private void onConfirmRegistration(ActionEvent event) {
        LopHocDangKy selected = tblClasses.getSelectionModel().getSelectedItem();
        if (student == null || selected == null) return;
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận đăng ký");
        confirmation.setHeaderText("Đăng ký lớp " + selected.getMaLop() + "?");
        confirmation.setContentText(selected.getMaMon() + " · " + selected.getTenMon() + " · "
                + selected.getSoTinChi()
                + " tín chỉ · Học kỳ " + selected.getHocKy()
                + " · " + selected.getLichHoc() + " · " + selected.getGiangVien());
        Optional<javafx.scene.control.ButtonType> response = confirmation.showAndWait();
        if (response.isEmpty() || response.get() != javafx.scene.control.ButtonType.OK) return;

        try {
            KetQuaDangKy result = registrationService.dangKyHoc(student.getMaSo(), selected.getMaLop());
            boolean success = result == KetQuaDangKy.THANH_CONG;
            setStatus(success ? "Đăng ký lớp thành công." : "Không thể đăng ký lớp này.", !success);
            if (!success) {
                Alert alert = new Alert(Alert.AlertType.WARNING);
                alert.setTitle("Đăng ký không thành công");
                alert.setHeaderText("Không thể đăng ký lớp " + selected.getMaLop());
                alert.setContentText(registrationFailureMessage(result));
                alert.showAndWait();
            }
            loadClasses();
        } catch (ServiceException e) {
            setStatus("Không thể xử lý đăng ký do lỗi hệ thống. Vui lòng thử lại sau.", true);
        } catch (RuntimeException e) {
            setStatus("Đã xảy ra lỗi khi đăng ký. Vui lòng thử lại sau.", true);
        }

    }

    private String registrationFailureMessage(KetQuaDangKy result) {
        return switch (result) {
            case SINH_VIEN_KHONG_HOAT_DONG -> "Tài khoản sinh viên không còn hoạt động.";
            case DOT_DANG_KY_DA_DONG -> "Đợt đăng ký của khoa/ngành chưa mở hoặc đã kết thúc.";
            case LOP_KHONG_HOAT_DONG -> "Lớp đã ngừng hoạt động hoặc hết chỗ.";
            case LOP_CHUA_XEP_LICH -> "Lớp chưa được xếp lịch học.";
            case DA_DANG_KY_MON_TRONG_HOC_KY -> "Bạn đã đăng ký môn này trong học kỳ.";
            case DA_QUA_MON -> "Bạn đã đạt môn này nên không thể đăng ký học lại.";
            case THIEU_MON_TIEN_QUYET -> "Bạn chưa đạt đủ các môn tiên quyết.";
            case TRUNG_LICH -> "Lịch học bị trùng với một lớp bạn đã đăng ký.";
            case VUOT_QUA_TIN_CHI -> "Tổng tín chỉ học kỳ không được vượt quá "
                    + QuyDinhTinChi.MAX_TIN_CHI + ".";
            case DA_CO_DIEM -> "Không thể hủy lớp đã có điểm thành phần.";
            case THANH_CONG -> "Thao tác thành công.";
            default -> "Thông tin đăng ký không còn hợp lệ. Hãy tải lại danh sách lớp.";
        };
    }

    @FXML
    private void onCancelRegistration(ActionEvent event) {
        LopHocDangKy selected = tblRegistered.getSelectionModel().getSelectedItem();
        if (student == null || selected == null || !selected.isCoTheHuy()) {
            return;
        }

        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận hủy đăng ký");
        confirmation.setHeaderText("Hủy lớp " + selected.getMaLop() + "?");
        confirmation.setContentText(selected.getTenMon() + " · Học kỳ " + selected.getHocKy()
                + " · " + selected.getLichHoc()
                + "\nLớp sẽ được cập nhật sĩ số và bạn có thể đăng ký lại nếu còn chỗ.");
        Optional<javafx.scene.control.ButtonType> response = confirmation.showAndWait();
        if (response.isEmpty() || response.get() != javafx.scene.control.ButtonType.OK) {
            return;
        }

        try {
            boolean success = registrationService.huyDangKyHoc(student.getMaSo(), selected.getMaLop());
            setStatus(success ? "Đã hủy đăng ký lớp." : "Không thể hủy đăng ký lớp.", !success);

            Alert alert = new Alert(success ? Alert.AlertType.INFORMATION : Alert.AlertType.WARNING);
            alert.setTitle(success ? "Đã hủy đăng ký" : "Không thể hủy đăng ký");
            alert.setHeaderText(selected.getTenMon() + " · " + selected.getMaLop());
            alert.setContentText(success ? "Lớp đã được xóa khỏi thời khóa biểu."
                    : "Lớp không còn đăng ký hoặc đã hoàn thành.");
            alert.showAndWait();
            loadClasses();
        } catch (ServiceException e) {
            setStatus("Không thể hủy đăng ký do lỗi hệ thống. Vui lòng thử lại sau.", true);
            showError("Không thể hủy đăng ký do lỗi hệ thống. Vui lòng thử lại sau.");
        } catch (RuntimeException e) {
            setStatus("Đã xảy ra lỗi khi hủy đăng ký. Vui lòng thử lại sau.", true);
            showError("Đã xảy ra lỗi khi hủy đăng ký. Vui lòng thử lại sau.");
        }
    }

    private void loadClasses() {
        if (student == null || student.getMaSo() == null || student.getMaSo().isBlank()) return;
        try {
            List<LopHocDangKy> items = registrationService.xemDanhSachLopDangKy(student.getMaSo());
            List<LopHocDangKy> registeredClasses =
                    registrationService.xemDanhSachLopDaDangKy(student.getMaSo());
            classes.setAll(items);
            applyFilters();
            tblRegistered.getSelectionModel().clearSelection();
            btnCancelRegistration.setDisable(true);
            tblRegistered.setItems(FXCollections.observableArrayList(
                    registeredClasses));
            fitTableHeight(tblRegistered);
            updateCreditStatus(java.util.stream.Stream.concat(
                    items.stream(), registeredClasses.stream()).toList());
            lblEmpty.setText("Chưa có lớp phù hợp. Hãy kiểm tra đợt đăng ký, chương trình đào tạo và lớp học phần.");
            updateConfirmButton(tblClasses.getSelectionModel().getSelectedItem());
            if (items.isEmpty()) {
                setStatus("Chưa có lớp học phần phù hợp với đợt đăng ký và chương trình đào tạo.", false);
            }
        } catch (ServiceException e) {
            LOGGER.log(Level.SEVERE, "Unable to load course registration data.", e);
            classes.clear();
            tblRegistered.setItems(FXCollections.observableArrayList());
            fitTableHeight(tblRegistered);
            btnCancelRegistration.setDisable(true);
            lblCreditStatus.setText("");
            lblCreditStatus.setVisible(false);
            lblCreditStatus.setManaged(false);
            lblEmpty.setText("Không thể tải danh sách lớp. Kiểm tra kết nối cơ sở dữ liệu rồi thử tải lại.");
            updateConfirmButton(null);
            setStatus("Lỗi tải dữ liệu lớp học. Vui lòng thử lại.", true);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while loading course registration data.", e);
            classes.clear();
            tblRegistered.setItems(FXCollections.observableArrayList());
            fitTableHeight(tblRegistered);
            btnCancelRegistration.setDisable(true);
            lblCreditStatus.setText("");
            lblCreditStatus.setVisible(false);
            lblCreditStatus.setManaged(false);
            lblEmpty.setText("Đã xảy ra lỗi khi tải danh sách lớp.");
            updateConfirmButton(null);
            setStatus("Đã xảy ra lỗi khi tải dữ liệu.", true);
        }
    }

    private void applyFilters() {
        String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        filteredClasses.setPredicate(item -> {
            String searchable = (item.getMaLop() + " " + item.getMaMon() + " " + item.getTenMon()
                    + " " + item.getGiangVien()).toLowerCase(Locale.ROOT);
            return searchable.contains(query);
        });
        boolean hasRows = !filteredClasses.isEmpty();
        lblEmpty.setVisible(!hasRows);
        lblEmpty.setManaged(!hasRows);
    }

    private void updateConfirmButton(LopHocDangKy item) {
        btnConfirm.setDisable(item == null || item.isDaDangKy()
                || !item.isCoLichHoc()
                || item.getSiSoHienTai() == null || item.getSiSoToiDa() == null
                || item.getSiSoHienTai() >= item.getSiSoToiDa());
    }

    private void fitTableHeight(TableView<?> table) {
        int visibleRows = Math.max(1, table.getItems().size());
        double height = 34 + visibleRows * table.getFixedCellSize();
        table.setMinHeight(height);
        table.setPrefHeight(height);
        table.setMaxHeight(height);
    }

    private void updateCreditStatus(List<LopHocDangKy> items) {
        List<String> semesters = items.stream()
                .map(LopHocDangKy::getHocKy)
                .filter(semester -> semester != null && !semester.isBlank())
                .distinct()
                .sorted()
                .toList();
        if (semesters.isEmpty()) {
            lblCreditStatus.setText("");
            lblCreditStatus.setVisible(false);
            lblCreditStatus.setManaged(false);
            return;
        }

        List<TinhTrangTinChi> statuses = semesters.stream()
                .map(semester -> registrationService.tinhTrangTinChi(student.getMaSo(), semester))
                .toList();
        List<String> summaries = java.util.stream.IntStream.range(0, semesters.size()).mapToObj(index -> {
            String semester = semesters.get(index);
            TinhTrangTinChi status = statuses.get(index);
            if (!status.datMucToiThieu()) {
                return semester + ": Bạn mới đăng ký " + status.tongTinChi() + "/"
                        + QuyDinhTinChi.MIN_TIN_CHI + " tín chỉ tối thiểu.";
            }
            return semester + ": đã đăng ký " + status.tongTinChi()
                    + " tín chỉ, đạt mức tối thiểu " + QuyDinhTinChi.MIN_TIN_CHI + ".";
        }).toList();
        boolean belowMinimum = statuses.stream().anyMatch(status -> !status.datMucToiThieu());
        lblCreditStatus.getStyleClass().removeAll(
                "registration-message-success", "registration-message-error");
        lblCreditStatus.getStyleClass().add(
                belowMinimum ? "registration-message-error" : "registration-message-success");
        lblCreditStatus.setText(String.join(System.lineSeparator(), summaries));
        lblCreditStatus.setVisible(true);
        lblCreditStatus.setManaged(true);
    }

    private void setStatus(String message, boolean error) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("registration-message-success", "registration-message-error");
        lblStatus.getStyleClass().add(error ? "registration-message-error" : "registration-message-success");
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR, message);
        alert.setHeaderText("Không thể tiếp tục");
        alert.showAndWait();
    }
}
