package dto.request;

public record ExchangeRateReqDTO(String baseCode, String targetCode, double rate) {
}
