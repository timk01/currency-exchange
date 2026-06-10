package controller.servlet;

import dao.CurrencyDAO;
import model.Currency;
import service.CurrencyService;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet(urlPatterns = "/currencies")
public class HelloWorld extends HttpServlet {

    private CurrencyService currencyService;
    public HelloWorld() {
        this.currencyService = new CurrencyService();
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setCharacterEncoding("UTF-8");
        resp.setContentType("text/plain; charset=UTF-8");

        PrintWriter writer = resp.getWriter();
        List<Currency> allCurrencies = currencyService.findAllCurrencies();
        writer.write(allCurrencies.toString());
    }
}
