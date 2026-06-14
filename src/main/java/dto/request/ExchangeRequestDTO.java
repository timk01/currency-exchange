package dto.request;

import java.math.BigDecimal;

public record ExchangeRequestDTO(String baseCode, String targetCode, BigDecimal amount) {
}
