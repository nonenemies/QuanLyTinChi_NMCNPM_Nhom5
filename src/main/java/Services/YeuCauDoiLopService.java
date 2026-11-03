package Services;

import java.sql.Connection;
import java.sql.SQLException;
import DAO.YeuCauDoiLopDAO;
import DAO.DangKyHocDAO;
import DAO.LopHocDAO;
import DAO.MonTienQuyetDAO;
import DAO.ChuongTrinhDaoTaoDAO;
import Model.YeuCauDoiLop;
import Model.MonHoc;
import Model.QuyDinhTinChi;
import Util.DBConnection;
import java.util.*;

public class YeuCauDoiLopService {

    private final YeuCauDoiLopDAO yeuCauDAO = new YeuCauDoiLopDAO();
    private final DangKyHocDAO dangKyDAO = new DangKyHocDAO();
    private final LopHocDAO lopHocDAO = new LopHocDAO();
    private final ChuongTrinhDaoTaoDAO chuongTrinhDaoTaoDAO = new ChuongTrinhDaoTaoDAO();

    private boolean checkTienQuyet(Connection conn, String maSV, String maLopMuonVao) throws SQLException {
        var lopHoc = lopHocDAO.layLopTheoMa(conn, maLopMuonVao);
        if (lopHoc == null) return false;
        String maMonMuonVao = lopHoc.getMaMon();

        // Đọc môn tiên quyết trên cùng kết nối transaction.
        java.util.List<MonHoc> listTienQuyet = MonTienQuyetDAO.layDanhSachMonTienQuyet(conn, maMonMuonVao);

        // 3. Quét từng môn
        for (MonHoc monTQ : listTienQuyet) {
            String maTienQuyet = monTQ.getMaMon();

            // 4. GỌI HÀM CỦA BẠN: Có conn
            if (!dangKyDAO.kiemTraDaQuaMon(conn, maSV, maTienQuyet)) {
                return false;
            }
        }
        return true;
    }

    public java.util.List<YeuCauDoiLop> moSanGiaoDich() {
        try {
            yeuCauDAO.huyYeuCauHetHan();
            return yeuCauDAO.laySanGiaoDich();
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải sàn giao dịch đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải sàn giao dịch đổi lớp.", e);
        }
    }

    public java.util.List<YeuCauDoiLop> xemYeuCauCuaSinhVien(String maSV) {
        if (maSV == null || maSV.isBlank()) throw new IllegalArgumentException("Mã sinh viên không hợp lệ.");
        try {
            yeuCauDAO.huyYeuCauHetHan();
            return yeuCauDAO.layYeuCauCuaSinhVien(maSV);
        } catch (SQLException e) {
            throw new ServiceException("Không thể tải lịch sử đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể tải lịch sử đổi lớp.", e);
        }
    }

