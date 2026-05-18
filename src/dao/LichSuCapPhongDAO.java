package dao;

import util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * DAO cho bảng lich_su_cap_phong
 */
public class LichSuCapPhongDAO {

    public List<Map<String, String>> findByDotId(long dotId) throws SQLException {
        String sql = "SELECT can_bo_1_ma, can_bo_2_ma, phong_thi_ten FROM lich_su_cap_phong WHERE dot_id = ?";
        List<Map<String, String>> res = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("can_bo_1_ma", rs.getString("can_bo_1_ma"));
                    row.put("can_bo_2_ma", rs.getString("can_bo_2_ma"));
                    row.put("phong_thi_ten", rs.getString("phong_thi_ten"));
                    res.add(row);
                }
            }
        }
        return res;
    }

    public void insertBatch(long dotId, List<Map<String, String>> pairs) throws SQLException {
        if (pairs == null || pairs.isEmpty()) return;
        String sql = "INSERT INTO lich_su_cap_phong (dot_id, can_bo_1_ma, can_bo_2_ma, phong_thi_ten) VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Map<String, String> p : pairs) {
                ps.setLong(1, dotId);
                ps.setString(2, p.getOrDefault("can_bo_1_ma", ""));
                ps.setString(3, p.getOrDefault("can_bo_2_ma", ""));
                ps.setString(4, p.getOrDefault("phong_thi_ten", ""));
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }
}
