package com.dbapp.data.generater;

import org.apache.ibatis.io.Resources;

import java.sql.*;
import java.text.ParseException;
import java.util.Map;

public class TestJdbc {
    public static final String url = "jdbc:mysql://localhost:3306/phabricator_daemon";
    public static final String name = "com.mysql.jdbc.Driver";
    public static final String user = "root";
    public static final String password = "root";
    public static final String sql = "select * from daemon_log";

    public static void main(String[] args) throws ParseException, Exception {
        Connection conn = null;
        PreparedStatement pst = null;
        ResultSet rs = null;
        try {
            // (1)注册驱动
            //Resources.classForName(name);
            Class.forName(name);

            //(2) 获取链接
            conn = DriverManager.getConnection(url, user, password);

            DatabaseMetaData databaseMetaData = conn.getMetaData();
            Map map = conn.getTypeMap();
            // (3)准备语句
            pst = conn.prepareStatement(sql);

            // (4)执行查询
            rs = pst.executeQuery();

            // (5)迭代结果
            while (rs.next()) {
                System.out.println(rs.getString("daemonID"));
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {

                if (rs != null) {
                    rs.close();
                }

                if (null == pst) {
                    pst.close();

                }

                if (null != conn) {
                    conn.close();

                }

            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
    }
}
