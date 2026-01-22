package com.openwifi.seoulopenwifi.servlet;

import com.openwifi.seoulopenwifi.Db;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;

public class HistoryDeleteServlet extends HttpServlet {

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        String idStr = req.getParameter("id");

        // ID 없으면 그냥 돌아가기
        if (idStr == null || idStr.trim().isEmpty()) {
            redirectBack(req, resp);
            return;
        }

        try {
            long id = Long.parseLong(idStr);

            // DB에서 해당 기록 삭제
            String sql = "DELETE FROM location_history WHERE id = ?";
            try (Connection conn = Db.getConnection();
                PreparedStatement ps = conn.prepareStatement(sql)) {

                ps.setLong(1, id);
                ps.executeUpdate();
            }

            // 삭제 끝나면 다시 목록으로
            redirectBack(req, resp);

        } catch (Exception e) {
            // 에러 나도 일단 히스토리 화면으로 돌아가게 처리
            try {
                req.setAttribute("error", "삭제 실패했어요: " + e.getMessage());
                RequestDispatcher rd = req.getRequestDispatcher("/index.jsp?view=history");
                rd.forward(req, resp);
            } catch (ServletException ex) {
                resp.setContentType("text/plain; charset=UTF-8");
                resp.getWriter().println("삭제 실패: " + e.getMessage());
            }
        }
    }

    // 히스토리 페이지로 리다이렉트
    private void redirectBack(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.sendRedirect(req.getContextPath() + "/index.jsp?view=history");
    }
}