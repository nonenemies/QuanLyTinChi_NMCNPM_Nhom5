package Controllers;

import Model.LopHocThoiKhoaBieu;
import Model.Users;
import Services.DangKyHocService;
import Services.ServiceException;
import javafx.geometry.Insets;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.layout.ColumnConstraints;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

import java.util.List;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;

public class TimetableController {
    private static final String[] DAY_NAMES = {
            "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7", "Chủ nhật"
    };
    private static final int SESSION_COUNT = 17;
    private static final int LUNCH_SESSION = 6;
    private static final int DAY_START_MINUTES = 7 * 60;
    private static final int SCHEDULE_MINUTES = SESSION_COUNT * 60;
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("H:mm");

    private final DangKyHocService registrationService = new DangKyHocService();
    private final List<Node> lessonCards = new ArrayList<>();
    private Users student;

    @FXML private Label lblStatus;
    @FXML private GridPane gridSchedule;

    @FXML
    private void initialize() {
        configureColumns(gridSchedule);
        addHeaderCell("CA", 0, "timetable-fixed-header");
        for (int day = 0; day < DAY_NAMES.length; day++) {
            addHeaderCell(DAY_NAMES[day], day + 1, "timetable-day-header");
        }
        addHeaderCell("GIỜ HỌC", 8, "timetable-fixed-header");
        for (int session = 0; session < SESSION_COUNT; session++) {
            addScheduleRow(session);
        }
    }

    public void setStudent(Users student) {
        this.student = student;
        if (student == null || !"SINHVIEN".equalsIgnoreCase(student.getRole())
                || student.getMaSo() == null || student.getMaSo().isBlank()) {
            showStatus("Không xác định được mã sinh viên. Vui lòng đăng nhập lại.", true);
            return;
        }
        loadTimetable();
    }

    @FXML
    private void onRefresh() {
        loadTimetable();
    }

    private void loadTimetable() {
        clearSchedule();
        if (student == null || student.getMaSo() == null || student.getMaSo().isBlank()) {
            return;
        }
        try {
            List<LopHocThoiKhoaBieu> lessons =
                    registrationService.xemLichHocTheoTuan(student.getMaSo());
            if (lessons.isEmpty()) {
                showStatus("Bạn chưa có lớp đang học được xếp lịch. Bảng 20 ca vẫn được hiển thị.", false);
                return;
            }

            for (LopHocThoiKhoaBieu lesson : lessons) {
                if (lesson.getThu() < 2 || lesson.getThu() > 8) {
                    continue;
                }
                addLessonToSchedule(lesson);
            }
            showStatus("Thời khóa biểu gồm " + lessons.size()
                    + " buổi học. Mỗi môn nằm theo đúng khung giờ thực tế.", false);
        } catch (ServiceException e) {
            showStatus("Không thể tải thời khóa biểu. Hãy kiểm tra kết nối cơ sở dữ liệu rồi thử lại.", true);
        }
    }

    private void addLessonToSchedule(LopHocThoiKhoaBieu lesson) {
        LocalTime start;
        LocalTime end;
        try {
            start = LocalTime.parse(lesson.getGioBatDau());
            end = LocalTime.parse(lesson.getGioKetThuc());
        } catch (DateTimeParseException e) {
            return;
        }
        int startOfDay = start.getHour() * 60 + start.getMinute();
        int endOfDay = end.getHour() * 60 + end.getMinute();
        int relativeStart = startOfDay - DAY_START_MINUTES;
        if (relativeStart < 0) {
            relativeStart += 24 * 60;
        }
        int relativeEnd = endOfDay - DAY_START_MINUTES;
        if (relativeEnd < 0) {
            relativeEnd += 24 * 60;
        }
        if (relativeEnd <= relativeStart) {
            relativeEnd += 24 * 60;
        }

        int duration = relativeEnd - relativeStart;
        if (relativeStart >= SCHEDULE_MINUTES || relativeEnd > SCHEDULE_MINUTES || duration <= 0) {
            return;
        }

        int sessionIndex = relativeStart / 60;
        int rowSpan = (relativeStart % 60 + duration + 59) / 60;
        VBox card = createLessonCard(lesson);
        card.setMaxHeight(Double.MAX_VALUE);
        GridPane.setHgrow(card, Priority.ALWAYS);
        GridPane.setVgrow(card, Priority.ALWAYS);
        GridPane.setMargin(card, new Insets(3));
        gridSchedule.add(card, lesson.getThu() - 1, sessionIndex + 1, 1, rowSpan);
        lessonCards.add(card);
    }

