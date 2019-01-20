package com.dbapp.data.generater;

import org.apache.ibatis.io.Resources;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.apache.ibatis.session.SqlSessionFactoryBuilder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;

/**
 * 描述:
 * 数据生成器
 *
 * @author lenghl
 * @create 2018-11-07 16:21
 */
public class DataGenerater {
    private static final Logger logger = LogManager.getLogger(DataGenerater.class);

    public static void main(String[] args) throws IOException {





        logger.info("dataGenerater start to run");
        String currentDir = System.getProperty("user.dir");
        String resource = "mybatis-config.xml";
        InputStream inputStream = Resources.getResourceAsStream(resource);
        SqlSessionFactory sqlSessionFactory = new SqlSessionFactoryBuilder().build(inputStream);
        SqlSession session = sqlSessionFactory.openSession();
        try {
            session.getConnection();
            session.selectCursor("select * from daemon_log");
        } finally {
            session.close();
        }
    }
}
