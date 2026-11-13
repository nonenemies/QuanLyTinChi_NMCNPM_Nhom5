package DAO;

import Model.DotDangKyNganh;
import Model.Nganh;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class DotDangKyNganhDAO {
    public DotDangKyNganh layDotDangKyHienTai(Connection conn, String maSV) throws SQLException {
        String sql = "SELECT n.MaNganh, n.TenNganh, dot.MoDangKy, dot.DongDangKy "
                + "FROM SinhVien sv JOIN Users u ON u.UserID = sv.UserID "
                + "JOIN Nganh n ON n.MaNganh = sv.MaNganh "
                + "JOIN DotDangKyNganh dot ON dot.MaNganh = n.MaNganh "
                + "WHERE sv.MaSinhVien = ? AND CURRENT_TIMESTAMP BETWEEN dot.MoDangKy AND dot.DongDangKy";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return null;
                return new DotDangKyNganh(
                        new Nganh(rs.getString("MaNganh"), rs.getString("TenNganh")),
                        rs.getTimestamp("MoDangKy").toLocalDateTime(),
                        rs.getTimestamp("DongDangKy").toLocalDateTime());
            }
        }
    }
}
