package dao;

import exception.CurrencyIsNotFoundException;
import exception.InternalServerException;
import model.Currency;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CurrencyDAO {

    public CurrencyDAO() {
    }

    public Currency findCurrency(String code) {
        try (Connection connection = DBConnectionFactory.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement("select * from Currencies where Code = ?")) {
                ps.setString(1, code);
                try (ResultSet resultSet = ps.executeQuery()) {
                    if (resultSet.next()) {
                        return new Currency(
                                resultSet.getInt("ID"),
                                resultSet.getString("Code"),
                                resultSet.getString("FullName"),
                                resultSet.getString("Sign")
                        );
                    } else {
                        throw new CurrencyIsNotFoundException("currency is not found");
                    }
                }
            }
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
    }
}
