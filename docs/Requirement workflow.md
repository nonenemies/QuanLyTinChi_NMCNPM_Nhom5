# **I. CONCEPT EXPLORATION**

## **1\. Thuật ngữ liên quan đến người**

* Sinh viên (Student)  
* Giảng viên (Lecturer)  
* Quản trị viên (Admin)  
* Tài khoản (Account)  
* Vai trò người dùng (User Role)

  ## **2\. Thuật ngữ liên quan đến đối tượng**

* Môn học (Course)  
* Lớp học phần (Course Section)  
* Học kỳ (Semester)  
* Tín chỉ (Credit)  
* Thời khóa biểu (Timetable)  
* Điểm học phần (Course Grade)  
* Thành phần đánh giá (Assessment Component)  
* Phiếu đăng ký học phần (Course Registration)

  ## **3\. Thuật ngữ liên quan đến hoạt động**

* Đăng nhập  
* Quản lý sinh viên, giảng viên  
* Quản lý môn học  
* Thiết lập thành phần đánh giá  
* Mở, cập nhật và xóa lớp học phần  
* Phân công giảng viên  
* Đăng ký và hủy đăng ký học phần  
* Xem thời khóa biểu  
* Nhập và cập nhật điểm  
* Xem kết quả học tập  
* Thống kê kết quả học tập

  ## **4\. Thuật ngữ liên quan đến quy định, luật lệ**

* Quy định về thời gian đăng ký tín chỉ  
* Quy định về số tín chỉ tối đa, tối thiểu  
* Quy định về sĩ số lớp học phần  
* Quy định về trùng lịch học  
* Quy định về môn học tiên quyết  
* Quy định về thành phần và tỷ trọng điểm  
* Quy định về quyền nhập, sửa và xem điểm

# **1.2 . BUSINESS MODEL**

## **1.2.1. Mô tả bằng ngôn ngữ tự nhiên**

### **1.2.1.1. Mục đích và phạm vi hệ thống**

**a. Mục đích**

Hệ thống quản lý đăng ký tín chỉ và kết quả học tập là phần mềm hỗ trợ sinh viên, giảng viên và quản trị viên trong việc quản lý các hoạt động học tập tại một trường đại học.

Hệ thống cho phép sinh viên đăng ký học phần, xem danh sách lớp học phần đã đăng ký và tra cứu kết quả học tập. Giảng viên có thể xem danh sách lớp phụ trách, nhập và cập nhật điểm cho sinh viên. Quản trị viên có thể quản lý thông tin sinh viên, giảng viên, môn học và lớp học phần.

Mục đích của hệ thống là hỗ trợ quản lý thông tin tập trung, giảm thao tác thủ công và hạn chế sai sót trong quá trình đăng ký học phần và quản lý điểm.

**b. Phạm vi hệ thống**

* **Kiểu ứng dụng:** Phần mềm quản lý trên máy tính.  
* **Phạm vi phục vụ:** Quản lý đăng ký tín chỉ và kết quả học tập trong phạm vi một trường đại học.  
* **Đối tượng sử dụng:** Sinh viên, giảng viên, quản trị viên.

**Các chức năng chính:**

* Đăng nhập và phân quyền người dùng.  
* Admin thêm thông tin sinh viên  
* Admin thêm thông tin giảng viên  
* Admin thêm môn học  
* Sinh viên đăng ký lớp học phần  
* Sinh viên hủy đăng ký các lớp học phần  
* Sinh viên xem danh sách lớp đã đăng ký  
* Giảng viên nhập điểm cho các lớp  
* Sinh viên xem kết quả học tập  
* Giảng viên xem danh sách lớp giảng dạy

![][image1]

### **1.2.1.2. Ai? Làm gì?**

**a. Sinh viên**

