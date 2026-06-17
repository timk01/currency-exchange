package model;

public record ExchangeRateTableProjection(int id, Currency baseCurrency, Currency targetCurrency, double rate) {
}

