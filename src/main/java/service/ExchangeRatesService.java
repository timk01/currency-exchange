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

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRatesService {
    private final CurrencyDAO currencyDAO;
    private final ExchangeRatesDAO exchangeRatesDAO;
    private final Converter<ExchangeRateRespDTO, ExchangeRateTableProjection> converter;

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
        Currency targetCurrency = currencyDAO.findCurrency(exchangeRate.targetCode());

        ExchangeRateTableProjection createdResult = exchangeRatesDAO.createExchangeRate(
                new ExchangeRateTransfer(
                        baseCurrency,
                        targetCurrency,
                        exchangeRate.rate().doubleValue()
                )
        );
        return converter.convert(createdResult);
    }

    public ExchangeRateRespDTO findExchangeRatePair(ExchangeRateCodePairDTO pair) {
        ExchangeRateTableProjection foundResult = exchangeRatesDAO.findExchangeRatePair(pair);
        return converter.convert(foundResult);
    }

    public ExchangeRateRespDTO updateExchangeRatePairRate(ExchangeRateCodePairDTO pair, BigDecimal rate) {
        ExchangeRateTableProjection updatedResult = exchangeRatesDAO.updateExchangeRatePairRate(
                pair, rate.doubleValue()
        );
        return converter.convert(updatedResult);
    }
}
