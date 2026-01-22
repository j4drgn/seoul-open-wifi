package com.openwifi.seoulopenwifi.dao;

import com.google.gson.JsonObject;
import com.openwifi.seoulopenwifi.Db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WifiDao {

    /* =========================
       내 위치에서 가까운 와이파이 20개 찾기
       ========================= */
    public List<Map<String, Object>> findNearest20(double lat, double lnt) {

        /* distance(거리) 계산: 내 위치(lat,lnt)와 wifi_info의 (lat,lnt) 사이의 거리(km)를 구함
         - 6371: 지구 반지름(km)
         - RADIANS: 도(degree) -> 라디안(radian) 변환 (삼각함수는 라디안 기준)
         - ACOS/COS/SIN: 구면(지구) 상의 두 점 거리 계산(대원거리) 공식에 사용
         */
        String sql =
            "SELECT " +
                " (6371 * ACOS( " +
                "   COS(RADIANS(?)) * COS(RADIANS(lat)) * COS(RADIANS(lnt) - RADIANS(?)) + " +
                "   SIN(RADIANS(?)) * SIN(RADIANS(lat)) " +
                " )) AS distance, " +
                " x_swifi_mgr_no, x_swifi_wrdofc, x_swifi_main_nm, x_swifi_adres1, x_swifi_adres2, " +
                " x_swifi_instl_floor, x_swifi_instl_ty, x_swifi_instl_mby, x_swifi_svc_se, " +
                " x_swifi_cmcwr, x_swifi_cnstc_year, x_swifi_inout_door, x_swifi_remars3, " +
                " lat, lnt, work_dttm " +
                "FROM wifi_info " +
                "ORDER BY distance " +
                "LIMIT 20";

        List<Map<String, Object>> list = new ArrayList<>();

        try (Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDouble(1, lat);
            ps.setDouble(2, lnt);
            ps.setDouble(3, lat);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();

                    row.put("distance", rs.getDouble("distance"));
                    row.put("x_swifi_mgr_no", rs.getString("x_swifi_mgr_no"));
                    row.put("x_swifi_wrdofc", rs.getString("x_swifi_wrdofc"));
                    row.put("x_swifi_main_nm", rs.getString("x_swifi_main_nm"));
                    row.put("x_swifi_adres1", rs.getString("x_swifi_adres1"));
                    row.put("x_swifi_adres2", rs.getString("x_swifi_adres2"));
                    row.put("x_swifi_instl_floor", rs.getString("x_swifi_instl_floor"));
                    row.put("x_swifi_instl_ty", rs.getString("x_swifi_instl_ty"));
                    row.put("x_swifi_instl_mby", rs.getString("x_swifi_instl_mby"));
                    row.put("x_swifi_svc_se", rs.getString("x_swifi_svc_se"));
                    row.put("x_swifi_cmcwr", rs.getString("x_swifi_cmcwr"));
                    row.put("x_swifi_cnstc_year", rs.getString("x_swifi_cnstc_year"));
                    row.put("x_swifi_inout_door", rs.getString("x_swifi_inout_door"));
                    row.put("x_swifi_remars3", rs.getString("x_swifi_remars3"));
                    row.put("lat", rs.getDouble("lat"));
                    row.put("lnt", rs.getDouble("lnt"));
                    row.put("work_dttm", rs.getString("work_dttm"));

                    list.add(row);
                }
            }

        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return list;
    }

    /* =========================
       OpenAPI 데이터 저장 (배치 처리용)
       ========================= */
    private static final String UPSERT_SQL =
        "INSERT INTO wifi_info (" +
            "x_swifi_mgr_no, x_swifi_wrdofc, x_swifi_main_nm, x_swifi_adres1, x_swifi_adres2, " +
            "x_swifi_instl_floor, x_swifi_instl_ty, x_swifi_instl_mby, x_swifi_svc_se, " +
            "x_swifi_cmcwr, x_swifi_cnstc_year, x_swifi_inout_door, x_swifi_remars3, " +
            "lat, lnt, work_dttm" +
            ") VALUES (?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?) " +
            "ON DUPLICATE KEY UPDATE " +
            "x_swifi_wrdofc=VALUES(x_swifi_wrdofc), " +
            "x_swifi_main_nm=VALUES(x_swifi_main_nm), " +
            "x_swifi_adres1=VALUES(x_swifi_adres1), " +
            "x_swifi_adres2=VALUES(x_swifi_adres2), " +
            "x_swifi_instl_floor=VALUES(x_swifi_instl_floor), " +
            "x_swifi_instl_ty=VALUES(x_swifi_instl_ty), " +
            "x_swifi_instl_mby=VALUES(x_swifi_instl_mby), " +
            "x_swifi_svc_se=VALUES(x_swifi_svc_se), " +
            "x_swifi_cmcwr=VALUES(x_swifi_cmcwr), " +
            "x_swifi_cnstc_year=VALUES(x_swifi_cnstc_year), " +
            "x_swifi_inout_door=VALUES(x_swifi_inout_door), " +
            "x_swifi_remars3=VALUES(x_swifi_remars3), " +
            "lat=VALUES(lat), " +
            "lnt=VALUES(lnt), " +
            "work_dttm=VALUES(work_dttm)";

    /** 배치용 PreparedStatement 미리 만들기 */
    public PreparedStatement prepareUpsert(Connection conn) throws Exception {
        return conn.prepareStatement(UPSERT_SQL);
    }

    /** 배치용 파라미터 세팅 */
    public void bindUpsert(PreparedStatement ps, JsonObject o) throws Exception {
        ps.setString(1, get(o, "X_SWIFI_MGR_NO"));
        ps.setString(2, get(o, "X_SWIFI_WRDOFC"));
        ps.setString(3, get(o, "X_SWIFI_MAIN_NM"));
        ps.setString(4, get(o, "X_SWIFI_ADRES1"));
        ps.setString(5, get(o, "X_SWIFI_ADRES2"));
        ps.setString(6, get(o, "X_SWIFI_INSTL_FLOOR"));
        ps.setString(7, get(o, "X_SWIFI_INSTL_TY"));
        ps.setString(8, get(o, "X_SWIFI_INSTL_MBY"));
        ps.setString(9, get(o, "X_SWIFI_SVC_SE"));
        ps.setString(10, get(o, "X_SWIFI_CMCWR"));
        ps.setString(11, get(o, "X_SWIFI_CNSTC_YEAR"));
        ps.setString(12, get(o, "X_SWIFI_INOUT_DOOR"));
        ps.setString(13, get(o, "X_SWIFI_REMARS3"));
        ps.setDouble(14, getDouble(o, "LAT"));
        ps.setDouble(15, getDouble(o, "LNT"));
        ps.setString(16, get(o, "WORK_DTTM"));
    }

    /* =========================
       (혹시 몰라서 남겨둠) 하나씩 저장할 때 씀
       ========================= */
    public void upsert(JsonObject o) {
        try (Connection conn = Db.getConnection();
            PreparedStatement ps = conn.prepareStatement(UPSERT_SQL)) {

            bindUpsert(ps, o);
            ps.executeUpdate();

        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /* =========================
       유틸 함수들
       ========================= */
    // JSON에서 String 값 꺼내기 (null 체크 포함)
    private String get(JsonObject o, String key) {
        return (o != null && o.has(key) && !o.get(key).isJsonNull())
            ? o.get(key).getAsString()
            : "";
    }

    // JSON에서 Double 값 꺼내기 (에러나면 0.0)
    private double getDouble(JsonObject o, String key) {
        if (o == null || !o.has(key) || o.get(key).isJsonNull()) {
            return 0.0;
        }
        try {
            return o.get(key).getAsDouble();
        } catch (Exception e) {
            return 0.0;
        }
    }
}