package controller.servlet;

import controller.servlet.util.ValidationsUtil;
import dto.request.ExchangeReqDto;
import dto.response.ExchangeRespDTO;
import exception.ExchangeRateNotFoundException;
import exception.InternalServerException;
import service.ExchangeService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(urlPatterns = "/exchange")
public class ExchangeServlet extends BaseApiServlet {
    private final static int PROPER_CODE_LENGTH = 3;

    private final ExchangeService service;

    public ExchangeServlet() {
        this.service = new ExchangeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCode = req.getParameter("from");
        String targetCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        if (ValidationsUtil.hasMissingRequiredFields(baseCode, targetCode, amount)) {
            writeError(
                    resp,
                    "Required field(s) is/are missing",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        baseCode = ValidationsUtil.normalizeCode(baseCode);
        targetCode = ValidationsUtil.normalizeCode(targetCode);

        if (ValidationsUtil.hasLengthNotEqualToExpected(baseCode, PROPER_CODE_LENGTH)
                || ValidationsUtil.hasLengthNotEqualToExpected(targetCode, PROPER_CODE_LENGTH)) {
            writeError(
                    resp,
                    "currency code has wrong length",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        amount = amount.trim();
        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amount);
            if (parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            writeError(
                    resp,
                    "Invalid amount",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            ExchangeRespDTO crossCourse = service.calculateExchange(
                    new ExchangeReqDto(
                            baseCode,
                            targetCode,
                            parsedAmount)
            );
            writeResponse(resp, crossCourse, HttpServletResponse.SC_OK);
        } catch (ExchangeRateNotFoundException e) {
            writeError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (InternalServerException e) {
            write500Error(resp, e);
        }
    }
}
