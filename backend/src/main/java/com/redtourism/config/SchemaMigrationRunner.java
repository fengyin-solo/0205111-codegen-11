package com.redtourism.config;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import javax.sql.DataSource;

/**
 * 旧库结构升级：为 user_custom_route 增加分享与软删除相关列/索引。
 * 全新部署由 schema.sql 建表；历史数据卷启动时通过 information_schema 幂等补列。
 */
@Component
@Order(0)
public class SchemaMigrationRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(SchemaMigrationRunner.class);

    private final DataSource dataSource;

    public SchemaMigrationRunner(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        try (Connection conn = dataSource.getConnection();
             Statement st = conn.createStatement()) {
            String db = conn.getCatalog();
            addColumnIfMissing(st, db, "share_token",
                    "ALTER TABLE user_custom_route ADD COLUMN share_token VARCHAR(64) COMMENT '只读分享令牌'");
            addColumnIfMissing(st, db, "share_expire_time",
                    "ALTER TABLE user_custom_route ADD COLUMN share_expire_time DATETIME COMMENT '分享链接失效时间'");
            addColumnIfMissing(st, db, "deleted",
                    "ALTER TABLE user_custom_route ADD COLUMN deleted TINYINT DEFAULT 0 COMMENT '0=正常 1=创建者已删除'");
            if (!indexExists(st, db, "uk_share_token")) {
                st.executeUpdate("ALTER TABLE user_custom_route ADD UNIQUE KEY uk_share_token (share_token)");
                log.info("[migration] added unique index uk_share_token");
            }
        } catch (Exception e) {
            log.warn("[migration] schema migration skipped/failed: {}", e.getMessage());
        }
    }

    private void addColumnIfMissing(Statement st, String db, String column, String ddl) throws Exception {
        try (ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM information_schema.COLUMNS "
                        + "WHERE TABLE_SCHEMA='" + db + "' AND TABLE_NAME='user_custom_route' AND COLUMN_NAME='" + column + "'")) {
            rs.next();
            if (rs.getInt(1) == 0) {
                st.executeUpdate(ddl);
                log.info("[migration] added column user_custom_route.{}", column);
            }
        }
    }

    private boolean indexExists(Statement st, String db, String index) throws Exception {
        try (ResultSet rs = st.executeQuery(
                "SELECT COUNT(*) FROM information_schema.STATISTICS "
                        + "WHERE TABLE_SCHEMA='" + db + "' AND TABLE_NAME='user_custom_route' AND INDEX_NAME='" + index + "'")) {
            rs.next();
            return rs.getInt(1) > 0;
        }
    }
}
