package Services;

import DAO.ThanhPhanDanhGiaDAO;
import Model.ThanhPhanDanhGia;
import Model.ThongTinQuyen;
import Model.Users;
import Util.DBConnection;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** Kiểm tra nghiệp vụ, phân quyền và quản lý toàn bộ transaction. */
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

    /** Nhận toàn bộ danh sách tên/trọng số khi người dùng bấm Lưu. */
    public List<Integer> themThanhPhan(Users nguoiDung, String maMon,
                                     List<ThanhPhanDanhGia> danhSach) {
        try {
            return luu(nguoiDung, maMon, danhSach, false);
        } catch (SQLException e) {
            throw new ServiceException("Không thể lưu thành phần đánh giá.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể lưu thành phần đánh giá.", e);
        }
    }

    /** Cho phép điều chỉnh nhiều trọng số cùng lúc mà tổng cuối cùng vẫn bằng 1. */
    public boolean suaThanhPhan(Users nguoiDung, String maMon,
                               List<ThanhPhanDanhGia> danhSach) {
        try {
            luu(nguoiDung, maMon, danhSach, true);
            return true;
        } catch (SQLException e) {
            throw new ServiceException("Không thể cập nhật thành phần đánh giá.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể cập nhật thành phần đánh giá.", e);
        }
    }

    private List<Integer> luu(Users nguoiDung, String maMon,
                              List<ThanhPhanDanhGia> danhSach, boolean sua) throws SQLException {
        int userID = layUserID(nguoiDung);
        if (maMon == null || maMon.isBlank() || maMon.length() > 20) {
            throw new ServiceException("Mã môn học không hợp lệ.");
        }
        if (danhSach == null || danhSach.isEmpty()) {
            throw new ServiceException("Cần ít nhất một thành phần đánh giá.");
        }
        // Sao chép dữ liệu nhập; khi thêm chỉ cần tên và trọng số.
        List<ThanhPhanDanhGia> duLieu = new ArrayList<>();
        for (ThanhPhanDanhGia tp : danhSach) {
            kiemTraDuLieu(tp);
            duLieu.add(new ThanhPhanDanhGia(tp.getMaTP(), maMon, tp.getTenTP().strip(),
                    tp.getTrongSo(), tp.getThuTu(), sua ? tp.getTrangThai() : true));
        }
        if (!sua) kiemTraTong(duLieu);

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                ThongTinQuyen quyen = dao.traQuyen(conn, userID);
                if (quyen == null || !"ADMIN".equals(quyen.role())
                        || quyen.trangThai() != Model.TrangThaiHoatDong.HOAT_DONG) {
                    throw new ServiceException("Chỉ admin đang hoạt động được phép thêm/sửa thành phần đánh giá.");
                }
                if (dao.timMonDeCapNhat(conn, maMon) == null) {
                    throw new ServiceException("Môn học không tồn tại.");
                }
                if (sua && dao.daCoDiem(conn, maMon)) {
                    throw new ServiceException("Không thể sửa trọng số hoặc trạng thái thành phần sau khi đã nhập điểm.");
                }
                List<ThanhPhanDanhGia> hienTai = dao.layTheoMonDeCapNhat(conn, maMon);
                List<ThanhPhanDanhGia> sauKhiLuu = new ArrayList<>(hienTai);
                if (sua) {
                    Set<Integer> maDaSua = new HashSet<>();
                    for (ThanhPhanDanhGia tp : duLieu) {
                        if (tp.getMaTP() <= 0 || tp.getThuTu() <= 0 || !maDaSua.add(tp.getMaTP())) {
                            throw new ServiceException("Mã phải dương, không trùng và thứ tự phải dương.");
                        }
                        if (!sauKhiLuu.removeIf(cu -> cu.getMaTP() == tp.getMaTP())) {
                            throw new ServiceException("Thành phần đánh giá không tồn tại trong môn học này.");
                        }
                    }
                } else {
                    if (hienTai.stream().anyMatch(ThanhPhanDanhGia::getTrangThai)) {
                        throw new ServiceException("Môn đã có cấu hình đánh giá. Hãy chỉnh sửa cấu hình hiện có.");
                    }
                    int thuTu = hienTai.stream().mapToInt(ThanhPhanDanhGia::getThuTu).max().orElse(0);
                    for (ThanhPhanDanhGia tp : duLieu) {
                        try {
                            thuTu = Math.incrementExact(thuTu);
                        } catch (ArithmeticException e) {
                            throw new ServiceException("Không thể tạo thứ tự thành phần tiếp theo.", e);
                        }
                        tp.setThuTu(thuTu);
                    }
                }
                sauKhiLuu.addAll(duLieu);
                kiemTraCauHinh(sauKhiLuu);
                List<Integer> maThanhPhan = new ArrayList<>();
                for (ThanhPhanDanhGia tp : duLieu) {
                    if (sua) {
                        dao.suaThanhPhan(conn, tp);
                        maThanhPhan.add(tp.getMaTP());
                    } else {
                        maThanhPhan.add(dao.themThanhPhan(conn, tp));
                    }
                }
                conn.commit();
                return maThanhPhan;
            } catch (SQLException | RuntimeException e) {
                try { conn.rollback(); } catch (SQLException rollbackError) { e.addSuppressed(rollbackError); }
                throw e;
            }
        }
    }

    private void kiemTraCauHinh(List<ThanhPhanDanhGia> danhSach) {
        Set<Integer> thuTu = new HashSet<>();
        for (ThanhPhanDanhGia tp : danhSach) {
            if (!thuTu.add(tp.getThuTu())) {
                throw new ServiceException("Thứ tự thành phần đã tồn tại trong môn học.");
            }
        }
        kiemTraTong(danhSach);
    }

    private void kiemTraTong(List<ThanhPhanDanhGia> danhSach) {
        BigDecimal tong = BigDecimal.ZERO;
        for (ThanhPhanDanhGia tp : danhSach) {
            if (tp.getTrangThai()) tong = tong.add(decimal(tp.getTrongSo()));
        }
        if (tong.compareTo(BigDecimal.ONE) != 0) {
            throw new ServiceException("Tổng trọng số các thành phần phải đúng bằng 1.");
        }
    }

    private void kiemTraDuLieu(ThanhPhanDanhGia tp) {
        if (tp == null || tp.getTenTP() == null || tp.getTenTP().isBlank()
                || tp.getTenTP().strip().length() > 200) {
            throw new ServiceException("Tên thành phần phải có từ 1 đến 200 ký tự và không được để trống.");
        }
        if (!Float.isFinite(tp.getTrongSo()) || tp.getTrongSo() <= 0 || tp.getTrongSo() > 1) {
            throw new ServiceException("Trọng số phải thuộc khoảng (0, 1].");
        }
        if (decimal(tp.getTrongSo()).stripTrailingZeros().scale() > 2) {
            throw new ServiceException("Trọng số chỉ được có tối đa 2 chữ số thập phân.");
        }
    }

    private BigDecimal decimal(float giaTri) {
        return new BigDecimal(Float.toString(giaTri));
    }

    private int layUserID(Users nguoiDung) {
        if (nguoiDung == null || nguoiDung.getUserID() == null || nguoiDung.getUserID() <= 0) {
            throw new ServiceException("Bạn cần đăng nhập bằng tài khoản admin.");
        }
        return nguoiDung.getUserID();
    }
}