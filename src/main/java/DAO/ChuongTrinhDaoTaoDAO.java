package DAO;

import Model.ChuongTrinhDaoTao;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ChuongTrinhDaoTaoDAO {
    public ChuongTrinhDaoTao layChuongTrinhMonHoc(
            Connection conn, String maSV, String maMon) throws SQLException {
        String sql = "SELECT ct.Nganh, ct.MaMon FROM SinhVien sv "
                + "JOIN Users u ON u.UserID = sv.UserID "
                + "JOIN ChuongTrinhDaoTao ct ON ct.Nganh = u.Nganh "
                + "JOIN MonHoc mh ON mh.MaMon = ct.MaMon AND mh.TrangThai = 'HOAT_DONG' "
                + "WHERE sv.MaSV = ? AND ct.MaMon = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next()
                        ? new ChuongTrinhDaoTao(rs.getString("Nganh"), rs.getString("MaMon"))
                        : null;
            }
        }
    }
}
