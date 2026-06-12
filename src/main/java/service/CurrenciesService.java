package service;

import converter.Converter;
import converter.CurrencyToCurrencyDTOConverter;
import dao.CurrenciesDAO;
import dto.request.CurrencyReqDTO;
import dto.responce.CurrencyRespDTO;
import model.Currency;

import java.util.List;
import java.util.stream.Collectors;

public class CurrenciesService {

    private CurrenciesDAO currencyDAO;
    private Converter<CurrencyRespDTO, Currency> converter;

    public CurrenciesService() {
        this.currencyDAO = new CurrenciesDAO();
        this.converter = new CurrencyToCurrencyDTOConverter();
    }

    public List<CurrencyRespDTO> findAllCurrencies() {
        List<Currency> currencies = currencyDAO.findAllCurrencies();
        return currencies.stream()
                .map(currency -> converter.convert(currency))
                .collect(Collectors.toList());
    }

    public CurrencyRespDTO createCurrency(CurrencyReqDTO currencyReqDTO) {
        Currency currency = currencyDAO.createCurrency(currencyReqDTO);
        return converter.convert(currency);
    }

}
