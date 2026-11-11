package Model;

/** Model ánh xạ tới bảng ChuongTrinhDaoTao trong database. */
public class ChuongTrinhDaoTao {
    private String nganh;
    private String maMon;

    public ChuongTrinhDaoTao() {
    }

    public ChuongTrinhDaoTao(String nganh, String maMon) {
        this.nganh = nganh;
        this.maMon = maMon;
    }

    public String getNganh() {
        return nganh;
    }

    public void setNganh(String nganh) {
        this.nganh = nganh;
    }

    public String getMaMon() {
        return maMon;
    }

    public void setMaMon(String maMon) {
        this.maMon = maMon;
    }
}
