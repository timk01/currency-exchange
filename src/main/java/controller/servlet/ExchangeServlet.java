package controller.servlet;

import dto.request.ExchangeRequestDTO;
import dto.responce.ExchangeRateExtendedRespDTO;
import exception.CrossCourseNotFoundException;
import exception.InternalServerException;
import service.ExchangeService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;

@WebServlet(urlPatterns = "/exchange")
public class ExchangeServlet extends BaseApiServlet {
    private final ExchangeService service;

    public ExchangeServlet() {
        this.service = new ExchangeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String baseCode = req.getParameter("from");
        String targetCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        if (hasMissingRequiredFields(baseCode, targetCode, amount)) {
            doWriteError(
                    resp,
                    "Required field(s) is/are missing",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amount);
            if (parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }
        } catch (NumberFormatException e) {
            doWriteError(
                    resp,
                    "Invalid amount",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        try {
            ExchangeRateExtendedRespDTO crossCourse = service.calculateExchange(
                    new ExchangeRequestDTO(baseCode, targetCode, parsedAmount)
            );
            doWriteResponse(resp, crossCourse, HttpServletResponse.SC_OK);
        } catch (CrossCourseNotFoundException e) {
            doWriteError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }

    private boolean hasMissingRequiredFields(String baseCode, String targetCode, String amount) {
        return baseCode == null || baseCode.isBlank()
                || targetCode == null || targetCode.isBlank()
                || amount == null || amount.isBlank();
    }
}
