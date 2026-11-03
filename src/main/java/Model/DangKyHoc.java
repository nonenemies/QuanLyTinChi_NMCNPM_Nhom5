package Model;
import java.util.*;

public class DangKyHoc {

    private int maDangKy;
    private String maLop;
    private String maSV;
    private Date ngayDangKy;
    private float diemTongKet;
    private TrangThaiDangKy trangThai;

    public DangKyHoc() {
    }

    public DangKyHoc(String maLop, String maSV) {
        this.maLop = maLop;
        this.maSV = maSV;
    }

    public DangKyHoc(int maDangKy, String maLop, String maSV, Date ngayDangKy, float diemTongKet, TrangThaiDangKy trangThai) {
        this.maDangKy = maDangKy;
        this.maLop = maLop;
        this.maSV = maSV;
        this.ngayDangKy = ngayDangKy;
        this.diemTongKet = diemTongKet;
        this.trangThai = trangThai;
    }

    public int getMaDangKy() {
        return maDangKy;
    }

    public void setMaDangKy(int maDangKy) {
        this.maDangKy = maDangKy;
    }

    public String getMaLop() {
        return maLop;
    }

    public void setMaLop(String maLop) {
        this.maLop = maLop;
    }

    public String getMaSV() {
        return maSV;
    }

    public void setMaSV(String maSV) {
        this.maSV = maSV;
    }

    public Date getNgayDangKy() {
        return ngayDangKy;
    }

    public void setNgayDangKy(Date ngayDangKy) {
        this.ngayDangKy = ngayDangKy;
    }

    public float getDiemTongKet() {
        return diemTongKet;
    }

    public void setDiemTongKet(float diemTongKet) {
        this.diemTongKet = diemTongKet;
    }

    public TrangThaiDangKy getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(TrangThaiDangKy trangThai) {
        this.trangThai = trangThai;
    }
}