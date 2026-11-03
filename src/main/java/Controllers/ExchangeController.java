package Controllers;

import DAO.LopHocDAO;
import Model.LopHocDangKy;
import Model.Users;
import Model.YeuCauDoiLop;
import Services.ServiceException;
import Services.YeuCauDoiLopService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.StringConverter;
import java.sql.SQLException;
import java.util.List;
import java.util.Locale;
import java.util.logging.Level;
import java.util.logging.Logger;

public class ExchangeController {
    private static final Logger LOGGER = Logger.getLogger(ExchangeController.class.getName());
    private final YeuCauDoiLopService exchangeService = new YeuCauDoiLopService();
    private final LopHocDAO classDAO = new LopHocDAO();
    private final ObservableList<YeuCauDoiLop> marketplace = FXCollections.observableArrayList();
    private final FilteredList<YeuCauDoiLop> filteredMarketplace =
            new FilteredList<>(marketplace, item -> true);
    private Users student;

    @FXML private Label lblStatus;
    @FXML private Label lblMarketEmpty;
    @FXML private Label lblMyEmpty;
    @FXML private ComboBox<LopHocDangKy> cmbCurrentClass;
    @FXML private ComboBox<LopHocDangKy> cmbWantedClass;
    @FXML private TextArea txtReason;
    @FXML private TextField txtSearch;
    @FXML private TableView<YeuCauDoiLop> tblMarketplace;
    @FXML private TableView<YeuCauDoiLop> tblMyRequests;
    @FXML private TableColumn<YeuCauDoiLop, Integer> colMarketId;
    @FXML private TableColumn<YeuCauDoiLop, String> colMarketStudent;
    @FXML private TableColumn<YeuCauDoiLop, String> colMarketFrom;
    @FXML private TableColumn<YeuCauDoiLop, String> colMarketTo;
    @FXML private TableColumn<YeuCauDoiLop, String> colMarketReason;
    @FXML private TableColumn<YeuCauDoiLop, String> colMarketDate;
    @FXML private TableColumn<YeuCauDoiLop, Integer> colMyId;
    @FXML private TableColumn<YeuCauDoiLop, String> colMyFrom;
    @FXML private TableColumn<YeuCauDoiLop, String> colMyTo;
    @FXML private TableColumn<YeuCauDoiLop, String> colMyStatus;
    @FXML private TableColumn<YeuCauDoiLop, String> colMyReason;
    @FXML private TableColumn<YeuCauDoiLop, String> colMyDate;

    @FXML
    private void initialize() {
        colMarketId.setCellValueFactory(new PropertyValueFactory<>("maYeuCau"));
        colMarketStudent.setCellValueFactory(new PropertyValueFactory<>("sinhVienHienThi"));
        colMarketFrom.setCellValueFactory(new PropertyValueFactory<>("tenLopCu"));
        colMarketTo.setCellValueFactory(new PropertyValueFactory<>("tenLopMoi"));
        colMarketReason.setCellValueFactory(new PropertyValueFactory<>("lyDo"));
        colMarketDate.setCellValueFactory(new PropertyValueFactory<>("ngayTaoHienThi"));
        colMyId.setCellValueFactory(new PropertyValueFactory<>("maYeuCau"));
        colMyFrom.setCellValueFactory(new PropertyValueFactory<>("tenLopCu"));
        colMyTo.setCellValueFactory(new PropertyValueFactory<>("tenLopMoi"));
        colMyStatus.setCellValueFactory(new PropertyValueFactory<>("trangThaiHienThi"));
        colMyReason.setCellValueFactory(new PropertyValueFactory<>("lyDo"));
        colMyDate.setCellValueFactory(new PropertyValueFactory<>("ngayTaoHienThi"));
        tblMarketplace.setItems(filteredMarketplace);
        cmbCurrentClass.setConverter(classConverter());
        cmbWantedClass.setConverter(classConverter());
        txtSearch.textProperty().addListener((observable, oldValue, newValue) -> applySearch());
    }

    public void setStudent(Users student) {
        this.student = student;
        if (student == null || !"SINHVIEN".equalsIgnoreCase(student.getRole())
                || student.getMaSo() == null || student.getMaSo().isBlank()) {
            setStatus("Không xác định được mã sinh viên. Vui lòng quay lại và đăng nhập lại.", true);
            return;
        }
        loadData();
    }

