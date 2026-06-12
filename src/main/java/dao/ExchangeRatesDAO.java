package dao;

import exception.ExchangeRateAlreadyExistsException;
import exception.InternalServerException;
import model.Currency;
import model.ExchangeRateTableProjection;
import model.ExchangeRateTransfer;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ExchangeRatesDAO {

    public ExchangeRatesDAO() {
    }

    public List<ExchangeRateTableProjection> findAllExchangeRates() {
        List<ExchangeRateTableProjection> exchangeRates = new ArrayList<>();
        try (Connection connection = DBConnectionFactory.getConnection()) {
            String query = """
                    SELECT ER.ID as ID,
                           ER.Rate as Rate,
                           baseCurrency.ID as base_id,
                           baseCurrency.FullName as base_name,
                           baseCurrency.Code as base_code,
                           baseCurrency.Sign as base_sign,
                           targetCurrency.ID as target_id,
                           targetCurrency.FullName as target_name,
                           targetCurrency.Code as target_code,
                           targetCurrency.Sign as target_sign
                    from ExchangeRates AS ER
                             JOIN Currencies as baseCurrency
                                  ON ER.BaseCurrencyId = baseCurrency.ID
                             JOIN Currencies as targetCurrency
                                  ON ER.TargetCurrencyId = targetCurrency.ID
                    """;
            try (PreparedStatement ps = connection.prepareStatement(query)) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        fillExchangeRates(exchangeRates, resultSet);
                    }
                }
            }
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
        return exchangeRates;
    }

    private void fillExchangeRates(List<ExchangeRateTableProjection> exchangeRates,
                                   ResultSet resultSet) throws SQLException {
        exchangeRates.add(
                new ExchangeRateTableProjection(
                        resultSet.getInt("ID"),
                        new Currency(
                                resultSet.getInt("base_id"),
                                resultSet.getString("base_code"),
                                resultSet.getString("base_name"),
                                resultSet.getString("base_sign")
                        ),
                        new Currency(
                                resultSet.getInt("target_id"),
                                resultSet.getString("target_code"),
                                resultSet.getString("target_name"),
                                resultSet.getString("target_sign")
                        ),
                        resultSet.getDouble("Rate")
                )
        );
    }

    public ExchangeRateTableProjection createExchangeRate(ExchangeRateTransfer transferData) {
        try (Connection connection = DBConnectionFactory.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(
                    "insert into ExchangeRates(BaseCurrencyId, TargetCurrencyId, Rate)\n" +
                            "values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ) {
                Currency baseCurrency = transferData.base();
                ps.setInt(1, baseCurrency.getId());
                Currency targetCurrency = transferData.target();
                ps.setInt(2, targetCurrency.getId());
                double rate = transferData.rate();
                ps.setDouble(3, rate);
                ps.executeUpdate();

                try (ResultSet resultSet = ps.getGeneratedKeys()) {
                    if (resultSet.next()) {
                        return new ExchangeRateTableProjection(
                                resultSet.getInt("ID"),
                                baseCurrency,
                                targetCurrency,
                                rate
                        );
                    } else {
                        throw new InternalServerException("internal server error");
                    }
                }
            }
        } catch (SQLiteException e) {
            checkUniquePairConstraint(e);
            throw new InternalServerException("internal server error", e);
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
    }

    private static void checkUniquePairConstraint(SQLiteException e) {
        if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
            throw new ExchangeRateAlreadyExistsException("this exchange pair already exists", e);
        }
    }
}
