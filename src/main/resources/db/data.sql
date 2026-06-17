INSERT INTO Currencies (Code, FullName, Sign)
VALUES
    ('USD', 'US Dollar', '$'),
    ('EUR', 'Euro', '€'),
    ('RUB', 'Russian Ruble', '₽');

INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
VALUES
    (1, 2, 0.9),
    (1, 3, 90.0),
    (2, 1, 1.1),
    (2, 3, 100.0),
    (3, 1, 0.011),
    (3, 2, 0.01);