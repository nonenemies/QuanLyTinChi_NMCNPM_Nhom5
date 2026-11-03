package DAO;

import Model.Admin;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Truy vấn hồ sơ bổ sung của tài khoản Admin. */
public class AdminDAO {
    public Admin layTheoUserID(int userID) throws SQLException {
        String sql = "SELECT u.UserID, u.HoTen, u.Email, u.PasswordHash, u.Role, u.MaSo, u.Khoa, "
                + "u.NgayTao, u.TrangThai, ad.ChucVu FROM Admin ad "
                + "JOIN Users u ON u.UserID = ad.UserID WHERE ad.UserID = ?";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docAdmin(rs) : null;
            }
        }
    }

    public List<Admin> layTatCa() throws SQLException {
        String sql = "SELECT u.UserID, u.HoTen, u.Email, u.PasswordHash, u.Role, u.MaSo, u.Khoa, "
                + "u.NgayTao, u.TrangThai, ad.ChucVu FROM Admin ad "
                + "JOIN Users u ON u.UserID = ad.UserID ORDER BY u.UserID";
        List<Admin> admins = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) admins.add(docAdmin(rs));
        }
        return admins;
    }

    public boolean them(Connection conn, Admin admin) throws SQLException {
        Objects.requireNonNull(admin, "admin không được null");
        try (PreparedStatement ps = conn.prepareStatement(
                "INSERT INTO Admin (UserID, ChucVu) VALUES (?, ?)")) {
            ps.setInt(1, admin.getUserID());
            ps.setString(2, admin.getChucVu());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean capNhat(Connection conn, Admin admin) throws SQLException {
        Objects.requireNonNull(admin, "admin không được null");
        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE Admin SET ChucVu = ? WHERE UserID = ?")) {
            ps.setString(1, admin.getChucVu());
            ps.setInt(2, admin.getUserID());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean xoa(Connection conn, int userID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM Admin WHERE UserID = ?")) {
            ps.setInt(1, userID);
            return ps.executeUpdate() == 1;
        }
    }

    private Admin docAdmin(ResultSet rs) throws SQLException {
        return new Admin(rs.getInt("UserID"), rs.getString("HoTen"), rs.getString("Email"),
                rs.getString("PasswordHash"), rs.getString("Role"), rs.getString("MaSo"),
                rs.getString("Khoa"), rs.getTimestamp("NgayTao"),
                Model.TrangThaiHoatDong.valueOf(rs.getString("TrangThai")), rs.getString("ChucVu"));
    }
}
