package com.openwifi.seoulopenwifi.servlet;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.openwifi.seoulopenwifi.Db;
import com.openwifi.seoulopenwifi.dao.WifiDao;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class LoadWifiServlet extends HttpServlet {

    // 서울시 공공와이파이 API 키
    private static final String API_KEY = "65656577626b696d36314c42516d53";

    // 한 번에 가져올 데이터 개수 (API 제한 때문에 1000개씩)
    private static final int PAGE_SIZE = 1000;

    // DB에 한 번에 넣을 배치 사이즈
    private static final int BATCH_SIZE = 500;

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        WifiDao wifiDao = new WifiDao();
        OkHttpClient client = new OkHttpClient();

        int start = 1;
        int end = PAGE_SIZE;
        int total = 0;
        int saved = 0;

        resp.setContentType("text/plain; charset=UTF-8");

        // DB 연결은 하나만 열어서 계속 씀 (성능 때문에)
        try (Connection conn = Db.getConnection()) {
            conn.setAutoCommit(false); // 수동 커밋 모드

            // PreparedStatement도 하나만 만들어서 재사용
            try (PreparedStatement ps = wifiDao.prepareUpsert(conn)) {

                while (true) {
                    // API URL 만들기
                    String url =
                        "http://openapi.seoul.go.kr:8088/" + API_KEY +
                            "/json/TbPublicWifiInfo/" + start + "/" + end;

                    Request request = new Request.Builder().url(url).build();

                    // API 요청 보내기 (Response 꼭 닫아줘야 함)
                    try (Response response = client.newCall(request).execute()) {
                        String json = response.body().string();

                        // JSON 파싱 (Gson 라이브러리 사용)
                        JsonObject root = new JsonParser().parse(json).getAsJsonObject();
                        JsonObject info = root.getAsJsonObject("TbPublicWifiInfo");

                        // 전체 개수는 처음에 한 번만 가져옴
                        if (total == 0) {
                            total = info.get("list_total_count").getAsInt();
                        }

                        JsonArray rows = info.getAsJsonArray("row");
                        if (rows == null || rows.size() == 0) {
                            break; // 데이터 없으면 끝
                        }

                        for (JsonElement e : rows) {
                            JsonObject o = e.getAsJsonObject();

                            // 배치에 추가
                            wifiDao.bindUpsert(ps, o);
                            ps.addBatch();
                            saved++;

                            // 배치 사이즈 차면 DB에 반영
                            if (saved % BATCH_SIZE == 0) {
                                ps.executeBatch();
                                conn.commit();
                            }
                        }
                    }

                    // 다음 페이지로 이동
                    start += PAGE_SIZE;
                    end += PAGE_SIZE;

                    // 전체 개수 넘어가면 종료
                    if (start > total) {
                        break;
                    }
                }

                // 남은 데이터들 마저 저장
                ps.executeBatch();
                conn.commit();
            }

            // 결과 페이지로 이동 (저장된 개수 넘겨줌)
            req.setAttribute("loadResult", saved);
            req.getRequestDispatcher("/index.jsp").forward(req, resp);

        } catch (Exception e) {
            resp.getWriter().println("에러 발생: " + e.getMessage());
        }
    }
}