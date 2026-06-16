package controller.servlet;

import dto.responce.CurrencyRespDTO;
import exception.CurrencyIsNotFoundException;
import exception.InternalServerException;
import service.CurrencyService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/currency/*")
public class CurrencyServlet extends BaseApiServlet {

    private final CurrencyService currencyService;

    public CurrencyServlet() {
        this.currencyService = new CurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawCode = req.getPathInfo();

        if (hasMissingCode(rawCode)) {
            doWriteError(
                    resp,
                    "Currency code is not provided",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            CurrencyRespDTO foundCurrency = currencyService.findCurrency(rawCode.substring(1));
            doWriteResponse(resp, foundCurrency, HttpServletResponse.SC_OK);
        } catch (CurrencyIsNotFoundException e) {
            doWriteError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }

    private boolean hasMissingCode(String code) {
        return code == null || "/".equals(code);
    }
}
