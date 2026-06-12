package service;

import converter.Converter;
import converter.CurrencyToCurrencyDTOConverter;
import converter.ExchangeRateProjectionToExchangeRateRespDTOConverter;
import dao.CurrenciesDAO;
import dao.ExchangeRatesDAO;
import dto.request.CurrencyReqDTO;
import dto.responce.CurrencyRespDTO;
import dto.responce.ExchangeRateRespDTO;
import model.Currency;
import model.ExchangeRateTableProjection;

import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRatesService {

    private ExchangeRatesDAO exchangeRatesDAO;
    private Converter<ExchangeRateRespDTO, ExchangeRateTableProjection> converter;

    public ExchangeRatesService() {
        this.exchangeRatesDAO = new ExchangeRatesDAO();
        this.converter = new ExchangeRateProjectionToExchangeRateRespDTOConverter();
    }

    public List<ExchangeRateRespDTO> findAllExchangeRates() {
        List<ExchangeRateTableProjection> exchangeRates = exchangeRatesDAO.findAllExchangeRates();
        return exchangeRates.stream()
                .map(rates -> converter.convert(rates))
                .collect(Collectors.toList());
    }

/*    public CurrencyRespDTO createCurrency(CurrencyReqDTO currencyReqDTO) {
        Currency currency = currencyDAO.createCurrency(currencyReqDTO);
        return converter.convert(currency);
    }*/

}
