package Model;
import java.util.Date;
import java.text.SimpleDateFormat;

public class YeuCauDoiLop {
    private int maYeuCau;
    private String maSV;
    private String lopCu;
    private String lopMoi;
    private TrangThaiDoiLop trangThai;
    private String lyDo;
    private Date ngayTao;
    private Date ngayXuLy;
    private int nguoiXuLy; // Dùng lưu ID của người Chốt đơn
    private String tenSinhVien;
    private String tenLopCu;
    private String tenLopMoi;

    public YeuCauDoiLop() {}

    public YeuCauDoiLop(String maSV, String lopCu, String lopMoi, String lyDo) {
        this.maSV = maSV;
        this.lopCu = lopCu;
        this.lopMoi = lopMoi;
        this.lyDo = lyDo;
    }

    public int getMaYeuCau() { return maYeuCau; }
    public void setMaYeuCau(int maYeuCau) { this.maYeuCau = maYeuCau; }
    public String getMaSV() { return maSV; }
    public void setMaSV(String maSV) { this.maSV = maSV; }
    public String getLopCu() { return lopCu; }
    public void setLopCu(String lopCu) { this.lopCu = lopCu; }
    public String getLopMoi() { return lopMoi; }
    public void setLopMoi(String lopMoi) { this.lopMoi = lopMoi; }
    public TrangThaiDoiLop getTrangThai() { return trangThai; }
    public void setTrangThai(TrangThaiDoiLop trangThai) { this.trangThai = trangThai; }
    public String getLyDo() { return lyDo; }
    public void setLyDo(String lyDo) { this.lyDo = lyDo; }
    public Date getNgayTao() { return ngayTao; }
    public void setNgayTao(Date ngayTao) { this.ngayTao = ngayTao; }
    public Date getNgayXuLy() { return ngayXuLy; }
    public void setNgayXuLy(Date ngayXuLy) { this.ngayXuLy = ngayXuLy; }
    public int getNguoiXuLy() { return nguoiXuLy; }
    public void setNguoiXuLy(int nguoiXuLy) { this.nguoiXuLy = nguoiXuLy; }
    public String getTenSinhVien() { return tenSinhVien; }
    public void setTenSinhVien(String tenSinhVien) { this.tenSinhVien = tenSinhVien; }
    public String getSinhVienHienThi() { return tenSinhVien + " · " + maSV; }
    public String getTenLopCu() { return tenLopCu; }
    public void setTenLopCu(String tenLopCu) { this.tenLopCu = tenLopCu; }
    public String getTenLopMoi() { return tenLopMoi; }
    public void setTenLopMoi(String tenLopMoi) { this.tenLopMoi = tenLopMoi; }
    public String getTrangThaiHienThi() {
        if (trangThai == null) return "Không rõ";
        switch (trangThai) {
            case CHO_XAC_NHAN: return "Đang chờ";
            case DA_DUYET: return "Đã trao đổi";
            case TU_CHOI: return "Bị từ chối";
            case HUY: return "Đã hủy";
            default: return trangThai.name();
        }
    }
    public String getNgayTaoHienThi() {
        return ngayTao == null ? "—" : new SimpleDateFormat("dd/MM/yyyy HH:mm").format(ngayTao);
    }
}