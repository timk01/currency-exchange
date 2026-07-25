package dao;

import dto.request.CurrencyReqDto;
import model.Currency;

import java.util.List;

public interface CurrencyDao {

    List<Currency> findAll();

    Currency findByCode(String code);

    Currency insert(CurrencyReqDto currencyReqDTO);
}
