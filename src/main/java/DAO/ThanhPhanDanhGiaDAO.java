package DAO;

import Model.ThanhPhanDanhGia;
import Model.TrangThaiHoatDong;
import Model.ThongTinQuyen;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/** Chỉ truy vấn dữ liệu; connection và transaction thuộc về Service. */
public class ThanhPhanDanhGiaDAO {
    public ThongTinQuyen traQuyen(Connection conn, int userID) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT Role, TrangThai FROM Users WHERE UserID = ? FOR UPDATE")) {
            ps.setInt(1, userID);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? new ThongTinQuyen(
                        rs.getString("Role"),
                        TrangThaiHoatDong.valueOf(rs.getString("TrangThai"))) : null;
            }
        }
    }

    /** Trả về mã môn hoặc null, đồng thời khóa dòng trong transaction hiện tại. */
    public String timMonDeCapNhat(Connection conn, String maMon) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT MaMon FROM MonHoc WHERE MaMon = ? FOR UPDATE")) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString("MaMon") : null;
            }
        }
    }

    public List<ThanhPhanDanhGia> layTheoMon(Connection conn, String maMon) throws SQLException {
        return docDanhSach(conn, maMon, false);
    }

    public List<ThanhPhanDanhGia> layTheoMonDeCapNhat(Connection conn, String maMon) throws SQLException {
        return docDanhSach(conn, maMon, true);
    }

    public boolean daCoDiem(Connection conn, String maMon) throws SQLException {
        String sql = "SELECT 1 FROM DiemThanhPhan d "
                + "JOIN DangKyHoc dk ON dk.MaDangKy = d.MaDangKy "
                + "JOIN LopHoc lh ON lh.MaLop = dk.MaLop "
                + "WHERE lh.MaMon = ? LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private List<ThanhPhanDanhGia> docDanhSach(Connection conn, String maMon, boolean khoa) throws SQLException {
        List<ThanhPhanDanhGia> ketQua = new ArrayList<>();
        String sql = "SELECT MaTP, MaMon, TenThanhPhan, TrongSo, ThuTu, TrangThai "
                + "FROM ThanhPhanDanhGia WHERE MaMon = ? ORDER BY ThuTu, MaTP"
                + (khoa ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maMon);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    ketQua.add(new ThanhPhanDanhGia(rs.getInt("MaTP"), rs.getString("MaMon"),
                            rs.getString("TenThanhPhan"), rs.getFloat("TrongSo"),
                            rs.getInt("ThuTu"), rs.getBoolean("TrangThai")));
                }
            }
        }
        return ketQua;
    }

    public int themThanhPhan(Connection conn, ThanhPhanDanhGia tp) throws SQLException {
        String sql = "INSERT INTO ThanhPhanDanhGia "
                + "(TenThanhPhan, TrongSo, ThuTu, TrangThai, MaMon) VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ganThamSo(ps, tp);
            if (ps.executeUpdate() != 1) throw new SQLException("Không thêm được thành phần đánh giá.");
            try (ResultSet keys = ps.getGeneratedKeys()) {
                if (!keys.next()) throw new SQLException("Không lấy được mã thành phần mới.");
                return keys.getInt(1);
            }
        }
    }

    public void suaThanhPhan(Connection conn, ThanhPhanDanhGia tp) throws SQLException {
        String sql = "UPDATE ThanhPhanDanhGia SET TenThanhPhan = ?, TrongSo = ?, ThuTu = ?, "
                + "TrangThai = ? WHERE MaMon = ? AND MaTP = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ganThamSo(ps, tp);
            ps.setInt(6, tp.getMaTP());
            // Service đã kiểm tra và khóa bản ghi; cập nhật không đổi giá trị vẫn hợp lệ.
            ps.executeUpdate();
        }
    }

    private void ganThamSo(PreparedStatement ps, ThanhPhanDanhGia tp) throws SQLException {
        ps.setString(1, tp.getTenTP());
        ps.setBigDecimal(2, new BigDecimal(Float.toString(tp.getTrongSo())));
        ps.setInt(3, tp.getThuTu());
        ps.setBoolean(4, tp.getTrangThai());
        ps.setString(5, tp.getMaMon());
    }
}
