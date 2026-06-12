package model;

public record ExchangeRateTableProjection (int id, Currency baseCurrency, Currency targetCurrency, double rate) {
}

/*
    public ExchangeRateTableProjection(int id, Currency baseCurrency, Currency targetCurrency, double rate) {
        this.id = id;
        this.baseCurrency = baseCurrency;
        this.targetCurrency = targetCurrency;
        this.rate = rate;
    }


    @Override
    public String toString() {
        return "ExchangeRateTableProjection{"
                + "id=" + id
                + ", baseCurrency=" + baseCurrency
                + ", targetCurrency=" + targetCurrency
                + ", rate=" + rate
                + '}';
    }*/

