package service;

import dao.CurrencyDAO;
import model.Currency;

import java.util.List;

public class CurrencyService {

    private CurrencyDAO currencyDAO;

    public CurrencyService() {
        this.currencyDAO = new CurrencyDAO();
    }

    public List<Currency>  findAllCurrencies() {
        return currencyDAO.findAll();
    }
}
