package dto.responce;

import java.math.BigDecimal;

public record ExchangeRateRespDTO(
        int id,
        CurrencyRespDTO baseCurrency,
        CurrencyRespDTO targetCurrency,
        BigDecimal rate) {
}
