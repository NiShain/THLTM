package bean;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * BEAN - Entity tương ứng bảng phong_thi_phan_cong
 */
public class PhongThiPhanCong implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private long dotPhanCongId;        // FK → dot_phan_cong
    private long phongThiNguonId;      // FK → phong_thi_nguon
    private int soPhong;               // Số thứ tự phòng trong đợt
    private int soLuongCanBo;          // Tổng số cán bộ gán cho phòng
    private Timestamp createdAt;

    // Transient (không lưu DB, chỉ dùng runtime)
    private String tenPhong;           // Tên phòng (lấy từ join)

    public PhongThiPhanCong() {
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getDotPhanCongId() { return dotPhanCongId; }
    public void setDotPhanCongId(long dotPhanCongId) { this.dotPhanCongId = dotPhanCongId; }

    public long getPhongThiNguonId() { return phongThiNguonId; }
    public void setPhongThiNguonId(long phongThiNguonId) { this.phongThiNguonId = phongThiNguonId; }

    public int getSoPhong() { return soPhong; }
    public void setSoPhong(int soPhong) { this.soPhong = soPhong; }

    public int getSoLuongCanBo() { return soLuongCanBo; }
    public void setSoLuongCanBo(int soLuongCanBo) { this.soLuongCanBo = soLuongCanBo; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    @Override
    public String toString() {
        return "PhongThiPhanCong{" +
                "id=" + id +
                ", dotPhanCongId=" + dotPhanCongId +
                ", soPhong=" + soPhong +
                ", soLuongCanBo=" + soLuongCanBo +
                ", tenPhong='" + tenPhong + '\'' +
                '}';
    }
}
