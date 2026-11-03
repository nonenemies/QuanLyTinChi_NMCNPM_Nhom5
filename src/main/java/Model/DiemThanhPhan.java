package Model;

import java.util.Date;

public class DiemThanhPhan {
    private int maDiemTP;
    private int maDangKy;
    private int maTP;
    private float diemDatDuoc;
    private Date ngayNhap;

    public DiemThanhPhan() {
    }

    public DiemThanhPhan(int maDiemTP, int maDangKy, int maTP, float diemDatDuoc) {
        this.maDiemTP = maDiemTP;
        this.maDangKy = maDangKy;
        this.maTP = maTP;
        this.diemDatDuoc = diemDatDuoc;
    }

    public int getMaDiemTP() {
        return maDiemTP;
    }

    public void setMaDiemTP(int maDiemTP) {
        this.maDiemTP = maDiemTP;
    }

    public int getMaDangKy() {
        return maDangKy;
    }

    public void setMaDangKy(int maDangKy) {
        this.maDangKy = maDangKy;
    }

    public int getMaTP() {
        return maTP;
    }

    public void setMaTP(int maTP) {
        this.maTP = maTP;
    }

    public float getDiemDatDuoc() {
        return diemDatDuoc;
    }

    public void setDiemDatDuoc(float diemDatDuoc) {
        this.diemDatDuoc = diemDatDuoc;
    }

    public Date getNgayNhap() {
        return ngayNhap;
    }

    public void setNgayNhap(Date ngayNhap) {
        this.ngayNhap = ngayNhap;
    }
}
