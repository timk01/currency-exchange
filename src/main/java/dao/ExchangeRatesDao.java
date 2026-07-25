package dao;

import dto.request.ExchangeRateCodePairDto;
import model.ExchangeRateTableProjection;
import model.ExchangeRateTransfer;

import java.util.List;
import java.util.Optional;

public interface ExchangeRatesDao {
    List<ExchangeRateTableProjection> findAll();

    ExchangeRateTableProjection findExchangeRatePair(ExchangeRateCodePairDto pair);

    Optional<ExchangeRateTableProjection> optionalFindExchangeRatePair(ExchangeRateCodePairDto pair);

    ExchangeRateTableProjection update(ExchangeRateCodePairDto pair, double rate);

    ExchangeRateTableProjection insert(ExchangeRateTransfer transferData);
}
