package dto.responce;

import model.Currency;

import java.math.BigDecimal;

public record ExchangeRateExtendedRespDTO(Currency baseCurrency,
                                          Currency targetCurrency,
                                          BigDecimal rate,
                                          BigDecimal amount,
                                          BigDecimal convertedAmount) {
}

