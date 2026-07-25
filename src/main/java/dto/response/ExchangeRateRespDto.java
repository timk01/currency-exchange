package dto.response;

import java.math.BigDecimal;

public record ExchangeRateRespDto(
        int id,
        CurrencyRespDto baseCurrency,
        CurrencyRespDto targetCurrency,
        BigDecimal rate) {
}
