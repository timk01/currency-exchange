package service;

import converter.Converter;
import converter.CurrencyToCurrencyDtoConverter;
import dao.CurrencyDao;
import dao.CurrencyDaoImpl;
import dto.response.CurrencyRespDto;
import model.Currency;

public class CurrencyService {
    private final CurrencyDao currencyDao;
    private final Converter<CurrencyRespDto, Currency> converter;

    public CurrencyService() {
        this.currencyDao = new CurrencyDaoImpl();
        this.converter = new CurrencyToCurrencyDtoConverter();
    }

    public CurrencyRespDto findCurrency(String code) {
        Currency currency = currencyDao.findByCode(code);
        return converter.convert(currency);
    }
}
