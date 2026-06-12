package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.request.CurrencyReqDTO;
import dto.responce.CurrencyRespDTO;
import dto.responce.ErrorResponseDTO;
import exception.CurrencyAlreadyExistsException;
import exception.InternalServerException;
import service.CurrenciesService;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/currencies")
public class CurrenciesServlet extends HttpServlet {

    private CurrenciesService currencyService;

    public CurrenciesServlet() {
        this.currencyService = new CurrenciesService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json; charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<CurrencyRespDTO> allCurrencies = currencyService.findAllCurrencies();
            String currenciesJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(allCurrencies);

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

        String name = req.getParameter("name");
        String code = req.getParameter("code");
        String sign = req.getParameter("sign");

        ObjectMapper objectMapper = new ObjectMapper();

        if (hasMissingRequiredFields(name, code, sign)) {
            sendBadRequest(resp, objectMapper);
            return;
        }

        try {
            CurrencyRespDTO currency = currencyService.createCurrency(new CurrencyReqDTO(name, code, sign));
            resp.setStatus(HttpServletResponse.SC_CREATED);
            String currencyJson = objectMapper
                    .writerWithDefaultPrettyPrinter()
                    .writeValueAsString(currency);
            resp.getWriter().write(currencyJson);
        } catch (CurrencyAlreadyExistsException e) {
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

    private boolean hasMissingRequiredFields(String name, String code, String sign) {
        return name == null || name.isBlank()
                || code == null || code.isBlank()
                || sign == null || sign.isBlank();
    }

    private void sendBadRequest(HttpServletResponse resp, ObjectMapper objectMapper) throws IOException {
        resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        String errorJson = objectMapper.writeValueAsString(
                new ErrorResponseDTO("Required field(s) is/are missing")
        );

        resp.getWriter().write(errorJson);
    }
}
