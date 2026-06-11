package converter;

import dto.responce.CurrencyRespDTO;
import model.Currency;

public class CurrencyToCurrencyDTOConverter implements Converter<CurrencyRespDTO, Currency> {
    @Override
    public CurrencyRespDTO convert(Currency currency) {
        return new CurrencyRespDTO(
                currency.getId(),
                currency.getFullName(),
                currency.getCode(),
                currency.getSign()
        );
    }
}
