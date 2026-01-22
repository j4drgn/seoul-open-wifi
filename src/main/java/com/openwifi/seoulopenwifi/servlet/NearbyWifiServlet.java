package com.openwifi.seoulopenwifi.servlet;

import com.openwifi.seoulopenwifi.dao.HistoryDao;
import com.openwifi.seoulopenwifi.dao.WifiDao;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.Map;

public class NearbyWifiServlet extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String latStr = req.getParameter("lat");
        String lntStr = req.getParameter("lnt");

        // 입력값 검증 (빈 값 체크)
        if (latStr == null || lntStr == null || latStr.trim().isEmpty() || lntStr.trim().isEmpty()) {
            forward(req, resp, "위도(LAT)랑 경도(LNT)를 입력해주세요!", null, null);
            return;
        }

        double lat;
        double lnt;
        try {
            lat = Double.parseDouble(latStr);
            lnt = Double.parseDouble(lntStr);
        } catch (NumberFormatException e) {
            forward(req, resp, "위도/경도는 숫자만 입력 가능해요.", null, null);
            return;
        }

        try {
            // 1. 검색 기록 저장하기
            new HistoryDao().insert(lat, lnt);

            // 2. 근처 와이파이 20개 가져오기
            List<Map<String, Object>> list = new WifiDao().findNearest20(lat, lnt);

            // 3. 결과 화면으로 보내기
            forward(req, resp, null, list, new double[]{lat, lnt});

        } catch (Exception e) {
            forward(req, resp, "에러 발생: " + e.getMessage(), null, new double[]{lat, lnt});
        }
    }

    // JSP로 포워딩하는 헬퍼 메소드
    private void forward(HttpServletRequest req,
        HttpServletResponse resp,
        String error,
        List<Map<String, Object>> list,
        double[] latlnt) throws IOException {
        try {
            if (error != null) {
                req.setAttribute("error", error);
            }
            if (list != null) {
                req.setAttribute("wifiList", list);
            }
            if (latlnt != null) {
                req.setAttribute("lat", latlnt[0]);
                req.setAttribute("lnt", latlnt[1]);
            }

            RequestDispatcher rd = req.getRequestDispatcher("/index.jsp");
            rd.forward(req, resp);

        } catch (ServletException e) {
            resp.setContentType("text/plain; charset=UTF-8");
            resp.getWriter().println(error != null ? error : "페이지 이동 실패");
        }
    }
}