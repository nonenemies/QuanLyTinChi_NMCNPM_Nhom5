package Services;

import DAO.UsersDAO;
import Model.Users;

public class AuthService {
    private final UsersDAO usersDAO = new UsersDAO();

    // Hàm này check DB. Nếu đúng trả về Object người dùng, sai trả về null
    public Users login(String email, String password) {
        try {
            Users user = usersDAO.layTheoEmailDangHoatDong(email);
            return user != null && user.getPasswordHash().equals(password) ? user : null;
        } catch (java.sql.SQLException e) {
            throw new ServiceException("Không thể xác thực tài khoản.", e);
        }
    }
}