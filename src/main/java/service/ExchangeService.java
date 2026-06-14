package service;

import dao.ExchangeRatesDAO;
import dto.request.ExchangeRateCodePairDTO;
import dto.request.ExchangeRequestDTO;
import dto.responce.ExchangeRateExtendedRespDTO;
import exception.CrossCourseNotFoundException;
import model.Currency;
import model.ExchangeRateTableProjection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {

    private ExchangeRatesDAO exchangeRatesDAO;

    public ExchangeService() {
        this.exchangeRatesDAO = new ExchangeRatesDAO();
    }

    public ExchangeRateExtendedRespDTO calculateExchange(ExchangeRequestDTO exchangeRequestDTO) {
        String baseCode = exchangeRequestDTO.baseCode();
        String targetCode = exchangeRequestDTO.targetCode();

        Optional<ExchangeRateTableProjection> optionalProjection = exchangeRatesDAO
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDTO(baseCode, targetCode));
        if (optionalProjection.isPresent()) {
            return buildStraightCourse(exchangeRequestDTO, optionalProjection.get());
        }

        optionalProjection = exchangeRatesDAO
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDTO(targetCode, baseCode));
        if (optionalProjection.isPresent()) {
            return buildBackWadCourse(exchangeRequestDTO, optionalProjection.get());
        }

        Optional<ExchangeRateTableProjection> usdBasedProjectionForBase = exchangeRatesDAO
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDTO("USD", baseCode));
        Optional<ExchangeRateTableProjection> usdBasedProjectionForTarget = exchangeRatesDAO
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDTO("USD", targetCode));
        if (usdBasedProjectionForBase.isPresent() && usdBasedProjectionForTarget.isPresent()) {
            return buildCrossUSDCourse(
                    exchangeRequestDTO,
                    usdBasedProjectionForBase.get(),
                    usdBasedProjectionForTarget.get()
            );
        }

        throw new CrossCourseNotFoundException("exchange rate for this pair is not found");
    }

    private ExchangeRateExtendedRespDTO buildStraightCourse(
            ExchangeRequestDTO exchangeRequestDTO,
            ExchangeRateTableProjection projection
    ) {
        BigDecimal amount = exchangeRequestDTO.amount();
        BigDecimal rate = BigDecimal.valueOf(projection.rate()).setScale(6, RoundingMode.HALF_UP);

        return buildExchangeResponse(
                projection.baseCurrency(),
                projection.targetCurrency(),
                rate,
                amount
        );
    }

    private ExchangeRateExtendedRespDTO buildBackWadCourse(
            ExchangeRequestDTO exchangeRequestDTO,
            ExchangeRateTableProjection projection
    ) {
        BigDecimal amount = exchangeRequestDTO.amount();
        BigDecimal rate = countSimpleRate(BigDecimal.valueOf(projection.rate()));

        return buildExchangeResponse(
                projection.targetCurrency(),
                projection.baseCurrency(),
                rate,
                amount
        );
    }

    private BigDecimal countSimpleRate(BigDecimal rate) {
        return BigDecimal.ONE.divide(rate, 6, RoundingMode.HALF_UP);
    }

    private ExchangeRateExtendedRespDTO buildCrossUSDCourse(
            ExchangeRequestDTO exchangeRequestDTO,
            ExchangeRateTableProjection usdBasedProjectionForBase,
            ExchangeRateTableProjection usdBasedProjectionForTarget
    ) {
        BigDecimal amount = exchangeRequestDTO.amount();
        double basedRate = usdBasedProjectionForBase.rate();
        double targetRate = usdBasedProjectionForTarget.rate();
        BigDecimal rate = countComplexRate(BigDecimal.valueOf(basedRate), BigDecimal.valueOf(targetRate));

        return buildExchangeResponse(
                usdBasedProjectionForBase.targetCurrency(),
                usdBasedProjectionForTarget.targetCurrency(),
                rate,
                amount
        );
    }

    private BigDecimal countComplexRate(BigDecimal baseRate, BigDecimal targetRate) {
        return targetRate.divide(baseRate, 6, RoundingMode.HALF_UP);
    }

    private ExchangeRateExtendedRespDTO buildExchangeResponse(
            Currency baseCurrency,
            Currency targetCurrency,
            BigDecimal rate,
            BigDecimal amount
    ) {
        return new ExchangeRateExtendedRespDTO(
                baseCurrency,
                targetCurrency,
                rate,
                amount,
                countAmount(amount, rate)
        );
    }

    private BigDecimal countAmount(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
