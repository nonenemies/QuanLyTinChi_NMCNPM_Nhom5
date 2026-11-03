package Model;

/** Thông tin lớp học và môn học dùng trong màn hình đăng ký tín chỉ. */
public class LopHocDangKy {
    private final String maLop;
    private final String maMon;
    private final String tenMon;
    private final String giangVien;
    private final String hocKy;
    private final String lichHoc;
    private final boolean coLichHoc;
    private final Integer soTinChi;
    private final Integer siSoHienTai;
    private final Integer siSoToiDa;
    private final boolean daDangKy;
    private final TrangThaiDangKy trangThaiDangKy;
    private final Float diemTongKet;
    private final String diemThanhPhan;

    public LopHocDangKy(String maLop, String maMon, String tenMon, String giangVien,
                        String hocKy, String lichHoc, boolean coLichHoc,
                        Integer soTinChi, Integer siSoHienTai, Integer siSoToiDa,
                        boolean daDangKy, TrangThaiDangKy trangThaiDangKy) {
        this(maLop, maMon, tenMon, giangVien, hocKy, lichHoc, coLichHoc,
                soTinChi, siSoHienTai, siSoToiDa, daDangKy, trangThaiDangKy, null);
    }

    public LopHocDangKy(String maLop, String maMon, String tenMon, String giangVien,
                        String hocKy, String lichHoc, boolean coLichHoc,
                        Integer soTinChi, Integer siSoHienTai, Integer siSoToiDa,
                        boolean daDangKy, TrangThaiDangKy trangThaiDangKy, Float diemTongKet) {
        this(maLop, maMon, tenMon, giangVien, hocKy, lichHoc, coLichHoc, soTinChi,
                siSoHienTai, siSoToiDa, daDangKy, trangThaiDangKy, diemTongKet, null);
    }

    public LopHocDangKy(String maLop, String maMon, String tenMon, String giangVien,
                        String hocKy, String lichHoc, boolean coLichHoc,
                        Integer soTinChi, Integer siSoHienTai, Integer siSoToiDa,
                        boolean daDangKy, TrangThaiDangKy trangThaiDangKy, Float diemTongKet,
                        String diemThanhPhan) {
        this.maLop = maLop;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.giangVien = giangVien;
        this.hocKy = hocKy;
        this.lichHoc = lichHoc;
        this.coLichHoc = coLichHoc;
        this.soTinChi = soTinChi;
        this.siSoHienTai = siSoHienTai;
        this.siSoToiDa = siSoToiDa;
        this.daDangKy = daDangKy;
        this.trangThaiDangKy = trangThaiDangKy;
        this.diemTongKet = diemTongKet;
        this.diemThanhPhan = diemThanhPhan;
    }

    public String getMaLop() { return maLop; }
    public String getMaMon() { return maMon; }
    public String getTenMon() { return tenMon; }
    public String getGiangVien() { return giangVien; }
    public String getHocKy() { return hocKy; }
    public String getLichHoc() { return lichHoc; }
    public boolean isCoLichHoc() { return coLichHoc; }
    public Integer getSoTinChi() { return soTinChi; }
    public Integer getSiSoHienTai() { return siSoHienTai; }
    public Integer getSiSoToiDa() { return siSoToiDa; }
    public boolean isDaDangKy() { return daDangKy; }
    public Float getDiemTongKet() { return diemTongKet; }
    public String getDiemThanhPhan() { return diemThanhPhan; }
    public TrangThaiDangKy getTrangThaiDangKy() { return trangThaiDangKy; }
    public boolean isCoTheHuy() {
        return daDangKy && trangThaiDangKy == TrangThaiDangKy.DANG_HOC;
    }
    public String getSiSo() { return siSoHienTai + " / " + siSoToiDa; }
    public String getTrangThai() {
        if (daDangKy) {
            return switch (trangThaiDangKy) {
                case DA_HOAN_THANH -> "Đã hoàn thành";
                case KHONG_DAT -> "Không đạt";
                case HUY -> "Đã hủy";
                default -> "Đang học";
            };
        }
        if (!coLichHoc) return "Chưa xếp lịch";
        if (siSoHienTai == null || siSoToiDa == null) return "Không rõ sĩ số";
        return siSoHienTai < siSoToiDa ? "Còn chỗ" : "Đã đầy";
    }
}
