package Model;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class CauHinhHocKy {
    private String hocKy;
    private LocalDate ketThucHocKy;
    private LocalDateTime hanNhapDiem;

    public CauHinhHocKy() {
    }

    public CauHinhHocKy(String hocKy, LocalDate ketThucHocKy, LocalDateTime hanNhapDiem) {
        this.hocKy = hocKy;
        this.ketThucHocKy = ketThucHocKy;
        this.hanNhapDiem = hanNhapDiem;
    }

    public String getHocKy() {
        return hocKy;
    }

    public void setHocKy(String hocKy) {
        this.hocKy = hocKy;
    }

    public LocalDate getKetThucHocKy() {
        return ketThucHocKy;
    }

    public void setKetThucHocKy(LocalDate ketThucHocKy) {
        this.ketThucHocKy = ketThucHocKy;
    }

    public LocalDateTime getHanNhapDiem() {
        return hanNhapDiem;
    }

    public void setHanNhapDiem(LocalDateTime hanNhapDiem) {
        this.hanNhapDiem = hanNhapDiem;
    }
}
