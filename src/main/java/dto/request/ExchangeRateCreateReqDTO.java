package dto.request;

import java.math.BigDecimal;

public record ExchangeRateCreateReqDTO(String baseCode, String targetCode, BigDecimal rate) {
}
