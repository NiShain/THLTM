package bean;

import java.io.Serializable;
import java.sql.Date;
import java.sql.Timestamp;

/**
 * BEAN - Entity tương ứng bảng can_bo_coi_thi
 */
public class CanBoCoiThi implements Serializable {
    private static final long serialVersionUID = 1L;

    private long id;
    private int ttNguon;           // TT nguồn từ Excel
    private String maGV;           // Mã giảng viên
    private String hoTen;          // Họ và tên
    private Date ngaySinh;         // Ngày sinh
    private String donViCongTac;   // Đơn vị công tác
    private int sourceRow;         // Dòng gốc trong Excel
    private Timestamp createdAt;

    public CanBoCoiThi() {
    }

    public CanBoCoiThi(int ttNguon, String maGV, String hoTen, Date ngaySinh, String donViCongTac, int sourceRow) {
        this.ttNguon = ttNguon;
        this.maGV = maGV;
        this.hoTen = hoTen;
        this.ngaySinh = ngaySinh;
        this.donViCongTac = donViCongTac;
        this.sourceRow = sourceRow;
    }

    // --- Getters & Setters ---

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public int getTtNguon() { return ttNguon; }
    public void setTtNguon(int ttNguon) { this.ttNguon = ttNguon; }

    public String getMaGV() { return maGV; }
    public void setMaGV(String maGV) { this.maGV = maGV; }

    public String getHoTen() { return hoTen; }
    public void setHoTen(String hoTen) { this.hoTen = hoTen; }

    public Date getNgaySinh() { return ngaySinh; }
    public void setNgaySinh(Date ngaySinh) { this.ngaySinh = ngaySinh; }

    public String getDonViCongTac() { return donViCongTac; }
    public void setDonViCongTac(String donViCongTac) { this.donViCongTac = donViCongTac; }

    public int getSourceRow() { return sourceRow; }
    public void setSourceRow(int sourceRow) { this.sourceRow = sourceRow; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }

    @Override
    public String toString() {
        return "CanBoCoiThi{" +
                "id=" + id +
                ", ttNguon=" + ttNguon +
                ", maGV='" + maGV + '\'' +
                ", hoTen='" + hoTen + '\'' +
                ", ngaySinh=" + ngaySinh +
                ", donViCongTac='" + donViCongTac + '\'' +
                '}';
    }
}
