package dao;

import bean.CanBoCoiThi;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO - Thao tác CRUD bảng can_bo_coi_thi
 */
public class CanBoCoiThiDAO {

    /**
     * Thêm một cán bộ vào DB. Nếu mã GV đã tồn tại thì bỏ qua (INSERT IGNORE).
     * Trả về id của bản ghi (cũ hoặc mới).
     */
    public long insertOrIgnore(CanBoCoiThi cb) throws SQLException {
        String sql = "INSERT IGNORE INTO can_bo_coi_thi (tt_nguon, ma_gv, ho_ten, ngay_sinh, don_vi_cong_tac, source_row) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, cb.getTtNguon());
            ps.setString(2, cb.getMaGV());
            ps.setString(3, cb.getHoTen());
            ps.setDate(4, cb.getNgaySinh());
            ps.setString(5, cb.getDonViCongTac());
            ps.setInt(6, cb.getSourceRow());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    cb.setId(newId);
                    return newId;
                }
            }
        }
        // Nếu INSERT IGNORE skip → lấy id bằng mã GV
        return findIdByMaGV(cb.getMaGV());
    }

    /**
     * Thêm danh sách cán bộ (batch).
     * Trả về danh sách đã được gán id từ DB.
     */
    public List<CanBoCoiThi> insertBatch(List<CanBoCoiThi> list) throws SQLException {
        String sql = "INSERT IGNORE INTO can_bo_coi_thi (tt_nguon, ma_gv, ho_ten, ngay_sinh, don_vi_cong_tac, source_row) "
                   + "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (CanBoCoiThi cb : list) {
                ps.setInt(1, cb.getTtNguon());
                ps.setString(2, cb.getMaGV());
                ps.setString(3, cb.getHoTen());
                ps.setDate(4, cb.getNgaySinh());
                ps.setString(5, cb.getDonViCongTac());
                ps.setInt(6, cb.getSourceRow());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
        // Lấy lại toàn bộ để có id
        return findAll();
    }

    /**
     * Tìm id theo mã GV.
     */
    public long findIdByMaGV(String maGV) throws SQLException {
        String sql = "SELECT id FROM can_bo_coi_thi WHERE ma_gv = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, maGV);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        return -1;
    }

    /**
     * Lấy toàn bộ danh sách cán bộ.
     */
    public List<CanBoCoiThi> findAll() throws SQLException {
        String sql = "SELECT * FROM can_bo_coi_thi ORDER BY tt_nguon";
        List<CanBoCoiThi> result = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    /**
     * Tìm cán bộ theo id.
     */
    public CanBoCoiThi findById(long id) throws SQLException {
        String sql = "SELECT * FROM can_bo_coi_thi WHERE id = ?";
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
     * Đếm tổng số cán bộ.
     */
    public int count() throws SQLException {
        String sql = "SELECT COUNT(*) FROM can_bo_coi_thi";
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            if (rs.next()) return rs.getInt(1);
        }
        return 0;
    }

    /**
     * Xóa toàn bộ (dùng khi cần reset dữ liệu nguồn).
     */
    public void deleteAll() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM can_bo_coi_thi");
        }
    }

    private CanBoCoiThi mapRow(ResultSet rs) throws SQLException {
        CanBoCoiThi cb = new CanBoCoiThi();
        cb.setId(rs.getLong("id"));
        cb.setTtNguon(rs.getInt("tt_nguon"));
        cb.setMaGV(rs.getString("ma_gv"));
        cb.setHoTen(rs.getString("ho_ten"));
        cb.setNgaySinh(rs.getDate("ngay_sinh"));
        cb.setDonViCongTac(rs.getString("don_vi_cong_tac"));
        cb.setSourceRow(rs.getInt("source_row"));
        cb.setCreatedAt(rs.getTimestamp("created_at"));
        return cb;
    }
}
