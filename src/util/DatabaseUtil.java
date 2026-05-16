package util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Tiện ích kết nối MySQL.
 * 
 * ĐỔI THÔNG SỐ KẾT NỐI TẠI ĐÂY khi chuyển sang máy khác.
 * Chỉ cần sửa 4 hằng số: URL, USER, PASSWORD, DRIVER.
 */
public class DatabaseUtil {

    // ==================== CẤU HÌNH KẾT NỐI ====================
    // Sửa các giá trị dưới đây khi deploy trên máy khác
    private static final String URL      = "jdbc:mysql://localhost:3306/thi_thltm?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Ho_Chi_Minh&characterEncoding=utf8";
    private static final String USER     = "root";
    private static final String PASSWORD = "th@i1317711";  // Đổi password tại đây
    private static final String DRIVER   = "com.mysql.cj.jdbc.Driver";
    // ===========================================================

    static {
        try {
            Class.forName(DRIVER);
            System.out.println("[DatabaseUtil] MySQL Driver loaded OK.");
        } catch (ClassNotFoundException e) {
            System.err.println("[DatabaseUtil] KHÔNG TÌM THẤY MySQL Driver: " + e.getMessage());
            System.err.println("[DatabaseUtil] Hãy thêm mysql-connector-j-*.jar vào classpath.");
        }
    }

    /**
     * Mở một connection mới tới MySQL.
     * Caller có trách nhiệm đóng connection sau khi dùng xong.
     */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    /**
     * Đóng connection an toàn (không throw exception).
     */
    public static void closeQuietly(AutoCloseable... closeables) {
        for (AutoCloseable c : closeables) {
            if (c != null) {
                try { c.close(); } catch (Exception ignored) { }
            }
        }
    }

    /**
     * Test kết nối - gọi để kiểm tra trước khi chạy.
     */
    public static boolean testConnection() {
        try (Connection conn = getConnection()) {
            System.out.println("[DatabaseUtil] Kết nối MySQL thành công!");
            return true;
        } catch (SQLException e) {
            System.err.println("[DatabaseUtil] Kết nối THẤT BẠI: " + e.getMessage());
            return false;
        }
    }
}
