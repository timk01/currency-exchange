package dto.responce;

public record ExchangeRateRespDTO(
        int id,
        CurrencyRespDTO baseCurrency,
        CurrencyRespDTO targetCurrency,
        double rate) {
}