* **Đăng nhập:** Sử dụng tài khoản được cấp để truy cập hệ thống.  
* **Xem danh sách lớp học phần:** Xem các lớp học phần hiện có, bao gồm mã lớp, tên môn học, giảng viên và sĩ số.  
* **Đăng ký học phần:** Lựa chọn lớp muốn đăng ký. Hệ thống kiểm tra sĩ số, đăng ký trùng và điều kiện môn tiên quyết trước khi lưu.  
* **Hủy đăng ký học phần:** Lựa chọn lớp học phần đã đăng ký để hủy, hệ thống cập nhật lại danh sách đăng ký.  
* **Xem danh sách học phần đã đăng ký:** Tra cứu các lớp học phần mà sinh viên đã đăng ký.  
  // Cái này bên code mình đăng ký thì nó sẽ hiện tự động danh sách HP nên ko cần viết kịch bản chức năng mà nó có trong cái đăng ký và hủy HP  
* **Xem điểm:** Xem điểm thành phần và điểm tổng kết của các môn học.

**b. Giảng viên**

* **Đăng nhập:** Sử dụng tài khoản giảng viên để đăng nhập và truy cập các chức năng được phân quyền trong hệ thống.  
* **Nhập điểm học phần:** Xem danh sách các lớp học phần được phân công giảng dạy, lựa chọn lớp học phần, xem danh sách sinh viên đã đăng ký và nhập điểm theo các thành phần đánh giá của môn học.  
* **Xem bảng điểm:** Xem lại điểm thành phần và điểm tổng kết của sinh viên trong lớp học phần sau khi nhập điểm.

**c. Quản trị viên (Admin)**

* **Đăng nhập:** Sử dụng tài khoản quản trị để đăng nhập và truy cập các chức năng được phân quyền trong hệ thống.  
* **Thêm sinh viên:** Tiếp nhận thông tin sinh viên mới và cập nhật hồ sơ sinh viên vào hệ thống.  
* **Thêm giảng viên:** Tiếp nhận thông tin giảng viên mới và cập nhật hồ sơ giảng viên vào hệ thống.  
* **Thêm môn học:** Cập nhật thông tin các môn học mới theo chương trình đào tạo đã được nhà trường phê duyệt.

### **1.2.1.3. Hoạt động của các chức năng.**

**1\. Chức năng đăng nhập và phân quyền người dùng**

### **1.2.1.3. Hoạt động của các chức năng**

**1\. Chức năng đăng nhập và phân quyền người dùng**

- Sinh viên, giảng viên và Admin được nhà trường hoặc bộ phận quản lý hệ thống cấp tài khoản đăng nhập gồm Email và mật khẩu, tương ứng với vai trò của từng người dùng.  
- Người dùng mở phần mềm quản lý đăng ký tín chỉ và kết quả học tập → Hệ thống hiển thị giao diện đăng nhập gồm ô nhập "Email", ô nhập "Mật khẩu", nút "Đăng nhập" và khu vực hiển thị thông báo.  
- Người dùng nhập Email và mật khẩu đã được cấp vào các ô tương ứng, mật khẩu được hiển thị dưới dạng các ký tự được che để bảo đảm tính bảo mật.  
- Sau khi nhập thông tin, người dùng nhấn nút "Đăng nhập" → Hệ thống tiếp nhận Email và Mật khẩu, đối chiếu với thông tin tài khoản trong cơ sở dữ liệu để xác thực người dùng và kiểm tra trạng thái hoạt động của tài khoản.  
- Sau khi xác thực thành công → Hệ thống lấy thông tin người dùng gồm họ tên, mã người dùng và vai trò tài khoản (Sinh viên, Giảng viên hoặc Admin).  
- Hệ thống xác định vai trò của tài khoản để quản lý quyền truy cập các chức năng tương ứng → Chuyển người dùng đến giao diện chính (Dashboard) và hiển thị lời chào kèm họ tên người dùng.  
- Người dùng xem giao diện chính và có thể lựa chọn các chức năng thuộc phạm vi quyền hạn của mình. Quá trình đăng nhập và xác định vai trò hoàn tất.

**2\. Sinh viên đăng ký lớp học phần.**

