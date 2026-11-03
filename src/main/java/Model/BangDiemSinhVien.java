package Model;

import java.util.LinkedHashMap;
import java.util.Map;

/** Dữ liệu một sinh viên dùng để hiển thị bảng điểm của lớp học phần. */
public class BangDiemSinhVien {
    private final int maDangKy;
    private final String maSV;
    private final String hoTen;
    private final TrangThaiDangKy trangThai;
    private final Map<Integer, Float> diemThanhPhan = new LinkedHashMap<>();
    private Float diemTongKet;

    public BangDiemSinhVien(int maDangKy, String maSV, String hoTen,
                            Float diemTongKet, TrangThaiDangKy trangThai) {
        this.maDangKy = maDangKy;
        this.maSV = maSV;
        this.hoTen = hoTen;
        this.diemTongKet = diemTongKet;
        this.trangThai = trangThai;
    }

    public int getMaDangKy() {
        return maDangKy;
    }

    public String getMaSV() {
        return maSV;
    }

    public String getHoTen() {
        return hoTen;
    }

    public Float getDiemTongKet() {
        return diemTongKet;
    }

    public TrangThaiDangKy getTrangThai() {
        return trangThai;
    }

    public Float getDiemThanhPhan(int maTP) {
        return diemThanhPhan.get(maTP);
    }

    public void setDiemThanhPhan(int maTP, Float diem) {
        if (diem != null) {
            diemThanhPhan.put(maTP, diem);
        }
    }
}
