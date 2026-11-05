package Services;

import DAO.GiangVienDAO;
import DAO.MonHocDAO;
import DAO.SinhVienDAO;
import DAO.UsersDAO;
import Model.Admin;
import Model.GiangVien;
import Model.MonHoc;
import Model.SinhVien;
import Model.TrangThaiHoatDong;
import Model.Users;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Nghiệp vụ quản trị tài khoản sinh viên, giảng viên và môn học. */
public class AdminService {
    private final UsersDAO usersDAO = new UsersDAO();
    private final SinhVienDAO sinhVienDAO = new SinhVienDAO();
    private final GiangVienDAO giangVienDAO = new GiangVienDAO();
    private final MonHocDAO monHocDAO = new MonHocDAO();

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
