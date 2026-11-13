package Model;

/** Model ánh xạ tới bảng ChuongTrinhDaoTao trong database. */
public class ChuongTrinhDaoTao {
    private Nganh nganh;
    private String maMon;

    public ChuongTrinhDaoTao() {
    }

    public ChuongTrinhDaoTao(Nganh nganh, String maMon) {
        this.nganh = nganh;
        this.maMon = maMon;
    }

    public Nganh getNganh() {
        return nganh;
    }

    public void setNganh(Nganh nganh) {
        this.nganh = nganh;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }
}