- Sinh viên đăng nhập thành công vào hệ thống → Hệ thống hiển thị giao diện chính của sinh viên gồm các chức năng: Tổng quan, Đăng ký tín chỉ, Yêu cầu đổi lớp và Đăng xuất.  
- Sinh viên chọn chức năng "Đăng ký tín chỉ" → Hệ thống hiển thị giao diện đăng ký tín chỉ gồm thông tin sinh viên (mã sinh viên, họ tên), danh sách chọn học kỳ, ô tìm kiếm, nút "Tải lại", bảng "Lớp học đang mở", khu vực "Chi tiết lớp được chọn", nút "Xác nhận đăng ký" và bảng "Lớp đã đăng ký".  
- Hệ thống tự động hiển thị danh sách lớp học phần đang mở gồm mã lớp, mã môn học, tên môn học, học kỳ, số tín chỉ, giảng viên phụ trách, sĩ số hiện tại, sĩ số tối đa và trạng thái lớp học phần. Đồng thời, bảng "Lớp đã đăng ký" hiển thị các lớp học phần mà sinh viên đã đăng ký trước đó, gồm mã lớp, tên môn học, học kỳ, số tín chỉ và trạng thái đăng ký.  
- Sinh viên chọn học kỳ muốn đăng ký → Hệ thống hiển thị danh sách các lớp học phần thuộc học kỳ đã chọn tại bảng "Lớp học đang mở".  
- Sinh viên nhập mã lớp, mã môn học, tên môn học hoặc tên giảng viên vào ô tìm kiếm → Hệ thống tự động lọc và hiển thị các lớp học phần phù hợp.  
- Sinh viên chọn một lớp học phần còn chỗ và chưa đăng ký trong danh sách → Hệ thống hiển thị thông tin tại khu vực "Chi tiết lớp được chọn" (gồm mã lớp, mã môn học, tên môn học, học kỳ, số tín chỉ, giảng viên phụ trách, sĩ số hiện tại, sĩ số tối đa và trạng thái lớp học phần), đồng thời kích hoạt nút "Xác nhận đăng ký".  
- Sinh viên nhấn nút "Xác nhận đăng ký" → Hệ thống hiển thị hộp thoại xác nhận với thông tin lớp học phần được chọn và hai nút "OK", "Cancel".  
- Sinh viên kiểm tra thông tin và nhấn "OK" → Hệ thống kiểm tra các điều kiện đăng ký gồm trạng thái lớp học phần, sĩ số, thông tin đăng ký trước đó và môn học tiên quyết → Hệ thống lưu thông tin đăng ký của sinh viên vào cơ sở dữ liệu, tự động ghi nhận ngày đăng ký, trạng thái đăng ký và cập nhật sĩ số lớp học phần.  
- Hệ thống hiển thị thông báo "Đăng ký thành công lớp \[Mã lớp\]" → Tự động cập nhật sĩ số tại bảng "Lớp học đang mở" và bổ sung lớp học phần vừa đăng ký vào bảng "Lớp đã đăng ký".  
- Sinh viên xem lại danh sách các lớp học phần đã đăng ký tại bảng "Lớp đã đăng ký" hoặc tiếp tục lựa chọn và đăng ký các lớp học phần khác.  
- Sau khi hoàn tất, sinh viên nhấn nút "Dashboard" → Hệ thống quay về giao diện chính của sinh viên.

**3\. Sinh viên hủy đăng ký học phần.**

