package DAO;

import Model.LopHocPhan;
import Model.LopHocDangKy;
import Model.TrangThaiHoatDong;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Các truy vấn dữ liệu và thao tác sĩ số cho bảng LopHoc. */
public class LopHocDAO {

    /** Lấy các lớp được phân công cho một giảng viên, gồm cả lớp đã kết thúc. */
    public List<LopHocPhan> layDanhSachLopCuaGiangVien(String maGV) throws SQLException {
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
                + "JOIN DotDangKyNganh dot ON dot.Nganh = u.Nganh "
                + "JOIN ChuongTrinhDaoTao ct ON ct.Nganh = u.Nganh AND ct.MaMon = lh.MaMon "
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

    /** Tìm lớp theo mã trên connection do bên gọi quản lý, để đọc trong cùng transaction. */
    public LopHocPhan layLopTheoMa(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT MaLop, MaMon, MaGV, HocKy, SiSoToiDa, SiSoHienTai, TrangThai "
                + "FROM LopHoc WHERE MaLop = ?";
        List<LopHocPhan> ketQua = truyVanDanhSach(conn, sql, maLop);
        return ketQua.isEmpty() ? null : ketQua.get(0);
    }

    /** Khóa lớp khi đăng ký để các lượt đăng ký cùng lớp được xử lý tuần tự. */
    public LopHocPhan layLopTheoMaForUpdate(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT MaLop, MaMon, MaGV, HocKy, SiSoToiDa, SiSoHienTai, TrangThai "
                + "FROM LopHoc WHERE MaLop = ? FOR UPDATE";
        List<LopHocPhan> ketQua = truyVanDanhSach(conn, sql, maLop);
        return ketQua.isEmpty() ? null : ketQua.get(0);
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

    /** Giảm sĩ số trên kết nối do bên gọi quản lý, để ghép vào transaction hiện có. */
    public boolean giamSiSo(Connection conn, String maLop) throws SQLException {
        String sql = "UPDATE LopHoc SET SiSoHienTai = SiSoHienTai - 1 "
                + "WHERE MaLop = ? AND SiSoHienTai > 0";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            return ps.executeUpdate() == 1;
        }
    }

    private List<LopHocPhan> truyVanDanhSach(String sql, String thamSoMaMonHoacLop) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return truyVanDanhSach(conn, sql, thamSoMaMonHoacLop);
        }
    }

    private List<LopHocPhan> truyVanDanhSach(Connection conn, String sql, String thamSoMaMonHoacLop)
            throws SQLException {
        List<LopHocPhan> danhSach = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            if (thamSoMaMonHoacLop != null) {
                ps.setString(1, thamSoMaMonHoacLop);
            }
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    danhSach.add(new LopHocPhan(
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
