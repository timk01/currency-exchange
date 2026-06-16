package controller.servlet;

import dto.request.CurrencyReqDTO;
import dto.responce.CurrencyRespDTO;
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

    private final CurrenciesService currencyService;

    public CurrenciesServlet() {
        this.currencyService = new CurrenciesService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        try {
            List<CurrencyRespDTO> allCurrencies = currencyService.findAllCurrencies();
            doWriteResponse(resp, allCurrencies, HttpServletResponse.SC_OK);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        if (hasMissingRequiredFields(name, code, sign)) {
            doWriteError(
                    resp,
                    "required field(s) is/are missing",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        if (hasWrongSignLength(sign)) {
            doWriteError(
                    resp,
                    "sign has wrong length",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            CurrencyRespDTO currency = currencyService.createCurrency(new CurrencyReqDTO(name, code, sign));
            doWriteResponse(resp, currency, HttpServletResponse.SC_CREATED);
        } catch (CurrencyAlreadyExistsException e) {
            doWriteError(resp, e.getMessage(), HttpServletResponse.SC_CONFLICT);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }

    private boolean hasMissingRequiredFields(String name, String code, String sign) {
        return name == null || name.isBlank()
                || code == null || code.isBlank()
                || sign == null || sign.isBlank();
    }

    private boolean hasWrongSignLength(String sign) {
        return sign.length() > MAX_SIGN_LENGTH;
    }
}
