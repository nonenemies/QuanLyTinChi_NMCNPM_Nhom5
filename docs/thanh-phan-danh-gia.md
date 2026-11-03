# Thêm/sửa thành phần đánh giá

Người dùng nhập từng tên và trọng số vào danh sách tạm. Chỉ khi bấm Lưu,
giao diện gọi Service một lần với toàn bộ danh sách của môn học.

```java
ThanhPhanDanhGiaService service = new ThanhPhanDanhGiaService();
List<ThanhPhanDanhGia> danhSach = new ArrayList<>();

ThanhPhanDanhGia giuaKy = new ThanhPhanDanhGia();
giuaKy.setTenTP("Giữa kỳ");
giuaKy.setTrongSo(0.3f);
danhSach.add(giuaKy);

ThanhPhanDanhGia cuoiKy = new ThanhPhanDanhGia();
cuoiKy.setTenTP("Cuối kỳ");
cuoiKy.setTrongSo(0.7f);
danhSach.add(cuoiKy);

List<Integer> cacMaMoi = service.themThanhPhan(nguoiDungDangNhap, "IT02", danhSach);
```

Import `Services.ThanhPhanDanhGiaService`, `Model.ThanhPhanDanhGia`,
`java.util.List` và `java.util.ArrayList`.
Khi thêm, Service tự gán mã môn, thứ tự theo danh sách và trạng thái hoạt động;
DAO lấy mã thành phần tự tăng từ database. Danh sách mã trả về theo thứ tự nhập.

Ràng buộc trước khi ghi:

- Danh sách không rỗng; tên là String không null, không trống hoặc chỉ có khoảng trắng,
  tối đa 200 ký tự theo schema database.
- Mỗi trọng số thuộc (0, 1], hữu hạn và tối đa hai chữ số thập phân.
- Tổng trọng số đúng bằng 1, tính bằng BigDecimal để tránh sai số cộng float.
- Môn học tồn tại. Nếu đã có thành phần đang hoạt động, dùng chức năng sửa;
  thêm mới không ghi đè cấu hình hiện có.
- Chỉ người dùng có Role ADMIN và TrangThai `HOAT_DONG` trong database được ghi dữ liệu.
  Đối tượng Users phải lấy từ phiên đăng nhập đã xác thực.

Service quản lý connection, phân quyền, nghiệp vụ và transaction. DAO chỉ thực
hiện SQL trên cùng connection; Model chỉ giữ dữ liệu. Mọi thành phần được lưu
trong một transaction: lỗi ở bất kỳ phần nào sẽ rollback toàn bộ.

Sửa một thành phần vẫn dùng `suaThanhPhan(nguoiDungDangNhap, thanhPhan)`.
Để đổi nhiều trọng số cùng lúc, dùng
`suaThanhPhan(nguoiDungDangNhap, maMon, danhSachCanSua)` với các mã và thứ tự
hiện có. Tổng trọng số đang hoạt động sau khi sửa, gồm cả thành phần không sửa,
phải đúng bằng 1. Thứ tự không được trùng trong cùng môn.

Lỗi quyền phát sinh SecurityException, lỗi dữ liệu phát sinh IllegalArgumentException,
lỗi database phát sinh SQLException để giao diện xử lý.
