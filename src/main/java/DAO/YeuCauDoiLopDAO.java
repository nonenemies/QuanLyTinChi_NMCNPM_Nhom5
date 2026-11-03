package DAO;

import java.util.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import Model.YeuCauDoiLop;
import Model.TrangThaiDoiLop;

public class YeuCauDoiLopDAO {

    public int huyYeuCauHetHan(Connection conn) throws SQLException {
        String sql = "UPDATE YeuCauDoiLop yc "
                + "JOIN LopHoc lh ON lh.MaLop = yc.LopCu "
                + "JOIN SinhVien sv ON sv.MaSV = yc.MaSV "
                + "JOIN Users u ON u.UserID = sv.UserID "
                + "LEFT JOIN DotDangKyNganh dot ON dot.Khoa = u.Khoa "
                + "SET yc.TrangThai = 'HUY', yc.NgayXuLy = CURRENT_TIMESTAMP "
                + "WHERE yc.TrangThai = 'CHO_XAC_NHAN' "
                + "AND (dot.Khoa IS NULL OR CURRENT_TIMESTAMP NOT BETWEEN dot.MoDangKy AND dot.DongDangKy)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            return ps.executeUpdate();
        }
    }

    public int huyYeuCauHetHan() throws SQLException {
        try (Connection conn = Util.DBConnection.getConnection()) {
            return huyYeuCauHetHan(conn);
        }
    }

    public boolean coYeuCauChoXacNhan(Connection conn, String maSV, String lopCu, String lopMoi)
            throws SQLException {
        String sql = "SELECT 1 FROM YeuCauDoiLop WHERE MaSV = ? "
                + "AND TrangThai = 'CHO_XAC_NHAN' "
                + "AND (LopCu = ? OR LopMoi = ? OR LopCu = ? OR LopMoi = ?) LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, lopCu);
            ps.setString(3, lopCu);
            ps.setString(4, lopMoi);
            ps.setString(5, lopMoi);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean themYeuCau(Connection conn, YeuCauDoiLop yc) throws SQLException {
        String sql = "INSERT INTO YeuCauDoiLop (MaSV, LopCu, LopMoi, LyDo) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, yc.getMaSV());
            ps.setString(2, yc.getLopCu());
            ps.setString(3, yc.getLopMoi());
            ps.setString(4, yc.getLyDo());
            return ps.executeUpdate() > 0;
        }
    }

    public boolean capNhatTrangThaiDuyet(Connection conn, int maYeuCau, String maSVNguoiXuLy, String trangThai) throws SQLException {
        String sql = "UPDATE YeuCauDoiLop yc "
                + "JOIN SinhVien sv ON sv.MaSV = ? "
                + "SET yc.TrangThai = ?, yc.NguoiXuLy = sv.UserID, yc.NgayXuLy = NOW() "
                + "WHERE yc.MaYeuCau = ? AND yc.TrangThai = 'CHO_XAC_NHAN'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSVNguoiXuLy);
            ps.setString(2, trangThai);
            ps.setInt(3, maYeuCau);
            return ps.executeUpdate() > 0;
        }
    }

    /** Chỉ gọi trong transaction; khóa yêu cầu đến lúc commit/rollback. */
    public YeuCauDoiLop getYeuCauByIdForUpdate(Connection conn, int maYeuCau) throws SQLException {
        return docYeuCau(conn, maYeuCau, true);
    }

    private YeuCauDoiLop docYeuCau(Connection conn, int maYeuCau, boolean khoa) throws SQLException {
        String sql = "SELECT * FROM YeuCauDoiLop WHERE MaYeuCau = ?" + (khoa ? " FOR UPDATE" : "");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    YeuCauDoiLop yc = new YeuCauDoiLop();
                    yc.setMaYeuCau(rs.getInt("MaYeuCau"));
                    yc.setMaSV(rs.getString("MaSV"));
                    yc.setLopCu(rs.getString("LopCu"));
                    yc.setLopMoi(rs.getString("LopMoi"));
                    yc.setLyDo(rs.getString("LyDo"));
                    yc.setTrangThai(TrangThaiDoiLop.valueOf(rs.getString("TrangThai")));
                    return yc;
                }
            }
        }
        return null;
    }

    public List<YeuCauDoiLop> laySanGiaoDich() throws SQLException {
        List<YeuCauDoiLop> list = new ArrayList<>();
        String sql = sqlThongTinYeuCau()
                + " JOIN SinhVien svDot ON svDot.MaSV = yc.MaSV "
                + " JOIN Users uDot ON uDot.UserID = svDot.UserID "
                + " JOIN DotDangKyNganh dot ON dot.Khoa = uDot.Khoa "
                + "WHERE yc.TrangThai = 'CHO_XAC_NHAN' "
                + "AND CURRENT_TIMESTAMP BETWEEN dot.MoDangKy AND dot.DongDangKy "
                + "ORDER BY yc.NgayTao DESC";
        try (Connection conn = Util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                list.add(docThongTinYeuCau(rs));
            }
        }
        return list;
    }

    public List<YeuCauDoiLop> layYeuCauCuaSinhVien(String maSV) throws SQLException {
        List<YeuCauDoiLop> list = new ArrayList<>();
        String sql = sqlThongTinYeuCau() + " WHERE yc.MaSV = ? ORDER BY yc.NgayTao DESC";
        try (Connection conn = Util.DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(docThongTinYeuCau(rs));
            }
        }
        return list;
    }

    public boolean huyYeuCauCuaSinhVien(String maSV, int maYeuCau) throws SQLException {
        try (Connection conn = Util.DBConnection.getConnection()) {
            return huyYeuCauCuaSinhVien(conn, maSV, maYeuCau);
        }
    }

    public boolean huyYeuCauCuaSinhVien(Connection conn, String maSV, int maYeuCau) throws SQLException {
        String sql = "UPDATE YeuCauDoiLop SET TrangThai = 'HUY', NgayXuLy = NOW() "
                + "WHERE MaYeuCau = ? AND MaSV = ? AND TrangThai = 'CHO_XAC_NHAN'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maYeuCau);
            ps.setString(2, maSV);
            return ps.executeUpdate() == 1;
        }
    }

    private String sqlThongTinYeuCau() {
        return "SELECT yc.*, COALESCE(u.HoTen, yc.MaSV) AS TenSinhVien, "
                + "CONCAT(yc.LopCu, ' · ', COALESCE(mhCu.MaMon, ''), ' - ', "
                + "COALESCE(mhCu.TenMon, ''), ' · HK ', COALESCE(lhCu.HocKy, '')) AS TenLopCu, "
                + "CONCAT(yc.LopMoi, ' · ', COALESCE(mhMoi.MaMon, ''), ' - ', "
                + "COALESCE(mhMoi.TenMon, ''), ' · HK ', COALESCE(lhMoi.HocKy, '')) AS TenLopMoi "
                + "FROM YeuCauDoiLop yc "
                + "LEFT JOIN SinhVien sv ON sv.MaSV = yc.MaSV "
                + "LEFT JOIN Users u ON u.UserID = sv.UserID "
                + "LEFT JOIN LopHoc lhCu ON lhCu.MaLop = yc.LopCu "
                + "LEFT JOIN MonHoc mhCu ON mhCu.MaMon = lhCu.MaMon "
                + "LEFT JOIN LopHoc lhMoi ON lhMoi.MaLop = yc.LopMoi "
                + "LEFT JOIN MonHoc mhMoi ON mhMoi.MaMon = lhMoi.MaMon";
    }

    private YeuCauDoiLop docThongTinYeuCau(ResultSet rs) throws SQLException {
        YeuCauDoiLop yc = new YeuCauDoiLop();
        yc.setMaYeuCau(rs.getInt("MaYeuCau"));
        yc.setMaSV(rs.getString("MaSV"));
        yc.setLopCu(rs.getString("LopCu"));
        yc.setLopMoi(rs.getString("LopMoi"));
        yc.setTrangThai(TrangThaiDoiLop.valueOf(rs.getString("TrangThai")));
        yc.setLyDo(rs.getString("LyDo"));
        yc.setNgayTao(rs.getTimestamp("NgayTao"));
        yc.setNgayXuLy(rs.getTimestamp("NgayXuLy"));
        yc.setTenSinhVien(rs.getString("TenSinhVien"));
        yc.setTenLopCu(rs.getString("TenLopCu"));
        yc.setTenLopMoi(rs.getString("TenLopMoi"));
        return yc;
    }
}