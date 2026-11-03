package DAO;

import Model.MonHoc;
import Model.TrangThaiHoatDong;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Các truy vấn dữ liệu cho bảng MonHoc. */
public class MonHocDAO {

    /** Thêm môn học mới. Mã môn phải chưa tồn tại. */
    public boolean themMonHoc(MonHoc monHoc) throws SQLException {
        Objects.requireNonNull(monHoc, "monHoc không được null");
        String sql = "INSERT INTO MonHoc (MaMon, TenMon, SoTinChi, LoaiMon, MoTa) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monHoc.getMaMon());
            ps.setString(2, monHoc.getTenMon());
            ps.setObject(3, monHoc.getSoTinChi());
            ps.setString(4, monHoc.getLoaiMon());
            ps.setString(5, monHoc.getMoTa());
            return ps.executeUpdate() == 1;
        }
    }

    /** Cập nhật thông tin môn học, không thay đổi mã môn. */
    public boolean suaMonHoc(MonHoc monHoc) throws SQLException {
        Objects.requireNonNull(monHoc, "monHoc không được null");
        String sql = "UPDATE MonHoc SET TenMon = ?, SoTinChi = ?, LoaiMon = ?, MoTa = ? "
                + "WHERE MaMon = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, monHoc.getTenMon());
            ps.setObject(2, monHoc.getSoTinChi());
            ps.setString(3, monHoc.getLoaiMon());
            ps.setString(4, monHoc.getMoTa());
            ps.setString(5, monHoc.getMaMon());
            return ps.executeUpdate() == 1;
        }
    }

    /** Ngừng kích hoạt môn học để giữ lại dữ liệu lịch sử và các khóa ngoại. */
    public boolean xoaMonHoc(String maMon) throws SQLException {
        String sql = "UPDATE MonHoc SET TrangThai = ? WHERE MaMon = ? AND TrangThai <> ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, TrangThaiHoatDong.NGUNG_HOAT_DONG.name());
            ps.setString(2, maMon);
            ps.setString(3, TrangThaiHoatDong.NGUNG_HOAT_DONG.name());
            return ps.executeUpdate() == 1;
        }
    }

    /** Lấy mọi môn, gồm cả môn đã ngừng hoạt động, dùng cho màn hình quản trị. */
    public List<MonHoc> layTatCaMonHoc() throws SQLException {
        return truyVanDanhSach("SELECT MaMon, TenMon, SoTinChi, LoaiMon, MoTa, TrangThai "
                + "FROM MonHoc ORDER BY MaMon");
    }

    private List<MonHoc> truyVanDanhSach(String sql) throws SQLException {
        List<MonHoc> danhSach = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) {
                danhSach.add(docMonHoc(rs));
            }
        }
        return danhSach;
    }

    private MonHoc docMonHoc(ResultSet rs) throws SQLException {
        return new MonHoc(
                rs.getString("MaMon"),
                rs.getString("TenMon"),
                rs.getObject("SoTinChi", Integer.class),
                rs.getString("LoaiMon"),
                rs.getString("MoTa"),
                TrangThaiHoatDong.valueOf(rs.getString("TrangThai"))
        );
    }

}