- Sinh viên đăng nhập thành công vào hệ thống → Hệ thống hiển thị giao diện chính của sinh viên gồm các chức năng: Tổng quan, Đăng ký tín chỉ, Yêu cầu đổi lớp và Đăng xuất.  
- Sinh viên chọn chức năng "Đăng ký tín chỉ" → Hệ thống hiển thị giao diện đăng ký tín chỉ gồm thông tin sinh viên, ô tìm kiếm lớp học phần, danh sách chọn học kỳ, bảng "Lớp học đang mở", bảng "Lớp đã đăng ký" và các nút "Tải lại", "Xác nhận đăng ký", "Hủy đăng ký", "Dashboard".  
- Hệ thống tự động hiển thị danh sách các lớp học phần mà sinh viên đã đăng ký tại bảng "Lớp đã đăng ký", gồm các thông tin: Mã lớp học phần, tên môn học, học kỳ, số tín chỉ và trạng thái đăng ký.  
- Sinh viên chọn một lớp học phần muốn hủy trong bảng "Lớp đã đăng ký" → Hệ thống ghi nhận lớp học phần được chọn và kích hoạt nút "Hủy đăng ký".  
- Sinh viên nhấn nút "Hủy đăng ký" → Hệ thống hiển thị hộp thoại xác nhận gồm mã lớp học phần, tên môn học, nội dung "Bạn có chắc chắn muốn hủy đăng ký lớp học phần này không?" và hai nút "Xác nhận", "Hủy".  
- Sinh viên kiểm tra thông tin lớp học phần và nhấn nút "Xác nhận" → Hệ thống kiểm tra thông tin đăng ký và điều kiện hủy học phần → Hệ thống cập nhật trạng thái đăng ký học phần thành "Đã hủy" trong cơ sở dữ liệu và cập nhật sĩ số hiện tại của lớp học phần.  
- Sau khi hủy thành công → Hệ thống hiển thị thông báo "Hủy đăng ký học phần thành công\!" và tự động cập nhật bảng "Lớp đã đăng ký", loại bỏ lớp học phần vừa hủy khỏi danh sách, đồng thời cập nhật sĩ số tại bảng "Lớp học đang mở".  
- Sinh viên có thể tiếp tục đăng ký hoặc hủy các lớp học phần khác. Sau khi hoàn tất, sinh viên nhấn nút "Dashboard" → Hệ thống quay về giao diện chính của sinh viên.

**4\. Admin thêm thông tin sinh viên.**

- Khi nhà trường tiếp nhận sinh viên mới, phòng đào tạo hoặc bộ phận tuyển sinh tổng hợp danh sách sinh viên đã hoàn tất thủ tục nhập học và chuyển thông tin cho bộ phận quản lý hệ thống.  
- Admin tiếp nhận danh sách và hồ sơ sinh viên, đối chiếu các thông tin gồm mã sinh viên, họ tên, Email, ngày sinh, giới tính, lớp, khóa học và hệ đào tạo để bảo đảm thông tin chính xác trước khi cập nhật lên hệ thống.  
- Admin đăng nhập thành công vào hệ thống → Hệ thống hiển thị giao diện chính của Admin với các chức năng (Quản lý tài khoản, sinh viên, giảng viên, môn học, lớp học phần và thành phần đánh giá).  
- Admin chọn chức năng "Quản lý sinh viên" → Hệ thống hiển thị giao diện quản lý sinh viên gồm danh sách sinh viên (mã sinh viên, họ tên, Email, lớp, khóa học, hệ đào tạo), ô tìm kiếm và các nút "Tìm kiếm", "Thêm sinh viên", "Sửa", "Xóa".  
- Admin nhấn nút "Thêm sinh viên" → Hệ thống hiển thị giao diện thêm sinh viên với các ô nhập thông tin (mã sinh viên, họ tên, Email, ngày sinh, giới tính, lớp, khóa học, hệ đào tạo) và hai nút "Lưu", "Hủy".  
- Admin căn cứ vào hồ sơ đã tiếp nhận để nhập thông tin sinh viên mới vào các ô tương ứng và nhấn nút "Lưu" → Hệ thống kiểm tra tính hợp lệ của thông tin và lưu thông tin sinh viên vào cơ sở dữ liệu.  
- Sau khi lưu thành công → Hệ thống hiển thị thông báo "Thêm sinh viên thành công\!" → Admin nhấn "OK" → Hệ thống quay về giao diện quản lý sinh viên với danh sách đã được cập nhật.  
- Admin nhập mã sinh viên vừa thêm vào ô tìm kiếm và nhấn nút "Tìm kiếm" → Hệ thống hiển thị thông tin sinh viên tương ứng → Admin đối chiếu lại thông tin đã lưu với hồ sơ ban đầu.  
- Sau khi hoàn tất, Admin thông báo cho phòng đào tạo hoặc bộ phận phụ trách sinh viên rằng hồ sơ sinh viên mới đã được cập nhật trên hệ thống, phục vụ công tác quản lý và đăng ký học phần.  
- Admin có thể tiếp tục thêm sinh viên khác hoặc quay về giao diện chính của Admin.

