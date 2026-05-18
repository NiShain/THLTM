package dao;

import util.DatabaseUtil;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * DAO cho bảng lich_su_giam_sat
 */
public class LichSuGiamSatDAO {

    public List<String> findByDotId(long dotId) throws SQLException {
        String sql = "SELECT can_bo_ma FROM lich_su_giam_sat WHERE dot_id = ?";
        List<String> res = new ArrayList<>();
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setLong(1, dotId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    res.add(rs.getString("can_bo_ma"));
                }
            }
        }
        return res;
    }

    public void insertBatch(long dotId, List<String> canBoMas) throws SQLException {
        if (canBoMas == null || canBoMas.isEmpty()) return;
        String sql = "INSERT INTO lich_su_giam_sat (dot_id, can_bo_ma) VALUES (?, ?)";
        try (Connection conn = DatabaseUtil.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            conn.setAutoCommit(false);
            for (String ma : canBoMas) {
                ps.setLong(1, dotId);
                ps.setString(2, ma != null ? ma : "");
                ps.addBatch();
            }
            ps.executeBatch();
            conn.commit();
        }
    }
}
