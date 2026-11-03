package Model;

/**
 * Model ánh xạ tới bảng LopHoc trong database.
 */
public class LopHoc {
    private String maLop;
    private String maMon;
    private String maGV;
    private String hocKy;
    private Integer siSoToiDa;
    private Integer siSoHienTai;
    private TrangThaiHoatDong trangThai;

    public LopHoc() {
    }

    public LopHoc(String maLop, String maMon, String maGV, String hocKy,
                  Integer siSoToiDa, Integer siSoHienTai, TrangThaiHoatDong trangThai) {
        this.maLop = maLop;
        this.maMon = maMon;
        this.maGV = maGV;
        this.hocKy = hocKy;
        this.siSoToiDa = siSoToiDa;
        this.siSoHienTai = siSoHienTai;
        this.trangThai = trangThai;
    }

    public String getMaLop() {
        return maLop;
    }

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getMaGV() {
        return maGV;
    }

    public void setMaGV(String maGV) {
        this.maGV = maGV;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public Integer getSiSoToiDa() {
        return siSoToiDa;
    }

    public void setSiSoToiDa(Integer siSoToiDa) {
        this.siSoToiDa = siSoToiDa;
    }

    public Integer getSiSoHienTai() {
        return siSoHienTai;
    }

    public void setSiSoHienTai(Integer siSoHienTai) {
        this.siSoHienTai = siSoHienTai;
    }

    public TrangThaiHoatDong getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiHoatDong trangThai) {
        this.trangThai = trangThai;
    }
}