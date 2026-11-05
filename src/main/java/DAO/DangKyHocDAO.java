package DAO;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Model.DangKyHoc;
import Model.LopHocThoiKhoaBieu;
import Model.TrangThaiDangKy;

public class DangKyHocDAO {

    /** Khóa dòng sinh viên để các đăng ký đồng thời của cùng một sinh viên được tuần tự hóa. */
    public boolean khoaSinhVienChoDangKy(Connection conn, String maSV) throws SQLException {
        String sql = "SELECT sv.MaSV FROM SinhVien sv "
                + "JOIN Users u ON u.UserID = sv.UserID "
                + "WHERE sv.MaSV = ? AND u.Role = 'SINHVIEN' AND u.TrangThai = 'HOAT_DONG' FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean coLichHoc(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT 1 FROM LichHoc WHERE MaLop = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    /** Kiểm tra lớp đăng ký mới có trùng lịch với các lớp đang học trong cùng học kỳ không. */
    public boolean coLopBiTrungLich(Connection conn, String maSV, String maLopMoi) throws SQLException {
        String sql = "SELECT 1 "
                + "FROM DangKyHoc dk "
                + "JOIN LopHoc lh ON lh.MaLop = dk.MaLop "
                + "JOIN LichHoc lich ON lich.MaLop = lh.MaLop "
                + "JOIN LopHoc lopMoi ON lopMoi.MaLop = ? "
                + "JOIN LichHoc lichMoi ON lichMoi.MaLop = lopMoi.MaLop "
                + "WHERE dk.MaSV = ? AND dk.TrangThai <> 'HUY' "
                + "AND lh.HocKy = lopMoi.HocKy "
                + "AND lich.Thu = lichMoi.Thu "
                + "AND lich.GioBatDau < lichMoi.GioKetThuc "
                + "AND lich.GioKetThuc > lichMoi.GioBatDau "
                + "ORDER BY lich.Thu, lich.GioBatDau LIMIT 1";

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLopMoi);
            ps.setString(2, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    // Ném SQLException, dùng Connection truyền vào
    public boolean kiemTraDaDangKy(Connection conn, String maSV, String maLop) throws SQLException {
        String sql = "SELECT 1 FROM DangKyHoc "
                + "WHERE MaSV = ? AND MaLop = ? AND TrangThai <> 'HUY'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean daDangKyMonTrongHocKy(Connection conn, String maSV, String maMon, String hocKy)
            throws SQLException {
        String sql = "SELECT 1 FROM DangKyHoc dk "
                + "JOIN LopHoc lh ON lh.MaLop = dk.MaLop "
                + "WHERE dk.MaSV = ? AND lh.MaMon = ? AND lh.HocKy = ? "
                + "AND dk.TrangThai = 'DANG_HOC' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maMon);
            ps.setString(3, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public int tongTinChiDangHoc(Connection conn, String maSV, String hocKy) throws SQLException {
        String sql = "SELECT COALESCE(SUM(mh.SoTinChi), 0) AS TongTinChi "
                + "FROM DangKyHoc dk JOIN LopHoc lh ON lh.MaLop = dk.MaLop "
                + "JOIN MonHoc mh ON mh.MaMon = lh.MaMon "
                + "WHERE dk.MaSV = ? AND lh.HocKy = ? AND dk.TrangThai = 'DANG_HOC'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("TongTinChi") : 0;
            }
        }
    }

    public int laySoTinChi(Connection conn, String maMon) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT SoTinChi FROM MonHoc WHERE MaMon = ? AND TrangThai = 'HOAT_DONG'")) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getInt("SoTinChi") : 0;
            }
        }
    }

    public boolean daDatMon(Connection conn, String maSV, String maMon) throws SQLException {
        return kiemTraDaQuaMon(conn, maSV, maMon);
    }

    public boolean coDiem(Connection conn, String maSV, String maLop) throws SQLException {
        String sql = "SELECT 1 FROM DiemThanhPhan d "
                + "JOIN DangKyHoc dk ON dk.MaDangKy = d.MaDangKy "
                + "WHERE dk.MaSV = ? AND dk.MaLop = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public TrangThaiDangKy layTrangThaiDangKyForUpdate(Connection conn, String maSV, String maLop)
            throws SQLException {
        String sql = "SELECT TrangThai FROM DangKyHoc WHERE MaSV = ? AND MaLop = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? TrangThaiDangKy.valueOf(rs.getString("TrangThai")) : null;
            }
        }
    }

    public boolean huyDangKy(Connection conn, String maSV, String maLop) throws SQLException {
        String sql = "UPDATE DangKyHoc SET TrangThai = 'HUY' "
                + "WHERE MaSV = ? AND MaLop = ? AND TrangThai = 'DANG_HOC'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maLop);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean kiemTraDaQuaMon(Connection conn, String maSV, String maMonHoc) throws SQLException {
        String sql = "SELECT 1 FROM DangKyHoc d " +
                "JOIN LopHoc l ON d.MaLop = l.MaLop " +
                "WHERE d.MaSV = ? AND l.MaMon = ? " +
                "AND d.DiemTongKet >= 4.0 AND d.TrangThai = 'DA_HOAN_THANH'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maMonHoc);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean themDangKyMoi(Connection conn, DangKyHoc dk) throws SQLException {
        String reactivateSql = "UPDATE DangKyHoc SET TrangThai = 'DANG_HOC' "
                + "WHERE MaSV = ? AND MaLop = ? AND TrangThai = 'HUY' "
                + "AND NOT EXISTS (SELECT 1 FROM DiemThanhPhan d WHERE d.MaDangKy = DangKyHoc.MaDangKy)";
        try (PreparedStatement ps = conn.prepareStatement(reactivateSql)) {
            ps.setString(1, dk.getMaSV());
            ps.setString(2, dk.getMaLop());
            if (ps.executeUpdate() == 1) {
                return true;
            }
        }

        String sql = "INSERT INTO DangKyHoc (MaLop, MaSV) VALUES (?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dk.getMaLop());
            ps.setString(2, dk.getMaSV());
            return ps.executeUpdate() > 0;
        }
    }

    public List<LopHocThoiKhoaBieu> layLichHocTheoTuan(String maSV) throws SQLException {
        List<LopHocThoiKhoaBieu> lichHoc = new ArrayList<>();
        String sql = "SELECT lh.MaLop, mh.MaMon, mh.TenMon, lh.HocKy, lich.Thu, "
                + "TIME_FORMAT(lich.GioBatDau, '%H:%i') AS GioBatDau, "
                + "TIME_FORMAT(lich.GioKetThuc, '%H:%i') AS GioKetThuc, lich.PhongHoc "
                + "FROM DangKyHoc dk "
                + "JOIN LopHoc lh ON lh.MaLop = dk.MaLop "
                + "JOIN MonHoc mh ON mh.MaMon = lh.MaMon "
                + "JOIN LichHoc lich ON lich.MaLop = lh.MaLop "
                + "WHERE dk.MaSV = ? AND dk.TrangThai <> 'HUY' "
                + "ORDER BY lich.Thu, lich.GioBatDau, lich.GioKetThuc, mh.TenMon";
        try (Connection conn = Util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    lichHoc.add(new LopHocThoiKhoaBieu(
                            rs.getString("MaLop"),
                            rs.getString("MaMon"),
                            rs.getString("TenMon"),
                            rs.getString("HocKy"),
                            rs.getInt("Thu"),
                            rs.getString("GioBatDau"),
                            rs.getString("GioKetThuc"),
                            rs.getString("PhongHoc")));
                }
            }
        }
        return lichHoc;
    }
}