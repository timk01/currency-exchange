package dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public final class DBConnectionFactory {

    private static final String URL = "jdbc:sqlite:C:/projects/currency-exchange/src/main/data/currency_exchange.db";

    static {
        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    private DBConnectionFactory() {
    }

    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(URL);
    }
}
