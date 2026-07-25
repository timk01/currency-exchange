package dto.request;

import java.math.BigDecimal;

public record ExchangeRateCreateReqDto(String baseCode, String targetCode, BigDecimal rate) {
}
