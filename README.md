# Quản lý tín chỉ

Ứng dụng JavaFX quản lý đăng ký tín chỉ và kết quả học tập. Phạm vi hiện tại gồm đúng các chức năng theo vai trò:

1. Đăng nhập và phân quyền theo vai trò sinh viên, giảng viên, quản trị viên.
2. Admin quản lý sinh viên và tài khoản sinh viên.
3. Admin quản lý giảng viên và tài khoản giảng viên.
4. Admin quản lý môn học.
5. Sinh viên xem thời khóa biểu.
6. Sinh viên xem điểm tổng kết.
7. Sinh viên đăng ký và hủy lớp học phần.
8. Giảng viên xem lớp được phân công và nhập/cập nhật điểm.

## Công nghệ

- Java 17, JavaFX 21.0.13, AtlantaFX 2.0.1
- Maven, MySQL, JDBC

## Cấu trúc mã nguồn

```text
src/main/java/
├── Controllers/   # Giao diện đăng nhập, vỏ ứng dụng và 8 chức năng
├── DAO/           # Truy vấn JDBC
├── Model/         # Tài khoản, môn học, đăng ký và điểm
├── Services/      # Nghiệp vụ đăng nhập, quản trị, đăng ký và điểm
├── Util/          # Kết nối MySQL
├── Launcher.java
└── MainApp.java
src/main/resources/
├── css/app.css
└── views/         # FXML cho các màn hình còn được sử dụng
Database/SQL/QuanLyTinChi.sql
```

`MainApp` khởi tạo JavaFX; `Launcher` là điểm vào được cấu hình cho Maven. `AppShellController` chỉ hiển thị menu theo vai trò và mở các màn hình thuộc phạm vi trên. Các lớp học, lịch học, đợt đăng ký, chương trình đào tạo và cấu hình học kỳ được nạp sẵn từ script cơ sở dữ liệu; ứng dụng không có màn hình quản trị các dữ liệu cấu hình này.

## Cơ sở dữ liệu và chạy ứng dụng

Chạy `Database/SQL/QuanLyTinChi.sql` trong MySQL Workbench để tạo lại database `QuanLyTinChi` và dữ liệu mẫu. **Script xóa database hiện có trước khi tạo lại**; chỉ chạy trong môi trường phát triển/test và sao lưu trước nếu cần giữ dữ liệu. Tài khoản demo dùng mật khẩu `123`; không dùng thông tin này trong môi trường production.

Kết nối đọc cấu hình từ biến môi trường:

| Biến | Bắt buộc | Giá trị mặc định |
| --- | --- | --- |
| `DB_URL` | Không | `jdbc:mysql://localhost:3306/QuanLyTinChi` |
| `DB_USER` | Có | Không có |
| `DB_PASSWORD` | Có | Không có |

Ví dụ PowerShell:

```powershell
$env:DB_URL = "jdbc:mysql://localhost:3306/QuanLyTinChi"
$env:DB_USER = "your_mysql_user"
$env:DB_PASSWORD = "your_mysql_password"
mvn javafx:run
```

## Nghiệp vụ chính

Đăng ký và hủy lớp được kiểm tra trong Service theo đợt đăng ký, chương trình đào tạo, môn tiên quyết, lịch trùng, sĩ số và giới hạn tín chỉ. Thay đổi đăng ký và sĩ số được thực hiện trong transaction; lớp đã có điểm không thể hủy. Thời khóa biểu và điểm được đọc theo tài khoản sinh viên đang đăng nhập.

Giảng viên chỉ xem được lớp được phân công. Điểm thành phần được nhập theo cấu hình đánh giá đã có trong cơ sở dữ liệu; điểm tổng kết chỉ được tính khi đủ điểm cho các thành phần đang hoạt động. Admin có thể thêm, sửa và ngừng hoạt động tài khoản sinh viên/giảng viên, cũng như thêm, sửa và ngừng hoạt động môn học.
