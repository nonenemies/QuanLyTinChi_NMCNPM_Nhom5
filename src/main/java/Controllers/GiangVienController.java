package Controllers;

import Model.BangDiemSinhVien;
import Model.GiangVien;
import Model.LopHoc;
import Model.ThanhPhanDanhGia;
import Model.TrangThaiHoatDong;
import Model.Users;
import Services.DiemService;
import Services.ServiceException;
import Services.ThanhPhanDanhGiaService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.util.StringConverter;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class GiangVienController {
    private final DiemService diemService = new DiemService();
    private final ThanhPhanDanhGiaService thanhPhanService = new ThanhPhanDanhGiaService();
    private final DecimalFormat diemFormat = new DecimalFormat("0.##");

    @FXML private Label lblStatus;
    @FXML private TableView<LopHoc> tblClasses;
    @FXML private TableColumn<LopHoc, String> colClassCode;
    @FXML private TableColumn<LopHoc, String> colCourseCode;
    @FXML private TableColumn<LopHoc, String> colSemester;
    @FXML private TableColumn<LopHoc, String> colEnrollment;
    @FXML private TableView<BangDiemSinhVien> tblStudents;
    @FXML private TableColumn<BangDiemSinhVien, String> colStudentCode;
    @FXML private TableColumn<BangDiemSinhVien, String> colStudentName;
    @FXML private TableColumn<BangDiemSinhVien, String> colFinalGrade;
    @FXML private ComboBox<ThanhPhanDanhGia> cboComponent;
    @FXML private TextField txtScore;

    private GiangVien giangVien;

    @FXML
    private void initialize() {
        colClassCode.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getMaLop()));
        colCourseCode.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getMaMon()));
        colSemester.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getHocKy()));
        colEnrollment.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                data.getValue().getSiSoHienTai() + "/" + data.getValue().getSiSoToiDa()));
        colStudentCode.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getMaSV()));
        colStudentName.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(data.getValue().getHoTen()));
        colFinalGrade.setCellValueFactory(data ->
                new ReadOnlyStringWrapper(formatScore(data.getValue().getDiemTongKet())));

        cboComponent.setConverter(new StringConverter<>() {
            @Override
            public String toString(ThanhPhanDanhGia component) {
                if (component == null) return "";
                return component.getTenTP() + " (" + diemFormat.format(component.getTrongSo() * 100) + "%)";
            }

            @Override
            public ThanhPhanDanhGia fromString(String value) {
                return null;
            }
        });
        tblClasses.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> loadGradebook(selected, null));
        tblStudents.getSelectionModel().selectedItemProperty().addListener(
                (observable, previous, selected) -> updateScoreInput());
        cboComponent.valueProperty().addListener(
                (observable, previous, selected) -> updateScoreInput());
    }

    public void setLecturer(Users user) {
        if (!(user instanceof GiangVien lecturer)
                || lecturer.getTrangThai() != TrangThaiHoatDong.HOAT_DONG
                || !"GIAOVIEN".equalsIgnoreCase(lecturer.getRole())
                || lecturer.getMaGiangVien() == null || lecturer.getMaGiangVien().isBlank()) {
            showStatus("Không xác định được tài khoản giảng viên. Vui lòng đăng nhập lại.", true);
            return;
        }
        giangVien = lecturer;
        loadClasses();
    }

    @FXML
    private void onRefresh() {
        loadClasses();
    }

    @FXML
    private void onSaveGrade() {
        LopHoc selectedClass = tblClasses.getSelectionModel().getSelectedItem();
        BangDiemSinhVien selectedStudent = tblStudents.getSelectionModel().getSelectedItem();
        ThanhPhanDanhGia selectedComponent = cboComponent.getValue();
        if (selectedClass == null || selectedStudent == null || selectedComponent == null) {
            showStatus("Chọn lớp, sinh viên và thành phần đánh giá trước khi lưu điểm.", true);
            return;
        }

        float score;
        try {
            score = Float.parseFloat(txtScore.getText().trim().replace(',', '.'));
        } catch (NumberFormatException e) {
            showStatus("Điểm phải là số từ 0 đến 10.", true);
            return;
        }
        if (!Float.isFinite(score) || score < 0 || score > 10) {
            showStatus("Điểm phải nằm trong khoảng từ 0 đến 10.", true);
            return;
        }

        try {
            boolean saved = diemService.nhapDiemVaTinhTongKet(giangVien, selectedStudent.getMaSV(),
                    selectedClass.getMaLop(), selectedComponent, score);
            if (!saved) {
                showStatus("Không thể lưu điểm. Hãy kiểm tra sinh viên và thành phần đánh giá.", true);
                return;
            }
            String studentCode = selectedStudent.getMaSV();
            txtScore.clear();
            loadGradebook(selectedClass, studentCode);
            showStatus("Đã lưu điểm thành phần.", false);
        } catch (ServiceException e) {
            showStatus("Không thể lưu điểm. Vui lòng thử lại sau.", true);
        }
    }

    private void loadClasses() {
        if (giangVien == null) return;
        tblClasses.getItems().clear();
        tblStudents.getItems().clear();
        cboComponent.getItems().clear();
        removeComponentColumns();
        try {
            List<LopHoc> classes = diemService.layLopPhuTrach(giangVien);
            tblClasses.setItems(FXCollections.observableArrayList(classes));
            if (classes.isEmpty()) {
                cboComponent.getItems().clear();
                tblStudents.getItems().clear();
                removeComponentColumns();
                showStatus("Bạn hiện chưa được phân công lớp học phần nào.", false);
                return;
            }
            tblClasses.getSelectionModel().selectFirst();
        } catch (ServiceException e) {
            showStatus("Không thể tải danh sách lớp phụ trách. Hãy kiểm tra kết nối rồi thử lại.", true);
        }
    }

    private void loadGradebook(LopHoc lopHoc, String maSVCanChon) {
        tblStudents.getItems().clear();
        removeComponentColumns();
        ThanhPhanDanhGia componentPreviouslySelected = cboComponent.getValue();
        cboComponent.getItems().clear();
        if (lopHoc == null || giangVien == null) return;

        try {
            List<ThanhPhanDanhGia> components = thanhPhanService.layTheoMon(lopHoc.getMaMon())
                    .stream().filter(ThanhPhanDanhGia::getTrangThai)
                    .sorted((first, second) -> Integer.compare(first.getThuTu(), second.getThuTu()))
                    .toList();
            cboComponent.setItems(FXCollections.observableArrayList(components));
            if (componentPreviouslySelected != null) {
                components.stream()
                        .filter(component -> component.getMaTP() == componentPreviouslySelected.getMaTP())
                        .findFirst().ifPresent(cboComponent.getSelectionModel()::select);
            }
            if (cboComponent.getValue() == null && !components.isEmpty()) {
                cboComponent.getSelectionModel().selectFirst();
            }
            addComponentColumns(components);

            List<BangDiemSinhVien> students = diemService.layBangDiem(giangVien, lopHoc.getMaLop());
            tblStudents.setItems(FXCollections.observableArrayList(students));
            if (maSVCanChon != null) {
                students.stream().filter(student -> maSVCanChon.equals(student.getMaSV()))
                        .findFirst().ifPresent(student -> tblStudents.getSelectionModel().select(student));
            }
            if (components.isEmpty()) {
                showStatus("Môn học chưa có thành phần đánh giá đang hoạt động.", true);
            } else if (students.isEmpty()) {
                showStatus("Lớp chưa có sinh viên đăng ký.", false);
            } else {
                showStatus("Đang xem bảng điểm " + lopHoc.getMaLop() + ".", false);
            }
        } catch (ServiceException e) {
            showStatus("Không thể tải bảng điểm lớp học phần. Vui lòng thử lại.", true);
        }
    }

    private void addComponentColumns(List<ThanhPhanDanhGia> components) {
        int insertionIndex = tblStudents.getColumns().indexOf(colFinalGrade);
        for (ThanhPhanDanhGia component : components) {
            TableColumn<BangDiemSinhVien, String> column = new TableColumn<>(component.getTenTP());
            column.setPrefWidth(110);
            column.setCellValueFactory(data -> new ReadOnlyStringWrapper(
                    formatScore(data.getValue().getDiemThanhPhan(component.getMaTP()))));
            tblStudents.getColumns().add(insertionIndex++, column);
        }
    }

    private void removeComponentColumns() {
        List<TableColumn<BangDiemSinhVien, ?>> dynamicColumns = new ArrayList<>(
                tblStudents.getColumns().subList(2, tblStudents.getColumns().size() - 1));
        tblStudents.getColumns().removeAll(dynamicColumns);
    }

    private String formatScore(Float score) {
        return score == null ? "—" : diemFormat.format(score);
    }

    private void updateScoreInput() {
        BangDiemSinhVien student = tblStudents.getSelectionModel().getSelectedItem();
        ThanhPhanDanhGia component = cboComponent.getValue();
        Float score = student == null || component == null
                ? null : student.getDiemThanhPhan(component.getMaTP());
        txtScore.setText(score == null ? "" : formatScore(score));
    }

    private void showStatus(String message, boolean error) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("teacher-status-error", "teacher-status-info");
        lblStatus.getStyleClass().add(error ? "teacher-status-error" : "teacher-status-info");
    }
}
