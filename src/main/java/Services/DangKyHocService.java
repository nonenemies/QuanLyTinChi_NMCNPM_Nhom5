package Services;

import java.util.*;
import java.sql.Connection;
import java.sql.SQLException;
import DAO.DangKyHocDAO;
import DAO.LopHocDAO;
import DAO.MonTienQuyetDAO;
import Model.MonTienQuyet;
import Model.DangKyHoc;
import Model.LopHocDangKy;
import Model.LopHocThoiKhoaBieu;
import Model.KetQuaDangKy;
import Model.TrangThaiDangKy;
import Model.QuyDinhTinChi;
import Model.TinhTrangTinChi;
import DAO.ChuongTrinhDaoTaoDAO;
import Util.DBConnection;

public class DangKyHocService {
    private final DangKyHocDAO dangKyDAO = new DangKyHocDAO();
    private final LopHocDAO lopHocDAO = new LopHocDAO();
    private final ChuongTrinhDaoTaoDAO chuongTrinhDaoTaoDAO = new ChuongTrinhDaoTaoDAO();

    public boolean xuLyDangKyHoc(String maSV, String maLop) {
        return dangKyHoc(maSV, maLop) == KetQuaDangKy.THANH_CONG;
    }

    public KetQuaDangKy dangKyHoc(String maSV, String maLop) {
        if (maSV == null || maSV.isBlank() || maLop == null || maLop.isBlank()) {
            return KetQuaDangKy.KHONG_HOP_LE;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try {
                if (!dangKyDAO.khoaSinhVienChoDangKy(conn, maSV)) {
                    return rollbackAndReturn(conn, KetQuaDangKy.SINH_VIEN_KHONG_HOAT_DONG);
                }
                var lopHoc = lopHocDAO.layLopTheoMaForUpdate(conn, maLop);
                if (lopHoc == null) {
                    return rollbackAndReturn(conn, KetQuaDangKy.LOP_KHONG_HOAT_DONG);
                }
                if (lopHoc.getTrangThai() != Model.TrangThaiHoatDong.HOAT_DONG) {
                    return rollbackAndReturn(conn, KetQuaDangKy.LOP_KHONG_HOAT_DONG);
                }
                if (!chuongTrinhDaoTaoDAO.dangTrongDotDangKy(conn, maSV)) {
                    return rollbackAndReturn(conn, KetQuaDangKy.DOT_DANG_KY_DA_DONG);
                }
                if (!chuongTrinhDaoTaoDAO.monThuocChuongTrinh(conn, maSV, lopHoc.getMaMon())) {
                    return rollbackAndReturn(conn, KetQuaDangKy.KHONG_HOP_LE);
                }
                if (dangKyDAO.kiemTraDaDangKy(conn, maSV, maLop)
                        || dangKyDAO.daDangKyMonTrongHocKy(conn, maSV, lopHoc.getMaMon(), lopHoc.getHocKy())) {
                    return rollbackAndReturn(conn, KetQuaDangKy.DA_DANG_KY_MON_TRONG_HOC_KY);
                }
                if (dangKyDAO.daDatMon(conn, maSV, lopHoc.getMaMon())) {
                    return rollbackAndReturn(conn, KetQuaDangKy.DA_QUA_MON);
                }
                if (!dangKyDAO.coLichHoc(conn, maLop)) {
                    return rollbackAndReturn(conn, KetQuaDangKy.LOP_CHUA_XEP_LICH);
                }
                if (dangKyDAO.coLopBiTrungLich(conn, maSV, maLop, null)) {
                    return rollbackAndReturn(conn, KetQuaDangKy.TRUNG_LICH);
                }
                for (MonTienQuyet prerequisite
                        : MonTienQuyetDAO.layDanhSachMonTienQuyet(conn, lopHoc.getMaMon())) {
                    if (prerequisite.getMonTienQuyet().getTrangThai()
                            != Model.TrangThaiHoatDong.HOAT_DONG
                            || !dangKyDAO.kiemTraDaQuaMon(conn, maSV,
                                    prerequisite.getMaMonTienQuyet())) {
                        return rollbackAndReturn(conn, KetQuaDangKy.THIEU_MON_TIEN_QUYET);
                    }
                }

                int credits = dangKyDAO.laySoTinChi(conn, lopHoc.getMaMon());
                if (credits <= 0 || dangKyDAO.tongTinChiDangHoc(conn, maSV, lopHoc.getHocKy()) + credits
                        > QuyDinhTinChi.MAX_TIN_CHI) {
                    return rollbackAndReturn(conn, KetQuaDangKy.VUOT_QUA_TIN_CHI);
                }

                if (!lopHocDAO.tangSiSo(conn, maLop)) {
                    return rollbackAndReturn(conn, KetQuaDangKy.LOP_KHONG_HOAT_DONG);
                }

                DangKyHoc dk = new DangKyHoc(maLop, maSV);
                if (!dangKyDAO.themDangKyMoi(conn, dk)) {
                    return rollbackAndReturn(conn, KetQuaDangKy.KHONG_HOP_LE);
                }

                conn.commit();
                return KetQuaDangKy.THANH_CONG;

            } catch (SQLException e) {
                boolean rollbackFailed = false;
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                    rollbackFailed = true;
                }
                if (!rollbackFailed && e.getErrorCode() == 1062) {
                    return KetQuaDangKy.DA_DANG_KY_MON_TRONG_HOC_KY;
                }
                throw e;
            } catch (RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể xử lý đăng ký học.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể xử lý đăng ký học.", e);
        }
    }

