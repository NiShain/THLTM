package dao;

import bean.DotPhanCong;
import util.DatabaseUtil;

import java.sql.*;

/**
 * DAO - Thao tác CRUD bảng dot_phan_cong
 */
public class DotPhanCongDAO {

    /**
     * Tạo một đợt phân công mới. Trả về id vừa tạo.
     */
    public long insert(DotPhanCong dot) throws SQLException {
        String sql = "INSERT INTO dot_phan_cong (ma_dot, ten_dot, so_giam_thi, so_phong_thi, "
                   + "file_xlsx_can_bo, file_xlsx_phong_thi, trang_thai) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setString(1, dot.getMaDot());
            ps.setString(2, dot.getTenDot());
            ps.setInt(3, dot.getSoGiamThi());
            ps.setInt(4, dot.getSoPhongThi());
            ps.setString(5, dot.getFileXlsxCanBo());
            ps.setString(6, dot.getFileXlsxPhongThi());
            ps.setString(7, dot.getTrangThai());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    dot.setId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    /**
     * Lấy đợt phân công DONE gần nhất (theo created_at). Trả về null nếu không có.
     */
    public DotPhanCong findLastDoneDot() throws SQLException {
        String sql = "SELECT * FROM dot_phan_cong WHERE trang_thai = 'DONE' ORDER BY created_at DESC LIMIT 1";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Cập nhật trạng thái đợt phân công.
     */
    public void updateTrangThai(long dotId, String trangThai) throws SQLException {
        String sql = "UPDATE dot_phan_cong SET trang_thai = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, trangThai);
            ps.setLong(2, dotId);
            ps.executeUpdate();
        }
    }

    /**
     * Tìm đợt phân công theo id.
     */
    public DotPhanCong findById(long id) throws SQLException {
        String sql = "SELECT * FROM dot_phan_cong WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    /**
     * Tìm đợt phân công theo tên đợt.
     * Dùng để kiểm tra trùng tên.
     */
    public DotPhanCong findByTenDot(String tenDot) throws SQLException {
        String sql = "SELECT * FROM dot_phan_cong WHERE ten_dot = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, tenDot);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    private DotPhanCong mapRow(ResultSet rs) throws SQLException {
        DotPhanCong d = new DotPhanCong();
        d.setId(rs.getLong("id"));
        d.setMaDot(rs.getString("ma_dot"));
        d.setTenDot(rs.getString("ten_dot"));
        d.setSoGiamThi(rs.getInt("so_giam_thi"));
        d.setSoPhongThi(rs.getInt("so_phong_thi"));
        d.setFileXlsxCanBo(rs.getString("file_xlsx_can_bo"));
        d.setFileXlsxPhongThi(rs.getString("file_xlsx_phong_thi"));
        d.setTrangThai(rs.getString("trang_thai"));
        d.setCreatedAt(rs.getTimestamp("created_at"));
        d.setUpdatedAt(rs.getTimestamp("updated_at"));
        return d;
    }
}
