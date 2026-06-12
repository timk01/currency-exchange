package dao;

import dto.request.CurrencyReqDTO;
import exception.CurrencyAlreadyExistsException;
import exception.InternalServerException;
import model.Currency;
import org.sqlite.SQLiteErrorCode;
import org.sqlite.SQLiteException;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class CurrencyDAO {

    public CurrencyDAO() {
    }

    public List<Currency> findAll() {
        List<Currency> currencies = new ArrayList<>();
        try (Connection connection = DBConnectionFactory.getConnection()) {
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

    /**
     * Контракт:
     * Создаёт новую валюту в БД и возвращает созданную модель с ID,
     * сгенерированным базой данных.
     * -- не возвращает null
     *
     * @param currencyReqDTO данные для создания валюты: code, name, sign
     * @return созданная валюта с заполненным ID
     * @throws CurrencyAlreadyExistsException если валюта с таким code уже существует (поле code униально)
     * @throws InternalServerException        если произошла ошибка БД ИЛИ не удалось получить generated key
     */
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
    }
}
