package dao;

import dto.request.CurrencyReqDTO;
import dto.responce.ExchangeRateRespDTO;
import exception.CurrencyAlreadyExistsException;
import exception.InternalServerException;
import model.Currency;
import model.ExchangeRate;
import model.ExchangeRateTableProjection;
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

    public static void main(String[] args) {
        ExchangeRatesDAO exchangeRatesDAO = new ExchangeRatesDAO();
        System.out.println(exchangeRatesDAO.findAllExchangeRates());
    }
    /*
     *//**
     * Контракт:
     * Создаёт новую валюту в БД и возвращает созданную модель с ID,
     * сгенерированным базой данных.
     * -- не возвращает null
     *
     * @param currencyReqDTO данные для создания валюты: code, name, sign
     * @return созданная валюта с заполненным ID
     * @throws CurrencyAlreadyExistsException если валюта с таким code уже существует (поле code униально)
     * @throws InternalServerException        если произошла ошибка БД ИЛИ не удалось получить generated key
     *//*
    public Currency createCurrency(CurrencyReqDTO currencyReqDTO) {
        try (Connection connection = DBConnectionFactory.getConnection()) {

            validateCurrencyCodeBefore(currencyReqDTO, connection);

            try (PreparedStatement ps = connection.prepareStatement(
                    "insert into Currencies(Code, FullName, Sign)\n" +
                            "values (?, ?, ?)", Statement.RETURN_GENERATED_KEYS);
            ) {
                String code = currencyReqDTO.code();
                ps.setString(1, code);
                String name = currencyReqDTO.name();
                ps.setString(2, name);
                String sign = currencyReqDTO.sign();
                ps.setString(3, sign);
                ps.executeUpdate();

                try (ResultSet addedCurrency = ps.getGeneratedKeys()) {
                    if (addedCurrency.next()) {
                        Currency currency = new Currency();
                        currency.setId(addedCurrency.getInt(1));
                        currency.setCode(code);
                        currency.setFullName(name);
                        currency.setSign(sign);
                        return currency;
                    } else {
                        throw new InternalServerException("internal server error");
                    }
                }
            }
        } catch (SQLiteException e) {
            checkUniqueConstraint(e);
            throw new InternalServerException("internal server error", e);
        } catch (SQLException e) {
            throw new InternalServerException("internal server error", e);
        }
    }

    private void validateCurrencyCodeBefore(CurrencyReqDTO currencyReqDTO, Connection connection) throws SQLException {
        try (PreparedStatement ps = connection.prepareStatement("select Code from Currencies where Code = ?")) {
            ps.setString(1, currencyReqDTO.code());
            try (ResultSet resultSet = ps.executeQuery()) {
                if (resultSet.next()) {
                    throw new CurrencyAlreadyExistsException("currency already exists");
                }
            }
        }
    }

    private static void checkUniqueConstraint(SQLiteException e) {
        if (e.getResultCode() == SQLiteErrorCode.SQLITE_CONSTRAINT_UNIQUE) {
            throw new CurrencyAlreadyExistsException("currency already exists", e);
        }
    }*/
}
