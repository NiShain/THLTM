package protocol;

import bean.CanBoCoiThi;
import bean.PhongThiNguon;

import java.io.Serializable;
import java.util.List;

/**
 * Request object gửi từ Client → Server qua TCP.
 * Chứa tất cả dữ liệu cần thiết để Server thực hiện phân công.
 */
public class PhanCongRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    private int soGiamThi;                     // m - số giám thị cần dùng
    private int soPhongThi;                    // n - số phòng thi cần dùng
    private String tenDot;                     // Tên đợt phân công
    private String fileNameCanBo;              // Tên file nguồn cán bộ
    private List<CanBoCoiThi> danhSachCanBo;   // Danh sách cán bộ đọc từ Excel
    private List<PhongThiNguon> danhSachPhong; // Danh sách phòng thi đọc từ Excel

    public PhanCongRequest() {
    }

    // --- Getters & Setters ---

    public int getSoGiamThi() { return soGiamThi; }
    public void setSoGiamThi(int soGiamThi) { this.soGiamThi = soGiamThi; }

    public int getSoPhongThi() { return soPhongThi; }
    public void setSoPhongThi(int soPhongThi) { this.soPhongThi = soPhongThi; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public String getFileNameCanBo() { return fileNameCanBo; }
    public void setFileNameCanBo(String fileNameCanBo) { this.fileNameCanBo = fileNameCanBo; }

    public List<CanBoCoiThi> getDanhSachCanBo() { return danhSachCanBo; }
    public void setDanhSachCanBo(List<CanBoCoiThi> danhSachCanBo) { this.danhSachCanBo = danhSachCanBo; }

    public List<PhongThiNguon> getDanhSachPhong() { return danhSachPhong; }
    public void setDanhSachPhong(List<PhongThiNguon> danhSachPhong) { this.danhSachPhong = danhSachPhong; }

    @Override
    public String toString() {
        return "PhanCongRequest{" +
                "soGiamThi=" + soGiamThi +
                ", soPhongThi=" + soPhongThi +
                ", tenDot='" + tenDot + '\'' +
                ", canBo=" + (danhSachCanBo != null ? danhSachCanBo.size() : 0) +
                ", phong=" + (danhSachPhong != null ? danhSachPhong.size() : 0) +
                '}';
    }
}
