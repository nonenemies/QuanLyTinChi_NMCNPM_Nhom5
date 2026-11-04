package Services;

import DAO.GiangVienDAO;
import DAO.LopHocDAO;
import DAO.CauHinhHocKyDAO;
import DAO.ChuongTrinhDaoTaoDAO;
import DAO.LichHocDAO;
import DAO.MonHocDAO;
import DAO.MonTienQuyetDAO;
import DAO.SinhVienDAO;
import DAO.UsersDAO;
import Model.Admin;
import Model.GiangVien;
import Model.LopHoc;
import Model.LichHoc;
import Model.MonHoc;
import Model.MonTienQuyet;
import Model.SinhVien;
import Model.TrangThaiHoatDong;
import Model.Users;
import Model.CauHinhHocKy;
import Model.DotDangKyNganh;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** Nghiệp vụ quản trị, gồm đồng bộ tài khoản với hồ sơ vai trò. */
public class AdminService {
    private final UsersDAO usersDAO = new UsersDAO();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final GiangVienDAO giangVienDAO = new GiangVienDAO();
    private final MonHocDAO monHocDAO = new MonHocDAO();
    private final MonTienQuyetDAO monTienQuyetDAO = new MonTienQuyetDAO();
    private final LopHocDAO lopHocDAO = new LopHocDAO();
    private final CauHinhHocKyDAO cauHinhHocKyDAO = new CauHinhHocKyDAO();
    private final ChuongTrinhDaoTaoDAO chuongTrinhDaoTaoDAO = new ChuongTrinhDaoTaoDAO();
    private final LichHocDAO lichHocDAO = new LichHocDAO();

    public CauHinhHocKy layCauHinhHocKy(Admin admin, String hocKy) {
        kiemTraAdminHienTai(admin);
        if (hocKy == null || hocKy.isBlank()) return null;
        try (Connection conn = DBConnection.getConnection()) {
            return cauHinhHocKyDAO.layTheoHocKy(conn, hocKy);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải cấu hình học kỳ.", e);
        }
    }

    public boolean luuCauHinhHocKy(Admin admin, String hocKy,
                                   LocalDate ketThucHocKy, LocalDateTime hanNhapDiem) {
        kiemTraAdmin(admin);
        if (hocKy == null || hocKy.isBlank() || hocKy.length() > 20
                || ketThucHocKy == null || hanNhapDiem == null
                || !hanNhapDiem.isAfter(ketThucHocKy.atStartOfDay())) {
            throw new ServiceException("Học kỳ và hạn nhập điểm không hợp lệ.");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraAdminTrongDatabase(conn, admin);
                boolean saved = cauHinhHocKyDAO.luu(conn,
                        new CauHinhHocKy(hocKy.trim(), ketThucHocKy, hanNhapDiem));
                if (!saved) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể lưu cấu hình học kỳ.", e);
        }
    }

    public List<MonHoc> layMonTheoKhoa(Admin admin, String khoa) {
        kiemTraAdminHienTai(admin);
        if (khoa == null || khoa.isBlank()) return List.of();
        try (Connection conn = DBConnection.getConnection()) {
            return chuongTrinhDaoTaoDAO.layMonTheoKhoa(conn, khoa.trim());
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải chương trình đào tạo.", e);
        }
    }

