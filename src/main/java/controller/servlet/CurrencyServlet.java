package controller.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.responce.CurrencyDTO;
import dto.responce.ErrorResponseDTO;
import exception.InternalServerException;
import service.CurrencyService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;

@WebServlet(urlPatterns = "/currencies")
public class CurrencyServlet extends HttpServlet {

    private CurrencyService currencyService;
    public CurrencyServlet() {
        this.currencyService = new CurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        ObjectMapper objectMapper = new ObjectMapper();
        try {
            List<CurrencyDTO> allCurrencies = currencyService.findAllCurrencies();
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
}
