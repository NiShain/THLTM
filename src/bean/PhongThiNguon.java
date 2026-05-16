package bean;

import java.io.Serializable;
import java.sql.Timestamp;

/**
 * BEAN - Entity tương ứng bảng phong_thi_nguon
 */
public class PhongThiNguon implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private int sttNguon;          // STT nguồn từ Excel
    private String phongThi;       // Tên/mã phòng thi
    private String ghiChu;         // Ghi chú
    private int sourceRow;         // Dòng gốc trong Excel
    private Timestamp createdAt;

    public PhongThiNguon() {
    }

    public PhongThiNguon(int sttNguon, String phongThi, String ghiChu, int sourceRow) {
        this.sttNguon = sttNguon;
        this.phongThi = phongThi;
        this.ghiChu = ghiChu;
        this.sourceRow = sourceRow;
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getSttNguon() { return sttNguon; }
    public void setSttNguon(int sttNguon) { this.sttNguon = sttNguon; }

    public String getPhongThi() { return phongThi; }
    public void setPhongThi(String phongThi) { this.phongThi = phongThi; }

    public String getGhiChu() { return ghiChu; }
    public void setGhiChu(String ghiChu) { this.ghiChu = ghiChu; }

    public int getSourceRow() { return sourceRow; }
    public void setSourceRow(int sourceRow) { this.sourceRow = sourceRow; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "PhongThiNguon{" +
                "id=" + id +
                ", sttNguon=" + sttNguon +
                ", phongThi='" + phongThi + '\'' +
                ", ghiChu='" + ghiChu + '\'' +
                '}';
    }
}
