package service;

import converter.Converter;
import converter.CurrencyToCurrencyDtoConverter;
import dao.CurrencyDao;
import dao.CurrencyDaoImpl;
import dto.request.CurrencyReqDto;
import dto.response.CurrencyRespDto;
import model.Currency;

import java.util.List;
import java.util.stream.Collectors;

public class CurrenciesService {
    private final CurrencyDao currencyDao;
    private final Converter<CurrencyRespDto, Currency> converter;

    public CurrenciesService() {
        this.currencyDao = new CurrencyDaoImpl();
        this.converter = new CurrencyToCurrencyDtoConverter();
    }

    public List<CurrencyRespDto> findAllCurrencies() {
        List<Currency> currencies = currencyDao.findAll();
        return currencies.stream()
                .map(currency -> converter.convert(currency))
                .collect(Collectors.toList());
    }

    public CurrencyRespDto createCurrency(CurrencyReqDto currencyReqDTO) {
        Currency currency = currencyDao.insert(currencyReqDTO);
        return converter.convert(currency);
    }
}
