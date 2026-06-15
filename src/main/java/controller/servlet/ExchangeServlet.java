package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.request.ExchangeRequestDTO;
import dto.responce.ErrorResponseDTO;
import dto.responce.ExchangeRateExtendedRespDTO;
import exception.CrossCourseNotFoundException;
import exception.InternalServerException;
import service.ExchangeService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@WebServlet(urlPatterns = "/exchange")
public class ExchangeServlet extends HttpServlet {
    private final ExchangeService service;

    public ExchangeServlet() {
        this.service = new ExchangeService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String baseCode = req.getParameter("from");
        String targetCode = req.getParameter("to");
        String amount = req.getParameter("amount");

        ObjectMapper objectMapper = new ObjectMapper();

        if (hasMissingRequiredFields(baseCode, targetCode, amount)) {
            sendMissingFieldsBadRequest(resp, objectMapper);
            return;
        }

        BigDecimal parsedAmount;
        try {
            parsedAmount = new BigDecimal(amount);
            if (parsedAmount.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            sendInvalidAmountBadRequest(resp, objectMapper);
            return;
        }

        try {
            ExchangeRateExtendedRespDTO crossCourse = service.calculateExchange(
                    new ExchangeRequestDTO(baseCode, targetCode, parsedAmount)
            );
            resp.setStatus(HttpServletResponse.SC_OK);
            String exchangeRates = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(crossCourse);
            resp.getWriter().write(exchangeRates);
        } catch (CrossCourseNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        } catch (InternalServerException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        }
    }

    private boolean hasMissingRequiredFields(String baseCode, String targetCode, String amount) {
        return baseCode == null || baseCode.isBlank()
                || targetCode == null || targetCode.isBlank()
                || amount == null || amount.isBlank();
    }

    private void sendInvalidAmountBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("Invalid amount")
        );

        resp.getWriter().write(errorJson);
    }

    private void sendMissingFieldsBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("Required field(s) is/are missing")
        );

        resp.getWriter().write(errorJson);
    }

    private void sendSameCurrencyPairBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("baseCode and targetCode should be different")
        );

        resp.getWriter().write(errorJson);
    }
}
