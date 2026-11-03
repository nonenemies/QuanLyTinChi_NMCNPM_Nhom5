package Model;

import java.util.Date;

public class SinhVien extends Users {
    private String maSinhVien;
    private Date ngaySinh;
    private Boolean gioiTinh;
    private String lop;
    private Integer khoaHoc;
    private String heDaoTao;

    public SinhVien(Integer userID, String hoTen, String email,
                    String passwordHash, String role, String maSo,
                    String khoa, Date ngayTao, TrangThaiHoatDong trangThai,
                    String maSinhVien, Date ngaySinh, Boolean gioiTinh,
                    String lop, Integer khoaHoc, String heDaoTao) {
        super(userID, hoTen, email, passwordHash, role, maSo, khoa, ngayTao, trangThai);
        this.maSinhVien = maSinhVien;
        this.ngaySinh = ngaySinh;
        this.gioiTinh = gioiTinh;
        this.lop = lop;
        this.khoaHoc = khoaHoc;
        this.heDaoTao = heDaoTao;
    }

    public String getMaSinhVien() {
        return maSinhVien;
    }

    public void setMaSinhVien(String maSinhVien) {
        this.maSinhVien = maSinhVien;
    }

    public Date getNgaySinh() {
        return ngaySinh;
    }

    public void setNgaySinh(Date ngaySinh) {
        this.ngaySinh = ngaySinh;
    }

    public Boolean getGioiTinh() {
        return gioiTinh;
    }

    public void setGioiTinh(Boolean gioiTinh) {
        this.gioiTinh = gioiTinh;
    }

    public String getLop() {
        return lop;
    }

    public void setLop(String lop) {
        this.lop = lop;
    }

    public Integer getKhoaHoc() {
        return khoaHoc;
    }

    public void setKhoaHoc(Integer khoaHoc) {
        this.khoaHoc = khoaHoc;
    }

    public String getHeDaoTao() {
        return heDaoTao;
    }

    public void setHeDaoTao(String heDaoTao) {
        this.heDaoTao = heDaoTao;
    }
}
