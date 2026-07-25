package dao;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;

public final class DBConnectionFactory {
    private static final String URL = System.getProperty("db.url");
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

    /**
     * метод нужен для ApplicationContextListener чтобы свалиться fail-fast на старте приложения,
     * если с БД что-то не то
     */
    public static void init() {
        try (Connection ignored = getConnection()) {
        } catch (SQLException e) {
            throw new IllegalStateException("Failed to initialize database connection pool", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return ds.getConnection();
    }

    public static void closePool() {
        if (ds != null && !ds.isClosed()) {
            ds.close();
        }
    }
}
