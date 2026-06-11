package service;

import converter.Converter;
import converter.CurrencyToCurrencyDTOConverter;
import dao.CurrencyDAO;
import dto.responce.CurrencyDTO;
import exception.InternalServerException;
import model.Currency;

import java.util.List;
import java.util.stream.Collectors;

public class CurrencyService {

    private CurrencyDAO currencyDAO;
    private Converter<CurrencyDTO, Currency> converter;

    public CurrencyService() {
        this.currencyDAO = new CurrencyDAO();
        this.converter = new CurrencyToCurrencyDTOConverter();
    }

    public List<CurrencyDTO> findAllCurrencies() {
        List<Currency> currencies = currencyDAO.findAll();
        return currencies.stream()
                .map(currency -> converter.convert(currency))
                .collect(Collectors.toList());
    }
}
