<%@ page contentType="text/html; charset=UTF-8" pageEncoding="UTF-8" %>
<%@ page import="java.util.*" %>
<%@ page import="java.sql.*" %>
<%@ page import="com.openwifi.seoulopenwifi.Db" %>

<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>서울 공공 와이파이</title>
    <link rel="stylesheet" href="<%=request.getContextPath()%>/assets/style.css">
    <script defer src="<%=request.getContextPath()%>/assets/app.js"></script>
</head>
<body>
<div class="container">

    <!-- ================= HEADER ================= -->
    <div class="header">
        <h2>서울 공공 와이파이</h2>
        <div class="nav">
            <a href="<%=request.getContextPath()%>/index.jsp">홈</a>
            <!-- OpenAPI는 누르면 /load-wifi로 갔다가 index.jsp로 forward 되어 결과가 아래에 나타남 -->
            <a href="<%=request.getContextPath()%>/load-wifi">OpenAPI 불러오기</a>
            <!-- 히스토리는 index.jsp?view=history 로만 열기 (기본 화면엔 안 보임) -->
            <a href="<%=request.getContextPath()%>/index.jsp?view=history">위치 히스토리</a>
        </div>
    </div>

    <!-- ================= 내 위치 입력 (항상 보임) ================= -->
    <div class="card">
        <h3 style="margin-top:0;">내 위치 입력</h3>

        <form method="get" action="<%=request.getContextPath()%>/nearby">
            <div class="row">
                <div class="field">
                    <label>LAT(위도)</label>
                    <input type="text" name="lat" placeholder="예: 37.5665"
                           value="<%= request.getAttribute("lat") != null ? request.getAttribute("lat") : "" %>"/>
                </div>

                <div class="field">
                    <label>LNT(경도)</label>
                    <input type="text" name="lnt" placeholder="예: 126.9780"
                           value="<%= request.getAttribute("lnt") != null ? request.getAttribute("lnt") : "" %>"/>
                </div>

                <button type="button" id="btn-my-location" class="secondary">내 위치 가져오기</button>
                <button type="submit">근처 WIFI 정보 보기</button>
            </div>
        </form>

        <p style="margin:12px 0 0; color: var(--muted);">
            팁: “내 위치 가져오기”는 브라우저 위치 권한이 필요합니다.
        </p>
    </div>

    <!-- ================= OpenAPI 결과 (OpenAPI 눌렀을 때만) ================= -->
    <%
        Integer loadResult = (Integer) request.getAttribute("loadResult");
    %>
    <% if (loadResult != null) { %>
    <div class="card">
        <h3 style="margin-top:0;">OpenAPI 저장 결과</h3>
        <p>총 <strong><%= loadResult %>
        </strong>개의 와이파이 정보를 저장했습니다.</p>
    </div>
    <% } %>

    <!-- ================= 위치 히스토리 (view=history 일 때만) ================= -->
    <%
        String view = request.getParameter("view");
        boolean showHistory = "history".equals(view);
    %>

    <% if (showHistory) { %>
    <div class="card">
        <h3 style="margin-top:0;">위치 히스토리</h3>

        <table class="table">
            <thead>
            <tr>
                <th>ID</th>
                <th>LAT</th>
                <th>LNT</th>
                <th>조회일시</th>
                <th>삭제</th>
            </tr>
            </thead>
            <tbody>
            <%
                String hsql = "SELECT id, lat, lnt, query_dttm FROM location_history ORDER BY id DESC";
                try (Connection conn = Db.getConnection();
                        PreparedStatement ps = conn.prepareStatement(hsql);
                        ResultSet rs = ps.executeQuery()) {

                    boolean hasRow = false;
                    while (rs.next()) {
                        hasRow = true;
            %>
            <tr>
                <td><%= rs.getInt("id") %>
                </td>
                <td><%= rs.getDouble("lat") %>
                </td>
                <td><%= rs.getDouble("lnt") %>
                </td>
                <td><%= rs.getTimestamp("query_dttm") %>
                </td>
                <td>
                    <form method="post" action="<%=request.getContextPath()%>/history-delete"
                          onsubmit="return confirm('이 히스토리를 삭제할까요?');" style="margin:0;">
                        <input type="hidden" name="id" value="<%= rs.getInt("id") %>"/>
                        <button type="submit" class="secondary">삭제</button>
                    </form>
                </td>
            </tr>
            <%
                }
                if (!hasRow) {
            %>
            <tr>
                <td colspan="5">조회 히스토리가 없습니다.</td>
            </tr>
            <%
                }
            } catch (Exception e) {
            %>
            <tr>
                <td colspan="5" class="error">에러: <%= e.getMessage() %>
                </td>
            </tr>
            <%
                }
            %>
            </tbody>
        </table>
    </div>
    <% } %>

    <!-- ================= 근처 WIFI 결과 (근처 WIFI 버튼 눌렀을 때만) ================= -->
    <%
        List<Map<String, Object>> wifiList =
                (List<Map<String, Object>>) request.getAttribute("wifiList");
    %>

    <% if (wifiList != null) { %>
    <div class="card">
        <h3 style="margin-top:0;">근처 와이파이 정보</h3>

        <table class="table">
            <thead>
            <tr>
                <th>거리(km)</th>
                <th>관리번호</th>
                <th>자치구</th>
                <th>와이파이명</th>
                <th>도로명주소</th>
                <th>상세주소</th>
                <th>설치위치</th>
                <th>설치유형</th>
                <th>설치기관</th>
                <th>서비스구분</th>
                <th>망종류</th>
                <th>설치년도</th>
                <th>실내외</th>
                <th>접속환경</th>
                <th>위도</th>
                <th>경도</th>
                <th>작업일자</th>
            </tr>
            </thead>
            <tbody>
            <%
                if (wifiList.isEmpty()) {
            %>
            <tr>
                <td colspan="17">조회 결과가 없습니다.</td>
            </tr>
            <%
            } else {
                for (Map<String, Object> row : wifiList) {
                    double dist = (row.get("distance") == null) ? 0.0 : ((Number) row.get("distance")).doubleValue();
            %>
            <tr>
                <td><%= String.format("%.4f", dist) %>
                </td>
                <td><%= row.get("x_swifi_mgr_no") %>
                </td>
                <td><%= row.get("x_swifi_wrdofc") %>
                </td>
                <td><%= row.get("x_swifi_main_nm") %>
                </td>
                <td><%= row.get("x_swifi_adres1") %>
                </td>
                <td><%= row.get("x_swifi_adres2") %>
                </td>
                <td><%= row.get("x_swifi_instl_floor") %>
                </td>
                <td><%= row.get("x_swifi_instl_ty") %>
                </td>
                <td><%= row.get("x_swifi_instl_mby") %>
                </td>
                <td><%= row.get("x_swifi_svc_se") %>
                </td>
                <td><%= row.get("x_swifi_cmcwr") %>
                </td>
                <td><%= row.get("x_swifi_cnstc_year") %>
                </td>
                <td><%= row.get("x_swifi_inout_door") %>
                </td>
                <td><%= row.get("x_swifi_remars3") %>
                </td>
                <td><%= row.get("lat") %>
                </td>
                <td><%= row.get("lnt") %>
                </td>
                <td><%= row.get("work_dttm") %>
                </td>
            </tr>
            <%
                    }
                }
            %>
            </tbody>
        </table>
    </div>
    <% } %>

</div>
</body>
</html>