    public int themTatCaMonVaoChuongTrinh(Admin admin, String khoa) {
        kiemTraAdmin(admin);
        if (khoa == null || khoa.isBlank() || khoa.trim().length() > 100) {
            throw new ServiceException("Khoa/ngành là bắt buộc.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            kiemTraAdminTrongDatabase(conn, admin);
            String normalizedMajor = khoa.trim();
            if (chuongTrinhDaoTaoDAO.dangTrongDot(conn, normalizedMajor)) {
                throw new ServiceException("Không thể sửa chương trình khi đợt đăng ký đang mở.");
            }
            return chuongTrinhDaoTaoDAO.themTatCaMonHoatDong(conn, normalizedMajor);
        } catch (SQLException e) {
            throw new ServiceException("Không thể thêm toàn bộ môn vào chương trình đào tạo.", e);
        }
    }

    public boolean xoaMonKhoiChuongTrinh(Admin admin, String khoa, String maMon) {
        kiemTraAdmin(admin);
        if (khoa == null || khoa.isBlank() || maMon == null || maMon.isBlank()) return false;
        try (Connection conn = DBConnection.getConnection()) {
            kiemTraAdminTrongDatabase(conn, admin);
            String normalizedMajor = khoa.trim();
            if (chuongTrinhDaoTaoDAO.dangTrongDot(conn, normalizedMajor)) {
                throw new ServiceException("Không thể sửa chương trình khi đợt đăng ký đang mở.");
            }
            return chuongTrinhDaoTaoDAO.xoaMon(conn, normalizedMajor, maMon);
        } catch (SQLException e) {
            throw new ServiceException("Không thể gỡ môn khỏi chương trình đào tạo.", e);
        }
    }

    public DotDangKyNganh layDotDangKyNganh(Admin admin, String khoa) {
        kiemTraAdminHienTai(admin);
        if (khoa == null || khoa.isBlank()) return null;
        try (Connection conn = DBConnection.getConnection()) {
            return chuongTrinhDaoTaoDAO.layDotDangKy(conn, khoa.trim());
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải lịch đăng ký của khoa/ngành.", e);
        }
    }

    public boolean luuDotDangKyNganh(Admin admin, String khoa,
                                     LocalDateTime moDangKy, LocalDateTime dongDangKy) {
        kiemTraAdmin(admin);
        if (khoa == null || khoa.isBlank() || khoa.length() > 100
                || moDangKy == null || dongDangKy == null || !dongDangKy.isAfter(moDangKy)) {
            throw new ServiceException("Khoa/ngành và khoảng thời gian đăng ký không hợp lệ.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraAdminTrongDatabase(conn, admin);
                boolean saved = chuongTrinhDaoTaoDAO.luuDotDangKy(conn,
                        new DotDangKyNganh(khoa.trim(), moDangKy, dongDangKy));
                if (!saved) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể lưu lịch đăng ký của khoa/ngành.", e);
        }
    }

    public List<LichHoc> layLichHocTheoLop(Admin admin, String maLop) {
        kiemTraAdminHienTai(admin);
        try {
            return lichHocDAO.layTheoLop(maLop);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải lịch học của lớp.", e);
        }
    }

    public boolean luuLichHoc(Admin admin, LichHoc lichHoc) {
        kiemTraAdmin(admin);
        if (lichHoc == null || lichHoc.getMaLop() == null || lichHoc.getMaLop().isBlank()
                || lichHoc.getThu() < 2 || lichHoc.getThu() > 8
                || lichHoc.getGioBatDau() == null || lichHoc.getGioKetThuc() == null
                || !lichHoc.getGioBatDau().isBefore(lichHoc.getGioKetThuc())
                || lichHoc.getPhongHoc() == null || lichHoc.getPhongHoc().isBlank()
                || lichHoc.getPhongHoc().length() > 50) {
            throw new ServiceException("Lịch cần có thứ 2–8, giờ hợp lệ và phòng học tối đa 50 ký tự.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraAdminTrongDatabase(conn, admin);
                if (!lichHocDAO.lopHoatDong(conn, lichHoc.getMaLop())) {
                    throw new ServiceException("Chỉ có thể xếp lịch cho lớp đang hoạt động.");
                }
                if (lichHoc.getMaLich() > 0) {
                    LichHoc lichHienTai = lichHocDAO.layTheoMa(conn, lichHoc.getMaLich());
                    if (lichHienTai == null) {
                        conn.rollback();
                        return false;
                    }
                    boolean currentClassHasStudents =
                            lichHocDAO.coSinhVienDangKy(conn, lichHienTai.getMaLop());
                    boolean targetClassHasStudents = !lichHienTai.getMaLop().equals(lichHoc.getMaLop())
                            && lichHocDAO.coSinhVienDangKy(conn, lichHoc.getMaLop());
                    if ((currentClassHasStudents || targetClassHasStudents)
                            && (!lichHienTai.getMaLop().equals(lichHoc.getMaLop())
                            || lichHienTai.getThu() != lichHoc.getThu()
                            || !lichHienTai.getGioBatDau().equals(lichHoc.getGioBatDau())
                            || !lichHienTai.getGioKetThuc().equals(lichHoc.getGioKetThuc()))) {
                        throw new ServiceException("Lớp đã có sinh viên, chỉ được đổi phòng.");
                    }
                } else if (lichHocDAO.coSinhVienDangKy(conn, lichHoc.getMaLop())) {
                    throw new ServiceException("Lớp đã có sinh viên đăng ký, không thể thêm lịch.");
                }
                if (lichHocDAO.coXungDot(conn, lichHoc)) {
                    throw new ServiceException("Lịch bị trùng với lớp khác của giảng viên, phòng học hoặc chính lớp này.");
                }
                boolean saved = lichHoc.getMaLich() > 0
                        ? lichHocDAO.capNhat(conn, lichHoc)
                        : lichHocDAO.them(conn, lichHoc);
                if (!saved) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể lưu lịch học.", e);
        }
    }

    public boolean xoaLichHoc(Admin admin, int maLich) {
        kiemTraAdmin(admin);
        if (maLich <= 0) return false;
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraAdminTrongDatabase(conn, admin);
                LichHoc lichHoc = lichHocDAO.layTheoMa(conn, maLich);
                if (lichHoc == null) {
                    conn.rollback();
                    return false;
                }
                if (lichHocDAO.coSinhVienDangKy(conn, lichHoc.getMaLop())) {
                    throw new ServiceException("Lớp đã có sinh viên đăng ký, không thể xóa lịch");
                }
                boolean deleted = lichHocDAO.xoa(conn, maLich);
                if (!deleted) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể xóa lịch học.", e);
        }
    }

    public List<Users> layDanhSachTaiKhoan(Admin admin) {
        kiemTraAdminHienTai(admin);
        try {
            return usersDAO.layTatCa().stream()
                    .filter(user -> user instanceof SinhVien || user instanceof GiangVien)
                    .toList();
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách tài khoản.", e);
        }
    }

    public List<MonHoc> layDanhSachMonHoc(Admin admin) {
        kiemTraAdminHienTai(admin);
        try {
            return monHocDAO.layTatCaMonHoc();
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách môn học.", e);
        }
    }

    public boolean themMonHoc(Admin admin, MonHoc monHoc) {
        kiemTraAdminHienTai(admin);
        try {
            return monHocDAO.themMonHoc(monHoc);
        } catch (SQLException e) {
            throw new ServiceException("Không thể thêm môn học. Hãy kiểm tra mã môn có bị trùng không.", e);
        }
    }

    public boolean suaMonHoc(Admin admin, MonHoc monHoc) {
        kiemTraAdminHienTai(admin);
        try {
            return monHocDAO.suaMonHoc(monHoc);
        } catch (SQLException e) {
            throw new ServiceException("Không thể cập nhật môn học.", e);
        }
    }

    public boolean ngungMonHoc(Admin admin, String maMon) {
        kiemTraAdminHienTai(admin);
        try {
            return monHocDAO.xoaMonHoc(maMon);
        } catch (SQLException e) {
            throw new ServiceException("Không thể ngừng hoạt động môn học.", e);
        }
    }

    public List<MonTienQuyet> layMonTienQuyet(Admin admin, String maMon) {
        kiemTraAdminHienTai(admin);
        try {
            return MonTienQuyetDAO.layDanhSachMonTienQuyet(maMon);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải môn tiên quyết.", e);
        }
    }

    public boolean themMonTienQuyet(Admin admin, String maMon, String maMonTienQuyet) {
        kiemTraAdminHienTai(admin);
        try {
            return monTienQuyetDAO.themMonTienQuyet(maMon, maMonTienQuyet);
        } catch (IllegalArgumentException e) {
            throw new ServiceException(e.getMessage(), e);
        } catch (SQLException e) {
            throw new ServiceException("Không thể thêm môn tiên quyết. Quan hệ có thể đã tồn tại.", e);
        }
    }

    public boolean xoaMonTienQuyet(Admin admin, String maMon, String maMonTienQuyet) {
        kiemTraAdminHienTai(admin);
        try {
            return monTienQuyetDAO.xoaMonTienQuyet(maMon, maMonTienQuyet);
        } catch (SQLException e) {
            throw new ServiceException("Không thể gỡ môn tiên quyết.", e);
        }
    }

    public List<LopHoc> layDanhSachLopHoc(Admin admin) {
        kiemTraAdminHienTai(admin);
        try {
            return lopHocDAO.layTatCaLopHoc();
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách lớp học phần.", e);
        }
    }

    public boolean themLopHoc(Admin admin, LopHoc lopHoc) {
        kiemTraAdminHienTai(admin);
        try {
            return lopHocDAO.themLopHoc(lopHoc);
        } catch (SQLException e) {
            throw new ServiceException("Không thể thêm lớp. Hãy kiểm tra mã lớp và liên kết môn/giảng viên.", e);
        }
    }

    public boolean suaLopHoc(Admin admin, LopHoc lopHoc) {
        kiemTraAdminHienTai(admin);
        try {
            return lopHocDAO.suaLopHoc(lopHoc);
        } catch (SQLException e) {
            throw new ServiceException("Không thể cập nhật lớp học phần.", e);
        }
    }

    public boolean ngungLopHoc(Admin admin, String maLop) {
        kiemTraAdminHienTai(admin);
        try {
            return lopHocDAO.xoaLopHoc(maLop);
        } catch (SQLException e) {
            throw new ServiceException("Không thể ngừng hoạt động lớp học phần.", e);
        }
    }

    public boolean taoTaiKhoan(Admin admin, Users taiKhoan) {
        kiemTraAdmin(admin);
        kiemTraHoSo(taiKhoan);
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraAdminTrongDatabase(conn, admin);
                int userID = usersDAO.them(conn, taiKhoan);
                taiKhoan.setUserID(userID);
                boolean profileCreated = taiKhoan instanceof SinhVien sinhVien
                        ? sinhVienDAO.them(conn, sinhVien)
                        : giangVienDAO.them(conn, (GiangVien) taiKhoan);
                if (!profileCreated) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể tạo tài khoản.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tạo tài khoản.", e);
        }
    }

    public boolean capNhatTaiKhoan(Admin admin, Users taiKhoan) {
        kiemTraAdmin(admin);
        kiemTraHoSo(taiKhoan);
        if (taiKhoan.getUserID() == null || taiKhoan.getUserID() <= 0) {
            throw new ServiceException("Tài khoản cần cập nhật không hợp lệ.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraAdminTrongDatabase(conn, admin);
                Users hienTai = usersDAO.layTheoID(conn, taiKhoan.getUserID());
                if (hienTai == null || hienTai instanceof Admin
                        || hienTai.getClass() != taiKhoan.getClass()
                        || !usersDAO.capNhat(conn, taiKhoan)) {
                    conn.rollback();
                    return false;
                }
                boolean profileUpdated = taiKhoan instanceof SinhVien sinhVien
                        ? sinhVienDAO.capNhat(conn, sinhVien)
                        : giangVienDAO.capNhat(conn, (GiangVien) taiKhoan);
                if (!profileUpdated) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể cập nhật tài khoản.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể cập nhật tài khoản.", e);
        }
    }

    public boolean ngungTaiKhoan(Admin admin, int userID) {
        kiemTraAdmin(admin);
        if (userID <= 0 || admin.getUserID().equals(userID)) {
            throw new ServiceException("Không thể ngừng tài khoản admin đang đăng nhập.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                kiemTraAdminTrongDatabase(conn, admin);
                Users target = usersDAO.layTheoID(conn, userID);
                if (target == null || target instanceof Admin
                        || target.getTrangThai() != TrangThaiHoatDong.HOAT_DONG
                        || !usersDAO.ngungHoatDong(conn, userID)) {
                    conn.rollback();
                    return false;
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                rollback(conn, e);
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể ngừng tài khoản.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể ngừng tài khoản.", e);
        }
    }

    private void kiemTraAdminTrongDatabase(Connection conn, Admin admin) throws SQLException {
        Users current = usersDAO.layTheoID(conn, admin.getUserID());
        if (!(current instanceof Admin)
                || current.getTrangThai() != TrangThaiHoatDong.HOAT_DONG) {
            throw new ServiceException("Tài khoản admin không còn hoạt động.");
        }
    }

    private void kiemTraAdmin(Admin admin) {
        if (admin == null || admin.getUserID() == null || admin.getUserID() <= 0
                || !"ADMIN".equalsIgnoreCase(admin.getRole())
                || admin.getTrangThai() != TrangThaiHoatDong.HOAT_DONG) {
            throw new ServiceException("Cần đăng nhập bằng tài khoản admin đang hoạt động.");
        }
    }

    private void kiemTraAdminHienTai(Admin admin) {
        kiemTraAdmin(admin);
        try {
            Users current = usersDAO.layTheoID(admin.getUserID());
            if (!(current instanceof Admin)
                    || current.getTrangThai() != TrangThaiHoatDong.HOAT_DONG) {
                throw new ServiceException("Tài khoản admin không còn hoạt động.");
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể xác thực tài khoản admin.", e);
        }
    }

    private void kiemTraHoSo(Users taiKhoan) {
        if (taiKhoan == null || taiKhoan.getHoTen() == null || taiKhoan.getHoTen().isBlank()
                || taiKhoan.getHoTen().strip().length() > 100
                || taiKhoan.getEmail() == null || taiKhoan.getEmail().isBlank()
                || taiKhoan.getEmail().strip().length() > 100
                || taiKhoan.getPasswordHash() == null || taiKhoan.getPasswordHash().isBlank()
                || taiKhoan.getPasswordHash().length() > 255
                || taiKhoan.getMaSo() == null || taiKhoan.getMaSo().isBlank()
                || taiKhoan.getMaSo().length() > 20
                || (taiKhoan.getKhoa() != null && taiKhoan.getKhoa().length() > 100)
                || taiKhoan.getTrangThai() == null) {
            throw new ServiceException("Thông tin tài khoản không hợp lệ.");
        }
        if (taiKhoan instanceof SinhVien sinhVien) {
            if (!"SINHVIEN".equalsIgnoreCase(taiKhoan.getRole())
                    || !taiKhoan.getMaSo().equals(sinhVien.getMaSinhVien())
                    || (sinhVien.getLop() != null && sinhVien.getLop().length() > 50)
                    || (sinhVien.getHeDaoTao() != null && sinhVien.getHeDaoTao().length() > 50)) {
                throw new ServiceException("Mã sinh viên hoặc vai trò không hợp lệ.");
            }
        } else if (taiKhoan instanceof GiangVien giangVien) {
            if (!"GIAOVIEN".equalsIgnoreCase(taiKhoan.getRole())
                    || !taiKhoan.getMaSo().equals(giangVien.getMaGiangVien())
                    || (giangVien.getChuyenNganh() != null && giangVien.getChuyenNganh().length() > 100)
                    || (giangVien.getHocVi() != null && giangVien.getHocVi().length() > 50)) {
                throw new ServiceException("Mã giảng viên hoặc vai trò không hợp lệ.");
            }
        } else {
            throw new ServiceException("Chỉ hỗ trợ tạo và cập nhật hồ sơ sinh viên, giảng viên.");
        }
    }

    private void rollback(Connection conn, Exception error) {
        try {
            conn.rollback();
        } catch (SQLException rollbackError) {
            error.addSuppressed(rollbackError);
        }
    }
}
