package DAO;

import Model.LopHoc;
import Model.LopHocDangKy;
import Model.TrangThaiHoatDong;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Các truy vấn dữ liệu và thao tác sĩ số cho bảng LopHoc. */
public class LopHocDAO {

    /** Tạo lớp mới; sĩ số hiện tại bắt đầu bằng 0 theo DEFAULT của database. */
    public boolean themLopHoc(LopHoc lopHoc) throws SQLException {
        Objects.requireNonNull(lopHoc, "lopHoc không được null");
        String sql = "INSERT INTO LopHoc (MaLop, MaMon, MaGV, HocKy, SiSoToiDa) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lopHoc.getMaLop());
            ps.setString(2, lopHoc.getMaMon());
            ps.setString(3, lopHoc.getMaGV());
            ps.setString(4, lopHoc.getHocKy());
            ps.setObject(5, lopHoc.getSiSoToiDa());
            return ps.executeUpdate() == 1;
        }
    }

    /** Sửa thông tin lớp; không ghi đè sĩ số hiện tại do dữ liệu có thể đã thay đổi. */
    public boolean suaLopHoc(LopHoc lopHoc) throws SQLException {
        Objects.requireNonNull(lopHoc, "lopHoc không được null");
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                String selectSql = "SELECT MaMon, MaGV, HocKy, SiSoHienTai "
                        + "FROM LopHoc WHERE MaLop = ? FOR UPDATE";
                String maMonHienTai;
                String maGVHienTai;
                String hocKyHienTai;
                int siSoHienTai;
                try (PreparedStatement ps = conn.prepareStatement(selectSql)) {
                    ps.setString(1, lopHoc.getMaLop());
                    try (ResultSet rs = ps.executeQuery()) {
                        if (!rs.next()) {
                            conn.rollback();
                            return false;
                        }
                        maMonHienTai = rs.getString("MaMon");
                        maGVHienTai = rs.getString("MaGV");
                        hocKyHienTai = rs.getString("HocKy");
                        siSoHienTai = rs.getInt("SiSoHienTai");
                    }
                }

                boolean academicFieldsChanged = !Objects.equals(maMonHienTai, lopHoc.getMaMon())
                        || !Objects.equals(maGVHienTai, lopHoc.getMaGV())
                        || !Objects.equals(hocKyHienTai, lopHoc.getHocKy());
                if (lopHoc.getSiSoToiDa() == null || lopHoc.getSiSoToiDa() < siSoHienTai) {
                    conn.rollback();
                    return false;
                }
                if (academicFieldsChanged) {
                    try (PreparedStatement ps = conn.prepareStatement(
                            "SELECT 1 FROM DangKyHoc WHERE MaLop = ? LIMIT 1")) {
                        ps.setString(1, lopHoc.getMaLop());
                        try (ResultSet rs = ps.executeQuery()) {
                            if (rs.next()) {
                                conn.rollback();
                                return false;
                            }
                        }
                    }
                }

                String updateSql = "UPDATE LopHoc SET MaMon = ?, MaGV = ?, HocKy = ?, SiSoToiDa = ? "
                        + "WHERE MaLop = ?";
                try (PreparedStatement ps = conn.prepareStatement(updateSql)) {
                    ps.setString(1, lopHoc.getMaMon());
                    ps.setString(2, lopHoc.getMaGV());
                    ps.setString(3, lopHoc.getHocKy());
                    ps.setInt(4, lopHoc.getSiSoToiDa());
                    ps.setString(5, lopHoc.getMaLop());
                    if (ps.executeUpdate() != 1) {
                        conn.rollback();
                        return false;
                    }
                }
                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        }
    }

    /** Ngừng kích hoạt lớp để giữ lại dữ liệu đăng ký và điểm đã phát sinh. */
    public boolean xoaLopHoc(String maLop) throws SQLException {
        String sql = "UPDATE LopHoc SET TrangThai = ? WHERE MaLop = ? AND TrangThai <> ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, TrangThaiHoatDong.NGUNG_HOAT_DONG.name());
            ps.setString(2, maLop);
            ps.setString(3, TrangThaiHoatDong.NGUNG_HOAT_DONG.name());
            return ps.executeUpdate() == 1;
        }
    }

    /** Lấy các lớp được phân công cho một giảng viên, gồm cả lớp đã kết thúc. */
    public List<LopHoc> layDanhSachLopCuaGiangVien(String maGV) throws SQLException {
        String sql = "SELECT MaLop, MaMon, MaGV, HocKy, SiSoToiDa, SiSoHienTai, TrangThai "
                + "FROM LopHoc WHERE MaGV = ? ORDER BY HocKy DESC, MaLop";
        return truyVanDanhSach(sql, maGV);
    }

    /** Danh sách lớp mở cho sinh viên cùng các lớp sinh viên đã đăng ký. */
    public List<LopHocDangKy> layDanhSachLopDangKy(String maSV) throws SQLException {
        return layDanhSachLopDangKy(maSV, false);
    }

    /** Danh sách đăng ký hiện tại của sinh viên, không phụ thuộc đợt đăng ký còn mở. */
    public List<LopHocDangKy> layDanhSachLopDaDangKy(String maSV) throws SQLException {
        return layDanhSachLopDangKy(maSV, true);
    }

    private List<LopHocDangKy> layDanhSachLopDangKy(String maSV, boolean chiLopDaDangKy)
            throws SQLException {
        String sql = "SELECT lh.MaLop, mh.MaMon, mh.TenMon, "
                + "COALESCE(u.HoTen, lh.MaGV, 'Chưa phân công') AS GiangVien, "
                + "lh.HocKy, COALESCE(lich.LichHoc, 'Chưa xếp lịch') AS LichHoc, "
                + "CASE WHEN lich.LichHoc IS NULL THEN 0 ELSE 1 END AS CoLichHoc, "
                + "mh.SoTinChi, lh.SiSoHienTai, lh.SiSoToiDa, "
                + "CASE WHEN dk.MaDangKy IS NULL THEN 0 ELSE 1 END AS DaDangKy, "
                + "dk.TrangThai AS TrangThaiDangKy "
                + "FROM LopHoc lh "
                + "JOIN MonHoc mh ON mh.MaMon = lh.MaMon "
                + "LEFT JOIN GiaoVien gv ON gv.MaGV = lh.MaGV "
                + "LEFT JOIN Users u ON u.UserID = gv.UserID "
                + "LEFT JOIN ("
                + "SELECT MaLop, GROUP_CONCAT(CONCAT('Thứ ', Thu, ' ', "
                + "TIME_FORMAT(GioBatDau, '%H:%i'), '-', TIME_FORMAT(GioKetThuc, '%H:%i'), "
                + "' · ', COALESCE(NULLIF(TRIM(PhongHoc), ''), 'Chưa cập nhật phòng')) "
                + "ORDER BY Thu, GioBatDau SEPARATOR ' | ') AS LichHoc "
                + "FROM LichHoc GROUP BY MaLop"
                + ") lich ON lich.MaLop = lh.MaLop "
                + "LEFT JOIN DangKyHoc dk ON dk.MaLop = lh.MaLop AND dk.MaSV = ? "
                + "AND dk.TrangThai <> 'HUY' "
                + (chiLopDaDangKy
                ? "WHERE dk.MaDangKy IS NOT NULL "
                : "WHERE (lh.TrangThai = 'HOAT_DONG' AND mh.TrangThai = 'HOAT_DONG' "
                + "AND EXISTS (SELECT 1 FROM SinhVien sv "
                + "JOIN Users u ON u.UserID = sv.UserID "
                + "JOIN DotDangKyNganh dot ON dot.Khoa = u.Khoa "
                + "JOIN ChuongTrinhDaoTao ct ON ct.Khoa = u.Khoa AND ct.MaMon = lh.MaMon "
                + "WHERE sv.MaSV = ? AND CURRENT_TIMESTAMP BETWEEN dot.MoDangKy AND dot.DongDangKy "
                + ")) OR dk.MaDangKy IS NOT NULL ")
                + "ORDER BY lh.HocKy DESC, mh.MaMon, lh.MaLop";

        List<LopHocDangKy> danhSach = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            if (!chiLopDaDangKy) {
                ps.setString(2, maSV);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new LopHocDangKy(
                            rs.getString("MaLop"), rs.getString("MaMon"),
                            rs.getString("TenMon"), rs.getString("GiangVien"),
                            rs.getString("HocKy"), rs.getString("LichHoc"),
                            rs.getBoolean("CoLichHoc"),
                            rs.getObject("SoTinChi", Integer.class),
                            rs.getObject("SiSoHienTai", Integer.class),
                            rs.getObject("SiSoToiDa", Integer.class),
                            rs.getBoolean("DaDangKy"),
                            rs.getString("TrangThaiDangKy") == null ? null
                                    : Model.TrangThaiDangKy.valueOf(rs.getString("TrangThaiDangKy")),
                            null, null));
                }
            }
        }
        return danhSach;
    }

    /** Lấy mọi lớp, gồm cả lớp đã ngừng hoạt động, cho màn hình quản trị. */
    public List<LopHoc> layTatCaLopHoc() throws SQLException {
        return truyVanDanhSach("SELECT MaLop, MaMon, MaGV, HocKy, SiSoToiDa, SiSoHienTai, TrangThai "
                + "FROM LopHoc ORDER BY MaLop", null);
    }

    /** Tìm lớp theo mã, gồm cả lớp đã ngừng hoạt động (tự mở connection). */
    public LopHoc layLopTheoMa(String maLop) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return layLopTheoMa(conn, maLop);
        }
    }

    /** Tìm lớp theo mã trên connection do bên gọi quản lý, để đọc trong cùng transaction. */
    public LopHoc layLopTheoMa(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT MaLop, MaMon, MaGV, HocKy, SiSoToiDa, SiSoHienTai, TrangThai "
                + "FROM LopHoc WHERE MaLop = ?";
        List<LopHoc> ketQua = truyVanDanhSach(conn, sql, maLop);
        return ketQua.isEmpty() ? null : ketQua.get(0);
    }

    /** Khóa lớp khi đăng ký để các lượt đăng ký cùng lớp được xử lý tuần tự. */
    public LopHoc layLopTheoMaForUpdate(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT MaLop, MaMon, MaGV, HocKy, SiSoToiDa, SiSoHienTai, TrangThai "
                + "FROM LopHoc WHERE MaLop = ? FOR UPDATE";
        List<LopHoc> ketQua = truyVanDanhSach(conn, sql, maLop);
        return ketQua.isEmpty() ? null : ketQua.get(0);
    }

    /** Tăng sĩ số nguyên tử nếu lớp còn hoạt động và chưa đầy. */
    public boolean tangSiSo(String maLop) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return tangSiSo(conn, maLop);
        }
    }

    /** Tăng sĩ số trên kết nối do bên gọi quản lý, để ghép vào transaction hiện có. */
    public boolean tangSiSo(Connection conn, String maLop) throws SQLException {
        String sql = "UPDATE LopHoc SET SiSoHienTai = SiSoHienTai + 1 "
                + "WHERE MaLop = ? AND TrangThai = '" + TrangThaiHoatDong.HOAT_DONG.name()
                + "' AND SiSoHienTai < SiSoToiDa";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            return ps.executeUpdate() == 1;
        }
    }

    /** Giảm sĩ số nguyên tử nếu sĩ số hiện tại lớn hơn 0. */
    public boolean giamSiSo(String maLop) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return giamSiSo(conn, maLop);
        }
    }

    /** Giảm sĩ số trên kết nối do bên gọi quản lý, để ghép vào transaction hiện có. */
    public boolean giamSiSo(Connection conn, String maLop) throws SQLException {
        String sql = "UPDATE LopHoc SET SiSoHienTai = SiSoHienTai - 1 "
                + "WHERE MaLop = ? AND SiSoHienTai > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            return ps.executeUpdate() == 1;
        }
    }

    private List<LopHoc> truyVanDanhSach(String sql, String thamSoMaMonHoacLop) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return truyVanDanhSach(conn, sql, thamSoMaMonHoacLop);
        }
    }

    private List<LopHoc> truyVanDanhSach(Connection conn, String sql, String thamSoMaMonHoacLop)
            throws SQLException {
        List<LopHoc> danhSach = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (thamSoMaMonHoacLop != null) {
                ps.setString(1, thamSoMaMonHoacLop);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new LopHoc(
                            rs.getString("MaLop"),
                            rs.getString("MaMon"),
                            rs.getString("MaGV"),
                            rs.getString("HocKy"),
                            rs.getObject("SiSoToiDa", Integer.class),
                            rs.getObject("SiSoHienTai", Integer.class),
                            TrangThaiHoatDong.valueOf(rs.getString("TrangThai"))
                    ));
                }
            }
        }
        return danhSach;
    }
}
