package com.dbapp.data.generater;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class TestHikariDataSource {

    public static final String sql = "select * from daemon_log";
    public static void main(String[] args) throws SQLException {
        HikariConfig config = new HikariConfig();
        config.setJdbcUrl("jdbc:mysql://localhost:3306/phabricator_daemon");
        config.setUsername("root");
        config.setPassword("root");
        config.addDataSourceProperty("cachePrepStmts", "true");
        config.addDataSourceProperty("prepStmtCacheSize", "250");
        config.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");

        DataSource ds = new HikariDataSource(config);

        Connection conn = ds.getConnection();

        PreparedStatement pst = null;
        ResultSet rs = null;
        // (3)准备语句
        pst = conn.prepareStatement(sql);

        // (4)执行查询
        rs = pst.executeQuery();

        // (5)迭代结果
        while (rs.next()) {
            System.out.println(rs.getString("daemonID"));
        }
    }
}