**5\. Admin thêm thông tin giảng viên.**

- Khi nhà trường tiếp nhận giảng viên mới, khoa bộ môn cung cấp thông tin giảng viên cho bộ phận quản lý, gồm mã giảng viên, họ tên, Email, học vị và chuyên môn, kèm hồ sơ hoặc quyết định tiếp nhận giảng viên.  
- Admin tiếp nhận hồ sơ và đối chiếu các thông tin do khoa hoặc bộ môn cung cấp để bảo đảm thông tin giảng viên được ghi nhận chính xác trước khi đưa vào hệ thống.  
- Admin đăng nhập vào hệ thống → Hệ thống hiển thị giao diện chính của Admin gồm các chức năng: Quản lý tài khoản, Quản lý sinh viên, Quản lý giảng viên, Quản lý môn học, Quản lý lớp học phần và Quản lý thành phần đánh giá.  
- Admin chọn chức năng "Quản lý giảng viên" → Hệ thống hiển thị danh sách giảng viên gồm mã giảng viên, họ tên, Email, học vị, chuyên môn, cùng ô tìm kiếm và các nút "Tìm kiếm", "Thêm giảng viên", "Sửa", "Xóa", "Quay lại".  
- Admin nhấn nút "Thêm giảng viên" → Hệ thống hiển thị giao diện nhập thông tin giảng viên mới gồm mã giảng viên, họ tên, Email, học vị, chuyên môn và hai nút "Lưu", "Hủy".  
- Admin căn cứ vào hồ sơ đã tiếp nhận để nhập đầy đủ thông tin giảng viên vào các ô tương ứng → Nhấn nút "Lưu" → Hệ thống kiểm tra tính hợp lệ của dữ liệu và lưu thông tin giảng viên mới vào cơ sở dữ liệu.  
- Sau khi lưu thành công → Hệ thống hiển thị thông báo "Thêm giảng viên thành công\!" cùng nút "OK" → Admin nhấn "OK" → Hệ thống quay về giao diện "Quản lý giảng viên" và cập nhật danh sách giảng viên.  
- Admin nhập mã giảng viên vừa thêm vào ô tìm kiếm và nhấn nút "Tìm kiếm" → Hệ thống hiển thị thông tin giảng viên tương ứng → Admin đối chiếu lại với hồ sơ ban đầu để bảo đảm thông tin đã được lưu chính xác.  
- Sau khi hoàn tất, Admin thông báo cho khoa bộ môn rằng thông tin giảng viên mới đã được cập nhật trên hệ thống để phục vụ công tác quản lý và phân công giảng dạy.  
- Admin có thể tiếp tục thêm giảng viên khác hoặc nhấn nút "Quay lại" → Hệ thống trở về giao diện chính của Admin.

### **6\. Chức năng giảng viên nhập điểm học phần.**

