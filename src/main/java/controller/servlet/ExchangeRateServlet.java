package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.request.ExchangeRateCodePairDTO;
import dto.responce.ErrorResponseDTO;
import dto.responce.ExchangeRateRespDTO;
import exception.ExchangeRatePairDoesNotExistException;
import exception.InternalServerException;
import service.ExchangeRatesService;

import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;

@WebServlet(urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private final static int PROPER_CODES_LENGTH = 6;
    private final static String RATE = "rate";

    private final ExchangeRatesService service;

    public ExchangeRateServlet() {
        this.service = new ExchangeRatesService();
    }

    @Override
    public void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (!"PATCH".equalsIgnoreCase(req.getMethod())) {
            super.service(req, resp);
            return;
        }

        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");
        ObjectMapper objectMapper = new ObjectMapper();

        String body = readMethodBody(req);

        if (body.isBlank()) {
            sendBadRequestForMissedBody(resp, objectMapper);
            return;
        }

        double parsedRate;
        try {
            parsedRate = parseRateFromPatchBody(body);
        } catch (IllegalArgumentException e) {
            sendInvalidRateBadRequest(resp, objectMapper);
            return;
        }

        doCustomPatch(req, resp, parsedRate);
    }

    private String readMethodBody(HttpServletRequest req) throws IOException {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
            return sb.toString();
        }
    }

    private double parseRateFromPatchBody(String body) {
        String[] split = body.split("=", 2);

        if (split.length != 2) {
            throw new IllegalArgumentException();
        }
        if (!RATE.equals(split[0])) {
            throw new IllegalArgumentException();
        }

        String rate = split[1];

        if (rate.isBlank()) {
            throw new IllegalArgumentException();
        }

        double parsedRate = Double.parseDouble(rate);

        if (parsedRate <= 0) {
            throw new IllegalArgumentException();
        }

        return parsedRate;
    }

    private static void sendBadRequestForMissedBody(HttpServletResponse resp, ObjectMapper objectMapper) throws
            IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("method body is empty")
        );

        resp.getWriter().write(errorJson);
    }

    private void sendInvalidRateBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("Invalid/missing rate")
        );

        resp.getWriter().write(errorJson);
    }

    protected void doCustomPatch(HttpServletRequest req, HttpServletResponse resp, double rate) throws IOException {
        ObjectMapper objectMapper = new ObjectMapper();
        String rawCodesPair = req.getPathInfo();

        if (hasMissingCode(rawCodesPair)) {
            sendBadRequest(resp, objectMapper);
            return;
        }

        String cleanCodesPair = rawCodesPair.substring(1);
        if (cleanCodesPair.length() != PROPER_CODES_LENGTH) {
            sendBadRequest(resp, objectMapper);
            return;
        }

        try {
            ExchangeRateRespDTO pair = service.updateExchangeRatePairRate(
                    new ExchangeRateCodePairDTO(cleanCodesPair.substring(0, PROPER_CODES_LENGTH / 2),
                            cleanCodesPair.substring(PROPER_CODES_LENGTH / 2, PROPER_CODES_LENGTH)
                    ),
                    rate
            );
            resp.setStatus(HttpServletResponse.SC_OK);
            String currencyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(pair);

            resp.getWriter().write(currencyJson);
        } catch (ExchangeRatePairDoesNotExistException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        } catch (
                InternalServerException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        String rawCodesPair = req.getPathInfo();

        if (hasMissingCode(rawCodesPair)) {
            sendBadRequest(resp, objectMapper);
            return;
        }

        String cleanCodesPair = rawCodesPair.substring(1);
        if (cleanCodesPair.length() != PROPER_CODES_LENGTH) {
            sendBadRequest(resp, objectMapper);
            return;
        }

        try {
            ExchangeRateRespDTO pair = service.findExchangeRatePair(
                    new ExchangeRateCodePairDTO(cleanCodesPair.substring(0, PROPER_CODES_LENGTH / 2),
                            cleanCodesPair.substring(PROPER_CODES_LENGTH / 2, PROPER_CODES_LENGTH)
                    )
            );
            resp.setStatus(HttpServletResponse.SC_OK);
            String currencyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(pair);

            resp.getWriter().write(currencyJson);
        } catch (
                ExchangeRatePairDoesNotExistException e) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        } catch (
                InternalServerException e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            String errorJson = objectMapper.writeValueAsString(
                    new ErrorResponseDTO(e.getMessage())
            );

            resp.getWriter().write(errorJson);
        }
    }

    private boolean hasMissingCode(String code) {
        return code == null || "/".equals(code);
    }

    private void sendBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("Currency pair code (one or both) is not provided")
        );

        resp.getWriter().write(errorJson);
    }
}
