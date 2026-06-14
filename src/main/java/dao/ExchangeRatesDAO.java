package dao;

import dto.request.ExchangeRateCodePairDTO;
import exception.ExchangeRateAlreadyExistsException;
import exception.ExchangeRatePairDoesNotExistException;
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
    private static final String SELECT_EXCHANGE_RATE_PROJECTION = """
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

    public ExchangeRatesDAO() {
    }

    public List<ExchangeRateTableProjection> findAllExchangeRates() {
        List<ExchangeRateTableProjection> exchangeRates = new ArrayList<>();
        try (Connection connection = DBConnectionFactory.getConnection()) {
            try (PreparedStatement ps = connection.prepareStatement(SELECT_EXCHANGE_RATE_PROJECTION)) {
                try (ResultSet resultSet = ps.executeQuery()) {
                    while (resultSet.next()) {
                        exchangeRates.add(fillExchangeRates(resultSet));
                    }
                }
            }
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
        return exchangeRates;
    }

    public ExchangeRateTableProjection findExchangeRatePair(ExchangeRateCodePairDTO pair) {
        try (Connection connection = DBConnectionFactory.getConnection()) {
            return getExchangeRateTableProjection(pair, connection);
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
    }

    /**
     * ps.executeUpdate():
     * НЕ проверяет возвращаемое значение как if( > 0),т.к к этому моменту:
     * а) мы УЖЕ нашли пару (SELECT)
     * б) update выполнился по ID найденной строки
     * ИЛИ ЖЕ, повторный PATCH с тем же rate ТАКЖЕ считается успешным
     *
     * @param pair
     * @param rate
     * @return
     */
    public ExchangeRateTableProjection updateExchangeRatePairRate(ExchangeRateCodePairDTO pair, double rate) {
        try (Connection connection = DBConnectionFactory.getConnection()) {
            connection.setAutoCommit(false);
            try {
                ExchangeRateTableProjection foundPair = getExchangeRateTableProjection(pair, connection);
                try (PreparedStatement ps = connection.prepareStatement(
                        """
                                UPDATE ExchangeRates
                                SET Rate = ?
                                WHERE ID = ?
                                """)
                ) {
                    ps.setDouble(1, rate);
                    ps.setInt(2, foundPair.id());

                    ps.executeUpdate();
                    connection.commit();
                    return new ExchangeRateTableProjection(
                            foundPair.id(),
                            foundPair.baseCurrency(),
                            foundPair.targetCurrency(),
                            rate);
                }
            } catch (ExchangeRatePairDoesNotExistException e) {
                connection.rollback();
                throw e;
            } catch (SQLException e) {
                connection.rollback();
                throw new InternalServerException("internal server error", e);
            } finally {
                connection.setAutoCommit(true);
            }
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
    }

    private ExchangeRateTableProjection getExchangeRateTableProjection(ExchangeRateCodePairDTO pair, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement(
                SELECT_EXCHANGE_RATE_PROJECTION + "WHERE baseCurrency.Code = ? AND targetCurrency.Code = ?")
        ) {
            ps.setString(1, pair.baseCode());
            ps.setString(2, pair.targetCode());
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    return fillExchangeRates(resultSet);
                } else {
                    throw new ExchangeRatePairDoesNotExistException("the following pair is not found: " + pair.baseCode() + pair.targetCode());
                }
            }
        }
    }

    private ExchangeRateTableProjection fillExchangeRates(ResultSet resultSet) throws SQLException {
        return new ExchangeRateTableProjection(
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

    private void checkUniquePairConstraint(SQLiteException e) {
        if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
            throw new ExchangeRateAlreadyExistsException("this exchange pair already exists", e);
        }
    }
}
