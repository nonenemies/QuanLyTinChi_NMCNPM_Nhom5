package DAO;

import Model.GiangVien;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Truy vấn hồ sơ chuyên môn bổ sung của tài khoản giảng viên. */
public class GiangVienDAO {
    private static final String SELECT = "SELECT u.UserID, u.HoTen, u.Email, u.PasswordHash, u.Role, "
            + "u.MaSo, u.Khoa, u.NgayTao, u.TrangThai, gv.MaGV, gv.ChuyenNganh, gv.HocVi "
            + "FROM GiaoVien gv JOIN Users u ON u.UserID = gv.UserID ";

    public GiangVien layTheoMaGV(String maGV) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return layTheoMaGV(conn, maGV);
        }
    }

    public GiangVien layTheoMaGV(Connection conn, String maGV) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE gv.MaGV = ?")) {
            ps.setString(1, maGV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docGiangVien(rs) : null;
            }
        }
    }

    public GiangVien layTheoUserID(int userID) throws SQLException {
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "WHERE gv.UserID = ?")) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docGiangVien(rs) : null;
            }
        }
    }

    public List<GiangVien> layTatCa() throws SQLException {
        List<GiangVien> lecturers = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(SELECT + "ORDER BY gv.MaGV");
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) lecturers.add(docGiangVien(rs));
        }
        return lecturers;
    }

    public boolean them(Connection conn, GiangVien giangVien) throws SQLException {
        Objects.requireNonNull(giangVien, "giangVien không được null");
        String sql = "INSERT INTO GiaoVien (UserID, MaGV, ChuyenNganh, HocVi) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, giangVien.getUserID());
            ps.setString(2, giangVien.getMaGiangVien());
            ps.setString(3, giangVien.getChuyenNganh());
            ps.setString(4, giangVien.getHocVi());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean capNhat(Connection conn, GiangVien giangVien) throws SQLException {
        Objects.requireNonNull(giangVien, "giangVien không được null");
        String sql = "UPDATE GiaoVien SET MaGV = ?, ChuyenNganh = ?, HocVi = ? WHERE UserID = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, giangVien.getMaGiangVien());
            ps.setString(2, giangVien.getChuyenNganh());
            ps.setString(3, giangVien.getHocVi());
            ps.setInt(4, giangVien.getUserID());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean xoa(Connection conn, int userID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM GiaoVien WHERE UserID = ?")) {
            ps.setInt(1, userID);
            return ps.executeUpdate() == 1;
        }
    }

    private GiangVien docGiangVien(ResultSet rs) throws SQLException {
        return new GiangVien(rs.getInt("UserID"), rs.getString("HoTen"), rs.getString("Email"),
                rs.getString("PasswordHash"), rs.getString("Role"), rs.getString("MaSo"),
                rs.getString("Khoa"), rs.getTimestamp("NgayTao"),
                Model.TrangThaiHoatDong.valueOf(rs.getString("TrangThai")),
                rs.getString("MaGV"), rs.getString("ChuyenNganh"), rs.getString("HocVi"));
    }
}
