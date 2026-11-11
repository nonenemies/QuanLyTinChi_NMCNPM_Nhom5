package Model;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record CauHinhHocKy(String hocKy, LocalDate ketThucHocKy,
                           LocalDateTime hanNhapDiem) {
}
