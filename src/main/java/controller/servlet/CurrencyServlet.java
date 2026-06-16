package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.responce.CurrencyRespDTO;
import dto.responce.ErrorResponseDTO;
import exception.CurrencyAlreadyExistsException;
import exception.CurrencyIsNotFoundException;
import exception.InternalServerException;
import service.CurrenciesService;
import service.CurrencyService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/currency/*")
public class CurrencyServlet extends HttpServlet {

    private CurrencyService currencyService;

    public CurrencyServlet() {
        this.currencyService = new CurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        String rawCode = req.getPathInfo();
        ObjectMapper objectMapper = new ObjectMapper();

        if (hasMissingCode(rawCode)) {
            sendBadRequest(resp, objectMapper);
            return;
        }

        try {
            CurrencyRespDTO foundCurrency = currencyService.findCurrency(rawCode.substring(1));
            resp.setStatus(HttpServletResponse.SC_OK);
            String currencyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(foundCurrency);

            resp.getWriter().write(currencyJson);
        }  catch (CurrencyIsNotFoundException e) {
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

    private boolean hasMissingCode(String code) {
        return code == null || "/".equals(code);
    }

    private void sendBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("Currency code is not provided")
        );

        resp.getWriter().write(errorJson);
    }
}
