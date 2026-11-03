package Util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {
    private static final String DEFAULT_URL = "jdbc:mysql://localhost:3306/QuanLyTinChi";

    public static Connection getConnection() throws SQLException {
        String url = System.getenv().getOrDefault("DB_URL", DEFAULT_URL);
        String user = System.getenv("DB_USER");
        String password = System.getenv("DB_PASSWORD");
        if (user == null || user.isBlank() || password == null || password.isBlank()) {
            throw new SQLException("Cần cấu hình biến môi trường DB_USER và DB_PASSWORD.");
        }

        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            throw new SQLException("Không tìm thấy MySQL JDBC driver.", e);
        }
        return DriverManager.getConnection(url, user, password);
    }
}