- Trong quá trình giảng dạy, giảng viên tổ chức các hoạt động đánh giá sinh viên theo quy định của môn học như bài tập, kiểm tra và thi kết thúc học phần. Sau khi chấm bài, giảng viên tổng hợp kết quả đánh giá của từng sinh viên để cập nhật lên hệ thống.  
- Giảng viên đăng nhập thành công vào hệ thống → Hệ thống hiển thị giao diện chính của giảng viên với các chức năng được cấp quyền.  
- Giảng viên chọn chức năng "Nhập điểm" → Hệ thống hiển thị giao diện quản lý điểm gồm: danh sách chọn học kỳ và danh sách các lớp học phần được phân công giảng dạy.  
- Giảng viên chọn học kỳ cần nhập điểm → Hệ thống hiển thị danh sách các lớp học phần do giảng viên phụ trách trong học kỳ đó (Mã lớp học phần, mã môn học, tên môn học, học kỳ và sĩ số hiện tại).  
- Giảng viên nhấn chọn một lớp học phần muốn nhập điểm → Hệ thống hiển thị danh sách sinh viên đã đăng ký lớp học phần đó (Số thứ tự, mã sinh viên, họ tên, các cột điểm thành phần và điểm tổng kết).  
- Các cột điểm thành phần được hệ thống hiển thị theo cấu hình đánh giá của môn học (Điểm bài tập, điểm kiểm tra và điểm thi, tương ứng với trọng số đã được thiết lập).  
- Giảng viên đối chiếu danh sách sinh viên trên hệ thống với bảng điểm đã tổng hợp → Chọn các ô điểm tương ứng và nhập kết quả đánh giá của từng sinh viên vào các cột điểm thành phần.  
- Sau khi hoàn tất nhập điểm, giảng viên kiểm tra lại các điểm vừa nhập và nhấn nút "Lưu" → Hệ thống ghi nhận điểm của từng sinh viên theo lớp học phần và thành phần đánh giá tương ứng vào cơ sở dữ liệu.  
- Khi sinh viên đã có đầy đủ điểm của các thành phần đánh giá → Hệ thống tự động tính điểm tổng kết theo trọng số của từng thành phần và cập nhật kết quả tại cột "Điểm tổng kết".  
- Sau khi lưu thành công → Hệ thống hiển thị thông báo "Lưu điểm thành công\!" và cập nhật bảng điểm của lớp học phần.  
- Giảng viên xem lại kết quả đã lưu và đối chiếu với bảng điểm tổng hợp ban đầu để bảo đảm thông tin chính xác.  
- Sau khi hoàn tất việc nhập điểm, giảng viên có thể tiếp tục chọn lớp học phần khác để nhập điểm hoặc nhấn nút "Quay lại" → Hệ thống trở về giao diện chính của giảng viên.

**7\. Sinh viên xem kết quả học tập.**

- Sau khi kết thúc các hoạt động kiểm tra, đánh giá của học phần, giảng viên tổng hợp và nhập điểm của sinh viên lên hệ thống. Sinh viên có thể truy cập phần mềm để xem kết quả học tập của mình.  
- Sinh viên đăng nhập thành công vào hệ thống → Hệ thống hiển thị giao diện chính của sinh viên với các chức năng được cấp quyền.  
- Sinh viên chọn chức năng "Xem kết quả học tập" → Hệ thống hiển thị giao diện kết quả học tập gồm thông tin sinh viên (mã sinh viên, họ tên), danh sách chọn học kỳ, bảng kết quả học tập và nút "Quay lại".  
- Hệ thống hiển thị danh sách các học kỳ có trong hệ thống tại ô chọn "Học kỳ" để sinh viên lựa chọn.  
- Sinh viên nhấn vào ô chọn "Học kỳ" → Hệ thống hiển thị danh sách các học kỳ → Sinh viên chọn học kỳ muốn xem kết quả học tập.  
- Hệ thống truy xuất kết quả học tập của sinh viên trong học kỳ đã chọn từ cơ sở dữ liệu → Hiển thị danh sách các học phần gồm mã lớp học phần, mã môn học, tên môn học, số tín chỉ và điểm tổng kết.  
- Sinh viên nhấn chọn một học phần trong bảng kết quả học tập → Hệ thống hiển thị bảng điểm chi tiết của học phần đó, gồm các thành phần đánh giá, trọng số, điểm thành phần và điểm tổng kết.  
- Sinh viên xem và đối chiếu kết quả học tập với các bài kiểm tra, bài thi đã thực hiện trong học kỳ.  
- Sau khi xem xong, sinh viên có thể tiếp tục chọn học kỳ khác trong danh sách → Hệ thống cập nhật bảng kết quả học tập tương ứng với học kỳ mới được chọn.  
- Sau khi hoàn tất việc xem kết quả học tập, sinh viên nhấn nút "Quay lại" → Hệ thống trở về giao diện chính của sinh viên.

