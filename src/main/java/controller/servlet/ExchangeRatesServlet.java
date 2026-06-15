package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.request.ExchangeRateCreateReqDTO;
import dto.responce.ErrorResponseDTO;
import dto.responce.ExchangeRateRespDTO;
import exception.CurrencyIsNotFoundException;
import exception.ExchangeRateAlreadyExistsException;
import exception.InternalServerException;
import service.ExchangeRatesService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@WebServlet(urlPatterns = "/exchangeRates")
public class ExchangeRatesServlet extends HttpServlet {

    private ExchangeRatesService exchangeRatesService;

    public ExchangeRatesServlet() {
        this.exchangeRatesService = new ExchangeRatesService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<ExchangeRateRespDTO> rates = exchangeRatesService.findAllExchangeRates();
            resp.setStatus(HttpServletResponse.SC_OK);
            String currenciesJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(rates);

            resp.getWriter().write(currenciesJson);
        } catch (InternalServerException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String baseCode = req.getParameter("baseCurrencyCode");
        System.out.println(baseCode);
        String targetCode = req.getParameter("targetCurrencyCode");
        System.out.println(targetCode);
        String rate = req.getParameter("rate");
        System.out.println(rate);

        ObjectMapper objectMapper = new ObjectMapper();

        if (hasMissingRequiredFields(baseCode, targetCode, rate)) {
            sendMissingFieldsBadRequest(resp, objectMapper);
            return;
        }

        if (baseCode.equals(targetCode)) {
            sendSameCurrencyPairBadRequest(resp, objectMapper);
            return;
        }

        BigDecimal normalizedRate;
        try {
            normalizedRate = new BigDecimal(rate).setScale(6, RoundingMode.HALF_UP);
            if (normalizedRate.compareTo(BigDecimal.ZERO) <= 0) {
                throw new NumberFormatException();
            }

        } catch (NumberFormatException e) {
            sendInvalidRateBadRequest(resp, objectMapper);
            return;
        }

        try {
            ExchangeRateRespDTO exchangeRate = exchangeRatesService.createExchangeRate(
                    new ExchangeRateCreateReqDTO(
                            baseCode, targetCode, normalizedRate
                    )
            );
            resp.setStatus(HttpServletResponse.SC_CREATED);
            String exchangeRates = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(exchangeRate);
            resp.getWriter().write(exchangeRates);
        } catch (CurrencyIsNotFoundException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        } catch (ExchangeRateAlreadyExistsException e) {
            resp.setStatus(HttpServletResponse.SC_CONFLICT);
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

    private boolean hasMissingRequiredFields(String baseCode, String targetCode, String rate) {
        return baseCode == null || baseCode.isBlank()
                || targetCode == null || targetCode.isBlank()
                || rate == null || rate.isBlank();
    }

    private void sendInvalidRateBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("Invalid rate")
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
