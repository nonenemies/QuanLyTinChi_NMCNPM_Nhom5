package Model;

/**
 * Model ánh xạ tới bảng MonHoc trong database.
 */
public class MonHoc {
    private String maMon;
    private String tenMon;
    private Integer soTinChi;
    private String loaiMon;
    private String moTa;
    private TrangThaiHoatDong trangThai;

    public MonHoc() {
    }

    public MonHoc(String maMon, String tenMon, Integer soTinChi,
                  String loaiMon, String moTa, TrangThaiHoatDong trangThai) {
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.soTinChi = soTinChi;
        this.loaiMon = loaiMon;
        this.moTa = moTa;
        this.trangThai = trangThai;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenMon() {
        return tenMon;
    }

    public void setTenMon(String tenMon) {
        this.tenMon = tenMon;
    }

    public Integer getSoTinChi() {
        return soTinChi;
    }

    public void setSoTinChi(Integer soTinChi) {
        this.soTinChi = soTinChi;
    }

    public String getLoaiMon() {
        return loaiMon;
    }

    public void setLoaiMon(String loaiMon) {
        this.loaiMon = loaiMon;
    }

    public String getMoTa() {
        return moTa;
    }

    public void setMoTa(String moTa) {
        this.moTa = moTa;
    }

    public TrangThaiHoatDong getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiHoatDong trangThai) {
        this.trangThai = trangThai;
    }
}
