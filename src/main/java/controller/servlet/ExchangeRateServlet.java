package controller.servlet;

import dto.request.ExchangeRateCodePairDTO;
import dto.responce.ExchangeRateRespDTO;
import exception.ExchangeRatePairDoesNotExistException;
import exception.InternalServerException;
import service.ExchangeRatesService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@WebServlet(urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends BaseApiServlet {
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

        String body = readMethodBody(req);

        if (body.isBlank()) {
            doWriteError(
                    resp,
                    "method body is empty",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        BigDecimal normalizedRate;
        try {
            normalizedRate = parseRateFromPatchBody(body);
        } catch (IllegalArgumentException e) {
            doWriteError(
                    resp,
                    "invalid/missing rate",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return;
        }

        doCustomPatch(req, resp, normalizedRate);
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

    private BigDecimal parseRateFromPatchBody(String body) {
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

        BigDecimal normalizedRate = new BigDecimal(rate).setScale(6, RoundingMode.HALF_UP);
        if (normalizedRate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException();
        }

        return normalizedRate;
    }

    protected void doCustomPatch(HttpServletRequest req, HttpServletResponse resp, BigDecimal rate) throws IOException {
        String cleanCodesPair = resolveCleanPairCode(req, resp);
        if (cleanCodesPair == null) {
            return;
        }

        try {
            ExchangeRateRespDTO pair = service.updateExchangeRatePairRate(
                    new ExchangeRateCodePairDTO(cleanCodesPair.substring(0, PROPER_CODES_LENGTH / 2),
                            cleanCodesPair.substring(PROPER_CODES_LENGTH / 2, PROPER_CODES_LENGTH)
                    ),
                    rate
            );
            doWriteResponse(resp, pair, HttpServletResponse.SC_OK);
        } catch (ExchangeRatePairDoesNotExistException e) {
            doWriteError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String cleanCodesPair = resolveCleanPairCode(req, resp);
        if (cleanCodesPair == null) {
            return;
        }

        try {
            ExchangeRateRespDTO pair = service.findExchangeRatePair(
                    new ExchangeRateCodePairDTO(cleanCodesPair.substring(0, PROPER_CODES_LENGTH / 2),
                            cleanCodesPair.substring(PROPER_CODES_LENGTH / 2, PROPER_CODES_LENGTH)
                    )
            );
            doWriteResponse(resp, pair, HttpServletResponse.SC_OK);
        } catch (ExchangeRatePairDoesNotExistException e) {
            doWriteError(resp, e.getMessage(), HttpServletResponse.SC_NOT_FOUND);
        } catch (InternalServerException e) {
            doWrite500Error(resp, e);
        }
    }

    private String resolveCleanPairCode(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawCodesPair = req.getPathInfo();

        if (hasMissingCode(rawCodesPair)) {
            doWriteError(
                    resp,
                    "Currency pair code (one or both) is not provided",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return null;
        }

        String cleanCodesPair = rawCodesPair.substring(1);
        if (hasWrongCodePairLength(cleanCodesPair)) {
            doWriteError(
                    resp,
                    "Currency pair code has wrong length",
                    HttpServletResponse.SC_BAD_REQUEST
            );
            return null;
        }
        return cleanCodesPair;
    }

    private boolean hasWrongCodePairLength(String cleanCodesPair) {
        return cleanCodesPair.length() != PROPER_CODES_LENGTH;
    }

    private boolean hasMissingCode(String code) {
        return code == null || "/".equals(code);
    }
}
