package dao;

import bean.PhongThiNguon;
import util.DatabaseUtil;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO - Thao tác CRUD bảng phong_thi_nguon
 */
public class PhongThiNguonDAO {

    /**
     * Thêm phòng thi (INSERT IGNORE nếu trùng).
     */
    public long insertOrIgnore(PhongThiNguon pt) throws SQLException {
        String sql = "INSERT IGNORE INTO phong_thi_nguon (stt_nguon, phong_thi, ghi_chu, source_row) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            ps.setInt(1, pt.getSttNguon());
            ps.setString(2, pt.getPhongThi());
            ps.setString(3, pt.getGhiChu());
            ps.setInt(4, pt.getSourceRow());
            ps.executeUpdate();

            try (ResultSet rs = ps.getGeneratedKeys()) {
                if (rs.next()) {
                    long newId = rs.getLong(1);
                    pt.setId(newId);
                    return newId;
                }
            }
        }
        return findIdByPhongThi(pt.getPhongThi());
    }

    /**
     * Thêm batch danh sách phòng thi.
     */
    public List<PhongThiNguon> insertBatch(List<PhongThiNguon> list) throws SQLException {
        String sql = "INSERT IGNORE INTO phong_thi_nguon (stt_nguon, phong_thi, ghi_chu, source_row) "
                   + "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (PhongThiNguon pt : list) {
                ps.setInt(1, pt.getSttNguon());
                ps.setString(2, pt.getPhongThi());
                ps.setString(3, pt.getGhiChu());
                ps.setInt(4, pt.getSourceRow());
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
        return findAll();
    }

    public long findIdByPhongThi(String phongThi) throws SQLException {
        String sql = "SELECT id FROM phong_thi_nguon WHERE phong_thi = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, phongThi);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getLong("id");
            }
        }
        return -1;
    }

    public List<PhongThiNguon> findAll() throws SQLException {
        String sql = "SELECT * FROM phong_thi_nguon ORDER BY stt_nguon";
        List<PhongThiNguon> result = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement();
             ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) {
                result.add(mapRow(rs));
            }
        }
        return result;
    }

    public PhongThiNguon findById(long id) throws SQLException {
        String sql = "SELECT * FROM phong_thi_nguon WHERE id = ?";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, id);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return mapRow(rs);
            }
        }
        return null;
    }

    public void deleteAll() throws SQLException {
        try (Connection conn = DatabaseUtil.getConnection();
             Statement st = conn.createStatement()) {
            st.executeUpdate("DELETE FROM phong_thi_nguon");
        }
    }

    private PhongThiNguon mapRow(ResultSet rs) throws SQLException {
        PhongThiNguon pt = new PhongThiNguon();
        pt.setId(rs.getLong("id"));
        pt.setSttNguon(rs.getInt("stt_nguon"));
        pt.setPhongThi(rs.getString("phong_thi"));
        pt.setGhiChu(rs.getString("ghi_chu"));
        pt.setSourceRow(rs.getInt("source_row"));
        pt.setCreatedAt(rs.getTimestamp("created_at"));
        return pt;
    }
}
