# Review nghiệp vụ BTL\_OOP\_Nhom5

Nov 1, 2026 · @Hoang Anh

## Phạm vi

Review chỉ dựa trên đọc code ở commit `0cb6f1e` của nhánh `main`; chưa chạy app hay DB. Đã đọc toàn bộ Service, DAO, `Database/SQL/test.sql`, các controller đăng ký, hủy, đổi lớp, điểm, admin và thư mục `docs/`.

Phần làm tốt: transaction có khóa dòng theo thứ tự sinh viên rồi lớp, tăng sĩ số nguyên tử, và ngừng hoạt động thay vì xóa. Lỗi nằm ở luật nghiệp vụ còn thiếu, không phải ở kỹ thuật.

## Nghiêm trọng

Bốn lỗi làm sai bảng điểm hoặc chặn hẳn luồng đăng ký, nên sửa trước.

1. **Sinh viên hủy lớp để xóa điểm rớt.**
   - Vị trí: `DiemDAO.java:76`, `DangKyHocService.java:105-111`, `LopHocDangKy.java:59`, `DangKyHocDAO.java:117-126`.
   - Môn có tổng kết dưới 4.0 vẫn mang trạng thái `DANG_HOC`, nên hủy được. Dòng bị `HUY` biến khỏi bảng điểm và GPA; đăng ký lại còn khôi phục điểm cũ.
   - Sửa: thêm trạng thái `KHONG_DAT`, chặn hủy khi đã có điểm, thêm hạn hủy.
2. **Đổi lớp làm hỏng dữ liệu điểm.**
   - Vị trí: `DangKyHocDAO.java:136` (`capNhatLopChoSinhVien`), `ExchangeController.java:115`.
   - Dòng đăng ký đổi `MaLop` nhưng giữ nguyên điểm, và được phép với dòng `DA_HOAN_THANH`, khác môn, khác học kỳ. Sinh viên đã đạt môn X đổi sang lớp môn Y thì `kiemTraDaQuaMon` coi như đã qua Y, lách được môn tiên quyết.
   - Sửa: chỉ cho đổi dòng `DANG_HOC` chưa có điểm, cùng môn, cùng học kỳ. Muốn giữ đổi khác môn thì phải xử lý điểm khi chuyển.
3. **Đăng ký được nhiều lớp cùng môn hoặc môn đã đạt.**
   - Vị trí: `DangKyHocService.java:36-49`.
   - Chỉ chặn trùng đúng `MaLop`. Hai lớp cùng môn lệch giờ vẫn đăng ký được, môn đã đạt vẫn đăng ký lại được và thêm một lượt vào GPA.
   - Sửa: chặn theo `MaMon` trong cùng học kỳ và chốt luật học lại.
4. **Admin không tạo được lịch học.**
   - Vị trí: `LichHocDAO` không nơi nào gọi; `AdminView.fxml` không có ô nhập lịch.
   - Lớp tạo trong app không có lịch nên `coLichHoc` trả về false và không ai đăng ký được. Hiện chỉ chèn SQL tay mới chạy. Cũng chưa có kiểm tra trùng lịch giảng viên hay trùng phòng.
   - Sửa: thêm màn hình và service quản lý lịch học, kèm kiểm tra trùng giảng viên, phòng.

## Trung bình

Tám vấn đề không làm sai ngay bảng điểm nhưng để lọt các trường hợp biên hoặc thiếu luật.

