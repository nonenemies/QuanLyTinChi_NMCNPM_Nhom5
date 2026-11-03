package Controllers;

import Model.LopHocDangKy;
import Model.SinhVien;
import Model.TrangThaiDangKy;
import Model.Users;
import Services.DiemService;
import Services.GeminiAIService;
import javafx.concurrent.Task;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;

import java.text.DecimalFormat;
import java.util.List;

public class ChatbotController {
    private static final DecimalFormat SCORE_FORMAT = new DecimalFormat("0.##");

    private final GeminiAIService geminiAIService = new GeminiAIService();
    private final DiemService diemService = new DiemService();

    @FXML private ScrollPane chatScrollPane;
    @FXML private VBox chatMessages;
    @FXML private TextField txtQuestion;
    @FXML private Button btnSend;
    @FXML private Label lblChatStatus;

    private SinhVien student;
    private boolean requestInProgress;

    public void setStudent(Users user) {
        if (user instanceof SinhVien sinhVien) {
            student = sinhVien;
            addBubble("Xin chào! Mình có thể hỗ trợ bạn tham khảo kết quả học tập và lập kế hoạch học tập.",
                    false);
        } else {
            addBubble("Không xác định được tài khoản sinh viên. Vui lòng đăng nhập lại.", false);
            setRequestInProgress(true);
        }
    }

    @FXML
    private void onSend() {
        String question = txtQuestion.getText() == null ? "" : txtQuestion.getText().trim();
        if (question.isEmpty() || requestInProgress) {
            return;
        }
        if (student == null) {
            setStatus("Không thể xác định tài khoản sinh viên.", true);
            return;
        }

        txtQuestion.clear();
        addBubble(question, true);
        Label typingMessage = addBubble("AI đang gõ...", false);
        setRequestInProgress(true);
        setStatus("", false);

        Task<String> task = new Task<>() {
            @Override
            protected String call() {
                String academicContext = buildAcademicContext(diemService.layKetQuaHocTap(student));
                return geminiAIService.askAI(question, academicContext);
            }
        };
        task.setOnSucceeded(event -> {
            chatMessages.getChildren().remove(typingMessage.getParent());
            addBubble(task.getValue(), false);
            setRequestInProgress(false);
        });
        task.setOnFailed(event -> {
            chatMessages.getChildren().remove(typingMessage.getParent());
            addBubble("Không thể tải dữ liệu học tập hoặc kết nối AI. Vui lòng thử lại.", false);
            setRequestInProgress(false);
        });

        Thread worker = new Thread(task, "gemini-chat-request");
        worker.setDaemon(true);
        worker.start();
    }

    @FXML
    private void onQuestionSubmitted() {
        onSend();
    }

    private String buildAcademicContext(List<LopHocDangKy> results) {
        if (results.isEmpty()) {
            return "Sinh viên hiện chưa có học phần nào trong dữ liệu bảng điểm.";
        }
        StringBuilder context = new StringBuilder("Các học phần của sinh viên:\n");
        for (LopHocDangKy result : results) {
            context.append("- Học kỳ ").append(valueOrUnknown(result.getHocKy()))
                    .append(": ").append(valueOrUnknown(result.getTenMon()))
                    .append(" (").append(valueOrUnknown(result.getMaMon())).append("), ")
                    .append(result.getSoTinChi() == null ? "chưa rõ" : result.getSoTinChi())
                    .append(" tín chỉ, trạng thái ")
                    .append(result.getTrangThaiDangKy() == null
                            ? "chưa rõ" : result.getTrangThaiDangKy().name());
            if (result.getDiemTongKet() == null) {
                context.append(", chưa có điểm tổng kết");
            } else {
                context.append(", điểm tổng kết ")
                        .append(SCORE_FORMAT.format(result.getDiemTongKet()));
                if (result.getTrangThaiDangKy() == TrangThaiDangKy.DA_HOAN_THANH
                        && result.getDiemTongKet() < 4) {
                    context.append(" (chưa đạt)");
                }
            }
            context.append('\n');
        }
        return context.toString();
    }

    private String valueOrUnknown(String value) {
        return value == null || value.isBlank() ? "chưa rõ" : value;
    }

    private Label addBubble(String text, boolean fromUser) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(560);
        bubble.getStyleClass().add(fromUser ? "chat-bubble-user" : "chat-bubble-ai");

        HBox row = new HBox(bubble);
        row.setAlignment(fromUser ? Pos.CENTER_RIGHT : Pos.CENTER_LEFT);
        row.getStyleClass().add("chat-message-row");
        chatMessages.getChildren().add(row);
        chatScrollPane.layout();
        chatScrollPane.setVvalue(1.0);
        return bubble;
    }

    private void setRequestInProgress(boolean inProgress) {
        requestInProgress = inProgress;
        btnSend.setDisable(inProgress);
        txtQuestion.setDisable(inProgress);
        btnSend.setText(inProgress ? "Đang gửi..." : "Gửi");
    }

    private void setStatus(String message, boolean error) {
        lblChatStatus.setText(message);
        lblChatStatus.getStyleClass().removeAll("chat-status-error", "chat-status-info");
        if (message != null && !message.isBlank()) {
            lblChatStatus.getStyleClass().add(error ? "chat-status-error" : "chat-status-info");
        }
    }
}
