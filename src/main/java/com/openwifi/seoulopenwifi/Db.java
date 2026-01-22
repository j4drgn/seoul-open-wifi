package com.openwifi.seoulopenwifi;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class Db {

    private static String url;
    private static String user;
    private static String password;

    static {
        try {
            // 프로퍼티 파일에서 DB 정보 읽어오기
            Properties props = new Properties();
            InputStream is = Db.class
                .getClassLoader()
                .getResourceAsStream("db.properties");
            props.load(is);

            url = props.getProperty("db.url");
            user = props.getProperty("db.user");
            password = props.getProperty("db.password");

            // 마리아DB 드라이버 로딩
            Class.forName("org.mariadb.jdbc.Driver");
        } catch (Exception e) {
            // 에러나면 그냥 런타임으로 던짐
            throw new RuntimeException(e);
        }
    }

    // 커넥션 필요할 때마다 이거 부르면 됨
    public static Connection getConnection() throws Exception {
        return DriverManager.getConnection(url, user, password);
    }
}