    @FXML
    private void onRefresh(ActionEvent event) {
        loadData();
    }

    @FXML
    private void onPostRequest(ActionEvent event) {
        LopHocDangKy from = cmbCurrentClass.getValue();
        LopHocDangKy to = cmbWantedClass.getValue();
        if (student == null || from == null || to == null) {
            setStatus("Vui lòng chọn lớp đang học và lớp mong muốn.", true);
            return;
        }
        if (from.getMaLop().equals(to.getMaLop())) {
            setStatus("Lớp muốn vào phải khác lớp bạn đang học.", true);
            return;
        }
        String reason = txtReason.getText() == null ? "" : txtReason.getText().trim();
        if (reason.length() > 200) {
            setStatus("Lý do không được vượt quá 200 ký tự.", true);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận đăng bài");
        confirmation.setHeaderText("Đăng đề nghị trao đổi lớp?");
        confirmation.setContentText("Bạn đang học " + from.getMaLop() + " · " + from.getTenMon()
                + " và muốn vào " + to.getMaLop() + " · " + to.getTenMon()
                + ". Sinh viên chốt phải đang học lớp bạn muốn vào. Khác môn học vẫn được phép.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            boolean success = exchangeService.dangBaiDoiLop(
                    student.getMaSo(), from.getMaLop(), to.getMaLop(), reason);
            setStatus(success ? "Đã đăng yêu cầu đổi lớp." : "Không thể đăng yêu cầu đổi lớp.", !success);
            if (success) {
                txtReason.clear();
                loadData();
            }
        } catch (ServiceException e) {
            setStatus("Không thể đăng bài do lỗi hệ thống. Vui lòng thử lại sau.", true);
        } catch (RuntimeException e) {
            setStatus("Đã xảy ra lỗi khi đăng bài. Vui lòng thử lại sau.", true);
        }
    }

