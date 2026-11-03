package Controllers;

import Model.LopHocDangKy;
import Model.SinhVien;
import Model.TrangThaiDangKy;
import Model.Users;
import Services.DiemService;
import Services.ServiceException;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class StudentGradesController {
    private static final Pattern YEAR_TERM = Pattern.compile("^(\\d{4})-(\\d+)$");
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.##");

    private final DiemService diemService = new DiemService();
    private SinhVien student;

    @FXML private VBox semesterResults;
    @FXML private Label lblStatus;
    @FXML private Button btnRefresh;

    public void setStudent(Users user) {
        if (!(user instanceof SinhVien sinhVien)) {
            showError("Không xác định được tài khoản sinh viên. Vui lòng đăng nhập lại.");
            return;
        }
        student = sinhVien;
        loadResults();
    }

    @FXML
    private void onRefresh() {
        loadResults();
    }

    private void loadResults() {
        if (student == null) return;
        btnRefresh.setDisable(true);
        semesterResults.getChildren().clear();
        try {
            List<LopHocDangKy> results = diemService.layKetQuaHocTap(student);
            if (results.isEmpty()) {
                showInfo("Chưa có học phần nào được ghi nhận trong bảng điểm.");
                return;
            }
            displayBySemester(results);
        } catch (ServiceException e) {
            showError("Không thể tải bảng điểm tổng kết. Vui lòng thử lại.");
        } finally {
            btnRefresh.setDisable(false);
        }
    }

    private void displayBySemester(List<LopHocDangKy> results) {
        Map<String, List<LopHocDangKy>> grouped = new LinkedHashMap<>();
        results.stream()
                .sorted(Comparator.comparing(LopHocDangKy::getHocKy, this::compareSemesters)
                        .thenComparing(LopHocDangKy::getMaMon)
                        .thenComparing(LopHocDangKy::getMaLop))
                .forEach(result -> grouped.computeIfAbsent(result.getHocKy(), key -> new ArrayList<>()).add(result));

        List<String> semesterOrder = new ArrayList<>(grouped.keySet());
        Set<String> accumulatedCourses = new LinkedHashSet<>();
        long totalEarnedCredits = 0;
        List<VBox> cards = new ArrayList<>();

        for (String semester : semesterOrder) {
            List<LopHocDangKy> semesterResults = grouped.get(semester);
            double semesterWeightedScore = 0;
            long semesterGradedCredits = 0;
            long semesterEarnedCredits = 0;
            Set<String> semesterPassedCourses = new LinkedHashSet<>();

            for (LopHocDangKy result : semesterResults) {
                int credits = result.getSoTinChi() == null ? 0 : result.getSoTinChi();
                if (result.getDiemTongKet() != null && credits > 0) {
                    semesterWeightedScore += result.getDiemTongKet() * credits;
                    semesterGradedCredits += credits;
                }
                if (isPassed(result)) {
                    if (semesterPassedCourses.add(result.getMaMon()) && result.getSoTinChi() != null) {
                        semesterEarnedCredits += result.getSoTinChi();
                    }
                    if (accumulatedCourses.add(result.getMaMon()) && result.getSoTinChi() != null) {
                        totalEarnedCredits += result.getSoTinChi();
                    }
                }
            }

            double semesterAverage = semesterGradedCredits == 0 ? Double.NaN
                    : semesterWeightedScore / semesterGradedCredits;
            double cumulativeAverage = diemService.tinhGpaTichLuy(results, semester);

            VBox card = createSemesterCard(semester, semesterResults, semesterAverage,
                    cumulativeAverage, semesterEarnedCredits, totalEarnedCredits);
            cards.add(card);
        }
        for (int i = cards.size() - 1; i >= 0; i--) {
            semesterResults.getChildren().add(cards.get(i));
        }
        showInfo("Điểm trung bình học kỳ tính theo tín chỉ; GPA tích lũy lấy điểm cao nhất của mỗi môn. "
                + "Tín chỉ tích lũy chỉ tính các học phần đã hoàn thành và đạt từ 4,0.");
    }

    private boolean isPassed(LopHocDangKy result) {
        return result.getTrangThaiDangKy() == TrangThaiDangKy.DA_HOAN_THANH
                && result.getDiemTongKet() != null && result.getDiemTongKet() >= 4.0f;
    }

    private VBox createSemesterCard(String semester, List<LopHocDangKy> results,
                                    double semesterAverage, double cumulativeAverage,
                                    long semesterCredits, long cumulativeCredits) {
        Label title = new Label("Học kỳ " + semester);
        title.getStyleClass().add("section-title");

        TableView<LopHocDangKy> table = new TableView<>(
                FXCollections.observableArrayList(results));
        table.getStyleClass().add("registration-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        table.setPrefHeight(Math.min(320, 44 + results.size() * 32));
        table.setMinHeight(Math.min(120, 44 + results.size() * 32));
        table.setPlaceholder(new Label("Chưa có kết quả học tập."));
        table.getColumns().add(column("Mã lớp", 90, LopHocDangKy::getMaLop));
        table.getColumns().add(column("Mã môn", 90, LopHocDangKy::getMaMon));
        table.getColumns().add(column("Học phần", 220, LopHocDangKy::getTenMon));
        table.getColumns().add(column("Tín chỉ", 70, result ->
                result.getSoTinChi() == null ? "—" : result.getSoTinChi().toString()));
        table.getColumns().add(column("Điểm tổng kết", 100, result -> result.getDiemTongKet() == null
                ? "Chưa có điểm" : SCORE_FORMAT.format(result.getDiemTongKet())));
        table.getColumns().add(column("Điểm thành phần", 250, result ->
                result.getDiemThanhPhan() == null || result.getDiemThanhPhan().isBlank()
                        ? "Chưa có điểm thành phần" : result.getDiemThanhPhan()));
        table.getColumns().add(column("Trạng thái", 115, LopHocDangKy::getTrangThai));

        HBox summary = new HBox(12,
                metric("ĐIỂM TB HỌC KỲ", formatAverage(semesterAverage)),
                metric("ĐIỂM TB TOÀN KHÓA", formatAverage(cumulativeAverage)),
                metric("TÍN CHỈ ĐẠT TRONG KỲ", semesterCredits + " tín chỉ"),
                metric("TỔNG TÍN CHỈ TÍCH LŨY", cumulativeCredits + " tín chỉ"));
        summary.getStyleClass().add("grades-summary");
        for (javafx.scene.Node child : summary.getChildren()) HBox.setHgrow(child, Priority.ALWAYS);

        VBox card = new VBox(12, title, table, summary);
        card.getStyleClass().add("registration-card");
        return card;
    }

    private TableColumn<LopHocDangKy, String> column(
            String title, double width, java.util.function.Function<LopHocDangKy, String> value) {
        TableColumn<LopHocDangKy, String> column = new TableColumn<>(title);
        column.setPrefWidth(width);
        column.setCellValueFactory(data -> new ReadOnlyStringWrapper(value.apply(data.getValue())));
        return column;
    }

    private VBox metric(String heading, String value) {
        Label headingLabel = new Label(heading);
        headingLabel.getStyleClass().add("grades-metric-heading");
        Label valueLabel = new Label(value);
        valueLabel.getStyleClass().add("grades-metric-value");
        VBox metric = new VBox(6, headingLabel, valueLabel);
        metric.getStyleClass().add("grades-metric");
        return metric;
    }

    private int compareSemesters(String first, String second) {
        Matcher firstMatch = YEAR_TERM.matcher(first == null ? "" : first);
        Matcher secondMatch = YEAR_TERM.matcher(second == null ? "" : second);
        if (firstMatch.matches() && secondMatch.matches()) {
            int yearComparison = Integer.compare(
                    Integer.parseInt(firstMatch.group(1)), Integer.parseInt(secondMatch.group(1)));
            if (yearComparison != 0) return yearComparison;
            return Integer.compare(Integer.parseInt(firstMatch.group(2)), Integer.parseInt(secondMatch.group(2)));
        }
        return Comparator.nullsFirst(String.CASE_INSENSITIVE_ORDER).compare(first, second);
    }

    private String formatAverage(double average) {
        return Double.isNaN(average) ? "—" : SCORE_FORMAT.format(average) + " / 10";
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
