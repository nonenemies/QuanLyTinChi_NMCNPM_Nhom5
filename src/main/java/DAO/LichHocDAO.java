package DAO;

import Model.LichHoc;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/** Truy vấn lịch học lặp theo tuần của lớp học phần. */
public class LichHocDAO {
    public LichHoc layTheoMa(Connection conn, int maLich) throws SQLException {
        String sql = "SELECT MaLich, MaLop, Thu, GioBatDau, GioKetThuc, PhongHoc "
                + "FROM LichHoc WHERE MaLich = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, maLich);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? docLichHoc(rs) : null;
            }
        }
    }

    public List<LichHoc> layTheoLop(String maLop) throws SQLException {
        try (Connection conn = DBConnection.getConnection()) {
            return layTheoLop(conn, maLop);
        }
    }

    public List<LichHoc> layTheoLop(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT MaLich, MaLop, Thu, GioBatDau, GioKetThuc, PhongHoc "
                + "FROM LichHoc WHERE MaLop = ? ORDER BY Thu, GioBatDau";
        List<LichHoc> schedules = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) schedules.add(docLichHoc(rs));
            }
        }
        return schedules;
    }

    public List<LichHoc> layTatCa() throws SQLException {
        String sql = "SELECT MaLich, MaLop, Thu, GioBatDau, GioKetThuc, PhongHoc "
                + "FROM LichHoc ORDER BY MaLop, Thu, GioBatDau";
        List<LichHoc> schedules = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            while (rs.next()) schedules.add(docLichHoc(rs));
        }
        return schedules;
    }

    public boolean them(Connection conn, LichHoc lichHoc) throws SQLException {
        Objects.requireNonNull(lichHoc, "lichHoc không được null");
        validate(lichHoc);
        String sql = "INSERT INTO LichHoc (MaLop, Thu, GioBatDau, GioKetThuc, PhongHoc) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lichHoc.getMaLop());
            ps.setInt(2, lichHoc.getThu());
            ps.setTime(3, Time.valueOf(lichHoc.getGioBatDau()));
            ps.setTime(4, Time.valueOf(lichHoc.getGioKetThuc()));
            ps.setString(5, lichHoc.getPhongHoc());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean capNhat(Connection conn, LichHoc lichHoc) throws SQLException {
        Objects.requireNonNull(lichHoc, "lichHoc không được null");
        validate(lichHoc);
        String sql = "UPDATE LichHoc SET MaLop = ?, Thu = ?, GioBatDau = ?, GioKetThuc = ?, "
                + "PhongHoc = ? "
                + "WHERE MaLich = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, lichHoc.getMaLop());
            ps.setInt(2, lichHoc.getThu());
            ps.setTime(3, Time.valueOf(lichHoc.getGioBatDau()));
            ps.setTime(4, Time.valueOf(lichHoc.getGioKetThuc()));
            ps.setString(5, lichHoc.getPhongHoc());
            ps.setInt(6, lichHoc.getMaLich());
            return ps.executeUpdate() == 1;
        }
    }

    public boolean xoa(Connection conn, int maLich) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement("DELETE FROM LichHoc WHERE MaLich = ?")) {
            ps.setInt(1, maLich);
            return ps.executeUpdate() == 1;
        }
    }

    public boolean coXungDot(Connection conn, LichHoc lichHoc) throws SQLException {
        String sql = "SELECT 1 FROM LichHoc currentSchedule "
                + "JOIN LopHoc currentClass ON currentClass.MaLop = currentSchedule.MaLop "
                + "JOIN LopHoc targetClass ON targetClass.MaLop = ? "
                + "WHERE currentSchedule.Thu = ? "
                + "AND currentClass.HocKy = targetClass.HocKy "
                + "AND currentSchedule.GioBatDau < ? "
                + "AND currentSchedule.GioKetThuc > ? "
                + "AND (currentClass.MaLop = targetClass.MaLop "
                + "OR currentClass.MaGV = targetClass.MaGV "
                + "OR (? IS NOT NULL AND TRIM(?) <> '' "
                + "AND currentSchedule.PhongHoc = ?)) "
                + (lichHoc.getMaLich() > 0 ? "AND currentSchedule.MaLich <> ? " : "")
                + "LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            int index = 1;
            ps.setString(index++, lichHoc.getMaLop());
            ps.setInt(index++, lichHoc.getThu());
            ps.setTime(index++, Time.valueOf(lichHoc.getGioKetThuc()));
            ps.setTime(index++, Time.valueOf(lichHoc.getGioBatDau()));
            ps.setString(index++, lichHoc.getPhongHoc());
            ps.setString(index++, lichHoc.getPhongHoc());
            ps.setString(index++, lichHoc.getPhongHoc());
            if (lichHoc.getMaLich() > 0) ps.setInt(index, lichHoc.getMaLich());
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean lopHoatDong(Connection conn, String maLop) throws SQLException {
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT 1 FROM LopHoc WHERE MaLop = ? AND TrangThai = 'HOAT_DONG'")) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    public boolean coSinhVienDangKy(Connection conn, String maLop) throws SQLException {
        String sql = "SELECT 1 FROM DangKyHoc "
                + "WHERE MaLop = ? AND TrangThai <> 'HUY' LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maLop);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }

    private void validate(LichHoc lichHoc) {
        if (lichHoc.getMaLop() == null || lichHoc.getMaLop().isBlank()
                || lichHoc.getThu() < 2 || lichHoc.getThu() > 8
                || lichHoc.getGioBatDau() == null || lichHoc.getGioKetThuc() == null
                || !lichHoc.getGioBatDau().isBefore(lichHoc.getGioKetThuc())) {
            throw new IllegalArgumentException("Lịch cần có lớp, thứ từ 2 đến 8 và giờ bắt đầu trước giờ kết thúc.");
        }
    }

    private LichHoc docLichHoc(ResultSet rs) throws SQLException {
        return new LichHoc(rs.getInt("MaLich"), rs.getString("MaLop"), rs.getInt("Thu"),
                rs.getTime("GioBatDau").toLocalTime(), rs.getTime("GioKetThuc").toLocalTime(),
                rs.getString("PhongHoc"));
    }
}