| # | Vấn đề | Vị trí | Hướng xử lý |
| --- | --- | --- | --- |
| 5 | Đổi lớp lỗi hệ thống khi từng hủy lớp đích: dòng `HUY` cũ vi phạm `uq_dangky_sv_lop` lúc `UPDATE MaLop` | `test.sql:94`, `YeuCauDoiLopService.java:170-171` | Xử lý riêng dòng `HUY` cũ, hoặc trả false thay vì ném lỗi |
| 6 | Sửa trọng số sau khi đã có điểm không tính lại tổng kết; giảng viên sửa điểm vô thời hạn và có thể lật trạng thái đạt/rớt | `ThanhPhanDanhGiaService.java:55-128` | Chặn sửa cấu hình khi đã có điểm, hoặc tính lại; thêm khóa điểm sau hạn |
| 7 | Không có học kỳ hiện tại, đợt đăng ký hay giới hạn tín chỉ, dù danh sách khái niệm trong tài liệu yêu cầu có nêu | `docs/Requirement workflow.md`, `DangKyHocService.java` | Thêm cấu hình học kỳ mở đăng ký và tín chỉ tối thiểu, tối đa |
| 8 | Sửa lớp đã có sinh viên vẫn đổi được `MaMon`, `HocKy`, `MaGV` | `LopHocDAO.java:37-52` | Khóa các trường này khi lớp đã có đăng ký |
| 9 | Phân quyền không đồng nhất: service admin kiểm tra lại DB, còn đăng ký, hủy, đổi lớp, nhập điểm tin vào mã truyền vào hoặc đối tượng đã đăng nhập; tài khoản bị ngừng giữa phiên vẫn thao tác được | `DiemService.java:115-121`, `DangKyHocService.java` | Kiểm tra trạng thái tài khoản trong DB ở mỗi thao tác ghi |
| 10 | Môn tiên quyết không chống vòng (A cần B, B cần A) và không kiểm tra môn tiên quyết đã ngừng | `MonTienQuyetDAO.java:18-33` | Dò chu trình khi thêm quan hệ |
| 11 | Đăng ký chỉ trả false, sinh viên không biết thiếu tiên quyết hay hết chỗ | `DangKyHocService.java`, `RegistrationController.java:118-122` | Trả kết quả có mã lý do |
| 12 | Sàn đổi lớp: một sinh viên đăng nhiều bài cho cùng lớp, bài tồn đọng khi lớp đã xong; không có bước duyệt của giáo vụ (cột `NguoiXuLy` đang lưu sinh viên chốt) | `YeuCauDoiLopService.java:74-109`, `YeuCauDoiLopDAO.java` | Chặn bài trùng, tự hủy bài hết hiệu lực; thêm bước duyệt nếu trường yêu cầu |

## Cần chốt chính sách hoặc khác tài liệu

Nhóm cần quyết định vài luật học vụ trước khi sửa code, và cập nhật tài liệu cho khớp.

- **GPA học lại:** hiện tính mọi lượt học (`StudentGradesController.java:98-103`, bản sao ở `DashBoardController.java:124-127`). Nhiều trường chỉ lấy lượt cao nhất hoặc mới nhất cho GPA tích lũy; nhóm nên chốt và ghi vào tài liệu. Nên chuyển logic này xuống Service để hai màn hình khỏi lệch nhau.
- **Điểm thành phần:** tài liệu yêu cầu sinh viên xem điểm thành phần, nhưng màn hình chỉ có điểm tổng kết.
- **Tài liệu yêu cầu:** chưa có use case đổi lớp, quản lý lớp học phần, thành phần đánh giá, môn tiên quyết và chatbot. Admin trong tài liệu "Xóa" còn code là ngừng hoạt động.
- **Schema:** thiếu `UNIQUE(MaDangKy, MaTP)` ở `DiemThanhPhan`, thiếu `UNIQUE(MaMon, ThuTu)` ở `ThanhPhanDanhGia`, thiếu `CHECK` cho sĩ số và tín chỉ; `HocKy` là chuỗi tự do.
- **Ngoài nghiệp vụ:** mật khẩu so sánh dạng thô (`AuthService.java:13`), và `test.sql` tạo sẵn tài khoản mật khẩu `123` dù README nói không có tài khoản mặc định.

## Thứ tự sửa đề xuất

Sửa các mục 1, 2 và 4 trước: mục 1 và 2 làm sai bảng điểm, mục 4 chặn toàn bộ luồng đăng ký lớp mới.

1. Mục 1 và 2: thêm trạng thái `KHONG_DAT`, chặn hủy khi đã có điểm, giới hạn đổi lớp trong dòng `DANG_HOC` chưa có điểm.
2. Mục 4: thêm quản lý lịch học cho admin để lớp mới đăng ký được.
3. Mục 3 và 7: chốt luật học lại, học kỳ mở đăng ký và giới hạn tín chỉ, rồi thêm kiểm tra vào đăng ký.
4. Mục 5, 6, 8 đến 12: sửa theo bảng ở phần Trung bình.
5. Phần chính sách: chốt luật GPA và cập nhật `docs/Requirement workflow.md` cho khớp code.

Bước tiếp theo: repo hiện chỉ mở quyền đọc, nên để sửa trực tiếp cần gắn repo với quyền ghi, hoặc viết sẵn diff để nhóm áp dụng.
