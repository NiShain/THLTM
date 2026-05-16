package dao;

import bean.PhongThiPhanCong;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO - Thao tác CRUD bảng phong_thi_phan_cong
 */
public class PhongThiPhanCongDAO {

    /**
     * Thêm một phòng thi vào đợt phân công.
     */
    public long insert(PhongThiPhanCong ptpc) throws SQLException {
        String sql = "INSERT INTO phong_thi_phan_cong (dot_phan_cong_id, phong_thi_nguon_id, so_phong, so_luong_can_bo) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, ptpc.getDotPhanCongId());
            ps.setLong(2, ptpc.getPhongThiNguonId());
            ps.setInt(3, ptpc.getSoPhong());
            ps.setInt(4, ptpc.getSoLuongCanBo());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    ptpc.setId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    /**
     * Cập nhật số lượng cán bộ sau khi phân công.
     */
    public void updateSoLuongCanBo(long id, int soLuong) throws SQLException {
        String sql = "UPDATE phong_thi_phan_cong SET so_luong_can_bo = ? WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, soLuong);
            ps.setLong(2, id);
            ps.executeUpdate();
        }
    }

    /**
     * Lấy danh sách phòng thi theo đợt phân công (có JOIN tên phòng).
     */
    public List<PhongThiPhanCong> findByDotId(long dotId) throws SQLException {
        String sql = "SELECT p.*, n.phong_thi AS ten_phong FROM phong_thi_phan_cong p "
                   + "JOIN phong_thi_nguon n ON p.phong_thi_nguon_id = n.id "
                   + "WHERE p.dot_phan_cong_id = ? ORDER BY p.so_phong";
        List<PhongThiPhanCong> result = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    result.add(mapRow(rs));
                }
            }
        }
        return result;
    }

    private PhongThiPhanCong mapRow(ResultSet rs) throws SQLException {
        PhongThiPhanCong p = new PhongThiPhanCong();
        p.setId(rs.getLong("id"));
        p.setDotPhanCongId(rs.getLong("dot_phan_cong_id"));
        p.setPhongThiNguonId(rs.getLong("phong_thi_nguon_id"));
        p.setSoPhong(rs.getInt("so_phong"));
        p.setSoLuongCanBo(rs.getInt("so_luong_can_bo"));
        p.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            p.setTenPhong(rs.getString("ten_phong"));
        } catch (SQLException ignored) {
            // Column may not exist if no JOIN
        }
        return p;
    }
}
