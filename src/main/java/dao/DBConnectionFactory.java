package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnectionFactory {
    private static final String URL = "jdbc:sqlite:/opt/tomcat/data/currency_exchange.db";
    private static final String DRIVER_NAME = "org.sqlite.JDBC";
    private static final HikariDataSource ds;

    static {
        HikariConfig config = new HikariConfig();
        config.setDriverClassName(DRIVER_NAME);
        config.setJdbcUrl(URL);
        config.setConnectionInitSql("PRAGMA foreign_keys = ON;");
        config.setMaximumPoolSize(1);

        ds = new HikariDataSource(config);
    }

    private DBConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closePool() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
            System.out.println("Hikari connection pool has been closed.");
        }
    }
}
