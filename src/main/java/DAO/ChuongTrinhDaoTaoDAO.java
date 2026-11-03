package DAO;

import Model.DotDangKyNganh;
import Model.MonHoc;
import Model.TrangThaiHoatDong;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ChuongTrinhDaoTaoDAO {
    public List<MonHoc> layMonTheoKhoa(Connection conn, String khoa) throws SQLException {
        String sql = "SELECT mh.MaMon, mh.TenMon, mh.SoTinChi, mh.LoaiMon, mh.MoTa, mh.TrangThai "
                + "FROM ChuongTrinhDaoTao ct JOIN MonHoc mh ON mh.MaMon = ct.MaMon "
                + "WHERE ct.Khoa = ? ORDER BY mh.MaMon";
        List<MonHoc> courses = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khoa);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    courses.add(new MonHoc(rs.getString("MaMon"), rs.getString("TenMon"),
                            rs.getObject("SoTinChi", Integer.class), rs.getString("LoaiMon"),
                            rs.getString("MoTa"), TrangThaiHoatDong.valueOf(rs.getString("TrangThai"))));
                }
            }
        }
        return courses;
    }

    public boolean themMon(Connection conn, String khoa, String maMon) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO ChuongTrinhDaoTao (Khoa, MaMon) VALUES (?, ?)")) {
            ps.setString(1, khoa);
            ps.setString(2, maMon);
            return ps.executeUpdate() == 1;
        }
    }

    public int themTatCaMonHoatDong(Connection conn, String khoa) throws SQLException {
        String sql = "INSERT INTO ChuongTrinhDaoTao (Khoa, MaMon) "
                + "SELECT ?, mh.MaMon FROM MonHoc mh "
                + "WHERE mh.TrangThai = 'HOAT_DONG' "
                + "AND NOT EXISTS (SELECT 1 FROM ChuongTrinhDaoTao ct "
                + "WHERE ct.Khoa = ? AND ct.MaMon = mh.MaMon)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khoa);
            ps.setString(2, khoa);
            return ps.executeUpdate();
        }
    }

    public boolean xoaMon(Connection conn, String khoa, String maMon) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "DELETE FROM ChuongTrinhDaoTao WHERE Khoa = ? AND MaMon = ?")) {
            ps.setString(1, khoa);
            ps.setString(2, maMon);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean dangTrongDot(Connection conn, String khoa) throws SQLException {
        String sql = "SELECT 1 FROM DotDangKyNganh "
                + "WHERE Khoa = ? AND CURRENT_TIMESTAMP BETWEEN MoDangKy AND DongDangKy";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khoa);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public DotDangKyNganh layDotDangKy(Connection conn, String khoa) throws SQLException {
        String sql = "SELECT Khoa, MoDangKy, DongDangKy FROM DotDangKyNganh WHERE Khoa = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, khoa);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new DotDangKyNganh(rs.getString("Khoa"),
                        rs.getTimestamp("MoDangKy").toLocalDateTime(),
                        rs.getTimestamp("DongDangKy").toLocalDateTime());
            }
        }
    }

    public boolean luuDotDangKy(Connection conn, DotDangKyNganh dot) throws SQLException {
        String sql = "INSERT INTO DotDangKyNganh (Khoa, MoDangKy, DongDangKy) VALUES (?, ?, ?) "
                + "ON DUPLICATE KEY UPDATE MoDangKy = VALUES(MoDangKy), "
                + "DongDangKy = VALUES(DongDangKy)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, dot.khoa());
            ps.setTimestamp(2, Timestamp.valueOf(dot.moDangKy()));
            ps.setTimestamp(3, Timestamp.valueOf(dot.dongDangKy()));
            ps.executeUpdate();
            return true;
        }
    }

    public boolean dangTrongDotDangKy(Connection conn, String maSV) throws SQLException {
        String sql = "SELECT 1 FROM SinhVien sv JOIN Users u ON u.UserID = sv.UserID "
                + "JOIN DotDangKyNganh dot ON dot.Khoa = u.Khoa "
                + "WHERE sv.MaSV = ? AND CURRENT_TIMESTAMP BETWEEN dot.MoDangKy AND dot.DongDangKy";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean monThuocChuongTrinh(Connection conn, String maSV, String maMon) throws SQLException {
        String sql = "SELECT 1 FROM SinhVien sv JOIN Users u ON u.UserID = sv.UserID "
                + "JOIN ChuongTrinhDaoTao ct ON ct.Khoa = u.Khoa "
                + "JOIN MonHoc mh ON mh.MaMon = ct.MaMon AND mh.TrangThai = 'HOAT_DONG' "
                + "WHERE sv.MaSV = ? AND ct.MaMon = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
