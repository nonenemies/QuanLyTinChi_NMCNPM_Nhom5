package DAO;

import Model.CauHinhHocKy;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;

public class CauHinhHocKyDAO {
    public CauHinhHocKy layTheoHocKy(Connection conn, String hocKy) throws SQLException {
        String sql = "SELECT HocKy, KetThucHocKy, HanNhapDiem "
                + "FROM CauHinhHocKy WHERE HocKy = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new CauHinhHocKy(rs.getString("HocKy"),
                        rs.getDate("KetThucHocKy").toLocalDate(),
                        rs.getTimestamp("HanNhapDiem").toLocalDateTime());
            }
        }
    }

    public boolean luu(Connection conn, CauHinhHocKy cauHinh) throws SQLException {
        String sql = "INSERT INTO CauHinhHocKy "
                + "(HocKy, KetThucHocKy, HanNhapDiem) "
                + "VALUES (?, ?, ?) ON DUPLICATE KEY UPDATE "
                + "KetThucHocKy = VALUES(KetThucHocKy), HanNhapDiem = VALUES(HanNhapDiem)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, cauHinh.hocKy());
            ps.setDate(2, java.sql.Date.valueOf(cauHinh.ketThucHocKy()));
            ps.setTimestamp(3, Timestamp.valueOf(cauHinh.hanNhapDiem()));
            ps.executeUpdate();
            return true;
        }
    }

    public boolean daKetThucHocKy(Connection conn, String hocKy) throws SQLException {
        String sql = "SELECT 1 FROM CauHinhHocKy "
                + "WHERE HocKy = ? AND CURRENT_DATE >= KetThucHocKy";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean dangTrongDotNhapDiem(Connection conn, String hocKy) throws SQLException {
        String sql = "SELECT 1 FROM CauHinhHocKy "
                + "WHERE HocKy = ? AND CURRENT_TIMESTAMP >= TIMESTAMP(KetThucHocKy) "
                + "AND CURRENT_TIMESTAMP <= HanNhapDiem";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
