package bean;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * BEAN - Entity tương ứng bảng phan_cong_chi_tiet
 */
public class PhanCongChiTiet implements Serializable {
    private static final long serialVersionUID = 1L;

    /** Các vai trò (role) trong phân công */
    public static final String ROLE_GIAMTHI1 = "GIAMTHI1";
    public static final String ROLE_GIAMTHI2 = "GIAMTHI2";
    public static final String ROLE_GIAMSAT  = "GIAMSAT";

    private long id;
    private long dotPhanCongId;          // FK → dot_phan_cong
    private long phongThiPhanCongId;     // FK → phong_thi_phan_cong
    private long canBoId;                // FK → can_bo_coi_thi
    private String role;                 // GIAMTHI1, GIAMTHI2, GIAMSAT
    private int viTriTrongPhong;         // Vị trí trong phòng (1, 2, 3...)
    private String rangeText;            // Phạm vi giám sát (cho GIAMSAT)
    private Timestamp createdAt;

    // Transient - dùng khi lập báo cáo
    private String maGV;
    private String hoTen;
    private String tenPhong;

    public PhanCongChiTiet() {
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public long getDotPhanCongId() { return dotPhanCongId; }
    public void setDotPhanCongId(long dotPhanCongId) { this.dotPhanCongId = dotPhanCongId; }

    public long getPhongThiPhanCongId() { return phongThiPhanCongId; }
    public void setPhongThiPhanCongId(long phongThiPhanCongId) { this.phongThiPhanCongId = phongThiPhanCongId; }

    public long getCanBoId() { return canBoId; }
    public void setCanBoId(long canBoId) { this.canBoId = canBoId; }

    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }

    public int getViTriTrongPhong() { return viTriTrongPhong; }
    public void setViTriTrongPhong(int viTriTrongPhong) { this.viTriTrongPhong = viTriTrongPhong; }

    public String getRangeText() { return rangeText; }
    public void setRangeText(String rangeText) { this.rangeText = rangeText; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public String getMaGV() { return maGV; }
    public void setMaGV(String maGV) { this.maGV = maGV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public String getTenPhong() { return tenPhong; }
    public void setTenPhong(String tenPhong) { this.tenPhong = tenPhong; }

    @Override
    public String toString() {
        return "PhanCongChiTiet{" +
                "id=" + id +
                ", role='" + role + '\'' +
                ", viTriTrongPhong=" + viTriTrongPhong +
                ", maGV='" + maGV + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", tenPhong='" + tenPhong + '\'' +
                '}';
    }
}
