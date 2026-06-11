package dao;

import exception.InternalServerException;
import model.Currency;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDAO {

    public List<Currency> findAll() {
        String url = "jdbc:sqlite:C:/projects/currency-exchange/src/main/data/currency_exchange.db";

        try {
            Class.forName("org.sqlite.JDBC");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(e);
        }

        List<Currency> currencies = new ArrayList<>();
        try (Connection connection = DriverManager.getConnection(url)) {
            try (PreparedStatement ps = connection.prepareStatement("select * from Currencies");
                 ResultSet resultSet = ps.executeQuery()) {
                while (resultSet.next()) {
                    currencies.add(new Currency(
                            resultSet.getInt("ID"),
                            resultSet.getString("Code"),
                            resultSet.getString("FullName"),
                            resultSet.getString("Sign")
                    ));
                }
            }
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
        return currencies;
    }

    public static void main(String[] args) {
        CurrencyDAO currencyDAO = new CurrencyDAO();
        System.out.println(currencyDAO.findAll());
    }
}
