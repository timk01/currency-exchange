package converter;

import dto.responce.ExchangeRateRespDTO;
import model.ExchangeRateTableProjection;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExchangeRateProjectionToExchangeRateRespDTOConverter
        implements Converter<ExchangeRateRespDTO, ExchangeRateTableProjection> {
    private final CurrencyToCurrencyDTOConverter currencyToCurrencyDTOConverter;

    public ExchangeRateProjectionToExchangeRateRespDTOConverter() {
        currencyToCurrencyDTOConverter = new CurrencyToCurrencyDTOConverter();
    }

    @Override
    public ExchangeRateRespDTO convert(ExchangeRateTableProjection rate) {
        return new ExchangeRateRespDTO(
                rate.id(),
                currencyToCurrencyDTOConverter.convert(rate.baseCurrency()),
                currencyToCurrencyDTOConverter.convert(rate.targetCurrency()),
                BigDecimal.valueOf(rate.rate()).setScale(6, RoundingMode.HALF_UP)
        );
    }
}
