# BTL OOP - Quản lý sinh viên

Ứng dụng JavaFX quản lý các nghiệp vụ học vụ gồm đăng nhập, đăng ký/hủy lớp, đổi lớp, thời khóa biểu, quản lý điểm và trợ lý AI tư vấn học tập.

Sau khi đăng nhập, sinh viên, giảng viên và quản trị viên đều vào màn hình tổng quan chung. Màn hình hiển thị hồ sơ và các số liệu phù hợp với vai trò, lấy từ dữ liệu hệ thống; các nghiệp vụ khác vẫn được truy cập qua thanh điều hướng bên trái.

## Công nghệ và yêu cầu

- Java 17
- JavaFX 21.0.13
- AtlantaFX 2.0.1
- Maven
- MySQL
- MySQL Connector/J 9.5.0
- JDBC với `PreparedStatement`
- Google Gemini API và Gson cho trợ lý AI tư vấn học tập

JavaFX 21.0.13 được chọn để chạy cùng JDK 17. AtlantaFX 2.0.1 được build với Java 17 và JavaFX 20, nên dùng cùng JavaFX 21 là lựa chọn tương thích gần với phiên bản nền của thư viện. Cả nhóm nên dùng JDK 17 và giữ các phiên bản dependency theo `pom.xml`.

Yêu cầu tải thêm SceneBuilder để sau này dùng

## Cấu trúc dự án

```text
Database/
└── SQL/
    └── QuanLyTinChi.sql
docs/
└── thanh-phan-danh-gia.md
src/
└── main/
    ├── java/
    │   ├── Controllers/
    │   │   ├── GiangVienController.java
    │   │   └── LoginController.java
    │   ├── DAO/
    │   │   ├── AdminDAO.java
    │   │   ├── DangKyHocDAO.java
    │   │   ├── DiemDAO.java
    │   │   ├── GiangVienDAO.java
    │   │   ├── LichHocDAO.java
    │   │   ├── LopHocDAO.java
    │   │   ├── MonHocDAO.java
    │   │   ├── MonTienQuyetDAO.java
    │   │   ├── SinhVienDAO.java
    │   │   ├── ThanhPhanDanhGiaDAO.java
    │   │   ├── UsersDAO.java
    │   │   └── YeuCauDoiLopDAO.java
    │   ├── Model/
    │   │   ├── Admin.java
    │   │   ├── BangDiemSinhVien.java
    │   │   ├── DangKyHoc.java
    │   │   ├── DiemThanhPhan.java
    │   │   ├── GiangVien.java
    │   │   ├── LichHoc.java
    │   │   ├── LopHoc.java
    │   │   ├── MonHoc.java
    │   │   ├── SinhVien.java
    │   │   ├── ThanhPhanDanhGia.java
    │   │   ├── ThongTinQuyen.java
    │   │   ├── TrangThaiDangKy.java
    │   │   ├── TrangThaiDoiLop.java
    │   │   ├── TrangThaiHoatDong.java
    │   │   ├── Users.java
    │   │   └── YeuCauDoiLop.java
    │   ├── Services/
    │   │   ├── DangKyHocService.java
    │   │   ├── DiemService.java
    │   │   ├── ServiceException.java
    │   │   ├── ThanhPhanDanhGiaService.java
    │   │   └── YeuCauDoiLopService.java
    │   ├── Util/
    │   │   └── DBConnection.java
    │   ├── Launcher.java
    │   └── MainApp.java
    └── resources/
        ├── css/
        │   └── app.css
        └── views/
            ├── DashBoardView.fxml
            ├── GiangVienView.fxml
            └── LoginView.fxml
pom.xml
README.md
```

`MainApp` khởi tạo JavaFX, `Launcher` là điểm vào để chạy ứng dụng, còn Controller xử lý sự kiện giao diện và gọi Service. Các DAO thực hiện truy vấn; Model chứa dữ liệu và enum; Service xử lý nghiệp vụ và transaction. Thư mục `src/main/resources/css/` hiện được dành sẵn cho stylesheet nhưng chưa có file CSS. `target/` và `.idea/` là dữ liệu sinh ra/cấu hình IDE, không phải mã nguồn nghiệp vụ.

Các DAO chính gồm `DangKyHocDAO`, `DiemDAO`, `LopHocDAO`, `MonHocDAO`, `MonTienQuyetDAO`, `ThanhPhanDanhGiaDAO` và `YeuCauDoiLopDAO`. Các Service chính gồm `DangKyHocService`, `DiemService`, `ThanhPhanDanhGiaService` và `YeuCauDoiLopService`.

## Cấu hình database

Chạy duy nhất `Database/SQL/QuanLyTinChi.sql` trong MySQL Workbench để tạo lại database `QuanLyTinChi` cùng schema và dữ liệu mẫu. **Script xóa toàn bộ database `QuanLyTinChi` hiện có trước khi tạo lại**, vì vậy chỉ chạy trên môi trường test/phát triển và sao lưu nếu cần giữ dữ liệu. Tài khoản mẫu dùng mật khẩu `123`; đây là dữ liệu phát triển, không dùng cho production. Kết nối ứng dụng đọc cấu hình từ biến môi trường:

