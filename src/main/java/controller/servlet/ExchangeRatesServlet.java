package controller.servlet;

import controller.servlet.util.ValidationsUtil;
import dto.request.ExchangeRateCreateReqDTO;
import dto.responce.ExchangeRateRespDTO;
import exception.CurrencyIsNotFoundException;
import exception.ExchangeRateAlreadyExistsException;
import exception.InternalServerException;
import service.ExchangeRatesService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@WebServlet(urlPatterns = "/exchangeRates")
public class ExchangeRatesServlet extends BaseApiServlet {

    private final ExchangeRatesService exchangeRatesService;

    public ExchangeRatesServlet() {
        this.exchangeRatesService = new ExchangeRatesService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateRespDTO> rates = exchangeRatesService.findAllExchangeRates();
            doWriteResponse(resp, rates, HttpServletResponse.SC_OK);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCode = req.getParameter("baseCurrencyCode");
        String targetCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        if (ValidationsUtil.hasMissingRequiredFields(baseCode, targetCode, rate)) {
            doWriteError(resp,
                    "Required field(s) is/are missing",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        if (baseCode.equals(targetCode)) {
            doWriteError(resp,
                    "baseCode and targetCode should be different",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        BigDecimal normalizedRate;
        try {
            normalizedRate = new BigDecimal(rate).setScale(6, RoundingMode.HALF_UP);
            if (normalizedRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            doWriteError(resp,
                    "Invalid rate",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            ExchangeRateRespDTO exchangeRate = exchangeRatesService.createExchangeRate(
                    new ExchangeRateCreateReqDTO(
                            baseCode, targetCode, normalizedRate
                    )
            );
            doWriteResponse(resp, exchangeRate, HttpServletResponse.SC_CREATED);
        } catch (CurrencyIsNotFoundException e) {
            doWriteError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ExchangeRateAlreadyExistsException e) {
            doWriteError(resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }
}
