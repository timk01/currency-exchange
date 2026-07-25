package service;

import converter.Converter;
import converter.CurrencyToCurrencyDtoConverter;
import dao.ExchangeRatesDao;
import dao.ExchangeRatesImpl;
import dto.request.ExchangeRateCodePairDto;
import dto.request.ExchangeReqDto;
import dto.response.CurrencyRespDto;
import dto.response.ExchangeRespDTO;
import exception.ExchangeRateNotFoundException;
import model.Currency;
import model.ExchangeRateTableProjection;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;

public class ExchangeService {
    private static final String CROSS_COURSE_CONSTANT = "USD";

    private final ExchangeRatesDao exchangeRatesDao;

    private final Converter<CurrencyRespDto, Currency> dtoCurrencyConverter;

    public ExchangeService() {
        this.exchangeRatesDao = new ExchangeRatesImpl();
        this.dtoCurrencyConverter = new CurrencyToCurrencyDtoConverter();
    }

    public ExchangeRespDTO calculateExchange(ExchangeReqDto exchangeReqDto) {
        String baseCode = exchangeReqDto.baseCode();
        String targetCode = exchangeReqDto.targetCode();

        Optional<ExchangeRateTableProjection> optionalProjection = exchangeRatesDao
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDto(baseCode, targetCode));
        if (optionalProjection.isPresent()) {
            return buildStraightCourse(exchangeReqDto, optionalProjection.get());
        }

        optionalProjection = exchangeRatesDao
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDto(targetCode, baseCode));
        if (optionalProjection.isPresent()) {
            return buildBackWardCourse(exchangeReqDto, optionalProjection.get());
        }

        Optional<ExchangeRateTableProjection> usdBasedProjectionForBase = exchangeRatesDao
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDto(CROSS_COURSE_CONSTANT, baseCode));
        Optional<ExchangeRateTableProjection> usdBasedProjectionForTarget = exchangeRatesDao
                .optionalFindExchangeRatePair(new ExchangeRateCodePairDto(CROSS_COURSE_CONSTANT, targetCode));
        if (usdBasedProjectionForBase.isPresent() && usdBasedProjectionForTarget.isPresent()) {
            return buildCrossUSDCourse(
                    exchangeReqDto,
                    usdBasedProjectionForBase.get(),
                    usdBasedProjectionForTarget.get()
            );
        }

        throw new ExchangeRateNotFoundException(String.format("Exchange rate '%s' - '%s' is not available",
                exchangeReqDto.baseCode(), exchangeReqDto.targetCode()));
    }

    private ExchangeRespDTO buildStraightCourse(
            ExchangeReqDto exchangeReqDTO,
            ExchangeRateTableProjection projection
    ) {
        BigDecimal amount = exchangeReqDTO.amount();
        BigDecimal rate = BigDecimal.valueOf(projection.rate()).setScale(6, RoundingMode.HALF_UP);

        return buildExchangeResponse(
                projection.baseCurrency(),
                projection.targetCurrency(),
                rate,
                amount
        );
    }

    private ExchangeRespDTO buildBackWardCourse(
            ExchangeReqDto exchangeReqDTO,
            ExchangeRateTableProjection projection
    ) {
        BigDecimal amount = exchangeReqDTO.amount();
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

    private ExchangeRespDTO buildCrossUSDCourse(
            ExchangeReqDto exchangeReqDTO,
            ExchangeRateTableProjection usdBasedProjectionForBase,
            ExchangeRateTableProjection usdBasedProjectionForTarget
    ) {
        BigDecimal amount = exchangeReqDTO.amount();
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

    private ExchangeRespDTO buildExchangeResponse(
            Currency baseCurrency,
            Currency targetCurrency,
            BigDecimal rate,
            BigDecimal amount
    ) {

        CurrencyRespDto baseDto = dtoCurrencyConverter.convert(baseCurrency);
        CurrencyRespDto targetDto = dtoCurrencyConverter.convert(targetCurrency);

        return new ExchangeRespDTO(
                baseDto,
                targetDto,
                rate,
                amount,
                countAmount(amount, rate)
        );
    }

    private BigDecimal countAmount(BigDecimal amount, BigDecimal rate) {
        return amount.multiply(rate).setScale(2, RoundingMode.HALF_UP);
    }
}