| Biến | Bắt buộc | Giá trị mặc định |
| --- | --- | --- |
| `DB_URL` | Không | `jdbc:mysql://localhost:3306/QuanLyTinChi` |
| `DB_USER` | Có | Không có |
| `DB_PASSWORD` | Có | Không có |
| `GEMINI_API_KEY` | Chỉ cần dùng chatbot | Không có |

Ví dụ PowerShell (chỉ áp dụng cho cửa sổ hiện tại):

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/QuanLyTinChi"
$env:DB_USER = "your_mysql_user"
$env:DB_PASSWORD = "your_mysql_password"
$env:GEMINI_API_KEY = "your_gemini_api_key"
mvn test
```

Trong IntelliJ IDEA, cấu hình các biến này trong **Run → Edit Configurations → Environment variables** cho cấu hình chạy. Chatbot dùng model Gemini 3.6 Flash và chỉ đọc API key từ `GEMINI_API_KEY`. Khi tư vấn, ứng dụng gửi câu hỏi cùng dữ liệu học phần, học kỳ, tín chỉ, trạng thái và điểm của sinh viên đang đăng nhập tới Google Gemini; không gửi mã sinh viên, email hoặc tên. Ứng dụng không tự tạo tài khoản mặc định và không tự đọc `.env` hay `*.local.properties`; riêng script SQL có các tài khoản demo với mật khẩu `123`. Không ghi credentials thật vào mã nguồn hoặc commit lên Git.

File SQL duy nhất tạo đầy đủ các bảng ứng dụng, chương trình đào tạo mẫu cho `CNTT`, lịch đăng ký mẫu đang mở và năm lớp/môn trong học kỳ `2026-2` (tổng cộng 15 tín chỉ để kiểm thử giới hạn tối thiểu 11). Các database đang tồn tại cần được tạo lại bằng script này; dự án không còn các file migration riêng.

## Mô hình dữ liệu và trạng thái

Các bảng chính: `Users`, `SinhVien`, `GiaoVien`, `Admin`, `MonHoc`, `MonTienQuyet`, `ChuongTrinhDaoTao`, `DotDangKyNganh`, `CauHinhHocKy`, `LopHoc`, `LichHoc`, `ThanhPhanDanhGia`, `DangKyHoc`, `DiemThanhPhan` và `YeuCauDoiLop`.

- `MaSV` và `MaGV` là mã nghiệp vụ kiểu chuỗi; khóa ngoại tới sinh viên/giảng viên dùng các mã này. `YeuCauDoiLop.NguoiXuLy` vẫn tham chiếu `Users.UserID`, vì đây là ID tài khoản người xử lý.
- Các mã tự tăng như `UserID`, `MaDangKy`, `MaYeuCau`, `MaTP` và `MaDiemTP` là ID số do database quản lý.
- `LichHoc` lưu lịch lặp theo tuần của lớp bằng thứ, giờ bắt đầu, giờ kết thúc và phòng học.
- `Users`, `MonHoc` và `LopHoc` sử dụng `Model.TrangThaiHoatDong` (`HOAT_DONG`, `NGUNG_HOAT_DONG`); các cột tương ứng trong MySQL dùng `ENUM`. `ThanhPhanDanhGia.TrangThai` là cờ bật/tắt riêng, không phải enum này.
- Các trạng thái đăng ký và yêu cầu đổi lớp được biểu diễn bằng enum model tương ứng.

## Nghiệp vụ chính

### Đăng ký học

Đợt đăng ký được cấu hình theo `Users.Khoa`, không theo học kỳ. Chương trình đào tạo được nạp/cố định trước khi mở đợt; Admin không thể sửa danh mục môn khi đợt đang mở. Trong thời gian đăng ký sinh viên chỉ thấy lớp hoạt động thuộc chương trình của khoa/ngành mình, bất kể giá trị học kỳ của lớp. `HocKy` trên lớp dùng để nhóm kết quả và áp dụng giới hạn tín chỉ cố định trong `QuyDinhTinChi`: mức tối thiểu là cảnh báo, mức tối đa là giới hạn cứng cho từng học kỳ. Hết đợt theo khoa/ngành thì đăng ký, hủy và đổi lớp bị khóa; sinh viên không cần chốt đăng ký và có thể tiếp tục với tổng tín chỉ dưới mức tối thiểu. Đăng ký, hủy và đổi lớp được kiểm tra lại trong Service; lịch được đối chiếu theo thứ và khoảng giờ trong cùng học kỳ. Dòng sinh viên được khóa để các yêu cầu đồng thời không vượt qua kiểm tra lịch/tín chỉ. Sĩ số được cập nhật cùng thao tác đăng ký/hủy trong transaction. Lớp đã có điểm không thể hủy. Lớp đã hủy có thể đăng ký lại nếu còn chỗ và vẫn đạt điều kiện. Lỗi database làm rollback transaction.

Màn hình `TimetableView.fxml` luôn hiển thị 17 hàng ca và các cột từ thứ 2 đến chủ nhật, kể cả khi chưa có lớp đăng ký. Ca 1 bắt đầu lúc 07:00, mỗi ca dài một giờ; ca 6 (12:00–13:00) được dành cho nghỉ trưa. Các lớp được đặt vào đúng khung giờ dựa trên lịch thực tế và kéo dài qua các hàng ca tương ứng. Cột cuối hiển thị khoảng giờ cố định của từng ca. Bảng cuộn dọc để xem hết các ca.

### Điểm

Giảng viên đăng nhập được chuyển đến màn hình tổng quan; các lớp được phân công và bảng điểm mở từ thanh điều hướng. Màn hình bảng điểm chỉ liệt kê các lớp được phân công, tải danh sách sinh viên đã đăng ký và bảng điểm theo các thành phần đánh giá đang hoạt động. Chức năng lưu điểm hỗ trợ cả nhập mới và cập nhật; Service kiểm tra quyền phụ trách lớp, lưu điểm và tính lại điểm tổng kết trong cùng transaction. Điểm tổng kết chỉ được cập nhật khi đã đủ điểm cho các thành phần đang hoạt động.

Sinh viên xem điểm tổng kết tại `StudentGradesView.fxml`, được nhóm theo học kỳ. Điểm trung bình học kỳ và toàn khóa là trung bình có trọng số tín chỉ trên thang 10, chỉ tính học phần đã có điểm tổng kết. Tín chỉ đạt trong kỳ và tín chỉ tích lũy chỉ tính học phần hoàn thành có điểm từ 4,0; một môn đã đạt chỉ được cộng tín chỉ tích lũy một lần. Lịch sử học lại được tính theo từng lượt có điểm trong trung bình.

Sinh viên có thể mở **Trợ lý AI học tập** từ sidebar để hỏi về kết quả học tập và kế hoạch học. Yêu cầu mạng chạy nền để không khóa giao diện; cần cấu hình `GEMINI_API_KEY` trước khi sử dụng.

Sau khi đăng nhập, `AppShell.fxml` giữ sidebar và khung ứng dụng cố định; các Controller nạp nội dung chức năng vào vùng trung tâm mà không thay toàn bộ Scene. Menu được cấu hình theo role: sinh viên có tổng quan, thời khóa biểu, điểm tổng kết, đăng ký và đổi lớp; giảng viên có lớp phụ trách/bảng điểm; admin có các mục quản lý sinh viên, giảng viên, môn học, lớp học phần và thành phần đánh giá.

### Quản trị viên

`AdminView.fxml` dùng lại các model và DAO hiện có để tìm kiếm, thêm, cập nhật và ngừng hoạt động tài khoản sinh viên/giảng viên; quản lý môn học và quan hệ môn tiên quyết; quản lý lớp học phần, sĩ số tối đa và giảng viên phụ trách; cấu hình trọng số thành phần đánh giá qua `ThanhPhanDanhGiaService`. Tài khoản, môn học và lớp được ngừng hoạt động thay vì xóa vật lý để giữ dữ liệu học vụ và lịch sử liên quan. Việc tạo/cập nhật tài khoản cùng hồ sơ vai trò được thực hiện trong một transaction.

### Thành phần đánh giá

`ThanhPhanDanhGiaService` kiểm tra tài khoản admin đang hoạt động, dữ liệu tên/trọng số/thứ tự và tổng trọng số bằng `1`. Các thay đổi được ghi trong một transaction; lỗi sẽ rollback. Hướng dẫn chi tiết tại `docs/thanh-phan-danh-gia.md`.

### Đổi lớp

`YeuCauDoiLopService` đăng yêu cầu sau khi kiểm tra đăng ký và môn tiên quyết. Khi một sinh viên chốt yêu cầu, hệ thống kiểm tra lịch của cả hai sinh viên (bỏ qua lớp mỗi người đang rời khỏi), rồi mới hoán đổi hai đăng ký và cập nhật trạng thái yêu cầu trong cùng transaction.

## Xử lý lỗi

DAO sử dụng `SQLException` cho lỗi JDBC. Các Service chuyển lỗi ra ngoài dưới dạng `ServiceException`; lỗi kỹ thuật được giữ nguyên nguyên nhân để ghi log, còn UI nên hiển thị thông báo thân thiện thay vì chi tiết JDBC/stack trace. Một số nghiệp vụ trả về `String` hoặc `boolean` cho kết quả nghiệp vụ dự kiến (ví dụ điều kiện đăng ký không đạt); cần giữ cách xử lý kết quả này ở caller.
