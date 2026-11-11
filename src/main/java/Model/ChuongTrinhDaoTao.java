package Model;

/** Model ánh xạ tới bảng ChuongTrinhDaoTao trong database. */
public class ChuongTrinhDaoTao {
    private String khoa;
    private String maMon;

    public ChuongTrinhDaoTao() {
    }

    public ChuongTrinhDaoTao(String khoa, String maMon) {
        this.khoa = khoa;
        this.maMon = maMon;
    }

    public String getKhoa() {
        return khoa;
    }

    public void setKhoa(String khoa) {
        this.khoa = khoa;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }
}
