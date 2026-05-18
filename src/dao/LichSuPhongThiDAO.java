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
 * DAO cho bảng lich_su_phong_thi
 */
public class LichSuPhongThiDAO {

    public List<Map<String, String>> findByDotId(long dotId) throws SQLException {
        String sql = "SELECT can_bo_ma, ten_phong FROM lich_su_phong_thi WHERE dot_id = ?";
        List<Map<String, String>> res = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, String> row = new HashMap<>();
                    row.put("can_bo_ma", rs.getString("can_bo_ma"));
                    row.put("ten_phong", rs.getString("ten_phong"));
                    res.add(row);
                }
            }
        }
        return res;
    }

    public void insertBatch(long dotId, List<Map<String, String>> rows) throws SQLException {
        if (rows == null || rows.isEmpty()) return;
        String sql = "INSERT INTO lich_su_phong_thi (dot_id, can_bo_ma, ten_phong) VALUES (?, ?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (Map<String, String> r : rows) {
                ps.setLong(1, dotId);
                ps.setString(2, r.getOrDefault("can_bo_ma", ""));
                ps.setString(3, r.getOrDefault("ten_phong", ""));
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }
}
