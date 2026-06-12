package service;

import converter.Converter;
import converter.CurrencyToCurrencyDTOConverter;
import dao.CurrenciesDAO;
import dao.CurrencyDAO;
import dto.request.CurrencyReqDTO;
import dto.responce.CurrencyRespDTO;
import model.Currency;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {

    private CurrencyDAO currencyDAO;
    private Converter<CurrencyRespDTO, Currency> converter;

    public CurrencyService() {
        this.currencyDAO = new CurrencyDAO();
        this.converter = new CurrencyToCurrencyDTOConverter();
    }

    public CurrencyRespDTO findCurrency(String code) {
        Currency currency = currencyDAO.findCurrency(code);
        return converter.convert(currency);
    }
}
