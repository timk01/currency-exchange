package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.request.ExchangeRateCodePairDTO;
import dto.responce.ErrorResponseDTO;
import dto.responce.ExchangeRateRespDTO;
import exception.ExchangeRatePairDoesNotExistException;
import exception.InternalServerException;
import service.ExchangeRatesService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@WebServlet(urlPatterns = "/exchangeRate/*")
public class ExchangeRateServlet extends HttpServlet {

    private final static int PROPER_CODES_LENGTH = 6;

    private final ExchangeRatesService service;

    public ExchangeRateServlet() {
        this.service = new ExchangeRatesService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        String rawCodesPair = req.getPathInfo();
        ObjectMapper objectMapper = new ObjectMapper();

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