    public boolean huyYeuCau(int maYeuCau, String maSV) {
        if (maSV == null || maSV.isBlank() || maYeuCau <= 0) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                yeuCauDAO.huyYeuCauHetHan(conn);
                if (!dangKyDAO.khoaSinhVienChoDangKy(conn, maSV)) {
                    return rollbackAndReturn(conn, false);
                }
                YeuCauDoiLop yc = yeuCauDAO.getYeuCauByIdForUpdate(conn, maYeuCau);
                if (yc == null || !maSV.equals(yc.getMaSV())) {
                    return rollbackAndReturn(conn, false);
                }
                var lopCu = lopHocDAO.layLopTheoMa(conn, yc.getLopCu());
                if (lopCu == null || !chuongTrinhDaoTaoDAO.dangTrongDotDangKy(conn, maSV)) {
                    return rollbackAndReturn(conn, false);
                }
                boolean canceled = yeuCauDAO.huyYeuCauCuaSinhVien(conn, maSV, maYeuCau);
                if (!canceled) return rollbackAndReturn(conn, false);
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
            throw new ServiceException("Không thể hủy yêu cầu đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể hủy yêu cầu đổi lớp.", e);
        }
    }

    public boolean dangBaiDoiLop(String maSVA, String lopCuA, String lopMoiA, String lyDo) {
        if (maSVA == null || maSVA.isBlank() || lopCuA == null || lopCuA.isBlank()
                || lopMoiA == null || lopMoiA.isBlank() || lopCuA.equals(lopMoiA)) {
            return false;
        }
        if (lyDo != null && lyDo.length() > 200) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);
            try {
                yeuCauDAO.huyYeuCauHetHan(conn);
                if (!dangKyDAO.khoaSinhVienChoDangKy(conn, maSVA)
                        || !dangKyDAO.dangHocChuaCoDiem(conn, maSVA, lopCuA)
                        || !chuongTrinhDaoTaoDAO.dangTrongDotDangKy(conn, maSVA)) {
                    return rollbackAndReturn(conn, false);
                }
                String firstClass = lopCuA.compareTo(lopMoiA) < 0 ? lopCuA : lopMoiA;
                String secondClass = lopCuA.compareTo(lopMoiA) < 0 ? lopMoiA : lopCuA;
                var classOne = lopHocDAO.layLopTheoMaForUpdate(conn, firstClass);
                var classTwo = lopHocDAO.layLopTheoMaForUpdate(conn, secondClass);
                var lopCu = lopCuA.equals(firstClass) ? classOne : classTwo;
                var lopMuonVao = lopMoiA.equals(firstClass) ? classOne : classTwo;
                if (lopCu == null || lopMuonVao == null
                        || lopCu.getTrangThai() != Model.TrangThaiHoatDong.HOAT_DONG
                        || lopMuonVao.getTrangThai() != Model.TrangThaiHoatDong.HOAT_DONG
                        || !lopCu.getHocKy().equals(lopMuonVao.getHocKy())
                        || !chuongTrinhDaoTaoDAO.monThuocChuongTrinh(conn, maSVA, lopMuonVao.getMaMon())
                        || dangKyDAO.kiemTraDaDangKy(conn, maSVA, lopMoiA)
                        || dangKyDAO.daDatMon(conn, maSVA, lopMuonVao.getMaMon())
                        || dangKyDAO.daDangKyMonKhacTrongHocKy(
                        conn, maSVA, lopMuonVao.getMaMon(), lopCu.getHocKy(), lopCuA)
                        || yeuCauDAO.coYeuCauChoXacNhan(conn, maSVA, lopCuA, lopMoiA)
                        || !dangKyDAO.coLichHoc(conn, lopCuA)
                        || !dangKyDAO.coLichHoc(conn, lopMoiA)
                        || dangKyDAO.coLopBiTrungLich(conn, maSVA, lopMoiA, lopCuA)
                        || !checkTienQuyet(conn, maSVA, lopMoiA)) {
                    return rollbackAndReturn(conn, false);
                }
                int currentClassCredits = dangKyDAO.laySoTinChiLop(conn, lopCuA);
                int targetClassCredits = dangKyDAO.laySoTinChiLop(conn, lopMoiA);
                int projectedCredits = dangKyDAO.tongTinChiDangHoc(conn, maSVA, lopCu.getHocKy())
                        - currentClassCredits + targetClassCredits;
                if (currentClassCredits <= 0 || targetClassCredits <= 0
                        || projectedCredits > QuyDinhTinChi.MAX_TIN_CHI) {
                    return rollbackAndReturn(conn, false);
                }
                YeuCauDoiLop yc = new YeuCauDoiLop(maSVA, lopCuA, lopMoiA, lyDo);
                if (!yeuCauDAO.themYeuCau(conn, yc)) {
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
            throw new ServiceException("Không thể đăng yêu cầu đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể đăng yêu cầu đổi lớp.", e);
        }
    }

    public boolean xacNhanDoiLop(int maYeuCau, String maSV_B) {
        if (maYeuCau <= 0 || maSV_B == null || maSV_B.isBlank()) {
            return false;
        }
        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false); // Bắt đầu Transaction

            try {
                yeuCauDAO.huyYeuCauHetHan(conn);
                YeuCauDoiLop baiDang = yeuCauDAO.getYeuCauByIdForUpdate(conn, maYeuCau);
                if (baiDang == null || !baiDang.getTrangThai().name().equals("CHO_XAC_NHAN")) {
                    return rollbackAndReturn(conn, false);
                }

                String maSV_A = baiDang.getMaSV();
                String lopA_DangHoc = baiDang.getLopCu();
                String lopA_MuonSang = baiDang.getLopMoi();

                if (Objects.equals(maSV_A, maSV_B)) {
                    return rollbackAndReturn(conn, false);
                }
                for (String maSV : java.util.stream.Stream.of(maSV_A, maSV_B).sorted().toList()) {
                    if (!dangKyDAO.khoaSinhVienChoDangKy(conn, maSV)) {
                        return rollbackAndReturn(conn, false);
                    }
                    if (!chuongTrinhDaoTaoDAO.dangTrongDotDangKy(conn, maSV)) {
                        return rollbackAndReturn(conn, false);
                    }
                }
                String firstClass = lopA_DangHoc.compareTo(lopA_MuonSang) < 0
                        ? lopA_DangHoc : lopA_MuonSang;
                String secondClass = lopA_DangHoc.compareTo(lopA_MuonSang) < 0
                        ? lopA_MuonSang : lopA_DangHoc;
                var classOne = lopHocDAO.layLopTheoMaForUpdate(conn, firstClass);
                var classTwo = lopHocDAO.layLopTheoMaForUpdate(conn, secondClass);
                var lopCu = lopHocDAO.layLopTheoMa(conn, lopA_DangHoc);
                var lopMoi = lopHocDAO.layLopTheoMa(conn, lopA_MuonSang);
                if (lopCu == null || lopMoi == null || classOne == null || classTwo == null
                        || lopCu.getTrangThai() != Model.TrangThaiHoatDong.HOAT_DONG
                        || lopMoi.getTrangThai() != Model.TrangThaiHoatDong.HOAT_DONG
                        || !lopCu.getHocKy().equals(lopMoi.getHocKy())
                        || !chuongTrinhDaoTaoDAO.monThuocChuongTrinh(conn, maSV_A, lopMoi.getMaMon())
                        || !chuongTrinhDaoTaoDAO.monThuocChuongTrinh(conn, maSV_B, lopCu.getMaMon())
                        || !dangKyDAO.dangHocChuaCoDiem(conn, maSV_A, lopA_DangHoc)
                        || !dangKyDAO.dangHocChuaCoDiem(conn, maSV_B, lopA_MuonSang)
                        || dangKyDAO.daDatMon(conn, maSV_A, lopMoi.getMaMon())
                        || dangKyDAO.daDatMon(conn, maSV_B, lopCu.getMaMon())
                        || dangKyDAO.kiemTraDaDangKy(conn, maSV_A, lopA_MuonSang)
                        || dangKyDAO.kiemTraDaDangKy(conn, maSV_B, lopA_DangHoc)
                        || dangKyDAO.daDangKyMonKhacTrongHocKy(
                        conn, maSV_A, lopMoi.getMaMon(), lopCu.getHocKy(), lopA_DangHoc)
                        || dangKyDAO.daDangKyMonKhacTrongHocKy(
                        conn, maSV_B, lopCu.getMaMon(), lopCu.getHocKy(), lopA_MuonSang)) {
                    return rollbackAndReturn(conn, false);
                }
                int oldClassCredits = dangKyDAO.laySoTinChiLop(conn, lopA_DangHoc);
                int newClassCredits = dangKyDAO.laySoTinChiLop(conn, lopA_MuonSang);
                int creditsA = dangKyDAO.tongTinChiDangHoc(conn, maSV_A, lopCu.getHocKy())
                        - oldClassCredits + newClassCredits;
                int creditsB = dangKyDAO.tongTinChiDangHoc(conn, maSV_B, lopCu.getHocKy())
                        - newClassCredits + oldClassCredits;
                if (oldClassCredits <= 0 || newClassCredits <= 0
                        || creditsA > QuyDinhTinChi.MAX_TIN_CHI
                        || creditsB > QuyDinhTinChi.MAX_TIN_CHI) {
                    return rollbackAndReturn(conn, false);
                }
                if (!dangKyDAO.coLichHoc(conn, lopA_MuonSang)) return rollbackAndReturn(conn, false);
                if (dangKyDAO.coLopBiTrungLich(
                        conn, maSV_A, lopA_MuonSang, lopA_DangHoc)) {
                    return rollbackAndReturn(conn, false);
                }
                if (!checkTienQuyet(conn, maSV_A, lopA_MuonSang)) {
                    return rollbackAndReturn(conn, false);
                }

                if (!dangKyDAO.coLichHoc(conn, lopA_DangHoc)) {
                    return rollbackAndReturn(conn, false);
                }
                if (dangKyDAO.coLopBiTrungLich(
                        conn, maSV_B, lopA_DangHoc, lopA_MuonSang)) {
                    return rollbackAndReturn(conn, false);
                }
                if (!checkTienQuyet(conn, maSV_B, lopA_DangHoc)) {
                    return rollbackAndReturn(conn, false);
                }

                // GIAO DỊCH HOÁN ĐỔI LỚP
                boolean doiA = dangKyDAO.capNhatLopChoSinhVien(conn, maSV_A, lopA_DangHoc, lopA_MuonSang);
                boolean doiB = dangKyDAO.capNhatLopChoSinhVien(conn, maSV_B, lopA_MuonSang, lopA_DangHoc);
                boolean daDuyetDon = yeuCauDAO.capNhatTrangThaiDuyet(conn, maYeuCau, maSV_B, "DA_DUYET");

                if (doiA && doiB && daDuyetDon) {
                    conn.commit();
                    return true;
                } else {
                    return rollbackAndReturn(conn, false);
                }
            } catch (SQLException | RuntimeException e) {
                try {
                    conn.rollback();
                } catch (SQLException rollbackError) {
                    e.addSuppressed(rollbackError);
                }
                throw e;
            }
        } catch (SQLException e) {
            throw new ServiceException("Không thể xử lý yêu cầu đổi lớp.", e);
        } catch (RuntimeException e) {
            throw ServiceException.wrap("Không thể xử lý yêu cầu đổi lớp.", e);
        }
    }

    private boolean rollbackAndReturn(Connection conn, boolean result) throws SQLException {
        conn.rollback();
        return result;
    }

}