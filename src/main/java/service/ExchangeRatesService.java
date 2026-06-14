package service;

import converter.Converter;
import converter.ExchangeRateProjectionToExchangeRateRespDTOConverter;
import dao.CurrencyDAO;
import dao.ExchangeRatesDAO;
import dto.request.ExchangeRateCodePairDTO;
import dto.request.ExchangeRateCreateReqDTO;
import dto.responce.ExchangeRateRespDTO;
import model.Currency;
import model.ExchangeRateTableProjection;
import model.ExchangeRateTransfer;

import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRatesService {

    private CurrencyDAO currencyDAO;
    private ExchangeRatesDAO exchangeRatesDAO;
    private Converter<ExchangeRateRespDTO, ExchangeRateTableProjection> converter;

    public ExchangeRatesService() {
        this.exchangeRatesDAO = new ExchangeRatesDAO();
        this.converter = new ExchangeRateProjectionToExchangeRateRespDTOConverter();
        this.currencyDAO = new CurrencyDAO();
    }

    public List<ExchangeRateRespDTO> findAllExchangeRates() {
        List<ExchangeRateTableProjection> exchangeRates = exchangeRatesDAO.findAllExchangeRates();
        return exchangeRates.stream()
                .map(rates -> converter.convert(rates))
                .collect(Collectors.toList());
    }

    public ExchangeRateRespDTO createExchangeRate(ExchangeRateCreateReqDTO exchangeRate) {
        Currency baseCurrency = currencyDAO.findCurrency(exchangeRate.baseCode());
        Currency targetCurrency= currencyDAO.findCurrency(exchangeRate.targetCode());

        ExchangeRateTableProjection createdResult = exchangeRatesDAO.createExchangeRate(
                new ExchangeRateTransfer(baseCurrency, targetCurrency, exchangeRate.rate())
        );
        return converter.convert(createdResult);
    }

    public ExchangeRateRespDTO findExchangeRatePair(ExchangeRateCodePairDTO pair) {
        ExchangeRateTableProjection foundResult = exchangeRatesDAO.findExchangeRatePair(pair);
        return converter.convert(foundResult);
    }

    public ExchangeRateRespDTO updateExchangeRatePairRate(ExchangeRateCodePairDTO pair, double rate) {
        ExchangeRateTableProjection updatedResult = exchangeRatesDAO.updateExchangeRatePairRate(pair, rate);
        return converter.convert(updatedResult);
    }
}
