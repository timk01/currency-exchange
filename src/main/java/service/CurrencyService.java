package service;

import converter.Converter;
import converter.CurrencyToCurrencyDTOConverter;
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

    public List<CurrencyRespDTO> findAllCurrencies() {
        List<Currency> currencies = currencyDAO.findAll();
        return currencies.stream()
                .map(currency -> converter.convert(currency))
                .collect(Collectors.toList());
    }

    public CurrencyRespDTO createCurrency(CurrencyReqDTO currencyReqDTO) {
        Currency currency = currencyDAO.createCurrency(currencyReqDTO);
        return converter.convert(currency);
    }

}
