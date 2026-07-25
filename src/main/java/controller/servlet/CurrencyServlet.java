package controller.servlet;

import controller.servlet.util.ValidationsUtil;
import dto.response.CurrencyRespDto;
import exception.CurrencyIsNotFoundException;
import exception.InternalServerException;
import service.CurrencyService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/currency/*")
public class CurrencyServlet extends BaseApiServlet {
    private final static int PROPER_CODE_LENGTH = 3;

    private final CurrencyService currencyService;

    public CurrencyServlet() {
        this.currencyService = new CurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawCode = req.getPathInfo();

        if (ValidationsUtil.hasMissingPathInfo(rawCode)) {
            writeError(
                    resp,
                    "Currency code is not provided",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        String normalizedCode = ValidationsUtil.normalizeCode(rawCode.substring(1));

        if (ValidationsUtil.hasLengthNotEqualToExpected(normalizedCode, PROPER_CODE_LENGTH)) {
            writeError(
                    resp,
                    "currency code has wrong length",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            CurrencyRespDto foundCurrency = currencyService.findCurrency(
                    normalizedCode
            );
            writeResponse(resp, foundCurrency, HttpServletResponse.SC_OK);
        } catch (CurrencyIsNotFoundException e) {
            writeError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (InternalServerException e) {
            write500Error(resp, e);
        }
    }
}