    private void addScheduleRow(int rowIndex) {
        int gridRow = rowIndex + 1;
        Label session = new Label("Ca " + (rowIndex + 1));
        session.getStyleClass().add("timetable-session");
        session.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(session, Priority.ALWAYS);
        gridSchedule.add(session, 0, gridRow);

        for (int day = 0; day < DAY_NAMES.length; day++) {
            VBox dayCell = new VBox(7);
            dayCell.getStyleClass().add("timetable-cell");
            dayCell.setMaxWidth(Double.MAX_VALUE);
            GridPane.setHgrow(dayCell, Priority.ALWAYS);
            if (rowIndex + 1 == LUNCH_SESSION) {
                Label lunch = new Label("NGHỈ TRƯA");
                lunch.setWrapText(true);
                lunch.getStyleClass().add("timetable-lunch");
                dayCell.getChildren().add(lunch);
            }
            gridSchedule.add(dayCell, day + 1, gridRow);
        }

        int startMinutes = DAY_START_MINUTES + rowIndex * 60;
        int endMinutes = startMinutes + 60;
        Label time = new Label(formatTime(startMinutes) + " - " + formatTime(endMinutes));
        time.getStyleClass().add("timetable-time");
        time.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(time, Priority.ALWAYS);
        gridSchedule.add(time, 8, gridRow);
    }

    private void clearSchedule() {
        gridSchedule.getChildren().removeAll(lessonCards);
        lessonCards.clear();
    }

    private String formatTime(int minutesFromMidnight) {
        int timeOfDay = Math.floorMod(minutesFromMidnight, 24 * 60);
        return LocalTime.of(timeOfDay / 60, timeOfDay % 60).format(TIME_FORMAT);
    }

    private VBox createLessonCard(LopHocThoiKhoaBieu lesson) {
        VBox card = new VBox(4);
        card.getStyleClass().add("timetable-lesson");
        Label courseName = new Label(lesson.getTenMon());
        courseName.setWrapText(true);
        courseName.getStyleClass().add("timetable-lesson-name");
        Label courseCode = new Label(lesson.getMaMon() + " · " + lesson.getMaLop());
        courseCode.setWrapText(true);
        courseCode.getStyleClass().add("timetable-lesson-code");
        Label actualTime = new Label(lesson.getGioBatDau() + " - " + lesson.getGioKetThuc());
        actualTime.getStyleClass().add("timetable-lesson-time");
        Label semester = new Label("Học kỳ " + lesson.getHocKy());
        semester.getStyleClass().add("timetable-lesson-semester");
        Label room = new Label(lesson.getPhongHoc() == null || lesson.getPhongHoc().isBlank()
                ? "Chưa cập nhật phòng học" : lesson.getPhongHoc());
        room.setWrapText(true);
        room.getStyleClass().add("timetable-lesson-room");
        card.getChildren().addAll(courseName, courseCode, actualTime, room, semester);
        return card;
    }

    private void configureColumns(GridPane grid) {
        double[] percentages = {8.0, 12.0, 12.0, 12.0, 12.0, 12.0, 12.0, 12.0, 8.0};
        for (double percentage : percentages) {
            ColumnConstraints column = new ColumnConstraints();
            column.setPercentWidth(percentage);
            column.setHgrow(Priority.ALWAYS);
            grid.getColumnConstraints().add(column);
        }
        grid.setMaxWidth(Double.MAX_VALUE);
    }

    private void addHeaderCell(String text, int column, String styleClass) {
        Label label = new Label(text);
        label.getStyleClass().add(styleClass);
        label.setMaxWidth(Double.MAX_VALUE);
        GridPane.setHgrow(label, Priority.ALWAYS);
        gridSchedule.add(label, column, 0);
    }

    private void showStatus(String message, boolean error) {
        lblStatus.setText(message);
        lblStatus.getStyleClass().removeAll("timetable-status-error", "timetable-status-info");
        lblStatus.getStyleClass().add(error ? "timetable-status-error" : "timetable-status-info");
        lblStatus.setVisible(true);
        lblStatus.setManaged(true);
    }

}
