package DAO;

import Model.BangDiemSinhVien;
import Model.LopHocDangKy;
import Model.TrangThaiDangKy;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

// Truy cập cơ sở dữ liệu để lưu điểm thành phần của sinh viên.
public class DiemDAO {

    /** Lấy các học phần sinh viên đã đăng ký, không gồm các đăng ký đã hủy. */
    public List<LopHocDangKy> layKetQuaHocTap(Connection conn, String maSV) throws SQLException {
        String sql = "SELECT lh.MaLop, mh.MaMon, mh.TenMon, lh.HocKy, mh.SoTinChi, "
                + "dk.DiemTongKet, dk.TrangThai, "
                + "COALESCE((SELECT GROUP_CONCAT(CONCAT(tp.TenThanhPhan, ': ', d.DiemDatDuoc) "
                + "ORDER BY tp.ThuTu, tp.MaTP SEPARATOR ' | ') "
                + "FROM DiemThanhPhan d JOIN ThanhPhanDanhGia tp ON tp.MaTP = d.MaTP "
                + "WHERE d.MaDangKy = dk.MaDangKy), '') AS DiemThanhPhan, "
                + "COALESCE(lich.LichHoc, 'Chưa xếp lịch') AS LichHoc, "
                + "CASE WHEN lich.MaLop IS NULL THEN 0 ELSE 1 END AS CoLichHoc "
                + "FROM DangKyHoc dk "
                + "JOIN LopHoc lh ON lh.MaLop = dk.MaLop "
                + "JOIN MonHoc mh ON mh.MaMon = lh.MaMon "
                + "LEFT JOIN ("
                + "SELECT MaLop, GROUP_CONCAT(CONCAT('Thứ ', Thu, ' ', "
                + "TIME_FORMAT(GioBatDau, '%H:%i'), '-', TIME_FORMAT(GioKetThuc, '%H:%i'), "
                + "' · ', COALESCE(NULLIF(TRIM(PhongHoc), ''), 'Chưa cập nhật phòng')) "
                + "ORDER BY Thu, GioBatDau SEPARATOR ' | ') AS LichHoc "
                + "FROM LichHoc GROUP BY MaLop"
                + ") lich ON lich.MaLop = lh.MaLop "
                + "WHERE dk.MaSV = ? AND dk.TrangThai <> 'HUY' "
                + "ORDER BY lh.HocKy, lh.MaLop, mh.MaMon";
        List<LopHocDangKy> results = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    java.math.BigDecimal finalGrade = rs.getBigDecimal("DiemTongKet");
                    results.add(new LopHocDangKy(
                            rs.getString("MaLop"),
                            rs.getString("MaMon"),
                            rs.getString("TenMon"),
                            null,
                            rs.getString("HocKy"),
                            rs.getString("LichHoc"),
                            rs.getBoolean("CoLichHoc"),
                            rs.getObject("SoTinChi", Integer.class),
                            null,
                            null,
                            true,
                            TrangThaiDangKy.valueOf(rs.getString("TrangThai")),
                            finalGrade == null ? null : finalGrade.floatValue(),
                            rs.getString("DiemThanhPhan")));
                }
            }
        }
        return results;
    }

    /** Chỉ cập nhật khi mỗi thành phần đang hoạt động có đúng một điểm. */
    public int capNhatDiemTongKet(Connection conn, String maSV, String maLop) throws SQLException {
        String sql = "UPDATE DangKyHoc dk JOIN ("
                + "SELECT dk2.MaDangKy, ROUND(SUM(tp.TrongSo * d.DiemDatDuoc), 2) AS TongDiem "
                + "FROM DangKyHoc dk2 "
                + "JOIN LopHoc lh ON lh.MaLop = dk2.MaLop "
                + "JOIN ThanhPhanDanhGia tp ON tp.MaMon = lh.MaMon AND tp.TrangThai = 1 "
                + "LEFT JOIN DiemThanhPhan d ON d.MaDangKy = dk2.MaDangKy AND d.MaTP = tp.MaTP "
                + "WHERE dk2.MaSV = ? AND dk2.MaLop = ? "
                + "GROUP BY dk2.MaDangKy "
                + "HAVING COUNT(*) = COUNT(DISTINCT tp.MaTP) "
                + "AND COUNT(d.DiemDatDuoc) = COUNT(*)"
                + ") ketQua ON ketQua.MaDangKy = dk.MaDangKy "
                + "SET dk.DiemTongKet = ketQua.TongDiem, "
                + "dk.TrangThai = IF(ketQua.TongDiem >= 4.0, 'DA_HOAN_THANH', 'KHONG_DAT')";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maSV);
            ps.setString(2, maLop);
            return ps.executeUpdate();
        }
    }

    /** Tải danh sách sinh viên và điểm của các thành phần đang hoạt động trong lớp. */
    public List<BangDiemSinhVien> layBangDiemTheoLop(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT dk.MaDangKy, dk.MaSV, u.HoTen, dk.DiemTongKet, dk.TrangThai, "
                + "tp.MaTP, d.DiemDatDuoc "
                + "FROM DangKyHoc dk "
                + "JOIN SinhVien sv ON sv.MaSV = dk.MaSV "
                + "JOIN Users u ON u.UserID = sv.UserID "
                + "JOIN LopHoc lh ON lh.MaLop = dk.MaLop "
                + "LEFT JOIN ThanhPhanDanhGia tp ON tp.MaMon = lh.MaMon AND tp.TrangThai = 1 "
                + "LEFT JOIN DiemThanhPhan d ON d.MaDangKy = dk.MaDangKy AND d.MaTP = tp.MaTP "
                + "WHERE dk.MaLop = ? AND dk.TrangThai <> 'HUY' "
                + "ORDER BY u.HoTen, dk.MaSV, tp.ThuTu, tp.MaTP";
        Map<Integer, BangDiemSinhVien> rows = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    int maDangKy = rs.getInt("MaDangKy");
                    BangDiemSinhVien row = rows.get(maDangKy);
                    if (row == null) {
                        java.math.BigDecimal total = rs.getBigDecimal("DiemTongKet");
                        row = new BangDiemSinhVien(maDangKy, rs.getString("MaSV"),
                                rs.getString("HoTen"), total == null ? null : total.floatValue(),
                                TrangThaiDangKy.valueOf(rs.getString("TrangThai")));
                        rows.put(maDangKy, row);
                    }
                    int maTP = rs.getInt("MaTP");
                    if (!rs.wasNull()) {
                        java.math.BigDecimal score = rs.getBigDecimal("DiemDatDuoc");
                        row.setDiemThanhPhan(maTP, score == null ? null : score.floatValue());
                    }
                }
            }
        }
        return new ArrayList<>(rows.values());
    }

    /** Thêm hoặc cập nhật điểm theo mã thành phần trong transaction của Service. */
    public boolean nhapDiem(Connection conn, String maSV, String maLop,
                            int maTP, float diemDatDuoc) throws SQLException {
        int maDangKy;
        String registrationSql = "SELECT MaDangKy FROM DangKyHoc "
                + "WHERE MaSV = ? AND MaLop = ? "
                + "AND TrangThai IN ('DANG_HOC', 'DA_HOAN_THANH', 'KHONG_DAT') FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(registrationSql)) {
            ps.setString(1, maSV);
            ps.setString(2, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
                maDangKy = rs.getInt("MaDangKy");
                if (rs.next()) {
                    throw new SQLException("Multiple active registrations found for the same student and class.");
                }
            }
        }

        String termSql = "SELECT lh.HocKy FROM LopHoc lh WHERE lh.MaLop = ?";
        String hocKy;
        try (PreparedStatement ps = conn.prepareStatement(termSql)) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) return false;
                hocKy = rs.getString("HocKy");
            }
        }
        if (!new CauHinhHocKyDAO().dangTrongDotNhapDiem(conn, hocKy)) return false;

        String componentSql = "SELECT 1 FROM ThanhPhanDanhGia tp "
                + "JOIN LopHoc lh ON lh.MaMon = tp.MaMon "
                + "WHERE tp.MaTP = ? AND lh.MaLop = ? AND tp.TrangThai = 1";
        try (PreparedStatement ps = conn.prepareStatement(componentSql)) {
            ps.setInt(1, maTP);
            ps.setString(2, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    return false;
                }
            }
        }

        int maDiem;
        String existingSql = "SELECT MaDiemTP FROM DiemThanhPhan "
                + "WHERE MaDangKy = ? AND MaTP = ? FOR UPDATE";
        try (PreparedStatement ps = conn.prepareStatement(existingSql)) {
            ps.setInt(1, maDangKy);
            ps.setInt(2, maTP);
            try (ResultSet rs = ps.executeQuery()) {
                if (!rs.next()) {
                    try (PreparedStatement insert = conn.prepareStatement(
                            "INSERT INTO DiemThanhPhan (MaDangKy, MaTP, DiemDatDuoc) VALUES (?, ?, ?)")) {
                        insert.setInt(1, maDangKy);
                        insert.setInt(2, maTP);
                        insert.setFloat(3, diemDatDuoc);
                        return insert.executeUpdate() == 1;
                    }
                }
                maDiem = rs.getInt("MaDiemTP");
                if (rs.next()) {
                    throw new SQLException("Duplicate scores exist for the same registration and component.");
                }
            }
        }
        try (PreparedStatement update = conn.prepareStatement(
                "UPDATE DiemThanhPhan SET DiemDatDuoc = ?, NgayNhap = CURRENT_TIMESTAMP "
                        + "WHERE MaDiemTP = ?")) {
            update.setFloat(1, diemDatDuoc);
            update.setInt(2, maDiem);
            return update.executeUpdate() == 1;
        }
    }

    public boolean coDiemTrongMon(Connection conn, String maMon) throws SQLException {
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

    public boolean giangVienConHoatDong(Connection conn, String maGV) throws SQLException {
        String sql = "SELECT 1 FROM GiaoVien gv JOIN Users u ON u.UserID = gv.UserID "
                + "WHERE gv.MaGV = ? AND u.Role = 'GIAOVIEN' AND u.TrangThai = 'HOAT_DONG'";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGV);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
