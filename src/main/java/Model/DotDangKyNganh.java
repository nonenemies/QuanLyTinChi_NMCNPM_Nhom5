package Model;

import java.time.LocalDateTime;

public class DotDangKyNganh {
    private Nganh nganh;
    private LocalDateTime moDangKy;
    private LocalDateTime dongDangKy;

    public DotDangKyNganh() {
    }

    public DotDangKyNganh(Nganh nganh, LocalDateTime moDangKy, LocalDateTime dongDangKy) {
        this.nganh = nganh;
        this.moDangKy = moDangKy;
        this.dongDangKy = dongDangKy;
    }

    public Nganh getNganh() {
        return nganh;
    }

    public void setNganh(Nganh nganh) {
        this.nganh = nganh;
    }

    public LocalDateTime getMoDangKy() {
        return moDangKy;
    }

    public void setMoDangKy(LocalDateTime moDangKy) {
        this.moDangKy = moDangKy;
    }

    public LocalDateTime getDongDangKy() {
        return dongDangKy;
    }

    public void setDongDangKy(LocalDateTime dongDangKy) {
        this.dongDangKy = dongDangKy;
    }
}
