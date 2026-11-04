package DAO;

import Model.MonHoc;
import Model.MonTienQuyet;
import Model.TrangThaiHoatDong;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Các truy vấn cho quan hệ môn học - môn tiên quyết. */
public class MonTienQuyetDAO {

    /** Ghi nhận môn tiên quyết của một môn học. */
    public boolean themMonTienQuyet(String maMon, String maMonTienQuyet) throws SQLException {
        if (maMon == null || maMon.isBlank() || maMonTienQuyet == null || maMonTienQuyet.isBlank()) {
            throw new IllegalArgumentException("Mã môn không được null");
        }
        if (maMon.equals(maMonTienQuyet)) {
            throw new IllegalArgumentException("Một môn không thể là tiên quyết của chính nó");
        }

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                try (PreparedStatement ps = conn.prepareStatement(
                        "SELECT MaMon FROM MonHoc WHERE MaMon IN (?, ?) "
                                + "AND TrangThai = 'HOAT_DONG' ORDER BY MaMon FOR UPDATE")) {
                    ps.setString(1, maMon);
                    ps.setString(2, maMonTienQuyet);
                    try (ResultSet rs = ps.executeQuery()) {
                        int activeCourses = 0;
                        while (rs.next()) activeCourses++;
                        if (activeCourses != 2) {
                            conn.rollback();
                            throw new IllegalArgumentException("Hai môn phải tồn tại và đang hoạt động.");
                        }
                    }
                }

                String cycleSql = "WITH RECURSIVE prerequisites (MaMon) AS ("
                        + "SELECT MaMonTienQuyet FROM MonTienQuyet WHERE MaMon = ? "
                        + "UNION DISTINCT "
                        + "SELECT mtq.MaMonTienQuyet FROM MonTienQuyet mtq "
                        + "JOIN prerequisites p ON mtq.MaMon = p.MaMon"
                        + ") SELECT 1 FROM prerequisites WHERE MaMon = ? LIMIT 1";
                try (PreparedStatement ps = conn.prepareStatement(cycleSql)) {
                    ps.setString(1, maMonTienQuyet);
                    ps.setString(2, maMon);
                    try (ResultSet rs = ps.executeQuery()) {
                        if (rs.next()) {
                            conn.rollback();
                            throw new IllegalArgumentException("Quan hệ này tạo thành chu trình môn tiên quyết.");
                        }
                    }
                }

                try (PreparedStatement ps = conn.prepareStatement(
                        "INSERT INTO MonTienQuyet (MaMon, MaMonTienQuyet) VALUES (?, ?)")) {
                    ps.setString(1, maMon);
                    ps.setString(2, maMonTienQuyet);
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

    /** Xóa một quan hệ tiên quyết cụ thể. */
    public boolean xoaMonTienQuyet(String maMon, String maMonTienQuyet) throws SQLException {
        String sql = "DELETE FROM MonTienQuyet WHERE MaMon = ? AND MaMonTienQuyet = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            ps.setString(2, maMonTienQuyet);
            return ps.executeUpdate() == 1;
        }
    }

    /** Lấy danh sách môn phải học trước một môn. */
    public static List<MonTienQuyet> layDanhSachMonTienQuyet(String maMon) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return layDanhSachMonTienQuyet(conn, maMon);
        }
    }

    /** Dùng cùng kết nối với transaction đăng ký hoặc đổi lớp. */
    public static List<MonTienQuyet> layDanhSachMonTienQuyet(Connection conn, String maMon)
            throws SQLException {
        String sql = "SELECT mtq.MaMon, mh.MaMon AS MaMonTienQuyet, mh.TenMon, "
                + "mh.SoTinChi, mh.LoaiMon, mh.MoTa, mh.TrangThai "
                + "FROM MonTienQuyet mtq JOIN MonHoc mh ON mh.MaMon = mtq.MaMonTienQuyet "
                + "WHERE mtq.MaMon = ? ORDER BY mh.MaMon";
        List<MonTienQuyet> danhSach = new ArrayList<>();

        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    MonHoc monTienQuyet = new MonHoc(
                            rs.getString("MaMonTienQuyet"),
                            rs.getString("TenMon"),
                            rs.getObject("SoTinChi", Integer.class),
                            rs.getString("LoaiMon"),
                            rs.getString("MoTa"),
                            TrangThaiHoatDong.valueOf(rs.getString("TrangThai")));
                    danhSach.add(new MonTienQuyet(rs.getString("MaMon"), monTienQuyet));
                }
            }
        }
        return danhSach;
    }

}
