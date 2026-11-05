package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

/** Kiểm tra khung thời gian nhập điểm của học kỳ. */
public class CauHinhHocKyDAO {
    public boolean dangTrongDotNhapDiem(Connection conn, String hocKy) throws SQLException {
        String sql = "SELECT 1 FROM CauHinhHocKy "
                + "WHERE HocKy = ? AND CURRENT_TIMESTAMP >= TIMESTAMP(KetThucHocKy) "
                + "AND CURRENT_TIMESTAMP <= HanNhapDiem";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, hocKy);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
