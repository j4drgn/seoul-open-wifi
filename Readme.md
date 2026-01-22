# Seoul Open WiFi

서울시 공공 와이파이(OpenAPI) 데이터를 불러와 DB에 저장하고, 내 위치 기준으로 가까운 와이파이를 조회하는 웹 프로젝트입니다.

## 주요 기능

- OpenAPI 불러오기 → DB 저장
- 내 위치 입력/가져오기 → 가까운 와이파이 20개 조회
- 위치 조회 히스토리 저장/조회/삭제

## 기술 스택

- Java 8, JSP/Servlet, Tomcat
- MariaDB
- OkHttp, Gson

## 실행 방법

1. DB 생성 및 테이블 준비 (wifi_info, location_history)
2. `db.properties` 설정 (host/port/user/password)
3. Tomcat 실행 후 접속
    - 홈: `/index.jsp`
    - OpenAPI 저장: `/load-wifi`
    - 근처 조회: `/nearby?lat=...&lnt=...`

## 시연 영상 및 ERD

[구글 클라우드 링크](https://drive.google.com/drive/folders/1bqFmHUfpOVHnnm_RBncHpEad3LfadtJT?usp=sharing)
