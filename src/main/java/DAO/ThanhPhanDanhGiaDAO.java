package DAO;

import Model.ThanhPhanDanhGia;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/** Đọc cấu hình thành phần đánh giá được dùng trong bảng điểm. */
public class ThanhPhanDanhGiaDAO {
    public List<ThanhPhanDanhGia> layTheoMon(Connection conn, String maMon) throws SQLException {
        List<ThanhPhanDanhGia> ketQua = new ArrayList<>();
        String sql = "SELECT MaThanhPhan, MaMon, TenThanhPhan, TrongSo, ThuTu, TrangThai "
                + "FROM ThanhPhanDanhGia WHERE MaMon = ? ORDER BY ThuTu, MaThanhPhan";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new ThanhPhanDanhGia(rs.getInt("MaThanhPhan"), rs.getString("MaMon"),
                            rs.getString("TenThanhPhan"), rs.getFloat("TrongSo"),
                            rs.getInt("ThuTu"), rs.getBoolean("TrangThai")));
                }
            }
        }
        return ketQua;
    }
}