**8\. Admin thêm môn học.**

- Khi nhà trường xây dựng hoặc cập nhật chương trình đào tạo, phòng đào tạo phối hợp với các khoa, bộ môn tổng hợp danh sách các môn học mới đã được phê duyệt, gồm mã môn học, tên môn học, số tín chỉ, loại môn học và mô tả nội dung môn học.  
- Phòng đào tạo chuyển danh sách môn học mới cho bộ phận quản lý hệ thống → Admin tiếp nhận và đối chiếu thông tin với chương trình đào tạo đã được phê duyệt để tiến hành cập nhật vào hệ thống.  
- Admin đăng nhập thành công vào hệ thống → Hệ thống hiển thị giao diện chính của Admin với các chức năng quản lý tài khoản, sinh viên, giảng viên, môn học, lớp học phần và thành phần đánh giá.  
- Admin chọn chức năng "Quản lý môn học" → Hệ thống hiển thị giao diện quản lý môn học gồm danh sách các môn học (mã môn học, tên môn học, số tín chỉ, loại môn học, mô tả và trạng thái), ô tìm kiếm và các nút "Tìm kiếm", "Thêm môn học", "Sửa", "Xóa", "Quay lại".  
- Admin nhấn nút "Thêm môn học" → Hệ thống hiển thị giao diện thêm môn học gồm các ô nhập thông tin (mã môn học, tên môn học, số tín chỉ, loại môn học, mô tả) và hai nút "Lưu", "Hủy".  
- Admin căn cứ vào danh sách do phòng đào tạo cung cấp để nhập thông tin môn học mới vào các ô tương ứng → Kiểm tra lại nội dung đã nhập và nhấn nút "Lưu" → Hệ thống kiểm tra tính hợp lệ của dữ liệu và lưu thông tin môn học mới vào cơ sở dữ liệu.  
- Sau khi lưu thành công → Hệ thống hiển thị thông báo "Thêm môn học thành công\!" → Admin nhấn "OK" → Hệ thống quay về giao diện "Quản lý môn học" và cập nhật danh sách, trong đó có môn học vừa được thêm.  
- Admin nhập mã môn học vừa tạo vào ô tìm kiếm và nhấn nút "Tìm kiếm" → Hệ thống hiển thị thông tin môn học tương ứng để Admin đối chiếu lại với danh sách môn học đã được phê duyệt.  
- Sau khi kiểm tra hoàn tất, Admin thông báo cho phòng đào tạo rằng môn học mới đã được cập nhật trên hệ thống, làm cơ sở để nhà trường mở lớp học phần và tổ chức đăng ký tín chỉ cho sinh viên.  
- Admin có thể tiếp tục thêm các môn học khác hoặc nhấn nút "Quay lại" → Hệ thống trở về giao diện chính của Admin.

### **1.2.1.4. Đối tượng thông tin cần xử lý**

