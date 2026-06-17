package service;

import converter.Converter;
import converter.CurrencyToCurrencyDTOConverter;
import dao.CurrencyDAO;
import dto.responce.CurrencyRespDTO;
import model.Currency;

public class CurrencyService {
    private final CurrencyDAO currencyDAO;
    private final Converter<CurrencyRespDTO, Currency> converter;

    public CurrencyService() {
        this.currencyDAO = new CurrencyDAO();
        this.converter = new CurrencyToCurrencyDTOConverter();
    }

    public CurrencyRespDTO findCurrency(String code) {
        Currency currency = currencyDAO.findCurrency(code);
        return converter.convert(currency);
    }
}
