package controller.servlet;

import controller.servlet.util.ValidationsUtil;
import dto.request.CurrencyReqDto;
import dto.response.CurrencyRespDto;
import exception.CurrencyAlreadyExistsException;
import exception.InternalServerException;
import service.CurrenciesService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/currencies")
public class CurrenciesServlet extends BaseApiServlet {
    private final static int MAX_SIGN_LENGTH = 3;
    private final static int PROPER_CODE_LENGTH = 3;

    private final CurrenciesService currencyService;

    public CurrenciesServlet() {
        this.currencyService = new CurrenciesService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyRespDto> allCurrencies = currencyService.findAllCurrencies();
            writeResponse(resp, allCurrencies, HttpServletResponse.SC_OK);
        } catch (InternalServerException e) {
            write500Error(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (ValidationsUtil.hasMissingRequiredFields(name, code, sign)) {
            writeError(
                    resp,
                    "required field(s) is/are missing",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        name = ValidationsUtil.trimNameOrSign(name);
        code = ValidationsUtil.normalizeCode(code);
        sign = ValidationsUtil.trimNameOrSign(sign);

        if (ValidationsUtil.hasLengthNotEqualToExpected(code, PROPER_CODE_LENGTH)) {
            writeError(
                    resp,
                    "currency code has wrong length",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        if (ValidationsUtil.hasLengthMoreThanExpected(sign, MAX_SIGN_LENGTH)) {
            writeError(
                    resp,
                    "sign has wrong length",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            CurrencyRespDto currency = currencyService.createCurrency(
                    new CurrencyReqDto(
                            name,
                            code,
                            sign
                    ));
            writeResponse(resp, currency, HttpServletResponse.SC_CREATED);
        } catch (CurrencyAlreadyExistsException e) {
            writeError(resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (InternalServerException e) {
            write500Error(resp, e);
        }
    }
}
