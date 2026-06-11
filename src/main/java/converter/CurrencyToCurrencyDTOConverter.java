package converter;

import dto.responce.CurrencyDTO;
import model.Currency;

public class CurrencyToCurrencyDTOConverter implements Converter<CurrencyDTO, Currency> {
    @Override
    public CurrencyDTO convert(Currency currency) {
        return new CurrencyDTO(
                currency.getId(),
                currency.getFullName(),
                currency.getCode(),
                currency.getSign()
        );
    }
}
