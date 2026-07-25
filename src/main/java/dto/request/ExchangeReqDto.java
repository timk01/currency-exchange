package dto.request;

import java.math.BigDecimal;

public record ExchangeReqDto(String baseCode, String targetCode, BigDecimal amount) {
}
