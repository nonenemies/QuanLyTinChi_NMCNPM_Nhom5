package Model;

/** Quan hệ giữa một môn học và môn học bắt buộc phải đạt trước đó. */
public class MonTienQuyet {
    private final String maMon;
    private final MonHoc monTienQuyet;

    public MonTienQuyet(String maMon, MonHoc monTienQuyet) {
        this.maMon = maMon;
        this.monTienQuyet = monTienQuyet;
    }

    public String getMaMon() {
        return maMon;
    }

    public String getMaMonTienQuyet() {
        return monTienQuyet.getMaMon();
    }

    public MonHoc getMonTienQuyet() {
        return monTienQuyet;
    }

    public String getTenMonTienQuyet() {
        return monTienQuyet.getTenMon();
    }
}
