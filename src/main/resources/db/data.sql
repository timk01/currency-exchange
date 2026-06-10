INSERT INTO Currencies (Code, FullName, Sign)
VALUES
    ('USD', 'US Dollar', '$'),
    ('EUR', 'Euro', '€'),
    ('RUB', 'Russian Ruble', '₽');

INSERT INTO ExchangeRates (BaseCurrencyId, TargetCurrencyId, Rate)
VALUES
    (1, 2, 0.9),   -- здесь и далее цифры скорее из потолка. читать как: за 1 доллар (1 колонка) ты платишь 0.9 евро (2)
    (1, 3, 90.0),
    (2, 1, 1.1),
    (2, 3, 100.0),
    (3, 1, 0.011),
    (3, 2, 0.01);