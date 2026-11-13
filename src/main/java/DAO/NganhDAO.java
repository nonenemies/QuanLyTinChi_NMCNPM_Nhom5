package DAO;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class NganhDAO {
    public boolean tonTai(Connection conn, String maNganh) throws SQLException {
        String sql = "SELECT 1 FROM Nganh WHERE MaNganh = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maNganh);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next();
            }
        }
    }
}
