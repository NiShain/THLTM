package bean;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * BEAN - Entity tương ứng bảng dot_phan_cong
 */
public class DotPhanCong implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private String maDot;              // Mã đợt (auto-gen)
    private String tenDot;             // Tên đợt phân công
    private int soGiamThi;             // m - số giám thị cần dùng
    private int soPhongThi;            // n - số phòng thi
    private String fileXlsxCanBo;      // Tên file nguồn cán bộ
    private String fileXlsxPhongThi;   // Tên file nguồn phòng thi
    private String trangThai;          // NEW, PROCESSING, DONE, ERROR
    private Timestamp createdAt;
    private Timestamp updatedAt;

    public DotPhanCong() {
        this.trangThai = "NEW";
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getMaDot() { return maDot; }
    public void setMaDot(String maDot) { this.maDot = maDot; }

    public String getTenDot() { return tenDot; }
    public void setTenDot(String tenDot) { this.tenDot = tenDot; }

    public int getSoGiamThi() { return soGiamThi; }
    public void setSoGiamThi(int soGiamThi) { this.soGiamThi = soGiamThi; }

    public int getSoPhongThi() { return soPhongThi; }
    public void setSoPhongThi(int soPhongThi) { this.soPhongThi = soPhongThi; }

    public String getFileXlsxCanBo() { return fileXlsxCanBo; }
    public void setFileXlsxCanBo(String fileXlsxCanBo) { this.fileXlsxCanBo = fileXlsxCanBo; }

    public String getFileXlsxPhongThi() { return fileXlsxPhongThi; }
    public void setFileXlsxPhongThi(String fileXlsxPhongThi) { this.fileXlsxPhongThi = fileXlsxPhongThi; }

    public String getTrangThai() { return trangThai; }
    public void setTrangThai(String trangThai) { this.trangThai = trangThai; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    public Timestamp getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(Timestamp updatedAt) { this.updatedAt = updatedAt; }

    @Override
    public String toString() {
        return "DotPhanCong{" +
                "id=" + id +
                ", maDot='" + maDot + '\'' +
                ", tenDot='" + tenDot + '\'' +
                ", soGiamThi=" + soGiamThi +
                ", soPhongThi=" + soPhongThi +
                ", trangThai='" + trangThai + '\'' +
                '}';
    }
}
