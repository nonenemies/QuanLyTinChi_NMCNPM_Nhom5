package Model;

public class ThanhPhanDanhGia {
    private int maTP;
    private String maMon;
    private String tenTP;
    private float trongSo;
    private int thuTu;
    private boolean trangThai = true;

    public ThanhPhanDanhGia() {
    }

    public ThanhPhanDanhGia(int maTP, String maMon, String tenTP, float trongSo,
                           int thuTu, boolean trangThai) {
        this.maTP = maTP;
        this.maMon = maMon;
        this.tenTP = tenTP;
        this.trongSo = trongSo;
        this.thuTu = thuTu;
        this.trangThai = trangThai;
    }

    public int getMaTP() {
        return maTP;
    }

    public void setMaTP(int maTP) {
        this.maTP = maTP;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }

    public String getTenTP() {
        return tenTP;
    }

    public void setTenTP(String tenTP) {
        this.tenTP = tenTP;
    }

    public float getTrongSo() {
        return trongSo;
    }

    public void setTrongSo(float trongSo) {
        this.trongSo = trongSo;
    }

    public int getThuTu() {
        return thuTu;
    }

    public void setThuTu(int thuTu) {
        this.thuTu = thuTu;
    }

    public boolean getTrangThai() {
        return trangThai;
    }

    public void setTrangThai(boolean trangThai) {
        this.trangThai = trangThai;
    }
}
