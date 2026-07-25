package dto.response;

import java.math.BigDecimal;

public record ExchangeRespDTO(CurrencyRespDto baseCurrency,
                              CurrencyRespDto targetCurrency,
                              BigDecimal rate,
                              BigDecimal amount,
                              BigDecimal convertedAmount) {
}

