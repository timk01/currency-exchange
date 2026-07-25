CREATE TABLE IF NOT EXISTS Currencies
(
    ID       INTEGER PRIMARY KEY AUTOINCREMENT,
    Code     VARCHAR(3) NOT NULL UNIQUE CHECK (length(Code) = 3),
    FullName VARCHAR(32) NOT NULL,
    Sign     VARCHAR(5) NOT NULL
);

CREATE TABLE IF NOT EXISTS ExchangeRates
(
    ID               INTEGER PRIMARY KEY AUTOINCREMENT,
    BaseCurrencyId   INTEGER NOT NULL,
    TargetCurrencyId INTEGER NOT NULL,
    Rate             REAL NOT NULL CHECK (Rate > 0),

    FOREIGN KEY (BaseCurrencyId)
        REFERENCES Currencies (ID),

    FOREIGN KEY (TargetCurrencyId)
        REFERENCES Currencies (ID),

    CHECK (BaseCurrencyId <> TargetCurrencyId),

    UNIQUE (BaseCurrencyId, TargetCurrencyId)
);
