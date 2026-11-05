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

/** Đọc điều kiện môn tiên quyết khi đăng ký học phần. */
public class MonTienQuyetDAO {
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