- **Tài khoản người dùng:** Mã người dùng, họ tên, Email, mật khẩu, vai trò, trạng thái tài khoản.  
- **Sinh viên:** Mã sinh viên, họ tên, Email, ngày sinh, giới tính, lớp, khóa học, hệ đào tạo.  
- **Giảng viên:** Mã giảng viên, họ tên, Email, học vị, chuyên môn.  
- **Admin:** Mã quản trị viên, thông tin tài khoản, vai trò quản trị.  
- **Môn học:** Mã môn học, tên môn học, số tín chỉ, loại môn học, mô tả, trạng thái.  
- **Môn học tiên quyết:** Mã môn học, mã môn học tiên quyết.  
- **Lớp học phần:** Mã lớp học phần, mã môn học, mã giảng viên, học kỳ, sĩ số hiện tại, sĩ số tối đa, trạng thái.  
- **Thông tin đăng ký học phần:** Mã đăng ký, mã sinh viên, mã lớp học phần, ngày đăng ký, trạng thái đăng ký, điểm tổng kết.  
- **Thành phần đánh giá:** Mã thành phần, mã môn học, tên thành phần, trọng số.  
- **Điểm thành phần:** Mã điểm, mã đăng ký học phần, mã thành phần đánh giá, điểm đạt được.  
- **Thông tin kết quả học tập:** Mã sinh viên, học kỳ, mã môn học, tên môn học, số tín chỉ, điểm thành phần, điểm tổng kết.

### **1.2.1.5. Quan hệ giữa các thông tin**

- Một tài khoản người dùng được gắn với một vai trò: Sinh viên, Giảng viên hoặc Admin.  
- Một sinh viên có một tài khoản đăng nhập vào hệ thống.  
- Một giảng viên có một tài khoản đăng nhập vào hệ thống.  
- Một môn học có thể được mở thành nhiều lớp học phần.  
- Một lớp học phần chỉ thuộc về một môn học.  
- Một giảng viên có thể phụ trách nhiều lớp học phần.  
- Một lớp học phần được phân công cho một giảng viên phụ trách.  
- Một sinh viên có thể đăng ký nhiều lớp học phần.  
- Một lớp học phần có thể có nhiều sinh viên đăng ký.  
- Một môn học có thể yêu cầu nhiều môn học tiên quyết.  
- Một môn học có thể có nhiều thành phần đánh giá.  
- Một thành phần đánh giá có thể được sử dụng để ghi điểm cho nhiều sinh viên.  
- Một lần đăng ký học phần có thể có nhiều điểm thành phần.  
- Mỗi điểm thành phần thuộc về một lần đăng ký học phần và một thành phần đánh giá cụ thể.  
- Mỗi lần đăng ký học phần có một điểm tổng kết khi sinh viên hoàn thành đầy đủ các thành phần đánh giá.

## Quy tắc nghiệp vụ được áp dụng

- Đăng ký, hủy đăng ký và đổi lớp chỉ được thực hiện trong đợt do Admin cấu hình cho học kỳ.
- Sinh viên có thể đăng ký tạm các lớp trong đợt; cần chốt học kỳ khi tổng tín chỉ từ 11 đến 26. Mọi thay đổi sau khi chốt sẽ bỏ trạng thái chốt và cần chốt lại.
- Không đăng ký đồng thời hai lớp cùng môn trong một học kỳ, và không đăng ký lại môn đã đạt. Sinh viên chỉ được học lại môn chưa đạt.
- Hủy lớp bị từ chối nếu đã phát sinh bất kỳ điểm thành phần nào.
- Đổi lớp yêu cầu hai sinh viên đang học lớp của nhau, cùng học kỳ, chưa có điểm thành phần; phải thỏa điều kiện tiên quyết, lịch học và giới hạn tín chỉ 11–26 cho cả hai sau khi đổi. Việc đổi bảo toàn lịch sử đăng ký và không chuyển điểm giữa các lớp.
- Bài đổi lớp đang chờ tự hết hiệu lực khi đợt đăng ký kết thúc. Mỗi sinh viên không thể đăng nhiều bài chờ có lớp nguồn hoặc lớp đích trùng nhau.
- Điểm chỉ được nhập hoặc sửa từ ngày kết thúc học kỳ đến hạn nhập điểm do Admin cấu hình. GPA tích lũy dùng điểm cao nhất của mỗi môn.
- Admin quản lý lịch học và mốc thời gian của từng học kỳ; không thể thay đổi môn, giảng viên hoặc học kỳ của lớp đã có đăng ký.

### 

[image1]: <data:image/png;base64,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>