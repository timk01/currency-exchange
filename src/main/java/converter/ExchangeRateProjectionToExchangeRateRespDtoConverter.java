package converter;

import dto.response.ExchangeRateRespDto;
import model.ExchangeRateTableProjection;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeRateProjectionToExchangeRateRespDtoConverter
        implements Converter<ExchangeRateRespDto, ExchangeRateTableProjection> {
    private final CurrencyToCurrencyDtoConverter currencyToCurrencyDTOConverter;

    public ExchangeRateProjectionToExchangeRateRespDtoConverter() {
        currencyToCurrencyDTOConverter = new CurrencyToCurrencyDtoConverter();
    }

    @Override
    public ExchangeRateRespDto convert(ExchangeRateTableProjection rate) {
        return new ExchangeRateRespDto(
                rate.id(),
                currencyToCurrencyDTOConverter.convert(rate.baseCurrency()),
                currencyToCurrencyDTOConverter.convert(rate.targetCurrency()),
                BigDecimal.valueOf(rate.rate()).setScale(6, RoundingMode.HALF_UP)
        );
    }
}
