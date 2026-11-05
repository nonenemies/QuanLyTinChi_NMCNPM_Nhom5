package Services;

import DAO.ThanhPhanDanhGiaDAO;
import Model.ThanhPhanDanhGia;
import Util.DBConnection;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

/** Đọc cấu hình đánh giá phục vụ giảng viên nhập điểm. */
public class ThanhPhanDanhGiaService {
    private final ThanhPhanDanhGiaDAO dao = new ThanhPhanDanhGiaDAO();

    public List<ThanhPhanDanhGia> layTheoMon(String maMon) {
        try (Connection conn = DBConnection.getConnection()) {
            return dao.layTheoMon(conn, maMon);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải thành phần đánh giá.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải thành phần đánh giá.", e);
        }
    }
}
