package model;

public record ExchangeRate(int id, int baseCurrencyId, int targetCurrencyId, double rate) {
}
