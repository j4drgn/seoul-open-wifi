package com.openwifi.seoulopenwifi.dao;

import com.openwifi.seoulopenwifi.Db;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class HistoryDao {

    // 위치 검색 기록 저장
    public void insert(double lat, double lnt) {
        String sql =
            "INSERT INTO location_history(lat, lnt, query_dttm) VALUES (?, ?, NOW())";

        try (Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, lat);
            ps.setDouble(2, lnt);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}