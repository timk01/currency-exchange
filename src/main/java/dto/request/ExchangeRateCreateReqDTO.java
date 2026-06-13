package dto.request;

public record ExchangeRateCreateReqDTO(String baseCode, String targetCode, double rate) {
}
