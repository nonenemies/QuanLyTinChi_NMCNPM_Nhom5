package Model;

public class LopHocThoiKhoaBieu {
    private final String maLop;
    private final String maMon;
    private final String tenMon;
    private final String hocKy;
    private final int thu;
    private final String gioBatDau;
    private final String gioKetThuc;
    private final String phongHoc;

    public LopHocThoiKhoaBieu(String maLop, String maMon, String tenMon, String hocKy,
                              int thu, String gioBatDau, String gioKetThuc) {
        this(maLop, maMon, tenMon, hocKy, thu, gioBatDau, gioKetThuc, null);
    }

    public LopHocThoiKhoaBieu(String maLop, String maMon, String tenMon, String hocKy,
                              int thu, String gioBatDau, String gioKetThuc, String phongHoc) {
        this.maLop = maLop;
        this.maMon = maMon;
        this.tenMon = tenMon;
        this.hocKy = hocKy;
        this.thu = thu;
        this.gioBatDau = gioBatDau;
        this.gioKetThuc = gioKetThuc;
        this.phongHoc = phongHoc;
    }

    public String getMaLop() { return maLop; }
    public String getMaMon() { return maMon; }
    public String getTenMon() { return tenMon; }
    public String getHocKy() { return hocKy; }
    public int getThu() { return thu; }
    public String getGioBatDau() { return gioBatDau; }
    public String getGioKetThuc() { return gioKetThuc; }
    public String getPhongHoc() { return phongHoc; }
}
