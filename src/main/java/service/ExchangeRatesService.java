package service;

import converter.Converter;
import converter.ExchangeRateProjectionToExchangeRateRespDtoConverter;
import dao.*;
import dto.request.ExchangeRateCodePairDto;
import dto.request.ExchangeRateCreateReqDto;
import dto.response.ExchangeRateRespDto;
import model.Currency;
import model.ExchangeRateTableProjection;
import model.ExchangeRateTransfer;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class ExchangeRatesService {
    private final CurrencyDao currencyDao;
    private final ExchangeRatesDao exchangeRatesDao;
    private final Converter<ExchangeRateRespDto, ExchangeRateTableProjection> converter;

    public ExchangeRatesService() {
        this.exchangeRatesDao = new ExchangeRatesImpl();
        this.converter = new ExchangeRateProjectionToExchangeRateRespDtoConverter();
        this.currencyDao = new CurrencyDaoImpl();
    }

    public List<ExchangeRateRespDto> findAllExchangeRates() {
        List<ExchangeRateTableProjection> exchangeRates = exchangeRatesDao.findAll();
        return exchangeRates.stream()
                .map(rates -> converter.convert(rates))
                .collect(Collectors.toList());
    }

    public ExchangeRateRespDto createExchangeRate(ExchangeRateCreateReqDto exchangeRate) {
        Currency baseCurrency = currencyDao.findByCode(exchangeRate.baseCode());
        Currency targetCurrency = currencyDao.findByCode(exchangeRate.targetCode());

        ExchangeRateTableProjection createdResult = exchangeRatesDao.insert(
                new ExchangeRateTransfer(
                        baseCurrency,
                        targetCurrency,
                        exchangeRate.rate().doubleValue()
                )
        );
        return converter.convert(createdResult);
    }

    public ExchangeRateRespDto findExchangeRatePair(ExchangeRateCodePairDto pair) {
        ExchangeRateTableProjection foundResult = exchangeRatesDao.findExchangeRatePair(pair);
        return converter.convert(foundResult);
    }

    public ExchangeRateRespDto updateExchangeRatePairRate(ExchangeRateCodePairDto pair, BigDecimal rate) {
        ExchangeRateTableProjection updatedResult = exchangeRatesDao.update(
                pair, rate.doubleValue()
        );
        return converter.convert(updatedResult);
    }
}
