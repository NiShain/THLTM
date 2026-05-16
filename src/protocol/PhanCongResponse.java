package protocol;

import bean.PhanCongChiTiet;
import java.io.Serializable;
import java.util.List;

/**
 * Response object gửi từ Server → Client qua TCP.
 * Chứa kết quả xử lý và file Excel (dạng byte[]).
 */
public class PhanCongResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean success;              // true = thành công
    private String message;               // Thông báo (lỗi hoặc thành công)
    private long dotPhanCongId;           // ID đợt phân công đã tạo

    private byte[] filePhanCong;          // Nội dung file DANHSACH PHANCONG.XLSX
    private String fileNamePhanCong;      // Tên file phân công

    private byte[] fileGiamSat;           // Nội dung file DANHSACH GIAMSAT.XLSX
    private String fileNameGiamSat;       // Tên file giám sát

    // Thống kê
    private int tongCanBo;               // Tổng cán bộ được sử dụng
    private int tongGiamThi;             // Tổng giám thị (2 * n)
    private int tongGiamSat;             // Tổng giám sát hành lang
    private int tongPhong;               // Tổng phòng thi

    // Kết quả chi tiết gửi về cho Client xem
    private List<PhanCongChiTiet> danhSachPhanCong;  // DS giám thị đã phân công
    private List<PhanCongChiTiet> danhSachGiamSat;   // DS giám sát hành lang
    private String tenDotActual;                      // Tên đợt thực tế (folder name)

    public PhanCongResponse() {
    }

    /** Tạo response lỗi nhanh */
    public static PhanCongResponse error(String message) {
        PhanCongResponse r = new PhanCongResponse();
        r.setSuccess(false);
        r.setMessage(message);
        return r;
    }

    /** Tạo response thành công nhanh */
    public static PhanCongResponse ok(String message) {
        PhanCongResponse r = new PhanCongResponse();
        r.setSuccess(true);
        r.setMessage(message);
        return r;
    }

    // --- Getters & Setters ---

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public long getDotPhanCongId() { return dotPhanCongId; }
    public void setDotPhanCongId(long dotPhanCongId) { this.dotPhanCongId = dotPhanCongId; }

    public byte[] getFilePhanCong() { return filePhanCong; }
    public void setFilePhanCong(byte[] filePhanCong) { this.filePhanCong = filePhanCong; }

    public String getFileNamePhanCong() { return fileNamePhanCong; }
    public void setFileNamePhanCong(String fileNamePhanCong) { this.fileNamePhanCong = fileNamePhanCong; }

    public byte[] getFileGiamSat() { return fileGiamSat; }
    public void setFileGiamSat(byte[] fileGiamSat) { this.fileGiamSat = fileGiamSat; }

    public String getFileNameGiamSat() { return fileNameGiamSat; }
    public void setFileNameGiamSat(String fileNameGiamSat) { this.fileNameGiamSat = fileNameGiamSat; }

    public int getTongCanBo() { return tongCanBo; }
    public void setTongCanBo(int tongCanBo) { this.tongCanBo = tongCanBo; }

    public int getTongGiamThi() { return tongGiamThi; }
    public void setTongGiamThi(int tongGiamThi) { this.tongGiamThi = tongGiamThi; }

    public int getTongGiamSat() { return tongGiamSat; }
    public void setTongGiamSat(int tongGiamSat) { this.tongGiamSat = tongGiamSat; }

    public int getTongPhong() { return tongPhong; }
    public void setTongPhong(int tongPhong) { this.tongPhong = tongPhong; }

    public List<PhanCongChiTiet> getDanhSachPhanCong() { return danhSachPhanCong; }
    public void setDanhSachPhanCong(List<PhanCongChiTiet> danhSachPhanCong) { this.danhSachPhanCong = danhSachPhanCong; }

    public List<PhanCongChiTiet> getDanhSachGiamSat() { return danhSachGiamSat; }
    public void setDanhSachGiamSat(List<PhanCongChiTiet> danhSachGiamSat) { this.danhSachGiamSat = danhSachGiamSat; }

    public String getTenDotActual() { return tenDotActual; }
    public void setTenDotActual(String tenDotActual) { this.tenDotActual = tenDotActual; }

    @Override
    public String toString() {
        return "PhanCongResponse{" +
                "success=" + success +
                ", message='" + message + '\'' +
                ", dotPhanCongId=" + dotPhanCongId +
                ", tongCanBo=" + tongCanBo +
                ", tongGiamThi=" + tongGiamThi +
                ", tongGiamSat=" + tongGiamSat +
                '}';
    }
}