    @FXML
    private void onAcceptRequest(ActionEvent event) {
        YeuCauDoiLop selected = tblMarketplace.getSelectionModel().getSelectedItem();
        if (selected == null || student == null) {
            setStatus("Hãy chọn một bài đăng của sinh viên khác để chốt.", true);
            return;
        }
        if (student.getMaSo().equals(selected.getMaSV())) {
            setStatus("Bạn không thể tự chốt bài đăng của mình.", true);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận chốt giao dịch");
        confirmation.setHeaderText("Hoán đổi hai lớp?");
        confirmation.setContentText("Bài đăng #" + selected.getMaYeuCau() + " cần bạn đang học lớp "
                + selected.getLopMoi() + ". Khi thành công, bạn sẽ sang lớp " + selected.getLopCu()
                + " và người đăng sẽ sang lớp " + selected.getLopMoi()
                + ". Hệ thống kiểm tra lại điều kiện và thực hiện hoán đổi trong một giao dịch.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;

        try {
            boolean success = exchangeService.xacNhanDoiLop(selected.getMaYeuCau(), student.getMaSo());
            setStatus(success ? "Đổi lớp thành công." : "Không thể hoàn tất yêu cầu đổi lớp.", !success);
            loadData();
        } catch (ServiceException e) {
            setStatus("Không thể xử lý giao dịch do lỗi hệ thống. Vui lòng thử lại sau.", true);
        } catch (RuntimeException e) {
            setStatus("Đã xảy ra lỗi khi xử lý giao dịch. Vui lòng thử lại sau.", true);
        }
    }

    @FXML
    private void onCancelRequest(ActionEvent event) {
        YeuCauDoiLop selected = tblMyRequests.getSelectionModel().getSelectedItem();
        if (selected == null || selected.getTrangThai() != Model.TrangThaiDoiLop.CHO_XAC_NHAN) {
            setStatus("Chỉ có thể hủy bài đăng của bạn đang ở trạng thái chờ xác nhận.", true);
            return;
        }
        Alert confirmation = new Alert(Alert.AlertType.CONFIRMATION);
        confirmation.setTitle("Xác nhận hủy bài");
        confirmation.setHeaderText("Hủy bài đăng #" + selected.getMaYeuCau() + "?");
        confirmation.setContentText("Bài sẽ không còn xuất hiện trên sàn. Không thể hủy sau khi đã được xử lý.");
        if (confirmation.showAndWait().orElse(ButtonType.CANCEL) != ButtonType.OK) return;
        try {
            boolean success = exchangeService.huyYeuCau(selected.getMaYeuCau(), student.getMaSo());
            setStatus(success ? "Đã hủy bài đăng đổi lớp." : "Không thể hủy bài đăng đổi lớp.", !success);
            loadData();
        } catch (ServiceException e) {
            setStatus("Không thể hủy bài do lỗi hệ thống. Vui lòng thử lại sau.", true);
        } catch (RuntimeException e) {
            setStatus("Đã xảy ra lỗi khi hủy bài. Vui lòng thử lại sau.", true);
        }
    }

    private void loadData() {
        if (student == null || student.getMaSo() == null || student.getMaSo().isBlank()) return;
        try {
            List<LopHocDangKy> classes = classDAO.layDanhSachLopDangKy(student.getMaSo());
            cmbCurrentClass.setItems(FXCollections.observableArrayList(
                    classes.stream().filter(LopHocDangKy::isDaDangKy).toList()));
            cmbWantedClass.setItems(FXCollections.observableArrayList(
                    classes.stream().filter(item -> !item.isDaDangKy()).toList()));
            List<YeuCauDoiLop> posts = exchangeService.moSanGiaoDich();
            marketplace.setAll(posts.stream()
                    .filter(item -> !student.getMaSo().equals(item.getMaSV())).toList());
            tblMyRequests.setItems(FXCollections.observableArrayList(
                    exchangeService.xemYeuCauCuaSinhVien(student.getMaSo())));
            applySearch();
            updateEmptyLabels();
        } catch (SQLException | ServiceException e) {
            LOGGER.log(Level.SEVERE, "Unable to load class-exchange data.", e);
            cmbCurrentClass.getItems().clear();
            cmbWantedClass.getItems().clear();
            marketplace.clear();
            tblMyRequests.setItems(FXCollections.observableArrayList());
            applySearch();
            updateEmptyLabels();
            setStatus("Không thể tải dữ liệu đổi lớp. Kiểm tra kết nối cơ sở dữ liệu rồi thử tải lại.", true);
        } catch (RuntimeException e) {
            LOGGER.log(Level.SEVERE, "Unexpected error while loading class-exchange data.", e);
            setStatus("Đã xảy ra lỗi khi tải dữ liệu. Vui lòng thử tải lại.", true);
        }
    }

    private void applySearch() {
        String query = txtSearch.getText() == null ? "" : txtSearch.getText().trim().toLowerCase(Locale.ROOT);
        filteredMarketplace.setPredicate(item -> {
            String searchable = (item.getMaYeuCau() + " " + item.getMaSV() + " " + item.getTenSinhVien()
                    + " " + item.getLopCu() + " " + item.getTenLopCu() + " " + item.getLopMoi()
                    + " " + item.getTenLopMoi() + " " + item.getLyDo()).toLowerCase(Locale.ROOT);
            return searchable.contains(query);
        });
        updateEmptyLabels();
    }

    private void updateEmptyLabels() {
        lblMarketEmpty.setVisible(filteredMarketplace.isEmpty());
        lblMarketEmpty.setManaged(filteredMarketplace.isEmpty());
        boolean noMine = tblMyRequests.getItems() == null || tblMyRequests.getItems().isEmpty();
        lblMyEmpty.setVisible(noMine);
        lblMyEmpty.setManaged(noMine);
    }

    private StringConverter<LopHocDangKy> classConverter() {
        return new StringConverter<>() {
            @Override
            public String toString(LopHocDangKy item) {
                return item == null ? "" : item.getMaLop() + " · " + item.getTenMon()
                        + " (" + item.getMaMon() + ", HK " + item.getHocKy() + ")";
            }
            @Override
            public LopHocDangKy fromString(String value) { return null; }
        };
    }

    private void setStatus(String message, boolean error) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("exchange-message-success", "exchange-message-error");
        lblStatus.getStyleClass().add(error ? "exchange-message-error" : "exchange-message-success");
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

}
