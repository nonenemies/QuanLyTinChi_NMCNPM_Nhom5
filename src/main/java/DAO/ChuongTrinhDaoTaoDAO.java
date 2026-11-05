package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChuongTrinhDaoTaoDAO {
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
