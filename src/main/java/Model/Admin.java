package Model;

import java.util.Date;

public class Admin extends Users {
        private String chucVu;

        public Admin(Integer userID, String hoTen, String email,
                     String passwordHash, String role, String maSo,
                     String khoa, Date ngayTao, TrangThaiHoatDong trangThai, String chucVu) {
                super(userID, hoTen, email, passwordHash, role, maSo, khoa, ngayTao, trangThai);
                this.chucVu = chucVu;
        }

        public String getChucVu() {
                return chucVu;
        }

        public void setChucVu(String chucVu) {
                this.chucVu = chucVu;
        }
}