    public boolean huyDangKyHoc(String maSV, String maLop) {
        if (maSV == null || maSV.isBlank() || maLop == null || maLop.isBlank()) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                if (!dangKyDAO.khoaSinhVienChoDangKy(conn, maSV)) {
                    return rollbackAndReturn(conn, false);
                }
                if (lopHocDAO.layLopTheoMaForUpdate(conn, maLop) == null) {
                    return rollbackAndReturn(conn, false);
                }

                var lopHoc = lopHocDAO.layLopTheoMa(conn, maLop);
                if (lopHoc == null || !chuongTrinhDaoTaoDAO.dangTrongDotDangKy(conn, maSV)) {
                    return rollbackAndReturn(conn, false);
                }
                TrangThaiDangKy trangThai = dangKyDAO.layTrangThaiDangKyForUpdate(conn, maSV, maLop);
                if (trangThai == null || trangThai == TrangThaiDangKy.HUY) {
                    return rollbackAndReturn(conn, false);
                }
                if (trangThai != TrangThaiDangKy.DANG_HOC
                        || dangKyDAO.coDiem(conn, maSV, maLop)) {
                    return rollbackAndReturn(conn, false);
                }
                if (!dangKyDAO.huyDangKy(conn, maSV, maLop)) {
                    return rollbackAndReturn(conn, false);
                }

                if (!lopHocDAO.giamSiSo(conn, maLop)) {
                    return rollbackAndReturn(conn, false);
                }

                conn.commit();
                return true;
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể hủy đăng ký lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể hủy đăng ký lớp.", e);
        }
    }

    public List<LopHocThoiKhoaBieu> xemLichHocTheoTuan(String maSV) {
        if (maSV == null || maSV.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên không hợp lệ.");
        }
        try {
            return dangKyDAO.layLichHocTheoTuan(maSV);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải thời khóa biểu.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải thời khóa biểu.", e);
        }
    }

    public List<LopHocDangKy> xemDanhSachLopDangKy(String maSV) {
        try {
            return lopHocDAO.layDanhSachLopDangKy(maSV);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách lớp đăng ký.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải danh sách lớp đăng ký.", e);
        }
    }

    public List<LopHocDangKy> xemDanhSachLopDaDangKy(String maSV) {
        if (maSV == null || maSV.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên không hợp lệ.");
        }
        try {
            return lopHocDAO.layDanhSachLopDaDangKy(maSV);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải danh sách lớp đã đăng ký.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải danh sách lớp đã đăng ký.", e);
        }
    }

    public TinhTrangTinChi tinhTrangTinChi(String maSV, String hocKy) {
        if (maSV == null || maSV.isBlank() || hocKy == null || hocKy.isBlank()) {
            throw new IllegalArgumentException("Mã sinh viên và học kỳ là bắt buộc.");
        }
        try (Connection conn = DBConnection.getConnection()) {
            int totalCredits = dangKyDAO.tongTinChiDangHoc(conn, maSV, hocKy);
            return new TinhTrangTinChi(totalCredits, totalCredits >= QuyDinhTinChi.MIN_TIN_CHI);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải tổng tín chỉ của học kỳ.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải tổng tín chỉ của học kỳ.", e);
        }
    }

    private <T> T rollbackAndReturn(Connection conn, T result) throws SQLException {
        conn.rollback();
        return result;
    }
}