package controller.servlet;

import controller.servlet.util.ValidationsUtil;
import dto.request.ExchangeRateCreateReqDto;
import dto.response.ExchangeRateRespDto;
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
    private final static int CODE_LENGTH = 3;

    private final ExchangeRatesService exchangeRatesService;

    public ExchangeRatesServlet() {
        this.exchangeRatesService = new ExchangeRatesService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<ExchangeRateRespDto> rates = exchangeRatesService.findAllExchangeRates();
            writeResponse(resp, rates, HttpServletResponse.SC_OK);
        } catch (InternalServerException e) {
            write500Error(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCode = req.getParameter("baseCurrencyCode");
        String targetCode = req.getParameter("targetCurrencyCode");
        String rate = req.getParameter("rate");

        if (ValidationsUtil.hasMissingRequiredFields(baseCode, targetCode, rate)) {
            writeError(resp,
                    "Required field(s) is/are missing",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        baseCode = ValidationsUtil.normalizeCode(baseCode);
        targetCode = ValidationsUtil.normalizeCode(targetCode);

        if (ValidationsUtil.hasLengthNotEqualToExpected(baseCode, CODE_LENGTH)
                || ValidationsUtil.hasLengthNotEqualToExpected(targetCode, CODE_LENGTH)) {
            writeError(resp,
                    "baseCode and targetCode should have proper length",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        if (baseCode.equals(targetCode)) {
            writeError(resp,
                    "baseCode and targetCode should be different",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        rate = rate.trim();
        BigDecimal normalizedRate;
        try {
            normalizedRate = new BigDecimal(rate).setScale(6, RoundingMode.HALF_UP);
            if (normalizedRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            writeError(resp,
                    "Invalid rate",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            ExchangeRateRespDto exchangeRate = exchangeRatesService.createExchangeRate(
                    new ExchangeRateCreateReqDto(
                            baseCode,
                            targetCode,
                            normalizedRate
                    )
            );
            writeResponse(resp, exchangeRate, HttpServletResponse.SC_CREATED);
        } catch (CurrencyIsNotFoundException e) {
            writeError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (ExchangeRateAlreadyExistsException e) {
            writeError(resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (InternalServerException e) {
            write500Error(resp, e);
        }
    }
}
