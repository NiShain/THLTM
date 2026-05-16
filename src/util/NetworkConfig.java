package util;

/**
 * Cấu hình mạng TCP - Thay đổi tại đây khi kết nối giữa 2 laptop.
 * 
 * Khi test trên localhost: SERVER_HOST = "localhost"
 * Khi kết nối 2 máy: SERVER_HOST = IP của máy chạy Server (ví dụ "192.168.1.100")
 */
public class NetworkConfig {

    // ==================== CẤU HÌNH MẠNG ====================
    // Sửa SERVER_HOST thành IP máy server khi kết nối 2 laptop
    public static final String SERVER_HOST = "localhost";
    public static final int    SERVER_PORT = 9999;
    // ========================================================

    // Kích thước buffer cho đọc/ghi stream
    public static final int BUFFER_SIZE = 8192;

    // Timeout kết nối (ms) - 30 giây
    public static final int CONNECTION_TIMEOUT = 30_000;

    // Timeout đọc dữ liệu (ms) - 60 giây
    public static final int READ_TIMEOUT = 60_000;

    private NetworkConfig() {
        // Utility class
    }
}
