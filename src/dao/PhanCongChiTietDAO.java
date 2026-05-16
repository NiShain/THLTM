package dao;

import bean.PhanCongChiTiet;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO - Thao tác CRUD bảng phan_cong_chi_tiet
 */
public class PhanCongChiTietDAO {

    /**
     * Thêm một bản ghi phân công chi tiết.
     */
    public long insert(PhanCongChiTiet pc) throws SQLException {
        String sql = "INSERT INTO phan_cong_chi_tiet "
                   + "(dot_phan_cong_id, phong_thi_phan_cong_id, can_bo_id, role, vi_tri_trong_phong, range_text) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setLong(1, pc.getDotPhanCongId());
            ps.setLong(2, pc.getPhongThiPhanCongId());
            ps.setLong(3, pc.getCanBoId());
            ps.setString(4, pc.getRole());
            ps.setInt(5, pc.getViTriTrongPhong());
            ps.setString(6, pc.getRangeText());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    pc.setId(newId);
                    return newId;
                }
            }
        }
        return -1;
    }

    /**
     * Thêm batch danh sách phân công.
     */
    public void insertBatch(List<PhanCongChiTiet> list) throws SQLException {
        String sql = "INSERT INTO phan_cong_chi_tiet "
                   + "(dot_phan_cong_id, phong_thi_phan_cong_id, can_bo_id, role, vi_tri_trong_phong, range_text) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (PhanCongChiTiet pc : list) {
                ps.setLong(1, pc.getDotPhanCongId());
                ps.setLong(2, pc.getPhongThiPhanCongId());
                ps.setLong(3, pc.getCanBoId());
                ps.setString(4, pc.getRole());
                ps.setInt(5, pc.getViTriTrongPhong());
                ps.setString(6, pc.getRangeText());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }

    /**
     * Lấy danh sách phân công giám thị theo đợt (có JOIN tên cán bộ + phòng).
     */
    public List<PhanCongChiTiet> findGiamThiByDotId(long dotId) throws SQLException {
        String sql = "SELECT pc.*, cb.ma_gv, cb.ho_ten, ptn.phong_thi AS ten_phong "
                   + "FROM phan_cong_chi_tiet pc "
                   + "JOIN can_bo_coi_thi cb ON pc.can_bo_id = cb.id "
                   + "JOIN phong_thi_phan_cong ptpc ON pc.phong_thi_phan_cong_id = ptpc.id "
                   + "JOIN phong_thi_nguon ptn ON ptpc.phong_thi_nguon_id = ptn.id "
                   + "WHERE pc.dot_phan_cong_id = ? AND pc.role IN ('GIAMTHI1','GIAMTHI2') "
                   + "ORDER BY ptpc.so_phong, pc.vi_tri_trong_phong";
        return executeQuery(sql, dotId);
    }

    /**
     * Lấy danh sách phân công giám sát hành lang theo đợt.
     */
    public List<PhanCongChiTiet> findGiamSatByDotId(long dotId) throws SQLException {
        String sql = "SELECT pc.*, cb.ma_gv, cb.ho_ten, ptn.phong_thi AS ten_phong "
                   + "FROM phan_cong_chi_tiet pc "
                   + "JOIN can_bo_coi_thi cb ON pc.can_bo_id = cb.id "
                   + "JOIN phong_thi_phan_cong ptpc ON pc.phong_thi_phan_cong_id = ptpc.id "
                   + "JOIN phong_thi_nguon ptn ON ptpc.phong_thi_nguon_id = ptn.id "
                   + "WHERE pc.dot_phan_cong_id = ? AND pc.role = 'GIAMSAT' "
                   + "ORDER BY pc.vi_tri_trong_phong";
        return executeQuery(sql, dotId);
    }

    private List<PhanCongChiTiet> executeQuery(String sql, long dotId) throws SQLException {
        List<PhanCongChiTiet> result = new ArrayList<>();
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

    private PhanCongChiTiet mapRow(ResultSet rs) throws SQLException {
        PhanCongChiTiet pc = new PhanCongChiTiet();
        pc.setId(rs.getLong("id"));
        pc.setDotPhanCongId(rs.getLong("dot_phan_cong_id"));
        pc.setPhongThiPhanCongId(rs.getLong("phong_thi_phan_cong_id"));
        pc.setCanBoId(rs.getLong("can_bo_id"));
        pc.setRole(rs.getString("role"));
        pc.setViTriTrongPhong(rs.getInt("vi_tri_trong_phong"));
        pc.setRangeText(rs.getString("range_text"));
        pc.setCreatedAt(rs.getTimestamp("created_at"));
        try {
            pc.setMaGV(rs.getString("ma_gv"));
            pc.setHoTen(rs.getString("ho_ten"));
            pc.setTenPhong(rs.getString("ten_phong"));
        } catch (SQLException ignored) {
            // Columns may not exist if no JOIN
        }
        return pc;
    }
}
