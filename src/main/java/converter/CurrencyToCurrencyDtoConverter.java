package converter;

import dto.response.CurrencyRespDto;
import model.Currency;

public class CurrencyToCurrencyDtoConverter implements Converter<CurrencyRespDto, Currency> {
    @Override
    public CurrencyRespDto convert(Currency currency) {
        return new CurrencyRespDto(
                currency.getId(),
                currency.getFullName(),
                currency.getCode(),
                currency.getSign()
        );
    }
}
