package DAO;

import Model.Admin;
import Model.GiangVien;
import Model.SinhVien;
import Model.TrangThaiHoatDong;
import Model.Users;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Truy vấn tài khoản và thông tin vai trò được lưu trực tiếp trong bảng Users. */
public class UsersDAO {
    private static final String USER_SELECT = "SELECT u.UserID, u.HoTen, u.Email, u.PasswordHash, "
            + "u.Role, u.MaSo, u.Khoa, u.NgayTao, u.TrangThai, "
            + "sv.MaSV, sv.NgaySinh, sv.GioiTinh, sv.Lop, sv.KhoaHoc, sv.HeDaoTao, "
            + "gv.MaGV, gv.ChuyenNganh, gv.HocVi, ad.ChucVu "
            + "FROM Users u LEFT JOIN SinhVien sv ON sv.UserID = u.UserID "
            + "LEFT JOIN GiaoVien gv ON gv.UserID = u.UserID "
            + "LEFT JOIN Admin ad ON ad.UserID = u.UserID ";

    public Users layTheoID(int userID) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return layTheoID(conn, userID);
        }
    }

    public Users layTheoID(Connection conn, int userID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(USER_SELECT + "WHERE u.UserID = ?")) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docNguoiDung(rs) : null;
            }
        }
    }

    public Users layTheoEmailDangHoatDong(String email) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            try (PreparedStatement ps = conn.prepareStatement(
                    USER_SELECT + "WHERE u.Email = ? AND u.TrangThai = 'HOAT_DONG'")) {
                ps.setString(1, email);
                try (ResultSet rs = ps.executeQuery()) {
                    return rs.next() ? docNguoiDung(rs) : null;
                }
            }
        }
    }

    public List<Users> layTatCa() throws SQLException {
        List<Users> users = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(USER_SELECT + "ORDER BY u.UserID");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) users.add(docNguoiDung(rs));
        }
        return users;
    }

    /** Chỉ tạo hàng tài khoản; tạo hàng trong bảng vai trò cùng transaction của Service. */
    public int them(Connection conn, Users user) throws SQLException {
        Objects.requireNonNull(user, "user không được null");
        String sql = "INSERT INTO Users (HoTen, Email, PasswordHash, Role, MaSo, Khoa, TrangThai) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, user.getHoTen());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getRole());
            ps.setString(5, user.getMaSo());
            ps.setString(6, user.getKhoa());
            ps.setString(7, user.getTrangThai().name());
            if (ps.executeUpdate() != 1) throw new SQLException("Không tạo được tài khoản.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không lấy được UserID vừa tạo.");
                return keys.getInt(1);
            }
        }
    }

    /** Cập nhật thông tin chung; Role và MaSo được quản lý đồng bộ với bảng hồ sơ vai trò. */
    public boolean capNhat(Connection conn, Users user) throws SQLException {
        Objects.requireNonNull(user, "user không được null");
        String sql = "UPDATE Users SET HoTen = ?, Email = ?, PasswordHash = ?, "
                + "Khoa = ?, TrangThai = ? WHERE UserID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, user.getHoTen());
            ps.setString(2, user.getEmail());
            ps.setString(3, user.getPasswordHash());
            ps.setString(4, user.getKhoa());
            ps.setString(5, user.getTrangThai().name());
            ps.setInt(6, user.getUserID());
            return ps.executeUpdate() == 1;
        }
    }

    /** Ngừng tài khoản thay vì xóa vật lý vì tài khoản có thể được tham chiếu bởi dữ liệu học vụ. */
    public boolean ngungHoatDong(Connection conn, int userID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Users SET TrangThai = 'NGUNG_HOAT_DONG' WHERE UserID = ? AND TrangThai = 'HOAT_DONG'")) {
            ps.setInt(1, userID);
            return ps.executeUpdate() == 1;
        }
    }

    private Users docNguoiDung(ResultSet rs) throws SQLException {
        int userID = rs.getInt("UserID");
        String hoTen = rs.getString("HoTen");
        String email = rs.getString("Email");
        String password = rs.getString("PasswordHash");
        String role = rs.getString("Role");
        String maSo = rs.getString("MaSo");
        String khoa = rs.getString("Khoa");
        java.util.Date ngayTao = rs.getTimestamp("NgayTao");
        TrangThaiHoatDong trangThai = TrangThaiHoatDong.valueOf(rs.getString("TrangThai"));
        boolean gioiTinhValue = rs.getBoolean("GioiTinh");
        Boolean gioiTinh = rs.wasNull() ? null : gioiTinhValue;
        return switch (role.toUpperCase(java.util.Locale.ROOT)) {
            case "SINHVIEN" -> new SinhVien(userID, hoTen, email, password, role, maSo, khoa, ngayTao,
                    trangThai, rs.getString("MaSV"), rs.getDate("NgaySinh"), gioiTinh, rs.getString("Lop"),
                    rs.getObject("KhoaHoc", Integer.class), rs.getString("HeDaoTao"));
            case "GIAOVIEN" -> new GiangVien(userID, hoTen, email, password, role, maSo, khoa, ngayTao,
                    trangThai, rs.getString("MaGV"), rs.getString("ChuyenNganh"), rs.getString("HocVi"));
            case "ADMIN" -> new Admin(userID, hoTen, email, password, role, maSo, khoa, ngayTao,
                    trangThai, rs.getString("ChucVu"));
            default -> throw new SQLException("Vai trò tài khoản không được hỗ trợ: " + role);
        };
    }
}
